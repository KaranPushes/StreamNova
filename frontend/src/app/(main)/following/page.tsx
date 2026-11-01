"use client";

import useSWR from "swr";
import Link from "next/link";
import { fetchApi } from "@/lib/api";
import { Heart, User } from "lucide-react";
import styles from "./following.module.css";
import { useAuthStore } from "@/store/useAuthStore";
import { useEffect, useState } from "react";

export default function FollowingPage() {
  const { isAuthenticated } = useAuthStore();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const { data: pageResponse, isLoading, error } = useSWR(
    mounted && isAuthenticated ? "/api/feed/following?size=50" : null,
    fetchApi
  );

  if (!mounted) return null;

  if (!isAuthenticated) {
    return (
      <div className={styles.emptyState}>
        <h2>You must be logged in to view your followed channels.</h2>
      </div>
    );
  }

  if (isLoading) {
    return <div className={styles.emptyState}>Loading followed channels...</div>;
  }

  if (error || !pageResponse) {
    return <div className={styles.emptyState}>Failed to load followed channels.</div>;
  }

  const follows = pageResponse.content || [];

  return (
    <div className={styles.pageContainer}>
      <header className={styles.header}>
        <Heart size={32} className={styles.headerIcon} />
        <h1 className={styles.title}>Following</h1>
      </header>

      {follows.length === 0 ? (
        <div className={styles.emptyState}>
          <h2>You aren't following anyone yet!</h2>
          <p>Discover new creators on the platform and hit the follow button.</p>
        </div>
      ) : (
        <div className={styles.grid}>
          {follows.map((channel: any) => {
            return (
              <Link href={`/${channel.slug}`} key={channel.channelId} className={styles.card}>
                <div className={styles.avatar}>
                  {channel.avatarUrl ? (
                    <img src={channel.avatarUrl} alt={channel.displayName} />
                  ) : (
                    <User size={40} color="var(--text-secondary)" />
                  )}
                </div>
                <div className={styles.info}>
                  <h3 className={styles.name}>{channel.displayName}</h3>
                  <span className={styles.status}>
                    {channel.liveStreamCount > 0 ? <span style={{color: "#ef4444"}}>Live Now</span> : "Offline"}
                  </span>
                </div>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}
