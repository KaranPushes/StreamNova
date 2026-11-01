"use client";

import { useSearchParams } from "next/navigation";
import useSWR from "swr";
import Link from "next/link";
import { fetchApi } from "@/lib/api";
import StreamCard from "@/components/feed/StreamCard";
import { User, LayoutGrid, Radio } from "lucide-react";
import styles from "./search.module.css";
import { Suspense } from "react";

function SearchContent() {
  const searchParams = useSearchParams();
  const query = searchParams.get("q") || "";

  const { data: results, isLoading, error } = useSWR(
    query ? `/api/feed/search?q=${encodeURIComponent(query)}` : null,
    fetchApi
  );

  if (!query) {
    return <div className={styles.emptyState}>Please enter a search term above.</div>;
  }

  if (isLoading) {
    return <div className={styles.emptyState}>Searching the galaxy...</div>;
  }

  if (error || !results) {
    return <div className={styles.emptyState}>Failed to load search results.</div>;
  }

  const hasChannels = results.channels?.length > 0;
  const hasCategories = results.categories?.length > 0;
  const hasStreams = results.streams?.length > 0;

  if (!hasChannels && !hasCategories && !hasStreams) {
    return <div className={styles.emptyState}>No results found for "{query}". Try something else!</div>;
  }

  return (
    <div className={styles.searchPage}>
      <h1 className={styles.title}>Search Results for "{query}"</h1>

      {hasChannels && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>
            <User size={20} className={styles.sectionIcon} />
            Channels
          </h2>
          <div className={styles.channelsGrid}>
            {results.channels.map((channel: any) => (
              <Link href={`/${channel.slug}`} key={channel.id} className={styles.channelCard}>
                <div className={styles.channelAvatar}>
                  {channel.avatarUrl ? (
                    <img src={channel.avatarUrl} alt={channel.displayName} />
                  ) : (
                    <User size={32} color="var(--text-secondary)" />
                  )}
                </div>
                <div className={styles.channelInfo}>
                  <h3 className={styles.channelName}>{channel.displayName}</h3>
                  <span className={styles.channelFollowers}>{channel.followerCount || 0} Followers</span>
                </div>
              </Link>
            ))}
          </div>
        </section>
      )}

      {hasCategories && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>
            <LayoutGrid size={20} className={styles.sectionIcon} />
            Categories
          </h2>
          <div className={styles.categoriesGrid}>
            {results.categories.map((cat: any) => (
              <Link href={`/categories/${encodeURIComponent(cat.name)}`} key={cat.name} className={styles.categoryCard}>
                <div className={styles.categoryInfo}>
                  <h3 className={styles.categoryName}>{cat.name}</h3>
                  <span className={styles.categoryViewers}>{cat.liveStreamCount} Live Streams</span>
                </div>
              </Link>
            ))}
          </div>
        </section>
      )}

      {hasStreams && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>
            <Radio size={20} className={styles.sectionIcon} />
            Live Streams
          </h2>
          <div className={styles.streamsGrid}>
            {results.streams.map((stream: any) => (
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
        </section>
      )}
    </div>
  );
}

export default function SearchPage() {
  return (
    <Suspense fallback={<div style={{ padding: 48, textAlign: 'center' }}>Loading search...</div>}>
      <SearchContent />
    </Suspense>
  );
}
