"use client";

import { useState, useEffect } from "react";
import useSWR from "swr";
import { 
  Eye, EyeOff, Copy, KeyRound, Settings, 
  AlertTriangle, Radio, BarChart3, User,
  Video, CheckCircle, XCircle
} from "lucide-react";
import styles from "./dashboard.module.css";
import { useAuthStore } from "@/store/useAuthStore";
import { fetchApi } from "@/lib/api";

type Tab = "overview" | "stream";

export default function Dashboard() {
  const { user, isAuthenticated } = useAuthStore();
  const [activeTab, setActiveTab] = useState<Tab>("overview");
  const [showKey, setShowKey] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  
  // Data Fetching
  const { data: channel, mutate: mutateChannel } = useSWR(isAuthenticated ? "/channels/me" : null, fetchApi);
  
  const [streamKey, setStreamKey] = useState<string>("Click 'Go Live' to generate your key");
  
  const { data: streams, mutate: mutateStreams } = useSWR(
    channel?.id ? `/streams/channel/${channel.id}` : null,
    fetchApi
  );

  const { data: followerData } = useSWR(
    channel?.id ? `/follows/channels/${channel.id}/count` : null,
    fetchApi
  );

  const activeStream = streams?.find((s: any) => s.status === "SCHEDULED" || s.status === "LIVE");

  // Editable States
  const [streamInfo, setStreamInfo] = useState({
    title: "",
    category: "",
    scheduledAt: ""
  });
  


  // Sync state with fetched data
  useEffect(() => {
    if (activeStream) {
      setStreamInfo({
        title: activeStream.title || "",
        category: activeStream.category || "",
        scheduledAt: activeStream.scheduledAt ? new Date(activeStream.scheduledAt).toISOString().slice(0,16) : ""
      });
      
      const savedKey = localStorage.getItem("streamnova_stream_key_" + activeStream.id);
      if (activeStream.streamKey) {
        setStreamKey(activeStream.streamKey);
      } else if (savedKey) {
        setStreamKey(savedKey);
      } else {
        setStreamKey(prev => prev.startsWith("Click") ? "Stream key hidden. End stream and create a new one to get a new key." : prev);
      }
    } else {
      setStreamInfo({ title: "", category: "", scheduledAt: "" });
      setStreamKey("Click 'Create Stream' to generate your key");
    }
  }, [activeStream]);


  const handleCopyKey = () => {
    navigator.clipboard.writeText(streamKey);
    alert("Stream key copied to clipboard!");
  };

  const handleCreateStream = async () => {
    if (!channel) {
      alert("You need a channel first! Please create one in Account Settings > Channel.");
      return;
    }
    
    setIsSaving(true);
    try {
      const scheduledDate = streamInfo.scheduledAt ? new Date(streamInfo.scheduledAt) : new Date();
      if (scheduledDate < new Date()) {
        scheduledDate.setMinutes(scheduledDate.getMinutes() + 5); // Fallback to 5 mins future
      }
      const res = await fetchApi("/streams", {
        method: "POST",
        body: JSON.stringify({
          title: streamInfo.title || "My Stream",
          category: streamInfo.category || "Just Chatting",
          scheduledAt: scheduledDate.toISOString()
        })
      });
      const newStreamKey = (res as any).streamKey;
      setStreamKey(newStreamKey);
      if (newStreamKey && (res as any).id) {
        localStorage.setItem("streamnova_stream_key_" + (res as any).id, newStreamKey);
      }
      mutateStreams();
      setShowCreateForm(false);
      alert("Stream created! Copy your Stream Key and paste it into OBS.");
    } catch (err: any) {
      alert(err.message || "Failed to create stream");
    } finally {
      setIsSaving(false);
    }
  };

  const handleSaveStream = async () => {
    if (!activeStream) {
      alert("No active stream found.");
      return;
    }
    setIsSaving(true);
    try {
      await fetchApi(`/streams/${activeStream.id}`, {
        method: "PATCH",
        body: JSON.stringify({
          title: streamInfo.title,
          category: streamInfo.category
        })
      });
      
      if (streamInfo.scheduledAt) {
        await fetchApi(`/streams/${activeStream.id}/schedule`, {
          method: "POST",
          body: JSON.stringify({ scheduledAt: new Date(streamInfo.scheduledAt).toISOString() })
        });
      }
      
      mutateStreams();
      alert("Stream details updated!");
    } catch (err: any) {
      alert(err.message || "Failed to update stream");
    } finally {
      setIsSaving(false);
    }
  };

  const handleStartStream = async () => {
    if (!activeStream) return;
    try {
      await fetchApi(`/streams/${activeStream.id}/start`, { method: "POST" });
      mutateStreams();
      alert("Stream has been marked as LIVE!");
    } catch (err: any) {
      alert("Failed to start stream: " + err.message);
    }
  };

  const handleEndStream = async () => {
    if (!activeStream) return;
    if (!confirm("Are you sure you want to end this stream?")) return;
    try {
      await fetchApi(`/streams/${activeStream.id}/end`, { method: "POST" });
      mutateStreams();
      alert("Stream has ended.");
    } catch (err: any) {
      alert("Failed to end stream: " + err.message);
    }
  };


  if (!isAuthenticated) {
    return (
      <div className={styles.dashboardLayout}>
        <div className={`${styles.card} ${styles.authWarning}`}>
          <h2 className={styles.cardTitle}>Authentication Required</h2>
          <p>You must be logged in to access the Creator Studio.</p>
        </div>
      </div>
    );
  }

  // If channel is deactivated, show reactivation screen
  if (channel && channel.status === "INACTIVE") {
    return (
      <div className={styles.dashboardLayout}>
        <div className={styles.card} style={{ textAlign: "center", padding: "64px" }}>
          <AlertTriangle size={64} style={{ color: "#ef4444", margin: "0 auto 24px" }} />
          <h2 className={styles.cardTitle}>Your Channel is Inactive</h2>
          <p style={{ color: "var(--text-secondary)", marginBottom: "32px" }}>
            You previously deactivated your channel. You must reactivate it to continue streaming and updating your profile.
          </p>
          <button className={styles.saveBtn} onClick={handleReactivate} style={{ width: "auto", margin: "0 auto" }}>
            Reactivate Channel
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.dashboardLayout}>
      
      {/* Sidebar Navigation */}
      <aside className={styles.sidebar}>
        <div className={styles.sidebarHeader}>
          <h2 className={styles.sidebarTitle}>Creator Studio</h2>
          <p className={styles.sidebarSubtitle}>{channel?.displayName || "Your Channel"}</p>
        </div>
        
        <div className={styles.navGroup}>
          <button 
            className={`${styles.navItem} ${activeTab === "overview" ? styles.navItemActive : ""}`}
            onClick={() => setActiveTab("overview")}
          >
            <BarChart3 size={20} />
            Dashboard
          </button>
          
          <button 
            className={`${styles.navItem} ${activeTab === "stream" ? styles.navItemActive : ""}`}
            onClick={() => setActiveTab("stream")}
          >
            <Radio size={20} />
            Stream Manager
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className={styles.mainContent}>
        <header className={styles.header}>
          <div>
            <h1 className={styles.title}>
              {activeTab === "overview" && "Channel Dashboard"}
              {activeTab === "stream" && "Stream Manager"}
            </h1>
            <p className={styles.subtitle}>
              {activeTab === "overview" && "At-a-glance view of your channel's performance."}
              {activeTab === "stream" && "Manage your live broadcast and stream settings."}
            </p>
          </div>
          {activeTab === "stream" && !activeStream && (
            <button className={styles.goLiveBtn} onClick={() => setShowCreateForm(!showCreateForm)} disabled={isSaving || !channel}>
              <Radio size={18} />
              {showCreateForm ? "Cancel" : "Create New Stream"}
            </button>
          )}
        </header>

        {activeTab === "overview" && (
          <div className={styles.card}>
            <h2 className={styles.cardTitle}>Recent Performance</h2>
            <div className={styles.statsGrid}>
              <div className={styles.statBox}>
                <span className={styles.statLabel}>Total Followers</span>
                <span className={styles.statValue}>{followerData?.followerCount || 0}</span>
              </div>
              <div className={styles.statBox}>
                <span className={styles.statLabel}>Peak Viewers (Last Stream)</span>
                <span className={styles.statValue}>-</span>
              </div>
              <div className={styles.statBox}>
                <span className={styles.statLabel}>Watch Time (Hours)</span>
                <span className={styles.statValue}>-</span>
              </div>
            </div>
          </div>
        )}

        {activeTab === "stream" && (
          <>
            {!activeStream ? (
              showCreateForm ? (
                <div className={styles.card}>
                  <h2 className={styles.cardTitle}>
                    <Radio size={20} className={styles.cardIcon} />
                    Create New Stream
                  </h2>
                  <p style={{ color: "var(--text-secondary)", marginBottom: "24px" }}>
                    Set up your stream details below. Once created, you will get your RTMP stream key to connect OBS and go live.
                  </p>
                
                <div className={styles.inputGroup}>
                  <label className={styles.label}>Stream Title</label>
                  <input 
                    type="text" 
                    className={styles.input} 
                    value={streamInfo.title}
                    onChange={(e) => setStreamInfo({...streamInfo, title: e.target.value})}
                    placeholder="E.g., Playing some games!"
                    maxLength={100}
                  />
                </div>

                <div className={styles.inputGroup}>
                  <label className={styles.label}>Category</label>
                  <input 
                    type="text" 
                    className={styles.input} 
                    value={streamInfo.category}
                    placeholder="E.g., Just Chatting, Gaming"
                    onChange={(e) => setStreamInfo({...streamInfo, category: e.target.value})}
                  />
                </div>
                
                <div className={styles.inputGroup}>
                  <label className={styles.label}>Scheduled Time (Optional)</label>
                  <input 
                    type="datetime-local" 
                    className={styles.input} 
                    value={streamInfo.scheduledAt}
                    onChange={(e) => setStreamInfo({...streamInfo, scheduledAt: e.target.value})}
                  />
                  <p className={styles.helpText}>Leave blank to go live immediately, or schedule for later.</p>
                </div>

                <button className={styles.saveBtn} onClick={handleCreateStream} disabled={isSaving}>
                  {isSaving ? "Creating..." : "Schedule / Create Stream"}
                </button>
              </div>
              ) : (
                <div className={styles.card} style={{ textAlign: "center", padding: "80px 20px" }}>
                  <Video size={64} color="var(--border-medium)" style={{ marginBottom: "24px", opacity: 0.5 }} />
                  <h2 style={{ fontSize: "1.5rem", marginBottom: "12px" }}>Ready to go live?</h2>
                  <p style={{ color: "var(--text-secondary)", marginBottom: "32px", maxWidth: "400px", margin: "0 auto 32px" }}>
                    Schedule your next broadcast or create a stream right now to start broadcasting to your audience.
                  </p>
                  <button className={styles.goLiveBtn} onClick={() => setShowCreateForm(true)} style={{ width: "auto", margin: "0 auto", padding: "12px 24px" }}>
                    <Radio size={18} />
                    Create New Stream
                  </button>
                </div>
              )
            ) : (
              <>
                {/* Status Banner */}
                <div className={styles.card} style={{ 
                  backgroundColor: activeStream.status === "LIVE" ? "rgba(239, 68, 68, 0.1)" : "var(--bg-surface)",
                  borderColor: activeStream.status === "LIVE" ? "rgba(239, 68, 68, 0.3)" : "var(--border-medium)",
                  flexDirection: "row",
                  alignItems: "center",
                  justifyContent: "space-between",
                  padding: "24px"
                }}>
                  <div>
                    <h2 style={{ margin: 0, display: "flex", alignItems: "center", gap: "8px", color: activeStream.status === "LIVE" ? "#ef4444" : "var(--text-primary)" }}>
                      <Radio size={24} />
                      {activeStream.status === "LIVE" ? "You are LIVE!" : "Stream Scheduled (Offline)"}
                    </h2>
                    <p style={{ margin: "8px 0 0", color: "var(--text-secondary)" }}>
                      {activeStream.status === "LIVE" 
                        ? "Your broadcast is currently live to your viewers." 
                        : "Connect your streaming software (OBS) using the key below and start broadcasting."}
                    </p>
                  </div>
                  <div>
                    {activeStream.status === "SCHEDULED" && (
                      <button className={styles.saveBtn} onClick={handleStartStream} style={{ margin: 0, backgroundColor: "#ef4444" }}>
                        Start Broadcast (Force Live)
                      </button>
                    )}
                    {activeStream.status === "LIVE" && (
                      <button className={styles.saveBtn} onClick={handleEndStream} style={{ margin: 0, backgroundColor: "transparent", border: "1px solid #ef4444", color: "#ef4444" }}>
                        End Stream
                      </button>
                    )}
                  </div>
                </div>

                <div className={styles.card}>
                  <h2 className={styles.cardTitle}>
                    <KeyRound size={20} className={styles.cardIcon} />
                    Stream Connection
                  </h2>
                  
                  <div className={styles.inputGroup}>
                    <label className={styles.label}>Stream URL (OBS / MediaMTX)</label>
                    <div className={styles.inputGroupRow}>
                      <input 
                        type="text" 
                        className={styles.input} 
                        value="rtmp://localhost:1935/live" 
                        readOnly 
                      />
                      <button 
                        className={styles.actionBtn}
                        onClick={() => {
                          navigator.clipboard.writeText("rtmp://localhost:1935/live");
                          alert("URL copied!");
                        }}
                      >
                        <Copy size={18} />
                      </button>
                    </div>
                    <p className={styles.helpText}>Enter this in your streaming software (OBS) under "Server".</p>
                  </div>

                  <div className={styles.inputGroup}>
                    <label className={styles.label}>Stream Key</label>
                    <div className={styles.inputGroupRow}>
                      <input 
                        type={showKey ? "text" : "password"} 
                        className={styles.input} 
                        value={streamKey} 
                        readOnly 
                      />
                      <button className={styles.actionBtn} onClick={() => setShowKey(!showKey)}>
                        {showKey ? <EyeOff size={18} /> : <Eye size={18} />}
                      </button>
                      <button className={styles.actionBtn} onClick={handleCopyKey}>
                        <Copy size={18} />
                      </button>
                    </div>
                    <div className={styles.warningBox}>
                      <AlertTriangle size={24} style={{ flexShrink: 0 }} />
                      <span><strong>Keep this key secret!</strong> Anyone with this key can broadcast to your channel. If it gets compromised, you must reset it immediately.</span>
                    </div>
                  </div>
                </div>

                <div className={styles.card}>
                  <h2 className={styles.cardTitle}>
                    <Settings size={20} className={styles.cardIcon} />
                    Stream Details
                  </h2>

                  <div className={styles.inputGroup}>
                    <label className={styles.label}>Stream Title</label>
                    <input 
                      type="text" 
                      className={styles.input} 
                      value={streamInfo.title}
                      onChange={(e) => setStreamInfo({...streamInfo, title: e.target.value})}
                      maxLength={100}
                    />
                    <p className={styles.helpText}>A catchy title helps viewers find your stream.</p>
                  </div>

                  <div className={styles.inputGroup}>
                    <label className={styles.label}>Category</label>
                    <input 
                      type="text" 
                      className={styles.input} 
                      value={streamInfo.category}
                      onChange={(e) => setStreamInfo({...streamInfo, category: e.target.value})}
                    />
                  </div>
                  
                  <div className={styles.inputGroup}>
                    <label className={styles.label}>Scheduled Time (Optional)</label>
                    <input 
                      type="datetime-local" 
                      className={styles.input} 
                      value={streamInfo.scheduledAt}
                      onChange={(e) => setStreamInfo({...streamInfo, scheduledAt: e.target.value})}
                    />
                    <p className={styles.helpText}>Let viewers know when you plan to go live.</p>
                  </div>

                  <button className={styles.saveBtn} onClick={handleSaveStream} disabled={isSaving}>
                    {isSaving ? "Saving..." : "Update Details"}
                  </button>
                </div>
              </>
            )}
          </>
        )}


      </main>
    </div>
  );
}
