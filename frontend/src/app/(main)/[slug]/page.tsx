"use client";

import { Suspense, useState, useEffect } from "react";
import useSWR, { mutate } from "swr";
import { useParams } from "next/navigation";
import styles from "./channel.module.css";
import StreamPlayer from "@/components/video/StreamPlayer";
import StreamChat from "@/components/chat/StreamChat";
import StreamCard from "@/components/feed/StreamCard";
import { User, Users } from "lucide-react";
import { fetchApi } from "@/lib/api";
import { useAuthStore } from "@/store/useAuthStore";

function FollowButton({ channelId }: { channelId: string }) {
  const { isAuthenticated, openAuthModal } = useAuthStore();
  const { data: status, mutate: mutateStatus, isLoading } = useSWR(
    isAuthenticated ? `/follows/channels/${channelId}/status` : null,
    fetchApi
  );

  const isFollowing = status?.following || false;

  const handleFollow = async () => {
    if (!isAuthenticated) {
      openAuthModal("login");
      return;
    }

    try {
      // Optimistic update
      mutateStatus({ following: !isFollowing }, false);
      
      if (isFollowing) {
        await fetchApi(`/follows/channels/${channelId}`, { method: "DELETE" });
      } else {
        await fetchApi(`/follows/channels/${channelId}`, { method: "POST" });
      }

      mutate(`/follows/channels/${channelId}/count`);
    } catch (err: any) {
      if (err.status === 409 || (err.message && err.message.includes("409"))) {
        // If 409 Conflict, it means they are already following it.
        mutateStatus({ following: true });
        mutate(`/follows/channels/${channelId}/count`);
      } else {
        // Revert optimistic update on failure
        mutateStatus({ following: isFollowing });
        alert("Failed to update follow status");
      }
    }
  };

  return (
    <button
      className={`${styles.followBtn} ${isFollowing ? styles.following : ''}`}
      onClick={handleFollow}
      disabled={isLoading}
    >
      {isLoading ? "..." : isFollowing ? "Following" : "Follow"}
    </button>
  );
}

function ChannelContent() {
  const params = useParams();
  const slug = params.slug as string;

  const { data: channel, error: channelError, isLoading: channelLoading } = useSWR(
    `/channels/profile/${slug}`,
    fetchApi
  );

  const { data: followerData } = useSWR(
    channel?.id ? `/follows/channels/${channel.id}/count` : null,
    fetchApi
  );

  const { data: streams, isLoading: streamsLoading } = useSWR(
    channel?.id ? `/streams/channel/${channel.id}` : null,
    fetchApi
  );

  if (channelLoading) {
    return <div className={styles.container}><div className={styles.loading}>Loading channel...</div></div>;
  }

  if (channelError || !channel) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>Channel not found or does not exist.</div>
      </div>
    );
  }

  const allStreams = streams || [];
  const liveStreams = allStreams.filter((s: any) => s.status === "LIVE");
  const pastStreams = allStreams.filter((s: any) => s.status === "ENDED");

  const activeStream = liveStreams.length > 0 ? liveStreams[0] : null;

  return (
    <div className={styles.container}>
      <div
        className={styles.banner}
        style={{ backgroundImage: channel.bannerUrl ? `url(${channel.bannerUrl.replace('8085', '8080')})` : "var(--accent-gradient)" }}
      />

      {activeStream ? (
        <div className={styles.activeStreamLayout}>
          <div className={styles.playerWrapper}>
            <StreamPlayer streamUrl={activeStream.playbackUrl} isLive={true} />
          </div>
          <div className={styles.chatWrapper}>
            <StreamChat streamId={activeStream.id} />
          </div>
        </div>
      ) : (
        <div className={styles.offlineHero}>
          <h2>{channel.displayName} is currently offline.</h2>
        </div>
      )}

      <div className={styles.channelHeader}>
        <div className={styles.avatar}>
          {channel.avatarUrl ? (
            <img src={channel.avatarUrl.replace('8085', '8080')} alt={channel.displayName} />
          ) : (
            <User size={40} color="var(--text-secondary)" />
          )}
        </div>
        <div className={styles.channelInfo}>
          <h1 className={styles.channelName}>{channel.displayName}</h1>
          <p className={styles.channelBio}>{channel.description || "Welcome to my channel!"}</p>
          <div className={styles.stats}>
            <Users size={16} />
            <span>{followerData?.followerCount || 0} Followers</span>
          </div>
        </div>
        <div className={styles.actions}>
          <FollowButton channelId={channel.id} />
        </div>
      </div>

      <hr className={styles.divider} />

      <div className={styles.streamsSection}>
        {liveStreams.length > 0 && (
          <div className={styles.streamCategory}>
            <h2>Live Now</h2>
            <div className={styles.grid}>
              {liveStreams.map((stream: any) => (
                <StreamCard
                  key={stream.id}
                  streamId={stream.id}
                  channelName={channel.displayName}
                  channelSlug={channel.slug}
                  title={stream.title}
                  category={stream.category}
                  viewers={0}
                  thumbnailUrl={stream.thumbnailUrl}
                  avatarUrl={channel.avatarUrl}
                  isLive={true}
                />
              ))}
            </div>
          </div>
        )}

        <div className={styles.streamCategory}>
          <h2>Past Streams</h2>
          {streamsLoading ? (
            <p>Loading streams...</p>
          ) : pastStreams.length > 0 ? (
            <div className={styles.grid}>
              {pastStreams.map((stream: any) => (
                <StreamCard
                  key={stream.id}
                  streamId={stream.id}
                  channelName={channel.displayName}
                  channelSlug={channel.slug}
                  title={stream.title}
                  category={stream.category}
                  viewers={0}
                  thumbnailUrl={stream.thumbnailUrl}
                  avatarUrl={channel.avatarUrl}
                  isLive={false}
                />
              ))}
            </div>
          ) : (
            <p className={styles.emptyText}>No past streams found.</p>
          )}
        </div>
      </div>
    </div>
  );
}

export default function ChannelPage() {
  return (
    <Suspense fallback={<div className={styles.container}><div className={styles.loading}>Loading channel...</div></div>}>
      <ChannelContent />
    </Suspense>
  );
}
