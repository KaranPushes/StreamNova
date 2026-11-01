"use client";

import useSWR from "swr";
import Link from "next/link";
import { fetchApi } from "@/lib/api";
import { LayoutGrid } from "lucide-react";
import styles from "./categories.module.css";

export default function CategoriesPage() {
  const { data: categories, isLoading, error } = useSWR(
    "/api/feed/categories",
    fetchApi
  );

  if (isLoading) {
    return <div className={styles.emptyState}>Loading categories...</div>;
  }

  if (error || !categories) {
    return <div className={styles.emptyState}>Failed to load categories.</div>;
  }

  return (
    <div className={styles.pageContainer}>
      <header className={styles.header}>
        <LayoutGrid size={32} className={styles.headerIcon} />
        <h1 className={styles.title}>Browse Categories</h1>
      </header>

      <div className={styles.grid}>
        {categories.map((cat: any) => (
          <Link href={`/categories/${encodeURIComponent(cat.name)}`} key={cat.name} className={styles.card}>
            <div className={styles.cardContent}>
              <h3 className={styles.name}>{cat.name}</h3>
              <span className={styles.viewers}>{cat.liveStreamCount || 0} Live Streams</span>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
