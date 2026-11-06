import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Return a UrlTree to redirect to /login
  // In modern Angular, returning a UrlTree cleanly redirects the router without canceling navigation
  return router.createUrlTree(['/login']);
};
