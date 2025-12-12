import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { RegisterService } from './services/register.service';
import { RegistrationType } from './types/register.types';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let registerService: RegisterService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [RegisterService],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    registerService = TestBed.inject(RegisterService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with no registration type selected', () => {
    expect(component.registrationType()).toBeNull();
  });

  it('should set registration type when selecting client', () => {
    component.selectRegistrationType(RegistrationType.CLIENT);
    expect(component.registrationType()).toBe(RegistrationType.CLIENT);
  });

  it('should set registration type when selecting establishment', () => {
    component.selectRegistrationType(RegistrationType.ESTABLISHMENT);
    expect(component.registrationType()).toBe(RegistrationType.ESTABLISHMENT);
  });

  it('should go back to selection when goBackToSelection is called', () => {
    component.selectRegistrationType(RegistrationType.CLIENT);
    component.goBackToSelection();
    expect(component.registrationType()).toBeNull();
  });

  it('should validate client form and show errors', () => {
    component.selectRegistrationType(RegistrationType.CLIENT);
    component.submitClientRegistration();
    expect(component.errors().length).toBeGreaterThan(0);
  });

  it('should calculate progress percentage correctly for establishment steps', () => {
    component.selectRegistrationType(RegistrationType.ESTABLISHMENT);
    component.currentStep.set(1);
    expect(component.getProgressPercentage()).toBe(10);
    
    component.currentStep.set(2);
    expect(component.getProgressPercentage()).toBe(30);
    
    component.currentStep.set(3);
    expect(component.getProgressPercentage()).toBe(80);
    
    component.currentStep.set(4);
    expect(component.getProgressPercentage()).toBe(90);
  });

  it('should return error message for field with error', () => {
    component.errors.set([{ field: 'email', message: 'Email is required' }]);
    expect(component.getErrorMessage('email')).toBe('Email is required');
  });

  it('should return null for field without error', () => {
    component.errors.set([{ field: 'email', message: 'Email is required' }]);
    expect(component.getErrorMessage('password')).toBeNull();
  });

  it('should detect if field has error', () => {
    component.errors.set([{ field: 'email', message: 'Email is required' }]);
    expect(component.hasError('email')).toBe(true);
    expect(component.hasError('password')).toBe(false);
  });
});
