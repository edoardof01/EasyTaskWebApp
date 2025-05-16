import {
  Component,
  ElementRef,
  EventEmitter,
  inject,
  Output,
  QueryList,
  signal,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import {MatInput, MatInputModule} from '@angular/material/input';
import {
  ReactiveFormsModule,
  FormControl,
  FormGroup,
  FormArray,
  FormBuilder,
  Validators,

} from '@angular/forms';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {Input} from '@angular/core';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-subtasks',
  imports: [MatFormField, MatLabel, ReactiveFormsModule, MatInput, MatFormField, MatInputModule, NgForOf, MatIconButton, MatButton, MatIcon, MatOption, MatSelect, NgIf, NgClass, TranslatePipe],
  templateUrl: './subtasks.component.html',
  standalone: true,
  styleUrl: './subtasks.component.css'
})
export class SubtasksComponent {
  @Input() tasksFormGroup!: FormGroup;
  @Input() subtasks!: FormArray;
  @Output() remove = new EventEmitter<void>();  // Evento per rimuovere il componente
  private fb = inject(FormBuilder);
  protected readonly value = signal('');
  sessionTimeSum = 0;


  constructor(private translate: TranslateService){}

  @ViewChildren('sessionCheckbox') sessionCheckboxes!: QueryList<ElementRef<HTMLInputElement>>;
  @ViewChildren('resourceCheckBox') resourceCheckboxes!: QueryList<ElementRef<HTMLInputElement>>;

  enableSessionCheckbox(subtask: FormGroup) {
    const sessions = subtask.get("subSessions") as FormArray<FormGroup>;
    if (sessions) {
      this.sessionCheckboxes.forEach(sessionCheckbox => {
        const start = sessionCheckbox.nativeElement.getAttribute('data-start');
        const end = sessionCheckbox.nativeElement.getAttribute('data-end');
        sessions.controls.forEach(session => {
          if (session.get('start')?.value === start && session.get('end')?.value === end) {
            sessionCheckbox.nativeElement.disabled = false;
            sessionCheckbox.nativeElement.checked = false;
          }
        });
      });
    }
  }

  enableResourceCheckbox(subtask: FormGroup) {
    const resources = subtask.get("subResources") as FormArray<FormGroup>;
    if (resources) {
      this.resourceCheckboxes.forEach(resourceCheckbox => {
        const name = resourceCheckbox.nativeElement.getAttribute('data-name');
        const value = resourceCheckbox.nativeElement.getAttribute('data-value');
        resources.controls.forEach(resource => {
          if (resource.get('name')?.value === name && resource.get('value')?.value === value) {
            resourceCheckbox.nativeElement.disabled = false;
            resourceCheckbox.nativeElement.checked = false;
          }
        });
      });
    }
  }



  subtaskFormGroup = this.fb.group({
    name: new FormControl('',
      [Validators.required,
      Validators.min(5),
      Validators.max(30)]),
    description: new FormControl('', [
      Validators.required,
      Validators.min(15),
      Validators.max(1000)
    ]),
    totalTime: new FormControl('', [
      Validators.required,
      Validators.min(1),
      Validators.max(1000)
    ]),
    level: new FormControl('', [
      Validators.required,
      Validators.min(1),
      Validators.max(5)
    ]),
    subSessions: this.fb.array([], Validators.required), // Validazione richiesta
    subResources: this.fb.array([]/*, Validators.required*/), // Validazione richiesta
  });




  get subSessions(): FormArray<FormGroup>{
    return this.subtaskFormGroup.get('subSessions') as
      FormArray<FormGroup>;
  }

  get taskSessions(): FormArray<FormGroup> {
    return this.tasksFormGroup.get('sessions') as
      FormArray<FormGroup>;
  }

  onSessionChange(event: Event, index: number) {
    const checkbox = event.target as HTMLInputElement;

    // Recupera il valore della sessione corrispondente all'indice
    const sessionValue = this.taskSessions?.at(index)?.value;
    console.log("session is:" + sessionValue.startDate);

    if (!sessionValue) return;

    if (checkbox.checked) {
      // Aggiungi la sessione se non è già presente in subSessions
      if (!this.subSessions.controls.some(ctrl =>
        ctrl.value.startDate.toString() === sessionValue.startDate.toString() && /** Ho fatto una modifica, prima non era startDate ma start **/
        ctrl.value.endDate.toString() === sessionValue.endDate.toString()
      )) {
        this.subSessions.push(
          this.fb.group({
            startDate: [sessionValue.startDate, Validators.required],
            endDate: [sessionValue.endDate, Validators.required]
          })
        );
        const startDate = new Date(sessionValue.startDate.toString());
        const endDate = new Date(sessionValue.endDate.toString());
        const durationInMilliseconds = endDate.getTime() - startDate.getTime();
        const durationInHours = durationInMilliseconds / (1000 * 60 * 60);
        this.sessionTimeSum += durationInHours;
      }
    } else {
      // Rimuovi la sessione se è stata deselezionata
      const sessionIndex = this.subSessions.controls.findIndex(ctrl =>
        ctrl.value.startDate.toString() === sessionValue.startDate.toString() &&
        ctrl.value.endDate.toString() === sessionValue.endDate.toString()
      );

      if (sessionIndex !== -1) {
        this.subSessions.removeAt(sessionIndex);
        const startDate = new Date(sessionValue.startDate.toString());
        const endDate = new Date(sessionValue.endDate.toString());
        const durationInMilliseconds = endDate.getTime() - startDate.getTime();
        const durationInHours = durationInMilliseconds / (1000 * 60 * 60);
        this.sessionTimeSum -= durationInHours;
      }
    }
  }



