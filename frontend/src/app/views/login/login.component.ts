/**
 * Login Component
 * Refactored following SOLID principles:
 * - Single Responsibility: Only handles login UI logic
 * - Open/Closed: Extensible through service injection
 * - Liskov Substitution: Can be substituted with any login implementation
 * - Interface Segregation: Uses focused interfaces (LoginFormData, ValidationError)
 * - Dependency Inversion: Depends on LoginService abstraction
 */

import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LoginService } from './services/login.service';
import {
  LoginFormData,
  ValidationError,
  OAuthProvider,
} from './types/login.types';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  // Expose enums to template
  readonly OAuthProvider = OAuthProvider;

  // Reactive state using signals for better performance
  formData = signal<Partial<LoginFormData>>({
    email: '',
    password: '',
  });

  errors = signal<ValidationError[]>([]);
  isSubmitting = signal<boolean>(false);
  rememberMe = signal<boolean>(false);

  constructor(
    private loginService: LoginService,
    private router: Router
  ) {}

  /**
   * Update form field value
   * Uses descriptive parameter names
   * @param field - Field name to update
   * @param value - New value for the field
   */
  updateField(field: keyof LoginFormData, value: string): void {
    const current = this.formData();
    this.formData.set({ ...current, [field]: value });
  }

  /**
   * Handle email input event
   * @param event - Input event from email field
   */
  onEmailInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.updateField('email', target.value);
  }

  /**
   * Handle password input event
   * @param event - Input event from password field
   */
  onPasswordInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.updateField('password', target.value);
  }

  /**
   * Handle remember me checkbox change
   * @param event - Change event from checkbox
   */
  onRememberMeChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.rememberMe.set(target.checked);
  }

  /**
   * Handle form submission
   * Prevents default form behavior and calls login
   * @param event - Form submission event
   */
  onFormSubmit(event: Event): void {
    event.preventDefault();
    this.submitLogin();
  }

  /**
   * Submit login form
   * Uses early returns to reduce nesting
   * Implements robust error handling
   */
  submitLogin(): void {
    // Early return if already submitting
    if (this.isSubmitting()) {
      return;
    }

    // Clear previous errors
    this.errors.set([]);

    const data = this.formData();

    // Validate form data
    const validationErrors = this.loginService.validateForm(data);

    // Early return if validation fails
    if (validationErrors.length > 0) {
      this.errors.set(validationErrors);
      return;
    }

    // Set submitting state
    this.isSubmitting.set(true);

    // Attempt login
    this.loginService.login(data as LoginFormData).subscribe({
      next: (result) => {
        this.isSubmitting.set(false);
        
        // Early return if login failed
        if (!result.success) {
          this.errors.set([{ field: 'form', message: result.message }]);
          return;
        }

        // Store auth token
        if (result.token) {
          this.loginService.storeAuthToken(result.token);
        }

        // Navigate to redirect URL or home
        const redirectUrl = result.redirectUrl || '/home';
        this.navigateToRoute(redirectUrl);
      },
      error: (error) => {
        this.isSubmitting.set(false);
        this.errors.set([
          { field: 'form', message: error.message || 'Error al iniciar sesión' },
        ]);
      },
    });
  }

  /**
   * Handle OAuth login
   * Uses early returns for cleaner flow
   * @param provider - OAuth provider (Google, Facebook)
   */
  loginWithOAuth(provider: OAuthProvider): void {
    // Early return if already submitting
    if (this.isSubmitting()) {
      return;
    }

    // Clear previous errors
    this.errors.set([]);
    
    // Set submitting state
    this.isSubmitting.set(true);

    // Attempt OAuth login
    this.loginService.loginWithOAuth(provider).subscribe({
      next: (result) => {
        this.isSubmitting.set(false);
        
        // Early return if login failed
        if (!result.success) {
          this.errors.set([{ field: 'form', message: result.message }]);
          return;
        }

        // Store auth token
        if (result.token) {
          this.loginService.storeAuthToken(result.token);
        }

        // Navigate to redirect URL or home
        const redirectUrl = result.redirectUrl || '/home';
        this.navigateToRoute(redirectUrl);
      },
      error: (error) => {
        this.isSubmitting.set(false);
        this.errors.set([
          { field: 'form', message: error.message || 'Error al iniciar sesión' },
        ]);
      },
    });
  }

  /**
   * Navigate to register page
   */
  navigateToRegister(): void {
    this.navigateToRoute('/register');
  }

  /**
   * Navigate to password reset page
   * TODO: Implement password reset flow
   */
  navigateToPasswordReset(): void {
    // For now, show error message through the error system
    // TODO: Navigate to password reset page when implemented
    this.errors.set([
      { 
        field: 'form', 
        message: 'Funcionalidad de recuperación de contraseña próximamente' 
      }
    ]);
  }

  /**
   * Get error message for a specific field
   * Uses early returns for clarity
   * @param field - Field name to check
   * @returns error message or null
   */
  getFieldError(field: string): string | null {
    const errors = this.errors();

    // Early return if no errors
    if (errors.length === 0) {
      return null;
    }

    const error = errors.find((e) => e.field === field);
    return error ? error.message : null;
  }

  /**
   * Check if field has error
   * Optimized to avoid double array traversal
   * @param field - Field name to check
   * @returns true if field has error
   */
  hasFieldError(field: string): boolean {
    const errors = this.errors();
    return errors.some((e) => e.field === field);
  }

  /**
   * Navigate to specified route
   * Uses early return for invalid routes
   * @param route - Route path to navigate to
   */
  private navigateToRoute(route: string): void {
    // Early return if no route provided
    if (!route) {
      return;
    }

    this.router.navigate([route]).catch((error) => {
      console.error('Navigation error:', error);
      this.errors.set([
        { field: 'form', message: 'Error al navegar. Por favor, intente nuevamente.' },
      ]);
    });
  }
}
