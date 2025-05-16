import { Component } from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {AsyncPipe, NgIf} from '@angular/common';
import {Observable} from 'rxjs';
import {AuthService} from '../services/auth.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-navbar',
  imports: [
    RouterLink,
    RouterLinkActive,
    NgIf,
    AsyncPipe,
    RouterOutlet,
    TranslatePipe
  ],
  templateUrl: './navbar.component.html',
  standalone: true,
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  isLoggedIn$: Observable<boolean>;

  constructor(private translate: TranslateService, private authService: AuthService) {
    this.isLoggedIn$ = this.authService.isLoggedIn();
  }
}
