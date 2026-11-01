"use client";

import Link from "next/link";
import { User, Home, TrendingUp, Radio, LayoutGrid, Heart } from "lucide-react";
import useSWR from "swr";
import { fetchApi } from "@/lib/api";
import styles from "./Sidebar.module.css";
import { usePathname } from "next/navigation";
import { useAuthStore } from "@/store/useAuthStore";
import { Suspense, useEffect, useState } from "react";

function SidebarContent() {
  const pathname = usePathname();
  const { isAuthenticated, openAuthModal } = useAuthStore();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  
  // Fetch followed channels if authenticated
  const { data: followsResponse } = useSWR(
    mounted && isAuthenticated ? "/api/feed/following?size=10" : null, 
    fetchApi
  );
  
  const followedChannels = followsResponse?.content || [];

  const navItems = [
    { name: "Home", href: "/", icon: <Home size={20} /> },
    { name: "Live Now", href: "/live", icon: <Radio size={20} /> },
    { name: "Trending", href: "/trending", icon: <TrendingUp size={20} /> },
    { name: "Categories", href: "/categories", icon: <LayoutGrid size={20} /> },
    { name: "Following", href: "/following", icon: <Heart size={20} /> },
  ];

  return (
    <aside className={styles.sidebar}>
      <nav className={styles.navSection}>
        {navItems.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link 
              key={item.name} 
              href={item.href} 
              className={`${styles.navLink} ${isActive ? styles.navLinkActive : ""}`}
            >
              {item.icon}
              <span className={styles.navLabel}>{item.name}</span>
            </Link>
          );
        })}
      </nav>

      <div className={styles.divider} />

      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>Followed Channels</h2>
        
        {!mounted ? (
           <p className={styles.emptyChannels}>Loading...</p>
        ) : !isAuthenticated ? (
           <div style={{ padding: "0 8px" }}>
             <p className={styles.emptyChannels} style={{ marginBottom: "12px" }}>
               Log in to see your followed channels.
             </p>
             <button 
               onClick={() => openAuthModal("login")}
               style={{ 
                 background: "var(--bg-surface-hover)", 
                 border: "none", 
                 color: "var(--text-primary)", 
                 padding: "8px 16px", 
                 borderRadius: "8px", 
                 cursor: "pointer",
                 fontSize: "0.85rem",
                 fontWeight: 600,
                 width: "100%"
               }}>
               Log In
             </button>
           </div>
        ) : followedChannels.length === 0 ? (
          <p className={styles.emptyChannels}>You aren't following anyone yet.</p>
        ) : (
          <div className={styles.channelList}>
            {followedChannels.map((channel: any) => {
              return (
                <Link href={`/${channel.slug}`} key={channel.channelId} className={styles.channelItem}>
                  <div className={styles.channelInfo}>
                    <div className={styles.avatar}>
                      {channel.avatarUrl ? (
                        <img src={channel.avatarUrl} alt={channel.displayName} />
                      ) : (
                        <User size={20} color="var(--text-secondary)" />
                      )}
                    </div>
                    <div className={styles.details}>
                      <span className={styles.name}>{channel.displayName}</span>
                      <span className={styles.category}>{channel.liveStreamCount > 0 ? <span style={{color: "#ef4444"}}>Live</span> : "Offline"}</span>
                    </div>
                  </div>
                </Link>
              );
            })}
          </div>
        )}
      </div>
    </aside>
  );
}

export default function Sidebar() {
  return (
    <Suspense fallback={<aside className={styles.sidebar} />}>
      <SidebarContent />
    </Suspense>
  );
}
