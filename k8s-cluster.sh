#!/bin/zsh

# ==============================================================================
# StreamNova Kubernetes Control Script
# Usage:
#   ./k8s-cluster.sh start   - Start cluster, deploy all services & port-forward
#   ./k8s-cluster.sh stop    - Stop port-forwards and pause/stop Colima VM
#   ./k8s-cluster.sh status  - Check running pods, Eureka, and port-forwards
#   ./k8s-cluster.sh ports   - Re-attach port forwards only
# ==============================================================================

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_FILE="$PROJECT_DIR/.port-forwards.pid"

# Colors for terminal output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

stop_port_forwards() {
    echo -e "${YELLOW}Stopping existing port-forwards...${NC}"
    if [ -f "$PID_FILE" ]; then
        while IFS= read -r pid; do
            if ps -p "$pid" > /dev/null 2>&1; then
                kill "$pid" 2>/dev/null
            fi
        done < "$PID_FILE"
        rm -f "$PID_FILE"
    fi
    pkill -f "kubectl port-forward" 2>/dev/null || true
    echo -e "${GREEN}Port-forwards stopped.${NC}"
}

start_port_forwards() {
    stop_port_forwards
    echo -e "${BLUE}Starting background port-forwards...${NC}"

    # Forward essential ports
    nohup kubectl port-forward svc/api-gateway 8080:8080 --address 0.0.0.0 > /dev/null 2>&1 &
    PID_GATEWAY=$!
    nohup kubectl port-forward svc/discovery-server 8761:8761 --address 0.0.0.0 > /dev/null 2>&1 &
    PID_DISCOVERY=$!
    nohup kubectl port-forward svc/mediamtx 1935:1935 --address 0.0.0.0 > /dev/null 2>&1 &
    PID_RTMP=$!
    nohup kubectl port-forward svc/nginx-hls 8090:8090 --address 0.0.0.0 > /dev/null 2>&1 &
    PID_HLS=$!
    nohup kubectl port-forward svc/postgres 5432:5432 --address 0.0.0.0 > /dev/null 2>&1 &
    PID_POSTGRES=$!

    echo "$PID_GATEWAY" > "$PID_FILE"
    echo "$PID_DISCOVERY" >> "$PID_FILE"
    echo "$PID_RTMP" >> "$PID_FILE"
    echo "$PID_HLS" >> "$PID_FILE"
    echo "$PID_POSTGRES" >> "$PID_FILE"

    sleep 2
    echo -e "${GREEN}Port-forwards active:${NC}"
    echo -e "  - ${CYAN}API Gateway (Main Backend)${NC} : http://localhost:8080"
    echo -e "  - ${CYAN}Eureka Dashboard${NC}          : http://localhost:8761"
    echo -e "  - ${CYAN}MediaMTX (OBS RTMP)${NC}        : rtmp://localhost:1935/live"
    echo -e "  - ${CYAN}Nginx (HLS Playback)${NC}       : http://localhost:8090/live"
    echo -e "  - ${CYAN}PostgreSQL Database${NC}        : localhost:5432 (user: streaming, pass: streaming123)"
}

start_cluster() {
    echo -e "${BLUE}===================================================${NC}"
    echo -e "${BLUE}       Starting StreamNova Kubernetes Backend      ${NC}"
    echo -e "${BLUE}===================================================${NC}"

    # 1. Check if Colima is running
    COLIMA_STATUS=$(colima status 2>&1 || true)
    if echo "$COLIMA_STATUS" | grep -q "colima is running"; then
        echo -e "${GREEN}Colima VM is already running.${NC}"
    else
        echo -e "${YELLOW}Starting Colima with 6 CPUs and 12GB RAM...${NC}"
        colima start --kubernetes --cpu 6 --memory 12
    fi

    # 2. Tune inotify instances for MediaMTX
    echo -e "${BLUE}Configuring VM kernel inotify limits...${NC}"
    colima ssh -- sudo sysctl -w fs.inotify.max_user_instances=8192 > /dev/null 2>&1 || true

    # 3. Apply manifests if not present
    echo -e "${BLUE}Applying Kubernetes manifests...${NC}"
    kubectl apply -k "$PROJECT_DIR/k8s"

    # 4. Wait for Gateway & Discovery Server to be ready
    echo -e "${YELLOW}Waiting for pods to be ready...${NC}"
    kubectl wait --for=condition=ready pod -l app=discovery-server --timeout=90s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=api-gateway --timeout=90s 2>/dev/null || true

    # 5. Start port-forwards
    start_port_forwards

    echo -e "\n${GREEN}Backend cluster is UP and ready for development!${NC}"
    echo -e "Run ${YELLOW}./k8s-cluster.sh status${NC} to check pods or ${YELLOW}./k8s-cluster.sh stop${NC} to shut down.\n"
}

stop_cluster() {
    echo -e "${BLUE}===================================================${NC}"
    echo -e "${BLUE}       Stopping StreamNova Kubernetes Backend      ${NC}"
    echo -e "${BLUE}===================================================${NC}"

    # 1. Stop all port forwards
    stop_port_forwards

    # 2. Stop Colima VM to release CPU & RAM to macOS
    echo -e "${YELLOW}Stopping Colima VM (frees all memory & CPU on your Mac)...${NC}"
    colima stop

    echo -e "${GREEN}StreamNova cluster stopped cleanly.${NC}"
}

check_status() {
    echo -e "${BLUE}===================================================${NC}"
    echo -e "${BLUE}            StreamNova Cluster Status              ${NC}"
    echo -e "${BLUE}===================================================${NC}"

    COLIMA_STATUS=$(colima status 2>&1 || true)
    if echo "$COLIMA_STATUS" | grep -q "colima is running"; then
        echo -e "Colima VM: ${GREEN}Running${NC}"
    else
        echo -e "Colima VM: ${RED}Stopped${NC}"
        exit 0
    fi

    echo -e "\n${CYAN}--- Kubernetes Pods ---${NC}"
    kubectl get pods

    echo -e "\n${CYAN}--- Eureka Registered Services ---${NC}"
    EUREKA_APPS=$(curl -s -H "Accept: application/json" http://127.0.0.1:8761/eureka/apps 2>/dev/null | grep -o '"name":"[^"]*"' | sort -u || true)
    if [ -n "$EUREKA_APPS" ]; then
        echo "$EUREKA_APPS" | tr -d '"' | sed 's/name:/  ✓ /'
    else
        echo -e "${YELLOW}Could not query Eureka (port 8761 may not be forwarded or Discovery Server initializing).${NC}"
    fi

    echo -e "\n${CYAN}--- Port-Forwarding Status ---${NC}"
    for port in 8080 8761 1935 8090 5432; do
        if lsof -i :$port > /dev/null 2>&1; then
            echo -e "  Port $port: ${GREEN}Active${NC}"
        else
            echo -e "  Port $port: ${RED}Inactive${NC}"
        fi
    done
}

case "$1" in
    start)
        start_cluster
        ;;
    stop)
        stop_cluster
        ;;
    status)
        check_status
        ;;
    ports)
        start_port_forwards
        ;;
    *)
        echo "Usage: $0 {start|stop|status|ports}"
        exit 1
        ;;
esac
