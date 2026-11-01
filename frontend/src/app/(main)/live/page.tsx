"use client";

import useSWR from "swr";
import { fetchApi } from "@/lib/api";
import StreamCard from "@/components/feed/StreamCard";
import { Radio } from "lucide-react";
import styles from "./live.module.css";

export default function LivePage() {
  // Sort by recent for Live Now page
  const { data: pageResponse, isLoading, error } = useSWR(
    "/api/feed/live?sort=recent&size=50",
    fetchApi
  );

  if (isLoading) {
    return <div className={styles.emptyState}>Loading live streams...</div>;
  }

  if (error || !pageResponse) {
    return <div className={styles.emptyState}>Failed to load streams.</div>;
  }

  const streams = pageResponse.content || [];

  return (
    <div className={styles.pageContainer}>
      <header className={styles.header}>
        <Radio size={32} className={styles.headerIcon} />
        <h1 className={styles.title}>Live Now</h1>
      </header>

      {streams.length === 0 ? (
        <div className={styles.emptyState}>No one is currently live. Be the first!</div>
      ) : (
        <div className={styles.grid}>
          {streams.map((stream: any) => (
            <StreamCard
              key={stream.streamId}
              streamId={stream.streamId}
              channelName={stream.channelDisplayName}
              channelSlug={stream.channelSlug}
              title={stream.title}
              category={stream.category}
              viewers={stream.viewerCount || 0}
              thumbnailUrl={stream.thumbnailUrl}
              avatarUrl={stream.channelAvatarUrl}
            />
          ))}
        </div>
      )}
    </div>
  );
}