  get subResources(): FormArray<FormGroup> {
    return this.subtaskFormGroup.get('subResources') as
      FormArray<FormGroup>;
  }

  get taskResources(): FormArray<FormGroup> {
    return this.tasksFormGroup.get('resources') as
      FormArray<FormGroup>;
  }

  onResourceChange(event: Event, index: number): void {
    const checkbox = event.target as HTMLInputElement;

    // Recupera il valore della risorsa selezionata dal tasksFormGroup
    const resourceValue = this.taskResources?.at(index)?.value;
    if (!resourceValue) {
      return; // Esci se il valore non esiste
    }
    if (checkbox.checked) {
      // Aggiungi la risorsa se non è già presente
      if (!this.subResources.controls.some(ctrl =>
        ctrl.value.name.toString() === resourceValue.name.toString() &&
        ctrl.value.type.toString() === resourceValue.type.toString() &&
        ctrl.value.value.toString() === resourceValue.value.toString()
      )) {
        this.subResources.push(this.fb.group({
          name: [resourceValue.name],
          type: [resourceValue.type],
          value: [resourceValue.value],
        }));
      }
    } else {
      // Rimuovi la risorsa dal FormArray
      const resourceIndex = this.subResources.controls.findIndex(ctrl =>
        ctrl.value.name.toString() === resourceValue.name.toString() &&
        ctrl.value.type.toString() === resourceValue.type.toString() &&
        ctrl.value.value.toString() === resourceValue.value.toString()
      );
      if (resourceIndex !== -1) {
        this.subResources.removeAt(resourceIndex);
      }
    }
  }


  onSubmit() {
    if (this.sessionTimeSum !== Number(this.subtaskFormGroup.get("totalTime")!.value)) {
      alert("Il tempo totale delle sessioni non corrisponde al valore inserito in 'totalTime'.");
      return;
    }

    if (!this.subtaskFormGroup.valid) {
      alert("Il form non è valido. Controlla i campi obbligatori.");
      return;
    }
    // Disabilita le checkbox per il subtask corrente
    const currentSubtaskIndex = this.subtasks.length;
    // Disabilita le checkbox delle sessioni
    document
      .querySelectorAll<HTMLInputElement>(`input[type="checkbox"][data-type="session"][data-subtask-index="${currentSubtaskIndex}"]`)
      .forEach(checkbox => {
        if (checkbox.checked) {
          checkbox.disabled = true;
        }
      });
    // Disabilita le checkbox delle risorse
    document
      .querySelectorAll<HTMLInputElement>(`input[type="checkbox"][data-type="resource"][data-subtask-index="${currentSubtaskIndex}"]`)
      .forEach(checkbox => {
        if (checkbox.checked) {
          checkbox.disabled = true;
        }
      });

    // Aggiungi il nuovo subtask al FormArray
    const newSubtask = this.fb.group({
      name: this.subtaskFormGroup.get('name')?.value,
      description: this.subtaskFormGroup.get('description')?.value,
      totalTime: this.subtaskFormGroup.get('totalTime')?.value,
      level: this.subtaskFormGroup.get('level')?.value,
      subSessions: this.fb.array(
        this.subSessions.controls.map(control => this.fb.group({
          startDate: control.get('startDate')?.value,
          endDate: control.get('endDate')?.value
        }))
      ),
      subResources: this.fb.array(
        this.subResources.controls.map(control => this.fb.group({
          name: control.get('name')?.value,
          type: control.get('type')?.value,
          value: control.get('value')?.value
        }))
      ),
    });
    this.subtasks.push(newSubtask);
    // Resetta il form per un nuovo subtask
    this.subtaskFormGroup.reset();
    this.sessionTimeSum = 0;

    this.subtaskFormGroup.setControl('subSessions', this.fb.array([]));
    this.subtaskFormGroup.setControl('subResources', this.fb.array([]));


  }



  get totalTimeControl(): FormControl {
    return this.subtaskFormGroup.get('totalTime') as FormControl;
  }

  get nameControl(): FormControl {
    return this.subtaskFormGroup.get('name') as FormControl;
  }

  protected onInput(event: Event) {
    this.value.set((event.target as HTMLInputElement).value);
  }

  get descriptionControl(): FormControl {
    return this.subtaskFormGroup.get('description') as FormControl;
  }

  get levelControl(): FormControl {
    return this.subtaskFormGroup.get('level') as FormControl;
  }



  onLevelChange(event: any): void {
    let value = event.target.value;
    if (value != "") {
      if (value < 1) {
        value = 1;
      } else if (value > 5) {
        value = 5;
      }
      this.levelControl.setValue(value);
    }
  }

  /* DESCRIPTION */
  onEnter(event: Event): void {
    event.preventDefault();  // Impedisce il comportamento predefinito (andare a capo)
    // Rimuovi il focus dal textarea
    const textarea = event.target as HTMLTextAreaElement;
    textarea.blur();  // Rimuove il focus dal textarea
  }

}
