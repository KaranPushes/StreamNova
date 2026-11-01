"use client";

import { useEffect, useState, useRef, FormEvent } from "react";
import { Client } from "@stomp/stompjs";
import { Send } from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { fetchApi } from "@/lib/api";
import UserContextMenu from "./UserContextMenu";
import styles from "./StreamChat.module.css";

interface ChatMessage {
  id: string;
  userId: string;
  username?: string;
  message: string;
  createdAt: string;
}

interface StreamChatProps {
  streamId: string;
}

export default function StreamChat({ streamId }: StreamChatProps) {
  const { user, accessToken, isAuthenticated, openAuthModal } = useAuthStore();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const [isConnected, setIsConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  
  const [contextMenu, setContextMenu] = useState<{
    visible: boolean;
    x: number;
    y: number;
    targetUserId: string;
    targetUsername: string;
    messageId: string;
    isOwner: boolean;
  } | null>(null);

  // Fetch initial chat history
  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const history = await fetchApi<ChatMessage[]>(`/api/chat/streams/${streamId}/messages`);
        // API returns desc (newest first), we want asc (oldest first) for chat UI
        if (history && Array.isArray(history)) {
          setMessages(history.reverse());
        }
      } catch (err) {
        console.error("Failed to load chat history:", err);
      }
    };
    
    fetchHistory();
  }, [streamId]);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    if (messagesEndRef.current && messagesEndRef.current.parentElement) {
      const container = messagesEndRef.current.parentElement;
      container.scrollTo({
        top: container.scrollHeight,
        behavior: "smooth"
      });
    }
  }, [messages]);

  useEffect(() => {
    // Only connect if the user is authenticated (as required by the backend API Gateway)
    if (!isAuthenticated || !accessToken) return;

    const client = new Client({
      brokerURL: "ws://localhost:8088/api/chat", // Connect directly to chat-service to bypass Gateway MVC WebSocket proxy issues
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        setIsConnected(true);
        // Subscribe to the specific stream's chat topic
        client.subscribe(`/topic/streams/${streamId}/chat`, (message) => {
          if (message.body) {
            const parsedMessage: ChatMessage = JSON.parse(message.body);
            setMessages((prev) => [...prev, parsedMessage]);
          }
        });

        // Subscribe to deleted messages (moderation)
        client.subscribe(`/topic/streams/${streamId}/chat.deleted`, (message) => {
          if (message.body) {
            const deleted = JSON.parse(message.body);
            setMessages((prev) => 
              // Either remove the message completely or replace text with [deleted]
              prev.map(msg => 
                msg.id === deleted.messageId 
                  ? { ...msg, message: "[message deleted]" } 
                  : msg
              )
            );
          }
        });

        // Subscribe to chat errors (e.g. rate limiting)
        client.subscribe(`/topic/streams/${streamId}/chat.errors`, (message) => {
          if (message.body) {
            const err = JSON.parse(message.body);
            console.error("Chat backend error:", err);
            // Optionally alert the user or show a toast
            if (err.code === "CHAT_RATE_LIMITED") {
              alert("You are sending messages too quickly!");
            }
          }
        });
      },
      onStompError: (frame) => {
        console.error("STOMP Error:", frame.headers["message"]);
        setIsConnected(false);
      },
      onWebSocketClose: () => {
        setIsConnected(false);
      }
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      setIsConnected(false);
    };
  }, [streamId, isAuthenticated, accessToken]);



  const handleSendMessage = (e: FormEvent) => {
    e.preventDefault();
    if (!input.trim() || !clientRef.current || !isConnected || !user) return;

    const payload = {
      streamId,
      message: input.trim(),
    };

    clientRef.current.publish({
      destination: "/app/chat.send",
      body: JSON.stringify(payload),
    });

    setInput("");
  };

  const handleDeleteMessage = async (messageId: string) => {
    try {
      await fetchApi(`/api/chat/messages/${messageId}`, { method: 'DELETE' });
    } catch (err) {
      console.error("Failed to delete message", err);
    }
  };

  const [canModerateData, setCanModerateData] = useState<{ isOwner: boolean, isMod: boolean }>({ isOwner: false, isMod: false });

  // Fetch whether the user can moderate
  useEffect(() => {
    if (!isAuthenticated || !streamId) return;

    fetchApi(`/api/chat/streams/${streamId}/can-moderate`)
      .then(res => setCanModerateData(res))
      .catch(err => console.error(err));
  }, [streamId, isAuthenticated]);

  const handleUsernameClick = (e: React.MouseEvent, msg: ChatMessage) => {
    if (!isAuthenticated || !msg.id) return;
    
    // Only stream owners or moderators can open the moderation menu
    if (!canModerateData.isOwner && !canModerateData.isMod) return;

    // Don't show menu for your own messages, you can use the delete button instead
    if (msg.userId === (user?.id || (user as any)?.userId)) return;

    e.preventDefault();
    setContextMenu({
      visible: true,
      x: e.clientX,
      y: e.clientY,
      targetUserId: msg.userId,
      targetUsername: msg.username || "Anonymous",
      messageId: msg.id,
      isOwner: canModerateData.isOwner,
    });
  };

  return (
    <div className={styles.chatContainer}>
      <div className={styles.messagesArea}>
        <div className={styles.systemMessage}>Welcome to the chat room!</div>

        {messages.map((msg, index) => (
          <div key={index} className={styles.message}>
            <span 
              className={styles.username}
              onClick={(e) => handleUsernameClick(e, msg)}
              style={{ cursor: isAuthenticated && msg.userId !== (user?.id || (user as any)?.userId) ? 'pointer' : 'default' }}
            >
              {msg.userId === (user?.id || (user as any)?.userId)
                ? (user?.displayName || user?.username || 'You')
                : (msg.username || 'Anonymous')}
            </span>
            <span className={styles.text}>{msg.message}</span>
            {msg.userId === (user?.id || (user as any)?.userId) && msg.message !== "[message deleted]" && msg.id && (
              <button 
                onClick={() => handleDeleteMessage(msg.id)}
                className={styles.deleteBtn}
                title="Delete message"
              >
                ×
              </button>
            )}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      <div className={styles.inputArea}>
        {isAuthenticated ? (
          <form className={styles.form} onSubmit={handleSendMessage}>
            <input
              type="text"
              className={styles.input}
              placeholder="Send a message..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              disabled={!isConnected}
              maxLength={200}
            />
            <button
              type="submit"
              className={styles.sendBtn}
              disabled={!isConnected || !input.trim()}
            >
              <Send size={18} />
            </button>
          </form>
        ) : (
          <div className={styles.loginPrompt}>
            <p>You must be logged in to chat.</p>
            <button className={styles.loginBtn} onClick={() => openAuthModal("login")}>
              Log In
            </button>
          </div>
        )}
      </div>

      {contextMenu?.visible && (
        <UserContextMenu
          streamId={streamId}
          targetUserId={contextMenu.targetUserId}
          targetUsername={contextMenu.targetUsername}
          messageId={contextMenu.messageId}
          position={{ x: contextMenu.x, y: contextMenu.y }}
          isOwner={contextMenu.isOwner}
          onClose={() => setContextMenu(null)}
        />
      )}
    </div>
  );
}
