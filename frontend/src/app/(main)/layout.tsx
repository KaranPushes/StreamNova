import Navbar from "@/components/layout/Navbar";
import Sidebar from "@/components/layout/Sidebar";

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="app-container">
      <div className="navbar-wrapper">
        <Navbar />
      </div>
      <div className="sidebar-wrapper">
        <Sidebar />
      </div>
      <main className="main-content">
        {children}
      </main>
    </div>
  );
}
