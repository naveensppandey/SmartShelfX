import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { MainLayoutComponent } from './layout/main-layout.component';
import { LoginComponent } from './pages/login/login.component';

export const routes: Routes = [
  // 1. Root redirect: If visiting '/', immediately go to login
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  // 2. Public Route: Login & Registration (Eagerly loaded for instant presentation)
  {
    path: 'login',
    component: LoginComponent
  },

  // 3. Authenticated Portal Routes wrapped in Main Layout
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./pages/dashboard/dashboard.component').then((m) => m.DashboardComponent)
      },
      {
        path: 'products',
        loadComponent: () =>
          import('./pages/products/products.component').then((m) => m.ProductsComponent)
      },
      {
        path: 'products/new',
        loadComponent: () =>
          import('./pages/product-form/product-form.component').then((m) => m.ProductFormComponent)
      },
      {
        path: 'products/edit/:id',
        loadComponent: () =>
          import('./pages/product-form/product-form.component').then((m) => m.ProductFormComponent)
      },
      {
        path: 'categories',
        loadComponent: () =>
          import('./pages/categories/categories.component').then((m) => m.CategoriesComponent)
      },
      {
        path: 'transactions',
        loadComponent: () =>
          import('./pages/transactions/transactions.component').then((m) => m.TransactionsComponent)
      },
      {
        path: 'forecasting',
        loadComponent: () =>
          import('./pages/forecasting/forecasting.component').then((m) => m.ForecastingComponent)
      },
      {
        path: 'purchase-orders',
        loadComponent: () =>
          import('./pages/purchase-orders/purchase-orders.component').then((m) => m.PurchaseOrdersComponent)
      },
      {
        path: 'analytics',
        loadComponent: () =>
          import('./pages/analytics/analytics.component').then((m) => m.AnalyticsComponent)
      },
      {
        path: 'notifications',
        loadComponent: () =>
          import('./pages/notifications/notifications.component').then((m) => m.NotificationsComponent)
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./pages/users/users.component').then((m) => m.UsersComponent)
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./pages/profile/profile.component').then((m) => m.ProfileComponent)
      }
    ]
  },

  // 4. Fallback wildcard: redirect any unmatched URL to login
  {
    path: '**',
    redirectTo: 'login'
  }
];
