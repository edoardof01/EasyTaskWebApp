import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, map, Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private tokenKey = 'authToken';
  private isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());

  constructor(private http: HttpClient) {}

  // Registrazione
  register(username: string, password: string): Observable<any> {
    return this.http.post('http://localhost:8080/EasyTask-1.0-SNAPSHOT/api/register', { username, password });
  }

  // Login
  login(username: string, password: string): Observable<any> {
    return this.http.post<{ token: string }>('http://localhost:8080/EasyTask-1.0-SNAPSHOT/api/auth/login', { username, password }).pipe(
      map((response) => {
        localStorage.setItem(this.tokenKey, response.token); // Salva il token
        this.isLoggedInSubject.next(true);
        return response;
      })
    );
  }

  // Logout
  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.isLoggedInSubject.next(false);
  }

  // Controlla se l'utente è autenticato
  isLoggedIn(): Observable<boolean> {
    return this.isLoggedInSubject.asObservable();
  }

  // Verifica se il token esiste
  private hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  // Aggiungi il token alle richieste
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

}
