import Link from "next/link";
import { Video, Settings, BarChart3, Home, LayoutDashboard, LogOut } from "lucide-react";
import styles from "./layout.module.css";
import React from "react";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className={styles.studioLayout}>
      <aside className={styles.studioSidebar}>
        <div className={styles.sidebarHeader}>
          <Video size={24} color="#ef4444" />
          <h2>Creator Studio</h2>
        </div>
        
        <nav className={styles.sidebarNav}>
          <Link href="/dashboard" className={styles.navLink}>
            <LayoutDashboard size={20} />
            Dashboard
          </Link>
          <Link href="/dashboard/analytics" className={styles.navLink}>
            <BarChart3 size={20} />
            Analytics
          </Link>
        </nav>
        
        <div className={styles.sidebarFooter}>
          <Link href="/" className={styles.navLink}>
            <Home size={20} />
            Back to App
          </Link>
        </div>
      </aside>
      
      <main className={styles.studioMain}>
        {children}
      </main>
    </div>
  );
}
