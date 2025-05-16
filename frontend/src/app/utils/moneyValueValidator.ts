import { AbstractControl, ValidationErrors } from '@angular/forms';

export function moneyValueValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.parent) {
    return null;
  }
  const type = control.parent.get('type')?.value;
  const rawValue = control.value;

  // Se type = MONEY, controlliamo che non sia vuoto e >= 0
  if (type === 'MONEY') {
    if (!rawValue || (typeof rawValue === 'string' && !rawValue.trim())) {
      return { requiredMoney: true };
    }
    const num = Number(rawValue);
    if (isNaN(num) || num < 0) {
      return { negativeMoney: true };
    }
  }
  return null;
}
