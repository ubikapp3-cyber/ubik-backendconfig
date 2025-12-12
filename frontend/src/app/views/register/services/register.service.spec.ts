import { TestBed } from '@angular/core/testing';
import { RegisterService } from './register.service';
import {
  ClientFormData,
  EstablishmentFormData,
  EstablishmentOwnerData,
  EstablishmentLocationData,
} from '../types/register.types';

describe('RegisterService', () => {
  let service: RegisterService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [RegisterService],
    });
    service = TestBed.inject(RegisterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('validateClientForm', () => {
    it('should return errors for empty form', () => {
      const errors = service.validateClientForm({});
      expect(errors.length).toBeGreaterThan(0);
    });

    it('should return errors for invalid email', () => {
      const data: Partial<ClientFormData> = {
        fullName: 'John Doe',
        email: 'invalid-email',
        birthDay: '01',
        birthMonth: '01',
        birthYear: '1990',
        password: 'password123',
        confirmPassword: 'password123',
      };
      const errors = service.validateClientForm(data);
      expect(errors.some((e) => e.field === 'email')).toBe(true);
    });

    it('should return errors for mismatched passwords', () => {
      const data: Partial<ClientFormData> = {
        fullName: 'John Doe',
        email: 'john@example.com',
        birthDay: '01',
        birthMonth: '01',
        birthYear: '1990',
        password: 'password123',
        confirmPassword: 'different123',
      };
      const errors = service.validateClientForm(data);
      expect(errors.some((e) => e.field === 'confirmPassword')).toBe(true);
    });

    it('should return no errors for valid form', () => {
      const data: Partial<ClientFormData> = {
        fullName: 'John Doe',
        email: 'john@example.com',
        birthDay: '01',
        birthMonth: '01',
        birthYear: '1990',
        password: 'password123',
        confirmPassword: 'password123',
      };
      const errors = service.validateClientForm(data);
      expect(errors.length).toBe(0);
    });
  });

  describe('validateEstablishmentOwnerInfo', () => {
    it('should return errors for empty owner data', () => {
      const errors = service.validateEstablishmentOwnerInfo({});
      expect(errors.length).toBeGreaterThan(0);
    });

    it('should return errors for missing required fields', () => {
      const owner: Partial<EstablishmentOwnerData> = {
        ownerName: 'John Doe',
        ownerEmail: '',
        identificationNumber: '',
        frontIdImage: null,
        backIdImage: null,
      };
      const errors = service.validateEstablishmentOwnerInfo({ 
        owner: owner as EstablishmentOwnerData 
      });
      expect(errors.length).toBeGreaterThan(0);
    });
  });

  describe('validateEstablishmentLocation', () => {
    it('should return errors for empty location data', () => {
      const errors = service.validateEstablishmentLocation({});
      expect(errors.length).toBeGreaterThan(0);
    });

    it('should return errors for missing required fields', () => {
      const location: Partial<EstablishmentLocationData> = {
        establishmentName: 'My Business',
        establishmentEmail: '',
        rues: '',
        rnt: '',
        password: '',
        confirmPassword: '',
        country: '',
        department: '',
        municipality: '',
      };
      const errors = service.validateEstablishmentLocation({ 
        location: location as EstablishmentLocationData 
      });
      expect(errors.length).toBeGreaterThan(0);
    });
  });

  describe('validateEstablishmentImages', () => {
    it('should return errors when no images provided', () => {
      const errors = service.validateEstablishmentImages({});
      expect(errors.length).toBeGreaterThan(0);
    });

    it('should return errors when images array is empty', () => {
      const errors = service.validateEstablishmentImages({ images: { images: [] } });
      expect(errors.length).toBeGreaterThan(0);
    });
  });

  describe('submitClientRegistration', () => {
    it('should return error for invalid form', (done) => {
      const data = {} as ClientFormData;
      service.submitClientRegistration(data).subscribe({
        next: () => {
          fail('Should have failed');
        },
        error: (error) => {
          expect(error.success).toBe(false);
          done();
        },
      });
    });
  });

  describe('submitEstablishmentRegistration', () => {
    it('should return error when terms not accepted', (done) => {
      const data: EstablishmentFormData = {
        owner: {} as EstablishmentOwnerData,
        location: {} as EstablishmentLocationData,
        images: { images: [] },
        acceptedTerms: false,
      };
      service.submitEstablishmentRegistration(data).subscribe({
        next: () => {
          fail('Should have failed');
        },
        error: (error) => {
          expect(error.success).toBe(false);
          done();
        },
      });
    });
  });
});
