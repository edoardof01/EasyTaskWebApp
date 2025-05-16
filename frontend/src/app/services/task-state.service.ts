import { Injectable } from '@angular/core';
import {FormGroup} from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class TaskStateService {

  constructor() { }

  private _formData: any = null;

  // Salva i dati del form
  setFormData(data: FormGroup): void {
    this._formData = data;
  }

  // Recupera i dati salvati (oppure null se non sono stati salvati)
  getFormData(): FormGroup {
    return this._formData;
  }

  // (Opzionale) Pulisce i dati salvati
  clearFormData(): void {
    this._formData = null;
  }
}
