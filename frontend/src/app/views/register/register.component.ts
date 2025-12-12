import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Button01 } from '../../components/button-01/button-01';
import { Inputcomponent } from '../../components/input/input';
import { RegisterService } from './services/register.service';
import {
  RegistrationType,
  EstablishmentStep,
  ClientFormData,
  EstablishmentFormData,
  ValidationError,
  EstablishmentOwnerData,
  EstablishmentLocationData,
  EstablishmentImagesData,
} from './types/register.types';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, Button01, Inputcomponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  // Expose enums to template
  readonly RegistrationType = RegistrationType;
  readonly EstablishmentStep = EstablishmentStep;

  // Reactive state using signals for better performance
  registrationType = signal<RegistrationType | null>(null);
  currentStep = signal<EstablishmentStep>(EstablishmentStep.INFO);
  errors = signal<ValidationError[]>([]);
  isSubmitting = signal<boolean>(false);

  // Client form data
  clientData = signal<Partial<ClientFormData>>({
    fullName: '',
    email: '',
    birthDay: '',
    birthMonth: '',
    birthYear: '',
    password: '',
    confirmPassword: '',
  });

  // Establishment form data
  establishmentOwner = signal<Partial<EstablishmentOwnerData>>({
    ownerName: '',
    ownerEmail: '',
    identificationNumber: '',
    frontIdImage: null,
    backIdImage: null,
  });

  establishmentLocation = signal<Partial<EstablishmentLocationData>>({
    establishmentName: '',
    establishmentEmail: '',
    rues: '',
    rnt: '',
    password: '',
    confirmPassword: '',
    country: '',
    department: '',
    municipality: '',
  });

  establishmentImages = signal<EstablishmentImagesData>({
    images: [],
  });

  acceptedTerms = signal<boolean>(false);

  constructor(private registerService: RegisterService) {}

  /**
   * Select registration type (Client or Establishment)
   * Uses early return pattern
   */
  selectRegistrationType(type: RegistrationType): void {
    if (!type) {
      return;
    }

    this.registrationType.set(type);
    this.errors.set([]);

    // Reset to first step for establishment
    if (type === RegistrationType.ESTABLISHMENT) {
      this.currentStep.set(EstablishmentStep.INFO);
    }
  }

  /**
   * Go back to registration type selection
   */
  goBackToSelection(): void {
    this.registrationType.set(null);
    this.currentStep.set(EstablishmentStep.INFO);
    this.errors.set([]);
  }

  /**
   * Navigate to next step in establishment registration
   * Uses early returns for validation
   */
  nextEstablishmentStep(): void {
    // Clear previous errors
    this.errors.set([]);

    const currentStepValue = this.currentStep();

    // Validate current step before proceeding
    let validationErrors: ValidationError[] = [];

    switch (currentStepValue) {
      case EstablishmentStep.INFO:
        validationErrors = this.registerService.validateEstablishmentOwnerInfo({
          owner: this.establishmentOwner() as EstablishmentOwnerData,
        });
        break;
      case EstablishmentStep.LOCATION:
        validationErrors = this.registerService.validateEstablishmentLocation({
          location: this.establishmentLocation() as EstablishmentLocationData,
        });
        break;
      case EstablishmentStep.IMAGES:
        validationErrors = this.registerService.validateEstablishmentImages({
          images: this.establishmentImages(),
        });
        break;
    }

    // Early return if validation fails
    if (validationErrors.length > 0) {
      this.errors.set(validationErrors);
      return;
    }

    // Move to next step
    if (currentStepValue < EstablishmentStep.CONFIRM) {
      this.currentStep.set((currentStepValue + 1) as EstablishmentStep);
    } else {
      this.submitEstablishmentRegistration();
    }
  }

  /**
   * Navigate to previous step in establishment registration
   * Uses early return pattern
   */
  previousEstablishmentStep(): void {
    const currentStepValue = this.currentStep();

    // Early return if at first step
    if (currentStepValue <= EstablishmentStep.INFO) {
      this.goBackToSelection();
      return;
    }

    this.currentStep.set((currentStepValue - 1) as EstablishmentStep);
    this.errors.set([]);
  }

  /**
   * Submit client registration
   * Uses early returns for validation and error handling
   */
  submitClientRegistration(): void {
    // Early return if already submitting
    if (this.isSubmitting()) {
      return;
    }

    // Clear previous errors
    this.errors.set([]);

    const formData = this.clientData();
    const validationErrors = this.registerService.validateClientForm(formData);

    // Early return if validation fails
    if (validationErrors.length > 0) {
      this.errors.set(validationErrors);
      return;
    }

    this.isSubmitting.set(true);

    this.registerService.submitClientRegistration(formData as ClientFormData).subscribe({
      next: (result) => {
        this.isSubmitting.set(false);
        if (result.success) {
          console.log('Registration successful:', result);
          // Navigate to success page or login
        }
      },
      error: (error) => {
        this.isSubmitting.set(false);
        this.errors.set([{ field: 'form', message: error.message || 'Error al registrar' }]);
      },
    });
  }

  /**
   * Submit establishment registration
   * Uses early returns for validation and error handling
   */
  submitEstablishmentRegistration(): void {
    // Early return if already submitting
    if (this.isSubmitting()) {
      return;
    }

    // Early return if terms not accepted
    if (!this.acceptedTerms()) {
      this.errors.set([
        { field: 'terms', message: 'Debe aceptar los términos y condiciones' },
      ]);
      return;
    }

    this.isSubmitting.set(true);

    const formData: EstablishmentFormData = {
      owner: this.establishmentOwner() as EstablishmentOwnerData,
      location: this.establishmentLocation() as EstablishmentLocationData,
      images: this.establishmentImages(),
      acceptedTerms: this.acceptedTerms(),
    };

    this.registerService.submitEstablishmentRegistration(formData).subscribe({
      next: (result) => {
        this.isSubmitting.set(false);
        if (result.success) {
          console.log('Registration successful:', result);
          // Navigate to success page or login
        }
      },
      error: (error) => {
        this.isSubmitting.set(false);
        this.errors.set([{ field: 'form', message: error.message || 'Error al registrar' }]);
      },
    });
  }

  /**
   * Handle file upload for establishment owner documents
   * Uses early returns for validation
   */
  handleOwnerDocumentUpload(event: Event, type: 'front' | 'back'): void {
    const input = event.target as HTMLInputElement;

    // Early return if no files
    if (!input.files || input.files.length === 0) {
      return;
    }

    const file = input.files[0];
    const currentOwner = this.establishmentOwner();

    // Update the appropriate field
    if (type === 'front') {
      this.establishmentOwner.set({ ...currentOwner, frontIdImage: file });
    } else {
      this.establishmentOwner.set({ ...currentOwner, backIdImage: file });
    }
  }

  /**
   * Handle file upload for establishment images
   * Uses early returns for validation
   */
  handleEstablishmentImagesUpload(event: Event): void {
    const input = event.target as HTMLInputElement;

    // Early return if no files
    if (!input.files || input.files.length === 0) {
      return;
    }

    const newFiles = Array.from(input.files);
    const currentImages = this.establishmentImages();

    this.establishmentImages.set({
      images: [...currentImages.images, ...newFiles],
    });
  }

  /**
   * Get progress percentage for establishment registration
   * Uses switch with early returns for clarity
   */
  getProgressPercentage(): number {
    switch (this.currentStep()) {
      case EstablishmentStep.INFO:
        return 10;
      case EstablishmentStep.LOCATION:
        return 30;
      case EstablishmentStep.IMAGES:
        return 80;
      case EstablishmentStep.CONFIRM:
        return 90;
      default:
        return 0;
    }
  }

  /**
   * Get error message for a specific field
   * Uses early returns for clarity
   */
  getErrorMessage(field: string): string | null {
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
   */
  hasError(field: string): boolean {
    return this.getErrorMessage(field) !== null;
  }
}
