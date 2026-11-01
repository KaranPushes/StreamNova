"use client";

import { useEffect, useRef, useState } from "react";
import Hls from "hls.js";
import { VideoOff } from "lucide-react";
import styles from "./StreamPlayer.module.css";

interface StreamPlayerProps {
  streamUrl: string;
  isLive: boolean;
}

export default function StreamPlayer({ streamUrl, isLive }: StreamPlayerProps) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!isLive || !streamUrl || !videoRef.current) return;

    let hls: Hls | null = null;

    if (Hls.isSupported()) {
      hls = new Hls({
        enableWorker: true,
        lowLatencyMode: true,
      });

      hls.loadSource(streamUrl);
      hls.attachMedia(videoRef.current);
      
      hls.on(Hls.Events.ERROR, (event, data) => {
        if (data.fatal) {
          console.error("Fatal HLS error:", data);
          setError(true);
          hls?.destroy();
        }
      });

    } else if (videoRef.current.canPlayType("application/vnd.apple.mpegurl")) {
      // Fallback for Safari (native HLS support)
      videoRef.current.src = streamUrl;
      videoRef.current.addEventListener('error', () => setError(true));
    }

    return () => {
      if (hls) hls.destroy();
    };
  }, [streamUrl, isLive]);

  if (!isLive || error) {
    return (
      <div className={styles.playerContainer}>
        <div className={styles.offlineState}>
          <VideoOff size={48} className={styles.offlineIcon} />
          <span className={styles.offlineText}>
            {error ? "Stream Offline or Encoutered an Error" : "Stream is Currently Offline"}
          </span>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.playerContainer}>
      <video
        ref={videoRef}
        className={styles.video}
        controls
        autoPlay
        muted // Muted by default to allow autoplay in most browsers
      />
    </div>
  );
}
