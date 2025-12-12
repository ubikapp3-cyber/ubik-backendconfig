/**
 * Type definitions for the register view
 * Follows SOLID principles with clear interfaces for each registration type
 */

export enum RegistrationType {
  CLIENT = 'CLIENT',
  ESTABLISHMENT = 'ESTABLISHMENT',
}

export enum EstablishmentStep {
  INFO = 1,
  LOCATION = 2,
  IMAGES = 3,
  CONFIRM = 4,
}

export interface ValidationError {
  field: string;
  message: string;
}

export interface ClientFormData {
  fullName: string;
  email: string;
  birthDay: string;
  birthMonth: string;
  birthYear: string;
  password: string;
  confirmPassword: string;
}

export interface EstablishmentOwnerData {
  ownerName: string;
  ownerEmail: string;
  identificationNumber: string;
  frontIdImage: File | null;
  backIdImage: File | null;
}

export interface EstablishmentLocationData {
  establishmentName: string;
  establishmentEmail: string;
  rues: string;
  rnt: string;
  password: string;
  confirmPassword: string;
  country: string;
  department: string;
  municipality: string;
}

export interface EstablishmentImagesData {
  images: File[];
}

export interface EstablishmentFormData {
  owner: EstablishmentOwnerData;
  location: EstablishmentLocationData;
  images: EstablishmentImagesData;
  acceptedTerms: boolean;
}

export interface RegisterState {
  registrationType: RegistrationType | null;
  currentStep: EstablishmentStep;
  clientData: Partial<ClientFormData>;
  establishmentData: Partial<EstablishmentFormData>;
  errors: ValidationError[];
  isSubmitting: boolean;
}
