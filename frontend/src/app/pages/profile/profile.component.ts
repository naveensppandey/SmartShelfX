import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { User, JwtResponse } from '../../core/models/models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  currentUser: JwtResponse | null = null;
  profile: User | null = null;
  loading: boolean = true;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.fetchProfile();
  }

  fetchProfile(): void {
    this.loading = true;
    this.authService.getCurrentUserProfile().subscribe({
      next: (userData) => {
        this.profile = userData;
        this.loading = false;
      },
      error: () => {
        // Fallback to local session user if /me fails
        this.loading = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getRolePermissions(): string[] {
    const role = this.currentUser?.role;
    switch (role) {
      case 'ADMIN':
        return [
          'Full CRUD access to Product Catalog & Categories',
          'Execute Stock-In and Stock-Out operations',
          'Approve, Reject, and Receive Purchase Orders',
          'Trigger AI Demand Forecasting & Restocking models',
          'User Management & Account Status control',
          'View all financial analytics and audit trails'
        ];
      case 'WAREHOUSE_MANAGER':
        return [
          'Full CRUD access to Product Catalog & Categories',
          'Execute Stock-In and Stock-Out operations',
          'Create Purchase Orders and mark them Received',
          'Run AI Demand Forecasting analysis',
          'View inventory movement trends and notifications'
        ];
      case 'VENDOR':
        return [
          'View assigned products and inventory thresholds',
          'View purchase orders assigned to your vendor line',
          'Monitor restocking demands'
        ];
      default:
        return ['Standard Read-Only access'];
    }
  }
}
