"use client";

import { useState, useEffect } from "react";
import useSWR from "swr";
import { User, Lock, Shield, UserX } from "lucide-react";
import styles from "./settings.module.css";
import { useAuthStore } from "@/store/useAuthStore";
import { fetchApi } from "@/lib/api";
import { useRouter } from "next/navigation";

type Tab = "profile" | "channel" | "security" | "sessions" | "danger";

export default function Settings() {
  const { user, isAuthenticated, logout } = useAuthStore();
  const [activeTab, setActiveTab] = useState<Tab>("profile");
  const [isSaving, setIsSaving] = useState(false);
  const router = useRouter();
  
  // Data Fetching
  const { data: userProfile, mutate: mutateProfile } = useSWR(isAuthenticated ? "/users/me" : null, fetchApi);
  const { data: sessions, mutate: mutateSessions } = useSWR(isAuthenticated && activeTab === "sessions" ? "/auth/sessions" : null, fetchApi);
  const { data: channel, mutate: mutateChannel } = useSWR(isAuthenticated ? "/channels/me" : null, fetchApi);

  // States
  const [profileInfo, setProfileInfo] = useState({ displayName: "", bio: "" });
  const [passwordInfo, setPasswordInfo] = useState({ currentPassword: "", newPassword: "", confirmPassword: "" });
  
  // Channel States
  const [channelInfo, setChannelInfo] = useState({ slug: "", displayName: "", description: "" });
  const [slugStatus, setSlugStatus] = useState<"idle" | "checking" | "available" | "taken">("idle");

  useEffect(() => {
    if (userProfile) {
      setProfileInfo({
        displayName: userProfile.displayName || "",
        bio: userProfile.bio || "" // Note: Depends on if UserResponse has bio, fallback to ""
      });
    }
  }, [userProfile]);

  useEffect(() => {
    if (channel) {
      setChannelInfo({
        slug: channel.slug || "",
        displayName: channel.displayName || "",
        description: channel.description || ""
      });
    }
  }, [channel]);

  // Slug availability checker
  useEffect(() => {
    if (!channelInfo.slug || channelInfo.slug === channel?.slug) {
      setSlugStatus("idle");
      return;
    }
    const timeout = setTimeout(async () => {
      setSlugStatus("checking");
      try {
        const res = await fetchApi(`/channels/availability?slug=${channelInfo.slug}`);
        setSlugStatus((res as any).available ? "available" : "taken");
      } catch {
        setSlugStatus("idle");
      }
    }, 500);
    return () => clearTimeout(timeout);
  }, [channelInfo.slug, channel?.slug]);

  if (!isAuthenticated) {
    return <div style={{ padding: 100, textAlign: "center" }}>Please log in to view settings.</div>;
  }

  const handleUpdateProfile = async () => {
    setIsSaving(true);
    try {
      await fetchApi("/users/me/profile", {
        method: "PATCH",
        body: JSON.stringify({
          displayName: profileInfo.displayName,
          bio: profileInfo.bio
        })
      });
      mutateProfile();
      alert("Profile updated successfully!");
    } catch (err: any) {
      alert(err.message || "Failed to update profile.");
    } finally {
      setIsSaving(false);
    }
  };

  const handleSaveChannel = async () => {
    if (slugStatus === "taken") {
      alert("Please choose an available slug.");
      return;
    }
    setIsSaving(true);
    try {
      if (!channel) {
        await fetchApi("/channels", {
          method: "POST",
          body: JSON.stringify({ slug: channelInfo.slug, displayName: channelInfo.displayName, description: channelInfo.description })
        });
      } else {
        await fetchApi("/channels/me", {
          method: "PATCH",
          body: JSON.stringify({ slug: channelInfo.slug, displayName: channelInfo.displayName, description: channelInfo.description })
        });
      }
      mutateChannel();
      alert("Channel updated successfully!");
    } catch (err: any) {
      alert(err.message || "Failed to save channel");
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeactivateChannel = async () => {
    if (!confirm("Are you sure you want to deactivate your channel? It will be hidden from the platform.")) return;
    try {
      await fetchApi("/channels/me", { method: "DELETE" });
      mutateChannel();
      alert("Channel deactivated successfully.");
    } catch (err: any) {
      alert(err.message || "Failed to deactivate channel");
    }
  };

  const handleReactivateChannel = async () => {
    try {
      await fetchApi("/channels/me/reactivate", { method: "POST" });
      mutateChannel();
      alert("Channel reactivated successfully!");
    } catch (err: any) {
      alert(err.message || "Failed to reactivate channel");
    }
  };

  const handleChangePassword = async () => {
    if (passwordInfo.newPassword !== passwordInfo.confirmPassword) {
      alert("New passwords do not match!");
      return;
    }
    setIsSaving(true);
    try {
      await fetchApi("/auth/change-password", {
        method: "POST",
        body: JSON.stringify({
          currentPassword: passwordInfo.currentPassword,
          newPassword: passwordInfo.newPassword
        })
      });
      alert("Password changed successfully!");
      setPasswordInfo({ currentPassword: "", newPassword: "", confirmPassword: "" });
    } catch (err: any) {
      alert(err.message || "Failed to change password.");
    } finally {
      setIsSaving(false);
    }
  };

  const handleRevokeSession = async (sessionId: string) => {
    if (!confirm("Are you sure you want to log out of this session?")) return;
    try {
      await fetchApi(`/auth/sessions/${sessionId}`, { method: "DELETE" });
      mutateSessions();
    } catch (err: any) {
      alert("Failed to revoke session.");
    }
  };

  const handleDeactivate = async () => {
    if (!confirm("WARNING: This will permanently deactivate your account. Are you absolutely sure?")) return;
    try {
      await fetchApi("/users/me", { method: "DELETE" });
      logout();
      router.push("/");
    } catch (err: any) {
      alert("Failed to deactivate account.");
    }
  };

  return (
    <div className={styles.settingsLayout}>
      {/* Sidebar Navigation */}
      <aside className={styles.sidebar}>
        <div className={styles.sidebarHeader}>
          <h2 className={styles.sidebarTitle}>Account Settings</h2>
        </div>
        
        <button 
          className={`${styles.navItem} ${activeTab === "profile" ? styles.navItemActive : ""}`}
          onClick={() => setActiveTab("profile")}
        >
          <User size={20} /> User Profile
        </button>
        
        <button 
          className={`${styles.navItem} ${activeTab === "channel" ? styles.navItemActive : ""}`}
          onClick={() => setActiveTab("channel")}
        >
          <User size={20} /> Channel Settings
        </button>
        
        <button 
          className={`${styles.navItem} ${activeTab === "security" ? styles.navItemActive : ""}`}
          onClick={() => setActiveTab("security")}
        >
          <Lock size={20} /> Password
        </button>
        
        <button 
          className={`${styles.navItem} ${activeTab === "sessions" ? styles.navItemActive : ""}`}
          onClick={() => setActiveTab("sessions")}
        >
          <Shield size={20} /> Active Sessions
        </button>

        <button 
          className={`${styles.navItem} ${activeTab === "danger" ? styles.navItemActive : ""}`}
          onClick={() => setActiveTab("danger")}
          style={activeTab === "danger" ? { color: "#ef4444" } : {}}
        >
          <UserX size={20} /> Danger Zone
        </button>
      </aside>

      {/* Main Content Area */}
      <main className={styles.mainContent}>
        <header className={styles.header}>
          <h1 className={styles.title}>
            {activeTab === "profile" && "User Profile"}
            {activeTab === "security" && "Security Settings"}
            {activeTab === "sessions" && "Active Sessions"}
            {activeTab === "danger" && "Danger Zone"}
          </h1>
        </header>

        {activeTab === "profile" && (
          <div className={styles.card}>
            <h2 className={styles.cardTitle}><User size={20} className={styles.cardIcon}/> Public Information</h2>
            
            <div className={styles.inputGroup}>
              <label className={styles.label}>Profile Picture (Avatar)</label>
              <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                <div style={{ width: '80px', height: '80px', borderRadius: '50%', background: 'var(--bg-base)', overflow: 'hidden', border: '2px solid var(--border-medium)' }}>
                   {userProfile?.avatarUrl ? <img src={userProfile.avatarUrl} alt="Avatar" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : <User size={40} style={{ margin: '20px' }} color="var(--text-secondary)" />}
                </div>
                <input 
                  type="file" 
                  accept="image/*"
                  onChange={async (e) => {
                    const file = e.target.files?.[0];
                    if (!file) return;
                    setIsSaving(true);
                    try {
                      const formData = new FormData();
                      formData.append("file", file);
                      const mediaRes = await fetchApi("/media/avatar", { method: "POST", body: formData });
                      const mediaId = (mediaRes as any).id;
                      await fetchApi("/users/me/avatar", { method: "PATCH", body: JSON.stringify({ mediaId }) });
                      mutateProfile();
                      alert("Profile picture updated!");
                    } catch (err: any) {
                      alert("Failed to upload avatar: " + err.message);
                    } finally {
                      setIsSaving(false);
                    }
                  }}
                />
              </div>
              <p className={styles.helpText}>This avatar will be used globally across the platform.</p>
            </div>

            <div className={styles.inputGroup}>
              <label className={styles.label}>Display Name</label>
              <input 
                type="text" 
                className={styles.input} 
                value={profileInfo.displayName}
                onChange={(e) => setProfileInfo({...profileInfo, displayName: e.target.value})}
              />
            </div>
            
            <div className={styles.inputGroup}>
              <label className={styles.label}>Bio / Description</label>
              <textarea 
                className={styles.input} 
                value={profileInfo.bio}
                onChange={(e) => setProfileInfo({...profileInfo, bio: e.target.value})}
                placeholder="Tell us a little bit about yourself..."
                rows={4}
                style={{ resize: "vertical" }}
              />
              <p className={styles.helpText}>This will be displayed on your public channel page.</p>
            </div>
            <button className={styles.saveBtn} onClick={handleUpdateProfile} disabled={isSaving}>
              {isSaving ? "Saving..." : "Save Profile"}
            </button>
          </div>
        )}

        {activeTab === "channel" && (
          <div className={styles.profileTab}>
            <div className={styles.card}>
              <h2 className={styles.cardTitle}>
                <User size={20} className={styles.cardIcon} />
                Channel Info
              </h2>

              <div className={styles.inputGroup}>
                <label className={styles.label}>Channel Avatar (Image)</label>
                <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                  <div className={styles.avatarPreview} style={{ width: '80px', height: '80px', borderRadius: '50%', background: 'var(--bg-base)', overflow: 'hidden', border: '2px solid var(--border-medium)' }}>
                     {channel?.avatarUrl ? <img src={channel.avatarUrl} alt="Avatar" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : <User size={40} style={{ margin: '20px' }} color="var(--text-secondary)" />}
                  </div>
                  <input 
                    type="file" 
                    accept="image/*"
                    onChange={async (e) => {
                      const file = e.target.files?.[0];
                      if (!file) return;
                      setIsSaving(true);
                      try {
                        const formData = new FormData();
                        formData.append("file", file);
                        const mediaRes = await fetchApi("/media/avatar", { method: "POST", body: formData });
                        const mediaId = (mediaRes as any).id;
                        await fetchApi("/channels/me/avatar", { method: "PATCH", body: JSON.stringify({ mediaId }) });
                        mutateChannel();
                        alert("Avatar updated!");
                      } catch (err: any) {
                        alert("Failed to upload avatar: " + err.message);
                      } finally {
                        setIsSaving(false);
                      }
                    }}
                  />
                </div>
              </div>

              <div className={styles.inputGroup}>
                <label className={styles.label}>Channel Banner (Image)</label>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {channel?.bannerUrl && (
                    <div style={{ width: '100%', height: '120px', borderRadius: '12px', overflow: 'hidden', background: 'var(--bg-base)' }}>
                      <img src={channel.bannerUrl} alt="Banner" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    </div>
                  )}
                  <input 
                    type="file" 
                    accept="image/*"
                    onChange={async (e) => {
                      const file = e.target.files?.[0];
                      if (!file) return;
                      setIsSaving(true);
                      try {
                        const formData = new FormData();
                        formData.append("file", file);
                        const mediaRes = await fetchApi("/media/banner", { method: "POST", body: formData });
                        const mediaId = (mediaRes as any).id;
                        await fetchApi("/channels/me/banner", { method: "PATCH", body: JSON.stringify({ mediaId }) });
                        mutateChannel();
                        alert("Banner updated!");
                      } catch (err: any) {
                        alert("Failed to upload banner: " + err.message);
                      } finally {
                        setIsSaving(false);
                      }
                    }}
                  />
                </div>
              </div>

              <div className={styles.inputGroup}>
                <label className={styles.label}>Channel URL (Slug)</label>
                <div className={styles.slugInputContainer} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span style={{ color: 'var(--text-secondary)' }}>streamnova.com/</span>
                  <input 
                    type="text" 
                    className={styles.input} 
                    value={channelInfo.slug}
                    onChange={(e) => setChannelInfo({...channelInfo, slug: e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, '')})}
                    style={{ flex: 1 }}
                  />
                  {slugStatus === "checking" && <span style={{ color: "var(--text-secondary)", fontSize: "0.85rem" }}>Checking...</span>}
                  {slugStatus === "available" && <CheckCircle size={18} color="#10b981" />}
                  {slugStatus === "taken" && <XCircle size={18} color="#ef4444" />}
                </div>
                <p className={styles.helpText} style={{ color: slugStatus === "taken" ? "#ef4444" : "var(--text-secondary)" }}>
                  {slugStatus === "taken" ? "This URL is already taken." : "Only lowercase letters, numbers, and hyphens allowed."}
                </p>
              </div>

              <div className={styles.inputGroup}>
                <label className={styles.label}>Display Name</label>
                <input 
                  type="text" 
                  className={styles.input} 
                  value={channelInfo.displayName}
                  onChange={(e) => setChannelInfo({...channelInfo, displayName: e.target.value})}
                />
                <p className={styles.helpText}>This is what viewers see on your channel page.</p>
              </div>

              <div className={styles.inputGroup}>
                <label className={styles.label}>Bio</label>
                <textarea 
                  className={`${styles.input} ${styles.textarea}`}
                  value={channelInfo.description}
                  onChange={(e) => setChannelInfo({...channelInfo, description: e.target.value})}
                  maxLength={300}
                />
                <p className={styles.helpText}>Tell viewers about yourself and what you stream.</p>
              </div>

              <button className={styles.saveBtn} onClick={handleSaveChannel} disabled={isSaving || slugStatus === "taken"}>
                {isSaving ? "Publishing..." : channel ? "Publish Profile" : "Create Channel"}
              </button>
            </div>

            {channel && (
              <div className={styles.card} style={{ border: "1px solid var(--danger-border)" }}>
                <h2 className={styles.cardTitle} style={{ color: "#ef4444" }}>
                  <UserX size={20} />
                  Danger Zone
                </h2>
                <p style={{ color: "var(--text-secondary)", marginBottom: "16px" }}>
                  Deactivating your channel will hide it from the platform. Your streams and VODs will no longer be visible.
                </p>
                <button 
                  className={styles.saveBtn} 
                  onClick={handleDeactivateChannel} 
                  style={{ backgroundColor: "transparent", color: "#ef4444", border: "1px solid #ef4444" }}
                >
                  Deactivate Channel
                </button>
              </div>
            )}
          </div>
        )}

        {activeTab === "security" && (
          <div className={styles.card}>
            <h2 className={styles.cardTitle}><Lock size={20} className={styles.cardIcon}/> Change Password</h2>
            
            <div className={styles.inputGroup}>
              <label className={styles.label}>Current Password</label>
              <input 
                type="password" 
                className={styles.input} 
                value={passwordInfo.currentPassword}
                onChange={(e) => setPasswordInfo({...passwordInfo, currentPassword: e.target.value})}
              />
            </div>

            <div className={styles.inputGroup}>
              <label className={styles.label}>New Password</label>
              <input 
                type="password" 
                className={styles.input} 
                value={passwordInfo.newPassword}
                onChange={(e) => setPasswordInfo({...passwordInfo, newPassword: e.target.value})}
              />
            </div>

            <div className={styles.inputGroup}>
              <label className={styles.label}>Confirm New Password</label>
              <input 
                type="password" 
                className={styles.input} 
                value={passwordInfo.confirmPassword}
                onChange={(e) => setPasswordInfo({...passwordInfo, confirmPassword: e.target.value})}
              />
            </div>

            <button className={styles.saveBtn} onClick={handleChangePassword} disabled={isSaving}>
              {isSaving ? "Updating..." : "Update Password"}
            </button>
          </div>
        )}

        {activeTab === "sessions" && (
          <div className={styles.card}>
            <h2 className={styles.cardTitle}><Shield size={20} className={styles.cardIcon}/> Where you're logged in</h2>
            <p style={{ color: "var(--text-secondary)", marginBottom: 16 }}>Manage the devices that are currently logged into your account.</p>
            
            <div className={styles.sessionList}>
              {!sessions ? <p>Loading sessions...</p> : null}
              {sessions?.map((session: any) => (
                <div key={session.sessionId} className={styles.sessionItem}>
                  <div className={styles.sessionInfo}>
                    <span className={styles.sessionDevice}>{session.deviceInfo || "Unknown Device"}</span>
                    <span className={styles.sessionIp}>IP: {session.ipAddress}</span>
                  </div>
                  <button className={styles.revokeBtn} onClick={() => handleRevokeSession(session.sessionId)}>
                    Revoke
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === "danger" && (
          <div className={styles.card}>
            <h2 className={styles.cardTitle} style={{ color: "#ef4444", borderBottomColor: "rgba(239,68,68,0.2)" }}>
              <UserX size={20} /> Deactivate Account
            </h2>
            <p style={{ color: "var(--text-secondary)" }}>
              Once you delete your account, there is no going back. Please be certain.
            </p>
            <button className={styles.dangerBtn} onClick={handleDeactivate}>
              Deactivate My Account
            </button>
          </div>
        )}
      </main>
    </div>
  );
}
