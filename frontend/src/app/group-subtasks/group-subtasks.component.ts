import {
  Component,
  ElementRef,
  EventEmitter,
  inject,
  Input,
  Output,
  QueryList,
  signal,
  ViewChildren
} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {NgClass, NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-group-subtasks',
  imports: [
    ReactiveFormsModule,
    NgClass,
    NgForOf,
    NgIf
  ],
  templateUrl: './group-subtasks.component.html',
  standalone: true,
  styleUrl: './group-subtasks.component.css'
})
export class GroupSubtasksComponent {
  @Input() groupTasksFormGroup!: FormGroup;
  @Input() subtasks!: FormArray;
  @Output() remove = new EventEmitter<void>();  // Evento per rimuovere il componente
  private fb = inject(FormBuilder);
  protected readonly value = signal('');
  sessionTimeSum = 0;
  overNumUser: boolean = false;
  errorMessage: boolean = false;

  @ViewChildren('sessionCheckbox') sessionCheckboxes!: QueryList<ElementRef<HTMLInputElement>>;
  @ViewChildren('resourceCheckBox') resourceCheckboxes!: QueryList<ElementRef<HTMLInputElement>>;

  enableGroupSessionCheckbox(subtask: FormGroup) {
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

  enableGroupResourceCheckbox(subtask: FormGroup) {
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

  get numUsers(): FormControl<number> {
    return this.groupTasksFormGroup.get("numUsers") as FormControl;
  }



  groupSubtaskFormGroup = this.fb.group({
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
    return this.groupSubtaskFormGroup.get('subSessions') as
      FormArray<FormGroup>;
  }

  get groupTaskSessions(): FormArray<FormGroup> {
    return this.groupTasksFormGroup.get('sessions') as
      FormArray<FormGroup>;
  }

  onSessionChange(event: Event, index: number) {
    const checkbox = event.target as HTMLInputElement;

    // Recupera il valore della sessione corrispondente all'indice
    const sessionValue = this.groupTaskSessions?.at(index)?.value;

    if (!sessionValue) return;

    if (checkbox.checked) {
      // Aggiungi la sessione se non è già presente in subSessions
      if (!this.subSessions.controls.some(ctrl =>
        ctrl.value.start.toString() === sessionValue.start.toString() &&
        ctrl.value.end.toString() === sessionValue.end.toString()
      )) {
        this.subSessions.push(
          this.fb.group({
            start: [sessionValue.start, Validators.required],
            end: [sessionValue.end, Validators.required]
          })
        );
        const startDate = new Date(sessionValue.start.toString());
        const endDate = new Date(sessionValue.end.toString());
        const durationInMilliseconds = endDate.getTime() - startDate.getTime();
        const durationInHours = durationInMilliseconds / (1000 * 60 * 60);
        this.sessionTimeSum += durationInHours;
      }
    } else {
      // Rimuovi la sessione se è stata deselezionata
      const sessionIndex = this.subSessions.controls.findIndex(ctrl =>
        ctrl.value.start.toString() === sessionValue.start.toString() &&
        ctrl.value.end.toString() === sessionValue.end.toString()
      );

      if (sessionIndex !== -1) {
        this.subSessions.removeAt(sessionIndex);
        const startDate = new Date(sessionValue.start.toString());
        const endDate = new Date(sessionValue.end.toString());
        const durationInMilliseconds = endDate.getTime() - startDate.getTime();
        const durationInHours = durationInMilliseconds / (1000 * 60 * 60);
        this.sessionTimeSum -= durationInHours;
      }
    }
  }



  get subResources(): FormArray<FormGroup> {
    return this.groupSubtaskFormGroup.get('subResources') as
      FormArray<FormGroup>;
  }

  get groupTaskResources(): FormArray<FormGroup> {
    return this.groupTasksFormGroup.get('resources') as
      FormArray<FormGroup>;
  }

  onResourceChange(event: Event, index: number): void {
    const checkbox = event.target as HTMLInputElement;

    // Recupera il valore della risorsa selezionata dal tasksFormGroup
    const resourceValue = this.groupTaskResources?.at(index)?.value;
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
    if (this.sessionTimeSum !== Number(this.groupSubtaskFormGroup.get("totalTime")!.value)) {
      alert("Il tempo totale delle sessioni non corrisponde al valore inserito in 'totalTime'.");
      return;
    }

    if (!this.groupSubtaskFormGroup.valid) {
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
      name: this.groupSubtaskFormGroup.get('name')?.value,
      description: this.groupSubtaskFormGroup.get('description')?.value,
      totalTime: this.groupSubtaskFormGroup.get('totalTime')?.value,
      level: this.groupSubtaskFormGroup.get('level')?.value,
      subSessions: this.fb.array(
        this.subSessions.controls.map(control => this.fb.group({
          start: control.get('start')?.value,
          end: control.get('end')?.value
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
    this.groupSubtaskFormGroup.reset();
    this.sessionTimeSum = 0;

    this.groupSubtaskFormGroup.setControl('subSessions', this.fb.array([]));
    this.groupSubtaskFormGroup.setControl('subResources', this.fb.array([]));
    if(this.groupTasksFormGroup.get("numUsers") && this.subtasks.length === this.groupTasksFormGroup.get("numUsers")!.value) {
      this.overNumUser = true;
      this.errorMessage = true;
    }

  }



  get totalTimeControl(): FormControl {
    return this.groupSubtaskFormGroup.get('totalTime') as FormControl;
  }

  get nameControl(): FormControl {
    return this.groupSubtaskFormGroup.get('name') as FormControl;
  }

  protected onInput(event: Event) {
    this.value.set((event.target as HTMLInputElement).value);
  }

  get descriptionControl(): FormControl {
    return this.groupSubtaskFormGroup.get('description') as FormControl;
  }

  get levelControl(): FormControl {
    return this.groupSubtaskFormGroup.get('level') as FormControl;
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
