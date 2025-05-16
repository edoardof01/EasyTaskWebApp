import {Component, ElementRef, inject, signal, ViewChild} from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  FormGroup,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {SubtasksComponent} from '../subtasks/subtasks.component';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {GroupSubtasksComponent} from '../group-subtasks/group-subtasks.component';

@Component({
  selector: 'app-group',
  imports: [
    ReactiveFormsModule,
    NgIf,
    NgClass,
    SubtasksComponent,
    NgForOf,
    GroupSubtasksComponent
  ],
  templateUrl: './group.component.html',
  standalone: true,
  styleUrl: './group.component.css'
})
export class GroupComponent {
  protected readonly value = signal('');
  isModalOpen: boolean = false;
  isAddingNewSession: boolean = false;
  newSessionStart: string | null = null;
  newSessionEnd: string | null = null;
  isListShowed: boolean = false;

  popupAddResources: boolean = false;
  resourceModule: boolean = false;
  startAddingResources: boolean = false;
  showResourceInput: boolean = false;

  subtasksShown: boolean = false;
  showSubs: boolean = false;
  areSubtasks: boolean = false;


  timetableIsDefined: boolean = false;

  errorMessage: string | null = null;
  blockErrorMessage: boolean = false;
  blockErrorMessage2: boolean = false;
  isErrorMessageVisible: boolean = false;

  areStrategies: boolean = false;
  skippingStrategy: boolean = false;
  totSkipped: boolean = false;
  totConsecSkipped: boolean = false;


  @ViewChild('UsersAndSubtasks') UsersAndSubtasks!: ElementRef<HTMLInputElement>




  private fb = inject(FormBuilder);

  @ViewChild(GroupSubtasksComponent) subtaskComponent!: GroupSubtasksComponent;


