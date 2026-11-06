import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { User, UserRole } from '../../core/models/models';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  loading: boolean = false;
  error: string | null = null;
  successMessage: string | null = null;

  searchTerm: string = '';
  roleFilter: string = 'ALL';

  currentUserId: number | null = null;
  isAdmin: boolean = false;

  constructor(
    private userService: UserService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const current = this.authService.getCurrentUser();
    this.currentUserId = current?.id || null;
    this.isAdmin = current?.role === 'ADMIN';

    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = null;
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load users: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredUsers = this.users.filter((u) => {
      const matchesRole = this.roleFilter === 'ALL' || u.role === this.roleFilter;
      const matchesSearch =
        !term ||
        u.username.toLowerCase().includes(term) ||
        u.fullName.toLowerCase().includes(term) ||
        u.email.toLowerCase().includes(term);
      return matchesRole && matchesSearch;
    });
  }

  toggleStatus(user: User): void {
    if (user.id === this.currentUserId) {
      alert('You cannot deactivate your own active admin account.');
      return;
    }

    const action = user.enabled ? 'deactivate' : 'activate';
    if (!confirm(`Are you sure you want to ${action} user "${user.username}"?`)) {
      return;
    }

    this.userService.toggleUserStatus(user.id).subscribe({
      next: (updated) => {
        user.enabled = updated.enabled;
        this.successMessage = `User ${user.username} is now ${user.enabled ? 'Active' : 'Disabled'}.`;
        setTimeout(() => (this.successMessage = null), 4000);
      },
      error: (err) => {
        this.error = 'Failed to update user status: ' + (err.error?.message || err.message);
      }
    });
  }
}
