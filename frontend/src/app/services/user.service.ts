import { Injectable } from '@angular/core';
import {BehaviorSubject, map, Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private idSubject = new BehaviorSubject<string | null>(null);
  private userProfileSubject = new BehaviorSubject<any | null>(null);
  private isProfileCompleteSubject = new BehaviorSubject<boolean>(this.loadProfileCompletionStatus());

  constructor(private http: HttpClient) {}

  // 🔹 Recupera lo stato di isProfileSaved dal localStorage
  private loadProfileCompletionStatus(): boolean {
    return localStorage.getItem('isProfileSaved') === 'true';
  }

  // 🔹 Observable per monitorare il completamento del profilo
  getProfileCompletionStatus$(): Observable<boolean> {
    return this.isProfileCompleteSubject.asObservable();
  }

  // 🔹 Metodo per aggiornare lo stato del profilo
  setProfileComplete(isComplete: boolean): void {
    localStorage.setItem('isProfileSaved', isComplete ? 'true' : 'false');
    this.isProfileCompleteSubject.next(isComplete);
  }

  // 🔹 Getter e setter per ID utente
  getUserId(): Observable<string | null> {
    return this.idSubject.asObservable();
  }

  fetchUserIdByUsername(username: string): Observable<string> {
    return this.http.get<{ id: string }>(`http://localhost:8080/EasyTask-1.0-SNAPSHOT/api/users/username/${encodeURIComponent(username)}`)
      .pipe(
        tap(response => {
          if (response.id) {
            console.log("ID utente trovato dal backend:", response.id);
            this.idSubject.next(response.id);
          } else {
            console.error('Errore: ID utente non trovato nella risposta.');
          }
        }),
        map(response => response.id)
      );
  }

  setUserId(id: string): void {
    this.idSubject.next(id);
  }

  setUserProfile(profile: any): void {
    this.userProfileSubject.next(profile);
  }
}
