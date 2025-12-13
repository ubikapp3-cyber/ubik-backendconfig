import { TestBed } from '@angular/core/testing';
import { LoginService } from './login.service';
import { LoginFormData, OAuthProvider } from '../types/login.types';

describe('LoginService', () => {
  let service: LoginService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoginService],
    });
    service = TestBed.inject(LoginService);
    
    // Clear localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    // Clean up localStorage after each test
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('validateForm', () => {
    it('should return error for empty email', () => {
      const data: Partial<LoginFormData> = { email: '', password: 'password123' };
      const errors = service.validateForm(data);
      
      expect(errors.length).toBeGreaterThan(0);
      expect(errors.some(e => e.field === 'email')).toBeTruthy();
    });

    it('should return error for invalid email format', () => {
      const data: Partial<LoginFormData> = { email: 'invalid-email', password: 'password123' };
      const errors = service.validateForm(data);
      
      expect(errors.length).toBeGreaterThan(0);
      expect(errors.some(e => e.field === 'email')).toBeTruthy();
    });

    it('should return error for empty password', () => {
      const data: Partial<LoginFormData> = { email: 'test@example.com', password: '' };
      const errors = service.validateForm(data);
      
      expect(errors.length).toBeGreaterThan(0);
      expect(errors.some(e => e.field === 'password')).toBeTruthy();
    });

    it('should return no errors for valid data', () => {
      const data: LoginFormData = { email: 'test@example.com', password: 'password123' };
      const errors = service.validateForm(data);
      
      expect(errors.length).toBe(0);
    });
  });

  describe('login', () => {
    it('should return success for valid credentials', (done) => {
      const data: LoginFormData = { email: 'test@example.com', password: 'password123' };
      
      service.login(data).subscribe({
        next: (result) => {
          expect(result.success).toBeTruthy();
          expect(result.token).toBeDefined();
          expect(result.userId).toBeDefined();
          done();
        },
      });
    });

    it('should return error for invalid data', (done) => {
      const data = { email: '', password: '' } as LoginFormData;
      
      service.login(data).subscribe({
        error: (error) => {
          expect(error.success).toBeFalsy();
          done();
        },
      });
    });
  });

  describe('loginWithOAuth', () => {
    it('should handle Google OAuth login', (done) => {
      service.loginWithOAuth(OAuthProvider.GOOGLE).subscribe({
        next: (result) => {
          expect(result.success).toBeTruthy();
          expect(result.token).toContain('GOOGLE');
          done();
        },
      });
    });

    it('should handle Facebook OAuth login', (done) => {
      service.loginWithOAuth(OAuthProvider.FACEBOOK).subscribe({
        next: (result) => {
          expect(result.success).toBeTruthy();
          expect(result.token).toContain('FACEBOOK');
          done();
        },
      });
    });
  });

  describe('token management', () => {
    it('should store auth token', () => {
      const token = 'test-token-123';
      service.storeAuthToken(token);
      
      expect(localStorage.getItem('auth_token')).toBe(token);
    });

    it('should retrieve stored token', () => {
      const token = 'test-token-456';
      localStorage.setItem('auth_token', token);
      
      expect(service.getAuthToken()).toBe(token);
    });

    it('should clear auth token', () => {
      localStorage.setItem('auth_token', 'test-token');
      service.clearAuthToken();
      
      expect(localStorage.getItem('auth_token')).toBeNull();
    });

    it('should check if user is authenticated', () => {
      expect(service.isAuthenticated()).toBeFalsy();
      
      service.storeAuthToken('test-token');
      expect(service.isAuthenticated()).toBeTruthy();
      
      service.clearAuthToken();
      expect(service.isAuthenticated()).toBeFalsy();
    });
  });

  describe('requestPasswordReset', () => {
    it('should send password reset request', (done) => {
      service.requestPasswordReset('test@example.com').subscribe({
        next: (result) => {
          expect(result.success).toBeTruthy();
          expect(result.message).toContain('correo');
          done();
        },
      });
    });

    it('should return error for empty email', (done) => {
      service.requestPasswordReset('').subscribe({
        error: (error) => {
          expect(error.success).toBeFalsy();
          done();
        },
      });
    });
  });
});
