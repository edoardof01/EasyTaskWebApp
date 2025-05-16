import {Component, inject} from '@angular/core';
import {FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {NgClass, NgIf} from '@angular/common';
import {AuthService} from '../services/auth.service';
import {Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {DataFromFormService} from '../services/data-from-form.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    NgIf,
    NgClass,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    TranslatePipe
  ],
  templateUrl: './login.component.html',
  standalone: true,
  styleUrl: './login.component.css'
})
export class LoginComponent {
  isLoading = false;
  errorMessage: string = '';

  private fb = inject(FormBuilder);

  constructor(private translate: TranslateService, private authService: AuthService, private router: Router,private dataFormFromService: DataFromFormService) {
  }

  loginForm = this.fb.group({
    username: new FormControl('',[ Validators.required, Validators.minLength(3), Validators.maxLength(20)]),
    password: new FormControl ('',[Validators.required,Validators.minLength(8), Validators.maxLength(20)]),
  })

  onSubmit() {
    if(this.loginForm.valid) {
      this.isLoading = true;
      const username = this.loginForm.controls['username'].value as string;
      const password = this.loginForm.controls['password'].value as string;

      this.authService.login(username, password).subscribe({
        next: (response: any) => {
          this.isLoading = false;
          this.dataFormFromService.getAllSessions('personal');
          // Salva lo username nel localStorage
          localStorage.setItem('username', username);

          this.router.navigate(['/navbar/profile']);
        },
        error: (err) => {
          this.isLoading = false;
          if (err.status === 401) {
            this.errorMessage = 'Invalid credentials. Please try again.';
          } else {
            this.errorMessage = 'An error occurred. Please try again later.';
            console.log(err);
          }
        }
      });
    }
  }

}
