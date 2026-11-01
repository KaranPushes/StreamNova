"use client";

import Link from "next/link";
import { Search, Video, User, Settings } from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import NotificationDropdown from "./NotificationDropdown";
import styles from "./Navbar.module.css";

export default function Navbar() {
  const { isAuthenticated, user, openAuthModal, logout } = useAuthStore();

  return (
    <nav className={styles.navbar}>
      <Link href="/" className={styles.logo}>
        <Video className={styles.logoIcon} size={28} strokeWidth={2.5} />
        <span>StreamNova</span>
      </Link>

      <form className={styles.searchContainer} onSubmit={(e) => {
        e.preventDefault();
        const formData = new FormData(e.currentTarget);
        const q = formData.get("q") as string;
        if (q.trim()) {
          window.location.href = `/search?q=${encodeURIComponent(q.trim())}`;
        }
      }}>
        <Search className={styles.searchIcon} />
        <input 
          type="text" 
          name="q"
          placeholder="Search channels, streams, or categories..." 
          className={styles.searchInput}
        />
      </form>

      <div className={styles.actions}>
        {isAuthenticated && <NotificationDropdown />}

        {isAuthenticated ? (
          <>
            <Link href="/dashboard" className={styles.iconButton} aria-label="Creator Dashboard">
              <Settings size={20} />
            </Link>
            <Link href="/settings" className={styles.userProfile} style={{ textDecoration: 'none' }}>
              <div className={styles.avatar}>
                {user?.avatarUrl ? (
                  <img src={user.avatarUrl} alt={user.displayName} />
                ) : (
                  <User size={20} color="var(--text-secondary)" />
                )}
              </div>
              <span className={styles.userName}>{user?.displayName}</span>
            </Link>
            <button className={styles.loginBtn} onClick={logout}>Log Out</button>
          </>
        ) : (
          <>
            <button className={styles.loginBtn} onClick={() => openAuthModal("login")}>Log In</button>
            <button className={styles.registerBtn} onClick={() => openAuthModal("register")}>Sign Up</button>
          </>
        )}
      </div>
    </nav>
  );
}
