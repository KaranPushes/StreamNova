"use client";

import useSWR from "swr";
import Link from "next/link";
import styles from "./page.module.css";
import StreamCard from "@/components/feed/StreamCard";
import { fetchApi } from "@/lib/api";
import { Play, Sparkles } from "lucide-react";

export default function Home() {
  // Fetch live streams from the Feed Service via API Gateway
  const { data, error, isLoading } = useSWR("/api/feed/live", fetchApi);

  const streams = data?.content || [];

  return (
    <div className={styles.container}>
      <header className={styles.hero}>
        <div className={styles.heroContent}>
          <div className={styles.badge}>
            <Sparkles size={14} className={styles.sparkleIcon} />
            <span>The Next Generation of Live Streaming</span>
          </div>
          <h1 className={styles.title}>
            Discover. Connect. <br />
            <span className={styles.textGradient}>StreamNova.</span>
          </h1>
          <p className={styles.subtitle}>
            Join the ultimate platform for gamers, creators, and communities to share moments in real-time. Experience ultra-low latency broadcasting like never before.
          </p>
          <div className={styles.heroActions}>
            <Link href="/trending" className={styles.primaryBtn}>
              <Play size={18} fill="currentColor" />
              Start Watching
            </Link>
            <Link href="/dashboard" className={styles.secondaryBtn}>
              Go Live Now
            </Link>
          </div>
        </div>
        
        {/* Animated Background Elements */}
        <div className={styles.glowBlob1} />
        <div className={styles.glowBlob2} />
      </header>

      <section className={styles.feed}>
        <div className={styles.feedHeader}>
          <h2>Live Now</h2>
          <Link href="/live" className={styles.viewAll}>View All</Link>
        </div>

        {isLoading && (
          <div className={styles.grid}>
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className={styles.emptyCard}>
                <div className={styles.loader} />
              </div>
            ))}
          </div>
        )}

        {error && (
          <div className={styles.emptyCard} style={{ color: 'var(--status-live)' }}>
            Failed to load streams. Make sure the backend services are running.
          </div>
        )}

        {!isLoading && !error && streams.length === 0 && (
          <div className={styles.emptyStateContainer}>
            <div className={styles.emptyStateBlob} />
            <h3>No one is currently live</h3>
            <p>Be the first to broadcast and share your gameplay with the world!</p>
            <Link href="/dashboard" className={styles.primaryBtn} style={{ marginTop: '16px' }}>
              Start Streaming
            </Link>
          </div>
        )}

        {!isLoading && !error && streams.length > 0 && (
          <div className={styles.grid}>
            {streams.map((stream: any) => (
              <StreamCard
                key={stream.streamId || stream.id}
                streamId={stream.streamId || stream.id}
                channelName={stream.channelDisplayName || stream.channelSlug || "Unknown Channel"}
                channelSlug={stream.channelSlug}
                title={stream.title || "Untitled Stream"}
                category={stream.category || "General"}
                viewers={stream.viewers || stream.viewerCount || 0}
                thumbnailUrl={stream.thumbnailUrl}
                avatarUrl={stream.channelAvatarUrl || stream.channel?.avatarUrl}
              />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
