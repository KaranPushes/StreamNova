"use client";

import useSWR from "swr";
import { fetchApi } from "@/lib/api";
import StreamCard from "@/components/feed/StreamCard";
import { TrendingUp } from "lucide-react";
import styles from "./trending.module.css";

export default function TrendingPage() {
  // We fetch live streams for trending. You can adjust the API query params if the backend supports sorting by viewers.
  const { data: pageResponse, isLoading, error } = useSWR(
    "/api/feed/live?size=50",
    fetchApi
  );

  if (isLoading) {
    return <div className={styles.emptyState}>Loading trending streams...</div>;
  }

  if (error || !pageResponse) {
    return <div className={styles.emptyState}>Failed to load streams.</div>;
  }

  const streams = pageResponse.content || [];

  return (
    <div className={styles.pageContainer}>
      <header className={styles.header}>
        <TrendingUp size={32} className={styles.headerIcon} />
        <h1 className={styles.title}>Trending</h1>
      </header>

      {streams.length === 0 ? (
        <div className={styles.emptyState}>No trending streams at the moment.</div>
      ) : (
        <div className={styles.grid}>
          {streams.map((stream: any, index: number) => (
            <div key={stream.streamId} className={styles.trendingWrapper}>
              <span className={styles.rank}>#{index + 1}</span>
              <div style={{ flex: 1 }}>
                <StreamCard 
                  streamId={stream.streamId}
                  channelName={stream.channelDisplayName}
                  channelSlug={stream.channelSlug}
                  title={stream.title}
                  category={stream.category}
                  viewers={stream.viewerCount || 0}
                  thumbnailUrl={stream.thumbnailUrl}
                  avatarUrl={stream.channelAvatarUrl}
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
