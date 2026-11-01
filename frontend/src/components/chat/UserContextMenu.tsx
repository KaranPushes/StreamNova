import React from 'react';
import { Shield, Ban, Clock, Trash2, ShieldPlus, ShieldMinus } from 'lucide-react';
import styles from './UserContextMenu.module.css';
import { fetchApi } from '@/lib/api';

interface UserContextMenuProps {
  streamId: string;
  targetUserId: string;
  targetUsername: string;
  messageId: string;
  onClose: () => void;
  position: { x: number; y: number };
  isOwner?: boolean;
}

export default function UserContextMenu({ 
  streamId, targetUserId, targetUsername, messageId, onClose, position, isOwner 
}: UserContextMenuProps) {

  const handleAction = async (action: string) => {
    try {
      if (action === "DELETE_MESSAGE") {
        await fetchApi(`/api/chat/streams/${streamId}/messages/${messageId}`, { method: 'DELETE' });
      } else if (action === "BAN" || action === "TIMEOUT") {
        await fetchApi(`/api/chat/streams/${streamId}/moderation`, {
          method: 'POST',
          body: JSON.stringify({
            userId: targetUserId,
            action: action,
            expiresAt: action === "TIMEOUT" ? new Date(Date.now() + 300 * 1000).toISOString() : null
          })
        });
        alert(`Successfully applied ${action} to ${targetUsername}`);
      } else if (action === "ADD_MOD") {
        await fetchApi(`/api/chat/streams/${streamId}/moderators/${targetUserId}`, { method: 'POST' });
        alert(`${targetUsername} is now a moderator!`);
      } else if (action === "REMOVE_MOD") {
        await fetchApi(`/api/chat/streams/${streamId}/moderators/${targetUserId}`, { method: 'DELETE' });
        alert(`${targetUsername} is no longer a moderator.`);
      }
      onClose();
    } catch (err: any) {
      alert(err.message || `Failed to perform ${action}`);
    }
  };

  return (
    <>
      <div className={styles.overlay} onClick={onClose} />
      <div className={styles.menu} style={{ top: position.y, left: position.x }}>
        <div className={styles.header}>
          <span className={styles.username}>{targetUsername}</span>
        </div>
        <button className={styles.menuItem} onClick={() => handleAction("DELETE_MESSAGE")}>
          <Trash2 size={16} /> Delete Message
        </button>
        <button className={styles.menuItem} onClick={() => handleAction("TIMEOUT")}>
          <Clock size={16} /> Timeout (5m)
        </button>
        <button className={styles.menuItem} onClick={() => handleAction("BAN")}>
          <Ban size={16} /> Ban User
        </button>
        {isOwner && (
          <>
            <div className={styles.divider} />
            <button className={styles.menuItem} onClick={() => handleAction("ADD_MOD")}>
              <ShieldPlus size={16} /> Add Mod
            </button>
            <button className={styles.menuItem} onClick={() => handleAction("REMOVE_MOD")}>
              <ShieldMinus size={16} /> Remove Mod
            </button>
          </>
        )}
      </div>
    </>
  );
}
