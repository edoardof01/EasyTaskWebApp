import {Component, inject, Inject, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {AuthService} from '../services/auth.service';
import {Router} from '@angular/router';
import {FormArray, FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {UserService} from '../services/user.service';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-profile',
  imports: [
    ReactiveFormsModule,
    NgIf,
    NgForOf,
    NgClass,
    TranslatePipe
  ],
  templateUrl: './profile.component.html',
  standalone: true,
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {



  isQualificationModalOpen: boolean = false;
  isAddingNewQualification: boolean = false;
  errorMessage: string | null = null;
  isErrorMessageVisible: boolean = false;
  isProfileSaved: boolean = false;
  username: string | null = null;

  isLoggedIn$: Observable<boolean>;
  private fb = inject(FormBuilder);

  ngOnInit() {
    // 🔹 Recupera lo stato del profilo salvato
    const savedProfileState = localStorage.getItem('isProfileSaved');
    const savedProfile = localStorage.getItem('userProfile');
    if (savedProfile) {
      const profileData = JSON.parse(savedProfile);
      this.profileForm.patchValue(profileData); // 🔹 Riempie il form con i dati salvati
      this.isProfileSaved = true;
      this.userService.setProfileComplete(true);
      if (profileData.qualifications && Array.isArray(profileData.qualifications)) {
        this.qualifications.clear(); // Puliamo eventuali dati esistenti
        profileData.qualifications.forEach((qualification: string) => {
          this.qualifications.push(this.fb.group({ qualification: qualification }));
        });
      }
    }
    this.isProfileSaved = savedProfileState === 'true';

    this.profileForm.updateValueAndValidity();  // 🔹 Forza validazione
  }

  changeLanguage(lang: string) {
    this.translate.use(lang); // Cambia lingua globalmente
  }


  constructor(private translate: TranslateService, private authService: AuthService, private userService: UserService, private router: Router, private http : HttpClient) {
    this.isLoggedIn$ = this.authService.isLoggedIn();
    this.username = localStorage.getItem('username');
  }


   // LENGUAGE CHOICE

  switchLanguage(language: string) {
    this.translate.use(language);
  }



  profileForm = this.fb.group({
    age: new FormControl('', [ Validators.required, Validators.min(14),Validators.max(100)]),
    sex: new FormControl('', [Validators.required]),
    description: new FormControl('', [Validators.required,Validators.minLength(15), Validators.maxLength(1000)]),
    profession: new FormControl('', [Validators.required,Validators.minLength(5), Validators.maxLength(200)]),
    qualifications: this.fb.array([]),
  })

  qualificationsForm = this.fb.group({
    qualification: new FormControl('', [Validators.minLength(15), Validators.maxLength(1000)])
  })

  get ageControl(): FormControl {
    return this.profileForm.get('age') as FormControl;
  }



  get sexControl(): FormControl {
    return this.profileForm.get('sex') as FormControl;
  }

  get qualificationControl(): FormControl{
    return this.qualificationsForm.get('qualification') as FormControl;
  }

  get professionControl(): FormControl{
    return this.profileForm.get('profession') as FormControl;
  }

  get descriptionControl(): FormControl{
    return this.profileForm.get('description') as FormControl;
  }

  get qualifications(): FormArray {
    return this.profileForm.get('qualifications') as FormArray;
  }


  /** Apre il modale delle qualifiche */
  openQualificationModal(): void {
    this.isQualificationModalOpen = true;
  }

  /** Chiude il modale delle qualifiche */
  closeQualificationModal(): void {
    this.isQualificationModalOpen = false;
    this.isAddingNewQualification = false;
    this.qualificationsForm.reset(); // Resetta il form di aggiunta
    this.errorMessage = null;
  }

  /** Inizia il processo per aggiungere una nuova qualifica */
  startAddingQualification(): void {
    this.isAddingNewQualification = true;
  }

  /** Annulla l'aggiunta di una nuova qualifica */
  cancelAddingQualification(): void {
    this.isAddingNewQualification = false;
    this.qualificationsForm.reset();
  }

  // -------------------------------
  // METODI PER GESTIRE LE QUALIFICHE
  // -------------------------------

  /** Aggiunge una nuova qualifica */
  saveQualification(): void {
    if (this.qualificationsForm.valid) {
      // Creazione dell'oggetto FormGroup per la nuova qualifica
      const qualificationGroup = this.fb.group({
        qualification: this.qualificationControl.value
      });

      // Aggiunge la qualifica all'array del profilo
      this.qualifications.push(qualificationGroup);

      // Resetta il form e chiude il form di aggiunta
      this.qualificationsForm.reset();
      this.isAddingNewQualification = false;
      this.errorMessage = null;
    } else {
      this.showErrorMessage("Qualification must be at least 15 characters long.");
    }
  }

  /** Rimuove una qualifica */
  removeQualification(index: number): void {
    this.qualifications.removeAt(index);
  }

  /** Mostra un messaggio di errore */
  private showErrorMessage(message: string): void {
    this.errorMessage = message;
    this.isErrorMessageVisible = true;
    setTimeout(() => {
      this.isErrorMessageVisible = false;
    }, 3000); // Il messaggio scompare dopo 3 secondi
  }



  onSubmit(): void {
    if (this.profileForm.valid) {
      const profileData = {
        ...this.profileForm.value,
        qualifications: this.qualifications.value.map((q: any) => q.qualification) // 🔹 Converte in array di stringhe
      };
      console.log("Dati inviati al backend:", profileData);

      this.http.post('http://localhost:8080/EasyTask-1.0-SNAPSHOT/api/users/create', profileData, {
        withCredentials: true
      }).subscribe({
        next: (response: any) => {
          console.log('Profilo creato con successo!', response);

          this.userService.setUserId(response.Id);
          this.userService.setUserProfile(profileData);

          // 🔹 Usa il metodo del servizio per segnare il profilo come completato
          this.isProfileSaved = true;
          this.userService.setProfileComplete(true);  // ✅ Ora aggiorna il BehaviorSubject e il localStorage

          localStorage.setItem('userProfile', JSON.stringify(profileData));
        },
        error: (err) => {
          console.error('Errore durante la creazione del profilo:', err);
        },
      });
    } else {
      console.log("Il form non è valido.");
    }
  }


  onLogout(): void {
    this.authService.logout(); // Rimuove il token e aggiorna lo stato
    this.router.navigate(['/login']); // Reindirizza alla pagina di login
  }

}
