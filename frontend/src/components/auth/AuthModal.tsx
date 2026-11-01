"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { X } from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { fetchApi } from "@/lib/api";
import styles from "./AuthModal.module.css";

const loginSchema = z.object({
  email: z.string().email({ message: "Invalid email address" }),
  password: z.string().min(6, { message: "Password must be at least 6 characters" }),
});

const registerSchema = loginSchema.extend({
  username: z.string().min(3, { message: "Username must be at least 3 characters" }),
  displayName: z.string().min(1, { message: "Display name is required" }),
});

type LoginFormData = z.infer<typeof loginSchema>;
type RegisterFormData = z.infer<typeof registerSchema>;

export default function AuthModal() {
  const { isAuthModalOpen, authModalView, closeAuthModal, setAuthModalView, login } = useAuthStore();
  const [isLoading, setIsLoading] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const isLogin = authModalView === "login";

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<RegisterFormData>({
    resolver: zodResolver(isLogin ? loginSchema : registerSchema),
  });

  if (!isAuthModalOpen) return null;

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true);
    setServerError(null);
    try {
      if (isLogin) {
        // Log in
        const response = await fetchApi<{ accessToken: string }>("/auth/login", {
          method: "POST",
          body: JSON.stringify({ email: data.email, password: data.password }),
        });
        
        // After getting token, we need the user profile
        // We set the token temporarily in the store to make the /auth/me call
        useAuthStore.setState({ accessToken: response.accessToken });
        
        const userProfile = await fetchApi<any>("/auth/me");
        login(userProfile, response.accessToken);
        
      } else {
        // Register
        await fetchApi("/auth/register", {
          method: "POST",
          body: JSON.stringify({
            email: data.email,
            password: data.password,
            username: data.username,
            displayName: data.displayName
          }),
        });
        
        // Auto-login after register
        const loginResponse = await fetchApi<{ accessToken: string }>("/auth/login", {
          method: "POST",
          body: JSON.stringify({ email: data.email, password: data.password }),
        });
        
        useAuthStore.setState({ accessToken: loginResponse.accessToken });
        const userProfile = await fetchApi<any>("/auth/me");
        
        login(userProfile, loginResponse.accessToken);
      }
      
      setIsLoading(false);
      reset();
      
    } catch (error: any) {
      console.error("Auth error:", error);
      setServerError(error.message || "Failed to authenticate. Please try again.");
      // Clear invalid token if it was set during the flow
      if (!useAuthStore.getState().isAuthenticated) {
         useAuthStore.setState({ accessToken: null });
      }
      setIsLoading(false);
    }
  };

  const toggleView = () => {
    reset();
    setServerError(null);
    setAuthModalView(isLogin ? "register" : "login");
  };

  return (
    <div className={styles.overlay} onClick={closeAuthModal}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <button className={styles.closeButton} onClick={closeAuthModal}>
          <X size={24} />
        </button>

        <div className={styles.header}>
          <h2 className={styles.title}>{isLogin ? "Welcome Back" : "Join StreamNova"}</h2>
          <p className={styles.subtitle}>
            {isLogin ? "Log in to chat and follow streams" : "Create an account to start broadcasting"}
          </p>
        </div>

        <form className={styles.form} onSubmit={handleSubmit(onSubmit)}>
          {!isLogin && (
            <>
              <div className={styles.inputGroup}>
                <label className={styles.label}>Username</label>
                <input
                  type="text"
                  className={styles.input}
                  placeholder="streamer123"
                  {...register("username")}
                />
                {errors.username && <span className={styles.errorText}>{errors.username.message}</span>}
              </div>

              <div className={styles.inputGroup}>
                <label className={styles.label}>Display Name</label>
                <input
                  type="text"
                  className={styles.input}
                  placeholder="Streamer Name"
                  {...register("displayName")}
                />
                {errors.displayName && <span className={styles.errorText}>{errors.displayName.message}</span>}
              </div>
            </>
          )}

          <div className={styles.inputGroup}>
            <label className={styles.label}>Email</label>
            <input
              type="email"
              className={styles.input}
              placeholder="you@example.com"
              {...register("email")}
            />
            {errors.email && <span className={styles.errorText}>{errors.email.message}</span>}
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>Password</label>
            <input
              type="password"
              className={styles.input}
              placeholder="••••••••"
              {...register("password")}
            />
            {errors.password && <span className={styles.errorText}>{errors.password.message}</span>}
          </div>

          {serverError && <div className={styles.serverError}>{serverError}</div>}

          <button type="submit" className={styles.submitBtn} disabled={isLoading}>
            {isLoading ? "Please wait..." : isLogin ? "Log In" : "Sign Up"}
          </button>
        </form>

        <div className={styles.footer}>
          {isLogin ? "Don't have an account?" : "Already have an account?"}
          <button className={styles.link} onClick={toggleView}>
            {isLogin ? "Sign Up" : "Log In"}
          </button>
        </div>
      </div>
    </div>
  );
}
