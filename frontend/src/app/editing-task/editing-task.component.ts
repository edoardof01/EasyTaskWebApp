import {Component, inject, Inject, OnDestroy, OnInit, signal, ViewChild} from '@angular/core';
import {
  AbstractControl, Form, FormArray,
  FormBuilder,
  FormControl, FormGroup,
  FormsModule,
  ReactiveFormsModule,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {SubtasksComponent} from '../subtasks/subtasks.component';
import {atLeastOneSessionValidator} from '../../custom-validators';
import {ActivatedRoute, Router} from '@angular/router';
import {filter, of, Subscription, switchMap, take} from 'rxjs';
import {UserService} from '../services/user.service';
import {BackendService} from '../services/backend.service';
import {DataFromFormService} from '../services/data-from-form.service';
import {resourceValueValidator} from '../utils/resourceValueValidator';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {moneyValueValidator} from '../utils/moneyValueValidator';

@Component({
  selector: 'app-editing-task',
  imports: [
    FormsModule,
    NgForOf,
    NgIf,
    ReactiveFormsModule,
    SubtasksComponent,
    NgClass,
    TranslatePipe
  ],
  templateUrl: './editing-task.component.html',
  standalone: true,
  styleUrl: './editing-task.component.css'
})
export class EditingTaskComponent implements OnInit,OnDestroy{

  private subscription!: Subscription;
  private fb = inject(FormBuilder);

  isInEditingStrategy = false;
  isInNameEditing: boolean = false;
  isInPriorityEditing: boolean = false;
  isInDescriptionEditing: boolean = false;
  isInTotalTimeEditing: boolean = false;
  isInTopicEditing: boolean = false;
  isInDeadlineEditing: boolean = false;
  isInTimeTableEditing: boolean = false;

  @ViewChild(SubtasksComponent) subtaskComponent!: SubtasksComponent;
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

  blockErrorMessage2: boolean = false;
  isErrorMessageVisible: boolean = false;

  areStrategies: boolean = false;
  skippingStrategy: boolean = false;
  totSkipped: boolean = false;
  totConsecSkipped: boolean = false;

  /*DATI PER L'EDITING*/
  public taskId!: number;
  public complexity!: number;
  public percentageOfCompletion!: number;
  public taskState!: string;
  public inProgress!: boolean;


  notPostPosedSelectionable = true;
  postPosed = true;
  eachStrategySelectionable = true;
  freezeTot = true;
  consecTot = true;


  constructor(private translate: TranslateService, private route1: Router, private route: ActivatedRoute,private userService: UserService, private backendService: BackendService, private dataFromFormService: DataFromFormService ){}

  public editForm = this.fb.group({
    priority: new FormControl({value: null, disabled: true}, [
      Validators.required,
      Validators.min(1),
      Validators.max(5)
    ]),
    name: new FormControl({value: '', disabled: true}, [
      Validators.required,
      Validators.minLength(5),
      Validators.maxLength(30)
    ]),
    description: new FormControl({value: '', disabled: true}, [
      Validators.required,
      Validators.minLength(15),
      Validators.maxLength(1000)
    ]),
    totalTime: new FormControl({value: '', disabled: true}, [
      Validators.required,
      Validators.min(1)
    ]),
    timetable: new FormControl({value: '', disabled: true}, [
      Validators.required,
    ]),
    deadline: new FormControl({value: '', disabled: true}, [
      this.futureDateValidator()
    ]),
    topic: new FormControl({value: '', disabled: true},[
      Validators.required,
    ]),
    subtasks: this.fb.array([]),
    sessions: this.fb.array([], [atLeastOneSessionValidator()]),
    resources: this.fb.array([]),
    strategies: this.fb.array([])
  });

  newStrategyForm = this.fb.group({
    strategy: [{value: '', disabled: false}, Validators.required],
    totSkippedCheckbox: [false], // Per gestire il checkbox FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS
    totSkippedValue: [null], // Valore numerico associato
    totConsecSkippedCheckbox: [false], // Per gestire il checkbox FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS
    totConsecSkippedValue: [null] // Valore numerico associato
  });

  get strategyControl():FormControl{
    return this.newStrategyForm.get('strategy') as FormControl;
  }

  newSessionForm = this.fb.group({
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
  });

  newResourcesForm = this.fb.group({
    name: ['', Validators.required],
    type: ['', Validators.required],
    value: ['', [resourceValueValidator]],
    money: ['',moneyValueValidator]
  });


  private initialFormValue: any;


  disableDeadline(){
    this.deadline.disable();
  }

  enableDeadline(){
    this.deadline.enable();
  }


  ngOnInit() {
    this.subscription = this.route.queryParams.subscribe((params: any) => {
      if (params.taskId) {
        // Salviamo l'id del task, convertendolo in numero
        this.taskId = Number(params.taskId);

        // Ora chiamiamo il service per ottenere il task completo dal backend
        this.dataFromFormService.getTask('personal', this.taskId).subscribe(task => {
          // Patchiamo il form con i valori ricevuti dal backend
          this.editForm.patchValue({
            priority: task.priority,
            name: task.name,
            description: task.description,
            totalTime: task.totalTime,
            timetable: task.timetable,
            deadline: task.deadline,
            topic: task.topic
          });
          // Impostiamo anche gli altri campi a livello di componente
          this.taskState = task.taskState;
          this.inProgress = task.inProgress;
          this.complexity = task.complexity;
          this.percentageOfCompletion = task.percentageOfCompletion;
          // Popoliamo i FormArray (sessions, subtasks, resources, strategies)
          this.populateFormArray(
            this.editForm.get('sessions') as FormArray,
            task.sessions,
            (session) => this.createSessionGroup(session)
          );
          this.populateFormArray(
            this.editForm.get('subtasks') as FormArray,
            task.subtasks,
            (subtask) => this.createSubtaskGroup(subtask)
          );
          this.populateFormArray(
            this.editForm.get('resources') as FormArray,
            task.resources,
            (resource) => this.createResourceGroup(resource)
          );
          this.populateFormArray(
            this.editForm.get('strategies') as FormArray,
            task.strategies,
            (strategy) => this.createStrategyGroup(strategy)
          );

          if (task.sessions && task.sessions.length > 0) {
            // Attiviamo i pulsanti per i Subtasks
            this.areSubtasks = true;
            this.blockErrorMessage2 = true;
          }
          if (this.strategies.length > 0) {
            this.strategies.controls.forEach(control => {
              const strategyValue = control.get('strategy')?.value;

              if (strategyValue === 'SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS') {
                // Blocca altre strategie
                this.postPosed = false;
                this.notPostPosedSelectionable = false;
              }
              if (strategyValue === 'EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING') {
                this.notPostPosedSelectionable = false;
                this.eachStrategySelectionable = false;
              }
              if (strategyValue.includes('FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS')) {
                this.freezeTot = false;
                // Se vuoi disattivare la “scelta skip”:
                // this.skippingStrategy = false; (se usi questa logica)
              }
              if (strategyValue.includes('FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS')) {
                this.consecTot = false;
                // Idem come sopra se vuoi modificare skippingStrategy
              }
            });
          }
          // Salviamo lo stato iniziale del form per eventuali confronti futuri
          this.initialFormValue = JSON.stringify(this.editForm.getRawValue());

          this.newResourcesForm.reset({
            name: '',
            type: '',
            value: '',
            money: ''
          });
        });
      }
    });
  }


  hasChanged(): boolean {
    // Confronta il valore corrente con quello iniziale
    return JSON.stringify(this.editForm.getRawValue()) !== this.initialFormValue;
  }


  private populateFormArray(formArray: FormArray, items: any[], createGroupFn: (item: any) => FormGroup) {
    formArray.clear(); // Pulisce il FormArray prima di aggiungere nuovi dati
    if (items && Array.isArray(items)) {
      items.forEach(item => {
        formArray.push(createGroupFn(item));
      });
    }
  }

  editName(){
    this.nameControl.enable();
    this.isInNameEditing = true;
  }

  editSession(){
    this.newSessionForm.enable();
    this.isAddingNewSession = true;
  }

  editResources(){
    this.newResourcesForm.enable();
    this.showResourceInput = true;
  }


  editStrategies(){
      this.newStrategyForm.enable();
      this.isInEditingStrategy = true;
    }

  editTotalTime(){
    this.totalTimeControl.enable();
    this.isInTotalTimeEditing = true;
  }

  editPriority(){
    this.priorityControl.enable();
    this.isInPriorityEditing = true;
  }

  editDescription(){
    this.descriptionControl.enable();
    this.isInDescriptionEditing = true;
  }

  editTopic(){
    this.topicControl.enable();
    this.isInTopicEditing = true;
  }

  editDeadline(){
    this.deadline.enable();
    this.isInDeadlineEditing = true;
  }

  editTimetable(){
    this.timeTableControl.enable();
    this.isInTimeTableEditing = true;
  }



  private createSessionGroup(session: any): FormGroup {
    return this.fb.group({
      startDate: [session.startDate, Validators.required],
      endDate: [session.endDate, Validators.required]
    });
  }

  private createResourceGroup(resource: any): FormGroup {
    return this.fb.group({
      name: [resource.name, Validators.required],
      type: [resource.type, Validators.required],
      value: [resource.value, Validators.required]
    });
  }

  private createStrategyGroup(strategy: any): FormGroup {
    return this.fb.group({
      strategy: [strategy.strategy, Validators.required],
      totSkippedCheckbox: [strategy.totSkippedCheckbox || false],
      totSkippedValue: [strategy.totSkippedValue || null],
      totConsecSkippedCheckbox: [strategy.totConsecSkippedCheckbox || false],
      totConsecSkippedValue: [strategy.totConsecSkippedValue || null]
    });
  }

  private createSubtaskGroup(subtask: any): FormGroup {
    return this.fb.group({
      name: [subtask.name, Validators.required], // Aggiungi i campi necessari
      completed: [subtask.completed || false]
    });
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
  closeModal(): void {
    this.areStrategies = false; // Nasconde il modal
    console.log('Modal chiuso');
  }

  timetableChosen(){
    this.timetableIsDefined = true;
  }

  removeSession(index: number) {
    this.sessions.removeAt(index);
    this.newSessionForm.enable(); // Abilita il form delle sessioni, se questo è quello che vuoi fare
    if(this.sessions.length === 0){
      this.areSubtasks = false;
      this.blockErrorMessage2 = false;
    }
  }

  removeResources(index: number) {
    this.resources.removeAt(index);
    this.newResourcesForm.enable();
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

  addSessionWithDetails() {
    if (!this.newSessionForm.valid) {
      throw new Error('Il modulo non è valido.');
    }

    const startValue = this.newSessionForm.value.startDate;
    const endValue = this.newSessionForm.value.endDate;

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
      startDate: [startValue, Validators.required],
      endDate: [endValue, Validators.required],
    });

    (this.editForm.get('sessions') as FormArray).push(sessionGroup);

    this.newSessionForm.reset();
  }

  controlTimeTable(start: Date, end: Date): boolean {
    const timeTable = this.editForm.get('timetable')!.value;
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
      case "NIGHT_MORNING":
        if (startHours < 0 || (endHours > 12 || (endHours === 12 && endMinutes !== 0))) {
          return false;
        }
        break;
      case "MORNING_EVENING":
        if (startHours < 6 || (endHours > 0 || (endHours === 0 && endMinutes !== 0))) {
          return false;
        }
        break;

      case "NIGHT_AFTERNOON":
        if (startHours < 0 || (endHours > 18 || (endHours === 18 && endMinutes !== 0))) {
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

  // Metodo per avviare l'aggiunta di una nuova sessione


  openSessionModal() {
    this.isListShowed = true;  // Mostra la lista delle sessioni
    this.isModalOpen = true;    // Apre il modal
    this.isAddingNewSession = false;  // Nasconde i campi di inserimento della nuova sessione
    this.newSessionStart = null;  // Ripristina la data di inizio
    this.newSessionEnd = null;// Ripristina la data di fine
  }

  validateSessions(): boolean {
    if (this.subtasks.length != 0) {
      const taskSessions = this.editForm.get('sessions') as FormArray;
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
    return true;
  }

  onSubmit() {
    if (this.editForm.valid && this.validateSessions()) {
      const username = localStorage.getItem('username');

      if (!username) {
        console.error('Errore: Username non trovato nel localStorage.');
        return;
      }
      this.userService.getUserId().pipe(
        take(1),
        switchMap((id) => {
          if (id) {
            return of(id); // Se l'ID esiste già, lo usiamo direttamente
          } else {
            console.warn('ID utente non disponibile, recupero dal backend...');
            return this.userService.fetchUserIdByUsername(username).pipe(
              switchMap(() => this.userService.getUserId().pipe(take(1), filter(id => id !== null))) // Aspettiamo il recupero dell'ID
            );
          }
        })
      ).subscribe((finalUserId) => {
        if (finalUserId) {
          console.log("ID utente recuperato correttamente:", finalUserId);
          this.processTask(Number(finalUserId));
        } else {
          console.error('Errore: ID utente ancora non disponibile.');
        }
      });

      this.route1.navigate(['/navbar/folders/personal/todo'])
    } else {
      console.error('Validation failed.');
    }

  }

// 🔹 Metodo separato per processare il task con l'ID utente
  processTask(id: number) {
    const formValue = this.editForm.getRawValue();
    // Mappa l'array strategies per inviare solo le chiavi richieste dal backend
    const mappedStrategies = formValue.strategies.map((s: any) => ({
      strategy: s.strategy,
      tot: s.totSkippedValue,           // mappa totSkippedValue in tot
      maxConsecSkipped: s.totConsecSkippedValue // mappa totConsecSkippedValue in maxConsecSkipped
    }));
    // Costruisci il payload includendo anche il campo "id"
    const taskWithUserId = {
      ...formValue,
      id: Number(this.taskId),  // Invia l'id del task come richiesto dal backend
      userId: id,
      complexity: this.complexity,
      percentageOfCompletion: this.percentageOfCompletion,
      strategies: mappedStrategies,
      taskState: this.taskState,
      inProgress: this.inProgress,
    };
    console.log("📤 Task inviato al backend:", JSON.stringify(taskWithUserId));
    // Assicurati di passare il campo corretto (in questo caso, l'id del task)
    this.dataFromFormService.editTask('personal', taskWithUserId.id, taskWithUserId).subscribe({
      next: (response: Response) => {
        console.log('✅ Dati modificati con successo:', response);
      },
      error: (error: Response) => {
        console.log('❌ Dati non modificati:', error);
      }
    });

    if (taskWithUserId.taskState === "INPROGRESS") {
      let currentSessions = this.dataFromFormService.calendarSessionsSubject.getValue();
      let newSessions = currentSessions.filter((session: any) => session.taskId !== taskWithUserId.id);
      this.dataFromFormService.calendarSessionsSubject.next(newSessions);
    }


    this.editForm.reset();
  }





  subtasksArea(){
    this.subtasksShown = true;
    this.showSubs = false;
  }



  saveStrategy(): void {
    const strategiesArray = this.editForm.get('strategies') as FormArray;

    if (this.newStrategyForm.valid) {
      let strategyText = this.newStrategyForm.get('strategy')?.value;

      // Se "Set Skip Behaviour" è selezionato
      if (strategyText === 'Set Skip Behaviour') {
        const totSkippedChecked = this.newStrategyForm.get('totSkippedCheckbox')?.value;
        const totSkippedValue = this.newStrategyForm.get('totSkippedValue')?.value;
        const totConsecSkippedChecked = this.newStrategyForm.get('totConsecSkippedCheckbox')?.value;
        const totConsecSkippedValue = this.newStrategyForm.get('totConsecSkippedValue')?.value;
        if (totSkippedChecked && totSkippedValue !== null) {
          const strategySkipped = this.fb.group({
            strategy: `FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS-${totSkippedValue}`,
          });
          strategiesArray.push(strategySkipped);
          // Aggiorna stati per la UI
          this.notPostPosedSelectionable = false;
          this.freezeTot = false;

          this.newStrategyForm.patchValue({
            totSkippedCheckbox: false,
            totSkippedValue: null,
          });
        }
        if (totConsecSkippedChecked && totConsecSkippedValue !== null) {
          const strategyConsecutive = this.fb.group({
            strategy: `FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS-${totConsecSkippedValue}`,
          });
          strategiesArray.push(strategyConsecutive);

          // Aggiorna stati per la UI
          this.notPostPosedSelectionable = false;
          this.consecTot = false;

          this.newStrategyForm.patchValue({
            totConsecSkippedCheckbox: false,
            totConsecSkippedValue: null,
          });
        }

        // Evita di eseguire la creazione di una strategia unica fuori da questo blocco
        return;
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

      if(strategyText == 'SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS'){
        this.postPosed = false;
        this.notPostPosedSelectionable = false;
      }
      if(strategyText == 'EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING'){
        this.notPostPosedSelectionable = false;
        this.eachStrategySelectionable = false;
      }
    }
  }

  disableFormControls() {
    this.newStrategyForm.disable(); // Disabilita l'intero gruppo di controllo
  }

  removeStrategy(index: number): void {
    const strategiesArray = this.editForm.get('strategies') as FormArray;

    const strategyValue = strategiesArray.at(index).get('strategy')!.value;

    strategiesArray.removeAt(index); // Rimuovi l'elemento dall'indice specificato

    // Controlla se ci sono ancora strategie "freeze" nel FormArray
    const hasFreezeTot = strategiesArray.controls.some(control =>
      control.get('strategy')?.value.includes('FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS')
    );

    const hasConsecTot = strategiesArray.controls.some(control =>
      control.get('strategy')?.value.includes('FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS')
    );

    if (strategyValue === "SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS") {
      this.enableDeadline();
      this.postPosed = true;
      this.notPostPosedSelectionable = true;
    }
    if(strategyValue === "EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING"){
      this.eachStrategySelectionable = true;
      this.postPosed = true;
    }

    // Se la strategia rimossa è una delle due "freeze", controlla se deve essere riabilitata "Set Skip Behaviour"
    if (strategyValue.includes('FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS')) {
      this.freezeTot = !hasFreezeTot; // Riabilita solo se non esistono altre istanze
    }
    if (strategyValue.includes('FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS')) {
      this.consecTot = !hasConsecTot; // Riabilita solo se non esistono altre istanze
    }

    // Se entrambe le strategie "freeze" sono rimosse, riabilita il campo "Set Skip Behaviour"
    if (!hasFreezeTot && !hasConsecTot) {
      this.skippingStrategy = false;
    }

    // Controllo se l'array è vuoto dopo la rimozione
    if (strategiesArray.length === 0) {
      this.enableFormControls();
      this.notPostPosedSelectionable = true;
    }
  }



  closeSessionModal() {
    this.isModalOpen = false;
    this.isAddingNewSession = false;
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
      (this.editForm.get('resources') as FormArray).push(resourcesGroup);

      // Resetta il form per una nuova risorsa
      this.newResourcesForm.reset();
    } else {
      console.error('All fields are required for the resource.');
    }
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


  removeSubtask(subtask: FormGroup) {
    // Logica per rimuovere il subtask
    this.subtaskComponent.enableSessionCheckbox(subtask);
    this.subtaskComponent.enableResourceCheckbox(subtask);
  }

  getSubtaskSessions(index: number): FormArray {
    const subtasks = this.editForm.get('subtasks') as FormArray;
    let sessions = subtasks.at(index).get('subSessions') as FormArray
    console.log(sessions.at(0).get('startDate'));
    return subtasks.at(index).get('subSessions') as FormArray;
  }

  getSubtaskResources(index: number): FormArray {
    const subtasks = this.editForm.get('subtasks') as FormArray;
    return subtasks.at(index).get('subResources') as FormArray;
  }

  exitSubtaskForm(){
    this.showSubs = true
  }


  activateResourceInput(){
    this.showResourceInput = true;
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

  enableFormControls() {
    this.newStrategyForm.enable(); // Riabilita l'intero gruppo di controllo
  }




  ngOnDestroy(){
    if(this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  protected onInput(event: Event) {
    this.value.set((event.target as HTMLInputElement).value);
  }

  get sessions(): FormArray<FormGroup> {
    return this.editForm.get('sessions') as
      FormArray<FormGroup>;
  }

  get strategies(): FormArray<FormControl>{
    return this.editForm.get('strategies') as
      FormArray<FormControl>;
  }

  get resources(): FormArray<FormGroup> {
    return this.editForm.get('resources') as FormArray<FormGroup>;
  }



  get startDateControl ():FormControl {
    return this.newSessionForm.get('startDate') as FormControl;
  }

  get endDateControl():FormControl {
    return this.newSessionForm.get('endDate') as FormControl;
  }

  get resourceNameControl (): FormControl {
    return this.newResourcesForm.get('name') as FormControl;
  }
  get typeControl (): FormControl {
    return this.newResourcesForm.get('type') as FormControl;
  }
  get valueControl (): FormControl {
    return this.newResourcesForm.get('value') as FormControl;
  }



  get descriptionControl(): FormControl {
    return this.editForm.get('description') as FormControl;
  }

  get priorityControl(): FormControl {
    return this.editForm.get('priority') as FormControl;
  }

  get totalTimeControl(): FormControl {
    return this.editForm.get('totalTime') as FormControl;
  }

  get timeTableControl(): FormControl {
    return this.editForm.get('timetable') as FormControl;
  }

  get nameControl(): FormControl {
    return this.editForm.get('name') as FormControl;
  }

  get deadline(): FormControl {
    return this.editForm.get('deadline') as FormControl;
  }

  get topicControl(): FormControl {
    return this.editForm.get('topic') as FormControl;
  }

  get subtasks(): FormArray {
    return this.editForm.get('subtasks') as FormArray;
  }




}
