/**
 * Login service
 * Follows Single Responsibility Principle (SRP) - handles authentication logic only
 * Follows Dependency Inversion Principle (DIP) - depends on abstractions (Observable)
 */

import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { delay, catchError } from 'rxjs/operators';
import {
  LoginFormData,
  AuthResult,
  ValidationError,
  OAuthProvider,
} from '../types/login.types';
import { validateLoginForm } from '../utils/login-validation.utils';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  /**
   * Validates login form data
   * Uses early returns for cleaner validation flow
   * @param data - Form data to validate
   * @returns array of validation errors
   */
  validateForm(data: Partial<LoginFormData>): ValidationError[] {
    // Early return if no data
    if (!data) {
      return [{ field: 'form', message: 'Datos de formulario inválidos' }];
    }

    return validateLoginForm(data);
  }

  /**
   * Authenticates user with email and password
   * Uses early returns for validation
   * Mock implementation - replace with actual API call
   * @param data - Login credentials
   * @returns Observable with authentication result
   */
  login(data: LoginFormData): Observable<AuthResult> {
    // Validate before submission
    const errors = this.validateForm(data);
    
    // Early return if validation fails
    if (errors.length > 0) {
      return throwError(() => ({
        success: false,
        message: 'Error de validación: ' + errors.map(e => e.message).join(', '),
      }));
    }

    // Mock API call - simulate network delay
    // TODO: Replace with actual HTTP call to backend
    return of({
      success: true,
      message: 'Inicio de sesión exitoso',
      token: 'mock-jwt-token-' + Date.now(),
      userId: 'mock-user-id',
      redirectUrl: '/home',
    }).pipe(
      delay(1000),
      catchError((error) => {
        return throwError(() => ({
          success: false,
          message: error.message || 'Error al iniciar sesión',
        }));
      })
    );
  }

  /**
   * Authenticates user with OAuth provider
   * Uses early returns for validation
   * Mock implementation - replace with actual OAuth flow
   * @param provider - OAuth provider (Google, Facebook)
   * @returns Observable with authentication result
   */
  loginWithOAuth(provider: OAuthProvider): Observable<AuthResult> {
    // Early return for invalid provider
    if (!provider) {
      return throwError(() => ({
        success: false,
        message: 'Proveedor de autenticación no válido',
      }));
    }

    // Mock OAuth flow - simulate network delay
    // TODO: Replace with actual OAuth implementation
    return of({
      success: true,
      message: `Inicio de sesión exitoso con ${provider}`,
      token: `mock-oauth-token-${provider}-${Date.now()}`,
      userId: `mock-oauth-user-${provider}`,
      redirectUrl: '/home',
    }).pipe(
      delay(1500),
      catchError((error) => {
        return throwError(() => ({
          success: false,
          message: error.message || `Error al iniciar sesión con ${provider}`,
        }));
      })
    );
  }

  /**
   * Sends password reset email
   * Uses early returns for validation
   * Mock implementation - replace with actual API call
   * @param email - User's email address
   * @returns Observable with result
   */
  requestPasswordReset(email: string): Observable<AuthResult> {
    // Early return for empty email
    if (!email || email.trim().length === 0) {
      return throwError(() => ({
        success: false,
        message: 'El correo electrónico es requerido',
      }));
    }

    // Mock API call
    // TODO: Replace with actual HTTP call to backend
    return of({
      success: true,
      message: 'Se ha enviado un correo de recuperación',
    }).pipe(
      delay(1000),
      catchError((error) => {
        return throwError(() => ({
          success: false,
          message: error.message || 'Error al enviar correo de recuperación',
        }));
      })
    );
  }

  /**
   * Stores authentication token
   * Uses early return for invalid token
   * 
   * SECURITY NOTE: localStorage is used for development convenience.
   * For production, consider using httpOnly cookies to prevent XSS attacks,
   * or implement additional security measures like token encryption.
   * 
   * @param token - JWT token to store
   */
  storeAuthToken(token: string): void {
    // Early return for invalid token
    if (!token) {
      return;
    }

    // Store in localStorage
    // TODO: In production, use httpOnly cookies for better security
    localStorage.setItem('auth_token', token);
  }

  /**
   * Retrieves stored authentication token
   * @returns stored token or null
   */
  getAuthToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  /**
   * Removes authentication token
   */
  clearAuthToken(): void {
    localStorage.removeItem('auth_token');
  }

  /**
   * Checks if user is authenticated
   * @returns true if authenticated, false otherwise
   */
  isAuthenticated(): boolean {
    const token = this.getAuthToken();
    return token !== null && token.length > 0;
  }
}