  groupTaskForm = this.fb.group({
    priority: new FormControl(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(5)
    ]),
    name: new FormControl('', [
      Validators.required,
      Validators.minLength(5),
      Validators.maxLength(30)
    ]),
    subtasksControl: new FormControl('', [
      Validators.required,
      Validators.min(0),
      Validators.max(15)
    ]),
    description: new FormControl('', [
      Validators.required,
      Validators.minLength(15),
      Validators.maxLength(1000)
    ]),
    totalTime: new FormControl('', [
      Validators.required,
      Validators.min(1)
    ]),
    timeTable: new FormControl('', [
      Validators.required,
    ]),
    deadline: new FormControl('', [
      this.futureDateValidator()
    ]),
    topic: new FormControl('',[
      Validators.required,
    ]),
    numUsers: new FormControl('', [
      Validators.required,
    ]),
    subtasks: this.fb.array([]),
    sessions: this.fb.array([]),
    resources: this.fb.array([]),
    strategies: this.fb.array([])
  });

  newStrategyForm = this.fb.group({
    strategy: ['', Validators.required],
    totSkippedCheckbox: [false], // Per gestire il checkbox FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS
    totSkippedValue: [null], // Valore numerico associato
    totConsecSkippedCheckbox: [false], // Per gestire il checkbox FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS
    totConsecSkippedValue: [null] // Valore numerico associato
  });



  newSessionForm = this.fb.group({
    start: ['', Validators.required],
    end: ['', Validators.required],
  });

  newResourcesForm = this.fb.group({
    name: ['', Validators.required],
    type: ['', Validators.required],
    value: ['', Validators.required]
  });



  removeSubtask(subtask: FormGroup) {
    // Logica per rimuovere il subtask
    this.subtaskComponent.enableGroupSessionCheckbox(subtask);
    this.subtaskComponent.enableGroupResourceCheckbox(subtask);
  }



  closeModal(): void {
    this.areStrategies = false; // Nasconde il modal
    console.log('Modal chiuso');
  }

  saveStrategy(): void {
    const strategiesArray = this.groupTaskForm.get('strategies') as FormArray;

    if (this.newStrategyForm.valid) {
      let strategyText = this.newStrategyForm.get('strategy')?.value;

      // Se "Set Skip Behaviour" è selezionato
      if (strategyText === 'Set Skip Behaviour') {
        const totSkippedChecked = this.newStrategyForm.get('totSkippedCheckbox')?.value;
        const totSkippedValue = this.newStrategyForm.get('totSkippedValue')?.value;
        const totConsecSkippedChecked = this.newStrategyForm.get('totConsecSkippedCheckbox')?.value;
        const totConsecSkippedValue = this.newStrategyForm.get('totConsecSkippedValue')?.value;

        const behaviourParts: string[] = [];

        if (totSkippedChecked && totSkippedValue !== null) {
          behaviourParts.push(`FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS-${totSkippedValue}`);
        }
        if (totConsecSkippedChecked && totConsecSkippedValue !== null) {
          behaviourParts.push(`FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS-${totConsecSkippedValue}`);
        }

        strategyText = behaviourParts.join(' AND ');
      }

      // Aggiungi la strategia al FormArray
      const newStrategy = this.fb.group({
        strategy: strategyText, // Salva la strategia finale
      });

      strategiesArray.push(newStrategy);
      this.newStrategyForm.reset();

      this.skippingStrategy = false;
      this.totSkipped = false;
      this.totConsecSkipped = false;

      // Disabilita i controlli
      this.disableFormControls();

    }
  }



  removeStrategy(index: number): void {
    const strategiesArray = this.groupTaskForm.get('strategies') as FormArray;
    strategiesArray.removeAt(index); // Rimuovi l'elemento dall'indice specificato

    // Se non ci sono più strategie, riabilita i controlli
    if (strategiesArray.length === 0) {
      this.enableFormControls();
    }
  }

  disableFormControls() {
    this.newStrategyForm.disable(); // Disabilita l'intero gruppo di controllo
  }

  enableFormControls() {
    this.newStrategyForm.enable(); // Riabilita l'intero gruppo di controllo
  }


  totSkippedMethod(){
    this.totSkipped = true;
  }

  totConsecSkippedMethod(){
    this.totConsecSkipped = true;
  }


  skippingChoice(){
    this.skippingStrategy = true;
  }

  onStrategies(){
    this.areStrategies = true;
  }


  futureDateValidator(): ValidatorFn {
    return (control: AbstractControl) => {
      const dateValue = control.value ? new Date(control.value) : null;
      const currentDate = new Date();
      if (dateValue && dateValue <= currentDate) {
        return { futureDate: true };
      }
      return null;
    };
  }




  protected onInput(event: Event) {
    this.value.set((event.target as HTMLInputElement).value);
  }

  timetableChosen(){
    this.timetableIsDefined = true;
    this.blockErrorMessage = true;
  }

  get sessions(): FormArray<FormGroup> {
    return this.groupTaskForm.get('sessions') as
      FormArray<FormGroup>;
  }

  get strategies(): FormArray<FormControl>{
    return this.groupTaskForm.get('strategies') as
      FormArray<FormControl>;
  }

  get resources(): FormArray<FormGroup> {
    return this.groupTaskForm.get('resources') as FormArray<FormGroup>;
  }

  onSaveSessionClick() {
    try {
      this.addSessionWithDetails()
      this.blockErrorMessage2 = true;
      this.areSubtasks = true;
    }catch (error) {
      this.errorMessage = 'Operazione fallita: ' + (error as Error).message;
      this.isErrorMessageVisible = true; // Mostra il messaggio con opacità completa

      setTimeout(() => {
        this.isErrorMessageVisible = false; // Avvia la dissolvenza
      }, 800); // Parte dopo 0.5 secondi

      setTimeout(() => {
        this.errorMessage = null; // Rimuovi il messaggio completamente
      }, 1500); // Dopo 1.6 secondi
    }
  }



  onSubmit() {
    if (this.groupTaskForm.valid && this.validateSessions()) {
      console.log('Form submitted successfully!', this.groupTaskForm.value);
    } else {
      console.error('Validation failed.');
    }
  }

  getSubtaskSessions(index: number): FormArray {
    const subtasks = this.groupTaskForm.get('subtasks') as FormArray;
    return subtasks.at(index).get('subSessions') as FormArray;
  }

  getSubtaskResources(index: number): FormArray {
    const subtasks = this.groupTaskForm.get('subtasks') as FormArray;
    return subtasks.at(index).get('subResources') as FormArray;
  }

  exitSubtaskForm(){
    this.showSubs = true
  }


  closeResourceModal(){
    this.popupAddResources = false;
    this.resourceModule = false;
    this.startAddingResources = false;
    this.showResourceInput = false;
  }


  ResourcesMenu(): void {
    this.popupAddResources = false;
    this.resourceModule = true;
    this.startAddingResources = true;
  }

  addResourcesWithDetails() {
    if (this.newResourcesForm.valid) {
      const resourcesGroup = this.fb.group({
        name: [this.newResourcesForm.get('name')?.value, Validators.required],
        type: [this.newResourcesForm.get('type')?.value, Validators.required],
        value: [this.newResourcesForm.get('value')?.value, Validators.required],
      });

      // Aggiungi la nuova risorsa al FormArray delle risorse esistenti
      (this.groupTaskForm.get('resources') as FormArray).push(resourcesGroup);

      // Resetta il form per una nuova risorsa
      this.newResourcesForm.reset();
    } else {
      console.error('All fields are required for the resource.');
    }
  }


  activateResourceInput(){
    this.showResourceInput = true;
  }

  get descriptionControl(): FormControl {
    return this.groupTaskForm.get('description') as FormControl;
  }

  get strategyControl(): FormControl {
    return this.groupTaskForm.get('strategies') as FormControl;
  }

  get numUsersControl(): FormControl {
    return this.groupTaskForm.get('numUsers') as FormControl;
  }

  onEnter(event: Event): void {
    event.preventDefault();  // Impedisce il comportamento predefinito (andare a capo)

    // Rimuovi il focus dal textarea
    const textarea = event.target as HTMLTextAreaElement;
    textarea.blur();  // Rimuove il focus dal textarea
  }

  get priorityControl(): FormControl {
    return this.groupTaskForm.get('priority') as FormControl;
  }

  onPriorityChange(event: any): void {
    let value = event.target.value;
    if (value != "") {
      if (value < 1) {
        value = 1;
      } else if (value > 5) {
        value = 5;
      }
      this.priorityControl.setValue(value);
    }
  }

  get totalTimeControl(): FormControl {
    return this.groupTaskForm.get('totalTime') as FormControl;
  }

  get timeTableControl(): FormControl {
    return this.groupTaskForm.get('timeTable') as FormControl;
  }

  get nameControl(): FormControl {
    return this.groupTaskForm.get('name') as FormControl;
  }

  get deadline(): FormControl {
    return this.groupTaskForm.get('deadline') as FormControl;
  }

  get topicControl(): FormControl {
    return this.groupTaskForm.get('topic') as FormControl;
  }

  get subtasks(): FormArray {
    return this.groupTaskForm.get('subtasks') as FormArray;
  }


  // Metodo per aprire il modal
  openSessionModal() {
    this.isListShowed = true;  // Mostra la lista delle sessioni
    this.isModalOpen = true;    // Apre il modal
    this.isAddingNewSession = false;  // Nasconde i campi di inserimento della nuova sessione
    this.newSessionStart = null;  // Ripristina la data di inizio
    this.newSessionEnd = null;// Ripristina la data di fine
  }


  // Metodo per chiudere il modal
  closeSessionModal() {
    this.isModalOpen = false;
    this.isAddingNewSession = false;
  }

  // Metodo per avviare l'aggiunta di una nuova sessione
  startAddingSession() {
    this.isAddingNewSession = true;  // Mostra i campi per aggiungere una nuova sessione
  }

  addSessionWithDetails() {
    if (!this.newSessionForm.valid) {
      throw new Error('Il modulo non è valido.');
    }

    const startValue = this.newSessionForm.value.start;
    const endValue = this.newSessionForm.value.end;

    if (!startValue || !endValue) {
      throw new Error('Entrambe le date di inizio e fine sono obbligatorie.');
    }

    const start = new Date(startValue as string);
    const end = new Date(endValue as string);
    const now = new Date();

    if (start < now || end < now) {
      throw new Error("Le sessioni non possono essere nel passato.");
    }

    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      throw new Error('Data di inizio o fine non valida.');
    }

    const durationInMilliseconds = end.getTime() - start.getTime();
    const durationInHours = durationInMilliseconds / (1000 * 60 * 60);
    const isExactMultipleOfHour = Number.isInteger(durationInHours);

    if (durationInHours <= 0 || !isExactMultipleOfHour) {
      throw new Error('La durata tra inizio e fine deve essere un multiplo positivo di un’ora.');
    }

    if (!this.controlTimeTable(start, end)) {
      throw new Error('Lo slot temporale selezionato non è valido.');
    }

    const sessionGroup = this.fb.group({
      start: [startValue, Validators.required],
      end: [endValue, Validators.required],
    });

    (this.groupTaskForm.get('sessions') as FormArray).push(sessionGroup);

    this.newSessionForm.reset();
  }

  controlTimeTable(start: Date, end: Date): boolean {
    const timeTable = this.groupTaskForm.get('timeTable')!.value;
    const startHours = start.getHours();
    const endHours = end.getHours();
    const endMinutes = end.getMinutes();

    switch (timeTable) {
      case "MORNING":
        if (startHours < 6 || endHours > 12 || (endHours === 12 && endMinutes !== 0)) {
          return false;
        }
        break;
      case "AFTERNOON":
        if (startHours < 12 || endHours > 18 || (endHours === 18 && endMinutes !== 0)) {
          return false;
        }
        break;
      case "EVENING":
        if (startHours < 18 || endHours > 23 || (endHours === 23 && endMinutes > 59)) {
          return false;
        }
        break;
      case "NIGHT":
        if (startHours < 0 || (endHours > 6 || (endHours === 6 && endMinutes !== 0))) {
          return false;
        }
        break;
      case "MORNING_AFTERNOON":
        if (startHours < 0 || (endHours > 18 || (endHours === 18 && endMinutes !== 0))) {
          return false;
        }
        break;
      case "AFTERNOON_EVENING":
        if (startHours < 12 || (endHours > 0 || (endHours === 0 && endMinutes !== 0))) {
          return false;
        }
        break;
      case "EVENING_NIGHT":
        if (startHours < 18 || (endHours > 6 || (endHours === 6 && endMinutes !== 0))) {
          return false;
        }
        break;
      case "MORNING_EVENING":
        if (startHours < 6 || (endHours > 0 || (endHours === 0 && endMinutes !== 0))) {
          return false;
        }
        break;

      case "AFTERNOON_NIGHT":
        if (startHours < 18 || (endHours > 6 || (endHours === 6 && endMinutes !== 0))) {
          return false;
        }
        break;
      case "ALL_DAY":
        break;
      default:
        return false; // Time slot non valido
    }
    return true; // Time slot valido
  }




  // Metodo per rimuovere una sessione
  removeSession(index: number) {
    this.sessions.removeAt(index);
  }
  removeResources(index: number) {
    this.resources.removeAt(index);
  }

  // Metodo per validare le sessioni
  validateSessions(): boolean {
    const taskSessions = this.groupTaskForm.get('sessions') as FormArray;
    const subtaskSessions: string[] = [];

    for (let i = 0; i < this.subtasks.length; i++) {
      const child = this.subtasks.at(i) as FormGroup;
      const childSessions = child.get('sessions') as FormArray;

      // Assicurati che le sessioni siano incluse nel task
      for (const session of childSessions.controls) {
        const sessionValue = session.value;
        if (!taskSessions.value.includes(sessionValue)) {
          console.error(`Session "${sessionValue}" is not part of the task's sessions.`);
          return false;
        }

        // Nessuna sessione condivisa tra subtasks
        if (subtaskSessions.includes(sessionValue)) {
          console.error(`Session "${sessionValue}" is already used by another subtask.`);
          return false;
        }

        subtaskSessions.push(sessionValue);
      }
    }
    // Verifica che il numero totale coincida
    if (subtaskSessions.length !== taskSessions.length) {
      console.error('The total number of sessions in subtasks does not match the task.');
      return false;
    }
    return true;
  }



  removeSubtaskByName(name: string): void {
    const index = this.subtasks.controls.findIndex(
      (control) => control.get('name')?.value === name
    );
    if (index !== -1) {
      const subtask = this.subtasks.at(index) as FormGroup;
      this.subtasks.removeAt(index);
      console.log("numero di subtasks:" + this.subtasks.length);
      console.log("indice del subtask:" + index);
      console.log(`Subtask "${name}" rimosso con successo.`);
      this.removeSubtask(subtask);
    } else {
      console.error(`Subtask con il nome "${name}" non trovato.`);
    }
  }




  subtasksArea(){
    this.subtasksShown = true;
    this.showSubs = false;
  }



  protected readonly FormGroup = FormGroup;
}
