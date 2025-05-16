import {Component, inject} from '@angular/core';
import {FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../services/auth.service';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';
import {NgIf} from '@angular/common';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    NgIf,
    RouterLink,
    RouterLinkActive,
    TranslatePipe
  ],
  templateUrl: './register.component.html',
  standalone: true,
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  constructor(private translate: TranslateService){}

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false; // 🔹 Aggiunto per la schermata di caricamento

  registerForm = this.fb.group({
    username:  new FormControl('', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]),
    password:  new FormControl('', [Validators.required, Validators.minLength(8), Validators.maxLength(20)]),
    confirmPassword: ['', [Validators.required]], // Campo per confermare la password
  }, { validators: this.passwordMatchValidator });

  onSubmit() {
    if (this.registerForm.valid) {
      this.isLoading = true; // 🔹 Mostra la schermata di caricamento

      const username = this.registerForm.controls['username'].value as string;
      const password = this.registerForm.controls['password'].value as string;

      localStorage.setItem('isProfileSaved', 'false');

      this.authService.register(username, password).subscribe({
        next: (response) => {
          console.log("✅ Registrazione avvenuta con successo!", response);
          this.successMessage = 'Registration successful! Redirecting to login...';


          setTimeout(() => {
            this.isLoading = false; // 🔹 Nasconde la schermata di caricamento
            this.router.navigate(['/login']);
          }, 3000);
        },
        error: (err) => {
          this.isLoading = false; // 🔹 Nasconde la schermata di caricamento
          console.error("❌ Errore durante la registrazione:", err);

          if (err.status === 201) {
            this.successMessage = 'Registration successful! Redirecting to login...';
            setTimeout(() => this.router.navigate(['/login']),0);
          } else {
            this.errorMessage = err.status === 400
              ? 'User already exists. Please try a different username.'
              : 'An error occurred. Please try again later.';
          }
        },
      });
    }
  }

  private passwordMatchValidator(group: any): { [key: string]: boolean } | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordsMismatch: true };
  }
}
