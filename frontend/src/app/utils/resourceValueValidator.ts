import {AbstractControl, ValidationErrors} from '@angular/forms';

export function resourceValueValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.parent) {
    return null;
  }
  const type = control.parent.get('type')?.value;
  const rawValue = control.value;

  // Se non c’è nulla, non generiamo errore
  if (!rawValue || (typeof rawValue === 'string' && !rawValue.trim())) {
    return null;
  }
  const num = Number(rawValue);

  // Se la risorsa è COMPETENCE o EQUIPMENT, range 1..5
  if (type === 'COMPETENCE' || type === 'EQUIPMENT') {
    if (isNaN(num) || num < 1 || num > 5) {
      return { rangeError: true };
    }
  }
  return null;
}
