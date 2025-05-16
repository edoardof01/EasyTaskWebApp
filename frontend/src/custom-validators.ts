import {AbstractControl, FormArray, ValidationErrors, ValidatorFn} from '@angular/forms';

export function atLeastOneSessionValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const sessions = control.value;
    return sessions && sessions.length > 0 ? null : { atLeastOneSession: true };
  };
}

export function atLeastOneStrategyValidator(control: AbstractControl) {
  const strategiesArray = control as FormArray;
  return strategiesArray.length > 0 ? null : { atLeastOneStrategyRequired: true };
}

