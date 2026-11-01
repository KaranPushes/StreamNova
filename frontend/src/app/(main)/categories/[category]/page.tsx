"use client";

import { use } from "react";
import useSWR from "swr";
import { fetchApi } from "@/lib/api";
import StreamCard from "@/components/feed/StreamCard";
import styles from "./categoryDetail.module.css";
import { LayoutGrid } from "lucide-react";

export default function CategoryDetailPage({ params }: { params: Promise<{ category: string }> }) {
  const { category: encodedCategory } = use(params);
  const category = decodeURIComponent(encodedCategory);

  const { data: pageResponse, isLoading, error } = useSWR(
    `/api/feed/categories/${encodeURIComponent(category)}`,
    fetchApi
  );

  if (isLoading) {
    return <div className={styles.emptyState}>Loading streams for {category}...</div>;
  }

  if (error || !pageResponse) {
    return <div className={styles.emptyState}>Failed to load streams.</div>;
  }

  const streams = pageResponse.content || [];

  return (
    <div className={styles.pageContainer}>
      <header className={styles.header}>
        <LayoutGrid size={32} className={styles.headerIcon} />
        <div>
          <h1 className={styles.title}>{category}</h1>
          <p className={styles.subtitle}>{streams.length} Live Streams</p>
        </div>
      </header>

      {streams.length === 0 ? (
        <div className={styles.emptyState}>No one is currently streaming in {category}.</div>
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
