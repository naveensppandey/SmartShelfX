import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { JwtResponse, User, UserRole } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'smartshelfx_jwt';
  private readonly USER_KEY = 'smartshelfx_user';

  private inMemoryToken: string | null = null;
  private inMemoryUser: JwtResponse | null = null;

  private currentUserSubject = new BehaviorSubject<JwtResponse | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.inMemoryUser = this.getStoredUser();
    this.inMemoryToken = this.getToken();
  }

  login(credentials: { username: string; password: string }): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((res) => this.setSession(res))
    );
  }

  register(data: any): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/register`, data).pipe(
      tap((res) => this.setSession(res))
    );
  }

  getCurrentUserProfile(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
  }

  logout(): void {
    this.inMemoryToken = null;
    this.inMemoryUser = null;
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
      }
    } catch {}
    this.currentUserSubject.next(null);
  }

  public getToken(): string | null {
    if (this.inMemoryToken) {
      return this.inMemoryToken;
    }
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        return localStorage.getItem(this.TOKEN_KEY);
      }
    } catch {}
    return null;
  }

  public isAuthenticated(): boolean {
    return !!this.getToken();
  }

  public getUserRole(): UserRole | null {
    const user = this.currentUserSubject.value || this.inMemoryUser;
    return user ? user.role : null;
  }

  public getCurrentUser(): JwtResponse | null {
    return this.currentUserSubject.value || this.inMemoryUser;
  }

  public hasRole(requiredRoles: UserRole[]): boolean {
    const role = this.getUserRole();
    if (!role) return false;
    return requiredRoles.includes(role);
  }

  private setSession(authResult: JwtResponse): void {
    this.inMemoryToken = authResult.token;
    this.inMemoryUser = authResult;
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.setItem(this.TOKEN_KEY, authResult.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(authResult));
      }
    } catch {}
    this.currentUserSubject.next(authResult);
  }

  private getStoredUser(): JwtResponse | null {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        const userJson = localStorage.getItem(this.USER_KEY);
        if (!userJson) return null;
        return JSON.parse(userJson);
      }
    } catch {}
    return null;
  }
}
