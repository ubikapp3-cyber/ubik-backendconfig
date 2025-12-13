/**
 * Validation utilities for login forms
 * Each validator follows Single Responsibility Principle (SRP)
 * Uses early returns to reduce nesting
 */

import { ValidationError, LoginFormData } from '../types/login.types';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Validates email format
 * Uses early returns for cleaner code flow
 * @param email - Email address to validate
 * @returns error message if invalid, null if valid
 */
export function validateEmail(email: string): string | null {
  // Early return for empty email
  if (!email || email.trim().length === 0) {
    return 'El correo electrónico es requerido';
  }

  // Early return for invalid format
  if (!EMAIL_REGEX.test(email)) {
    return 'El correo electrónico no es válido';
  }

  return null;
}

/**
 * Validates password is provided
 * Uses early return pattern
 * @param password - Password to validate
 * @returns error message if invalid, null if valid
 */
export function validatePassword(password: string): string | null {
  // Early return for empty password
  if (!password || password.trim().length === 0) {
    return 'La contraseña es requerida';
  }

  return null;
}

/**
 * Validates entire login form
 * Collects all validation errors
 * Uses early returns for null/undefined data
 * @param data - Form data to validate
 * @returns array of validation errors (empty if valid)
 */
export function validateLoginForm(data: Partial<LoginFormData>): ValidationError[] {
  // Early return if no data provided
  if (!data) {
    return [{ field: 'form', message: 'Datos de formulario inválidos' }];
  }

  const errors: ValidationError[] = [];

  // Validate email
  const emailError = validateEmail(data.email || '');
  if (emailError) {
    errors.push({ field: 'email', message: emailError });
  }

  // Validate password
  const passwordError = validatePassword(data.password || '');
  if (passwordError) {
    errors.push({ field: 'password', message: passwordError });
  }

  return errors;
}

/**
 * Checks if a specific field has an error
 * @param errors - Array of validation errors
 * @param field - Field name to check
 * @returns true if field has error, false otherwise
 */
export function hasFieldError(errors: ValidationError[], field: string): boolean {
  return errors.some(error => error.field === field);
}

/**
 * Gets error message for a specific field
 * Uses early return pattern
 * @param errors - Array of validation errors
 * @param field - Field name to get error for
 * @returns error message or null if no error
 */
export function getFieldError(errors: ValidationError[], field: string): string | null {
  // Early return if no errors
  if (errors.length === 0) {
    return null;
  }

  const error = errors.find(e => e.field === field);
  return error ? error.message : null;
}
