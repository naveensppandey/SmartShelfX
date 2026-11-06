import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationItem } from '../../core/models/models';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit {
  notifications: NotificationItem[] = [];
  filteredNotifications: NotificationItem[] = [];
  filter: 'ALL' | 'UNREAD' | 'READ' = 'ALL';
  loading: boolean = false;
  error: string | null = null;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.loading = true;
    this.error = null;
    this.notificationService.getAllNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.applyFilter();
        this.loading = false;
        // Also update unread count badge in layout header
        this.notificationService.fetchUnreadCount().subscribe();
      },
      error: (err) => {
        this.error = 'Failed to load notifications: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  setFilter(newFilter: 'ALL' | 'UNREAD' | 'READ'): void {
    this.filter = newFilter;
    this.applyFilter();
  }

  applyFilter(): void {
    if (this.filter === 'UNREAD') {
      this.filteredNotifications = this.notifications.filter((n) => !n.read);
    } else if (this.filter === 'READ') {
      this.filteredNotifications = this.notifications.filter((n) => n.read);
    } else {
      this.filteredNotifications = [...this.notifications];
    }
  }

  markAsRead(item: NotificationItem): void {
    if (item.read) return;
    this.notificationService.markAsRead(item.id).subscribe({
      next: (updated) => {
        item.read = true;
        this.applyFilter();
      }
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach((n) => (n.read = true));
        this.applyFilter();
      }
    });
  }

  getUnreadCount(): number {
    return this.notifications.filter((n) => !n.read).length;
  }
}
