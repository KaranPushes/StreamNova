"use client";

import Link from "next/link";
import { User } from "lucide-react";
import styles from "./StreamCard.module.css";

interface StreamCardProps {
  streamId: string;
  channelName: string;
  channelSlug?: string;
  title: string;
  category: string;
  viewers: number;
  thumbnailUrl?: string;
  avatarUrl?: string;
  isLive?: boolean;
}

export default function StreamCard({
  channelName,
  channelSlug,
  title,
  category,
  viewers,
  thumbnailUrl,
  avatarUrl,
  isLive = true
}: StreamCardProps) {
  // Use a generic placeholder gradient if no thumbnail is provided
  const bgStyle = thumbnailUrl
    ? { backgroundImage: `url(${thumbnailUrl.replace('8085', '8080')})` }
    : { background: "var(--accent-gradient)" };

  const targetUrl = channelSlug ? `/${channelSlug}` : `/${channelName.toLowerCase()}`;

  return (
    <Link href={targetUrl} className={styles.card}>
      <div className={styles.thumbnailWrapper} style={bgStyle}>
        {isLive && <div className={styles.liveBadge}>LIVE</div>}
        {isLive && (
          <div className={styles.viewersBadge}>
            {viewers.toLocaleString()} viewers
          </div>
        )}
      </div>
      
      <div className={styles.info}>
        <div className={styles.avatar}>
          {avatarUrl ? (
            <img src={avatarUrl.replace('8085', '8080')} alt={channelName} />
          ) : (
            <User size={20} color="var(--text-secondary)" />
          )}
        </div>
        
        <div className={styles.details}>
          <h3 className={styles.title} title={title}>
            {title}
          </h3>
          <span className={styles.channelName}>{channelName}</span>
          <span className={styles.category}>{category}</span>
        </div>
      </div>
    </Link>
  );
}
