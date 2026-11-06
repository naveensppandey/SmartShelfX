import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotificationItem } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly apiUrl = `${environment.apiUrl}/notifications`;

  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  getAllNotifications(): Observable<NotificationItem[]> {
    return this.http.get<NotificationItem[]>(this.apiUrl);
  }

  getUnreadNotifications(): Observable<NotificationItem[]> {
    return this.http.get<NotificationItem[]>(`${this.apiUrl}/unread`);
  }

  fetchUnreadCount(): Observable<{ unreadCount: number }> {
    return this.http.get<{ unreadCount: number }>(`${this.apiUrl}/unread-count`).pipe(
      tap((res) => this.unreadCountSubject.next(res.unreadCount))
    );
  }

  markAsRead(id: number): Observable<NotificationItem> {
    return this.http.put<NotificationItem>(`${this.apiUrl}/${id}/read`, {}).pipe(
      tap(() => this.decrementCount())
    );
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/mark-all-read`, {}).pipe(
      tap(() => this.unreadCountSubject.next(0))
    );
  }

  private decrementCount(): void {
    const current = this.unreadCountSubject.value;
    if (current > 0) {
      this.unreadCountSubject.next(current - 1);
    }
  }
}
