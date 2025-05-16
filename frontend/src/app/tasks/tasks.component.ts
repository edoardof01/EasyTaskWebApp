import {Component, inject, input, OnInit, signal, ViewChild} from '@angular/core';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import { MatInput } from '@angular/material/input';
import { resourceValueValidator} from '../utils/resourceValueValidator'
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {JsonPipe, NgClass, NgForOf, NgIf} from '@angular/common';
import { SubtasksComponent } from '../subtasks/subtasks.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { AbstractControl } from '@angular/forms';
import { MatIcon } from '@angular/material/icon';
import { MatError } from '@angular/material/form-field';
import { MatHint } from '@angular/material/form-field';
import {BackendService} from '../services/backend.service';
import {atLeastOneSessionValidator,atLeastOneStrategyValidator} from '../../custom-validators';
import {UserService} from '../services/user.service';
import {catchError, filter, of, switchMap, take} from 'rxjs';
import {ActivatedRoute, Router} from '@angular/router';
import {DataFromFormService} from '../services/data-from-form.service';
import {TaskStateService} from '../services/task-state.service';
import {moneyValueValidator} from '../utils/moneyValueValidator';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-tasks',
  imports: [
    MatFormField,
    MatLabel,
    MatOption,
    MatSelect,
    MatInput,
    ReactiveFormsModule,
    NgIf,
    NgForOf,
    SubtasksComponent,
    MatButton,
    MatIcon,
    MatIconButton,
    MatError,
    MatHint,
    FormsModule,
    JsonPipe,
    NgClass,
    TranslatePipe
  ],
  templateUrl: './tasks.component.html',
  standalone: true,
  styleUrls: ['./tasks.component.css']
})
export class TasksComponent implements OnInit{
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

  isProfileComplete = false;
  showProfileWarning = false;

  isBackFromCalendar = false;

  notPostPosedSelectionable = true;
  postPosed = true;
  eachStrategySelectionable = true;
  freezeTot = true;
  consecTot = true;
  hidSkipBehavior = false;
  hidSkipBehaviorConsec = false;

  selectedTab: string = 'sessions';

  scheduleForm: FormGroup;

  days: string[] = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

  private fb = inject(FormBuilder);
  errorOnSubmit = false;


  disableDeadline(){
    this.deadline.disable();
    if(!this.hidSkipBehavior && !this.hidSkipBehaviorConsec){
      this.skippingStrategy = false;
    }
  }

  eachMethod() {
    if(!this.hidSkipBehavior && !this.hidSkipBehaviorConsec){
      this.skippingStrategy = false;
    }
  }

  enableDeadline(){
    this.deadline.enable();
  }

  updateSubtaskButtonState(): void {
    this.blockErrorMessage2 = this.sessions.length > 0;
    this.areSubtasks = this.sessions.length > 0;
  }

  constructor(private translate: TranslateService, private taskStateService: TaskStateService,private dataFromFormService: DataFromFormService,private route:ActivatedRoute, private router: Router, private userService: UserService, private backendService: BackendService){
    this.scheduleForm = this.fb.group({
      startScheduleDate: [''],
      endScheduleDate: [''],
      weeklySchedule: this.fb.array(
        this.days.map(() => this.createDaySchedule())
      )
    });
  }

  get weeklySchedule(): FormArray {
    return this.scheduleForm.get('weeklySchedule') as FormArray;
  }

  private createDaySchedule(): FormGroup {
    return this.fb.group({
      startTime: [''],
      endTime: ['']
    });
  }

  @ViewChild(SubtasksComponent) subtaskComponent!: SubtasksComponent;

  ngOnInit() {
    this.userService.getProfileCompletionStatus$().subscribe(isComplete => {
      this.isProfileComplete = isComplete;
    });
    this.route.queryParams.subscribe((params) => {
      this.isBackFromCalendar = (params['backToPersonal'] === 'true')
    })
    if(this.isBackFromCalendar){
      let temporaryForm = this.taskStateService.getFormData();
      if (temporaryForm) {
        this.taskForm = temporaryForm;
        this.timetableIsDefined = true;
        this.areSubtasks = true;
      }
    }
    this.isBackFromCalendar = false;
    this.newResourcesForm.get('type')?.valueChanges.subscribe((selectedType) => {
      if (selectedType === 'MONEY') {
        // Se passa a MONEY, azzero "value"
        this.newResourcesForm.patchValue({ value: '' });
      } else {
        // Se passa a COMPETENCE/EQUIPMENT, azzero "money"
        this.newResourcesForm.patchValue({ money: '' });
      }
    });
  }

  taskForm = this.fb.group({
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
    description: new FormControl('', [
      Validators.required,
      Validators.minLength(15),
      Validators.maxLength(1000)
    ]),
    totalTime: new FormControl('', [
      Validators.required,
      Validators.min(1)
    ]),
    timetable: new FormControl('', [
      Validators.required,
    ]),
    deadline: new FormControl('', [
      this.futureDateValidator()
    ]),
    topic: new FormControl('',[
      Validators.required,
    ]),
    subtasks: this.fb.array([]),
    sessions: this.fb.array([], [atLeastOneSessionValidator()]),
    resources: this.fb.array([]),
    strategies: this.fb.array([],[atLeastOneStrategyValidator])
  });

  newStrategyForm = this.fb.group({
    strategy: ['', Validators.required],
    totSkippedCheckbox: [false],
    totSkippedValue: [null],
    totConsecSkippedCheckbox: [false],
    totConsecSkippedValue: [null]
  });

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



  closeModal(): void {
    this.areStrategies = false; // Nasconde il modal
    console.log('Modal chiuso');
  }

  saveStrategy(): void {
    const strategiesArray = this.taskForm.get('strategies') as FormArray;

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
            strategy: 'FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS',
            tot: totSkippedValue,
            maxConsecSkipped: null
          });
          strategiesArray.push(strategySkipped);
          this.notPostPosedSelectionable = false;
          this.freezeTot = false;

          this.newStrategyForm.patchValue({
            totSkippedCheckbox: false,
            totSkippedValue: null,
          });
        }
        if (totConsecSkippedChecked && totConsecSkippedValue !== null) {
          const strategyConsecutive = this.fb.group({
            strategy: 'FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS',
            tot: null,
            maxConsecSkipped: totConsecSkippedValue
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



  removeStrategy(index: number): void {
    const strategiesArray = this.taskForm.get('strategies') as FormArray;

    const strategyValue = strategiesArray.at(index).get('strategy')!.value;

    strategiesArray.removeAt(index);

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
    if (strategyValue === 'FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS') {
      this.freezeTot = !hasFreezeTot;
    }
    if (strategyValue ==='FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS') {
      this.consecTot = !hasConsecTot;
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




  enableFormControls() {
    this.newStrategyForm.enable(); // Riabilita l'intero gruppo di controllo
  }


  totSkippedMethod(){
    this.totSkipped = !this.totSkipped;
    this.hidSkipBehavior =!this.hidSkipBehavior;
  }

  totConsecSkippedMethod(){
    this.totConsecSkipped = !this.totConsecSkipped;
    this.hidSkipBehaviorConsec = !this.hidSkipBehaviorConsec;
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


  removeSubtask(subtask: FormGroup) {
    // Logica per rimuovere il subtask
    this.subtaskComponent.enableSessionCheckbox(subtask);
    this.subtaskComponent.enableResourceCheckbox(subtask);
  }


  protected onInput(event: Event) {
    this.value.set((event.target as HTMLInputElement).value);
  }

  timetableChosen(){
    this.timetableIsDefined = true;
    this.blockErrorMessage = true;
  }

  get sessions(): FormArray<FormGroup> {
    return this.taskForm.get('sessions') as
      FormArray<FormGroup>;
  }

  get strategies(): FormArray<FormControl>{
    return this.taskForm.get('strategies') as
      FormArray<FormControl>;
  }



  get resources(): FormArray<FormGroup> {
    return this.taskForm.get('resources') as FormArray<FormGroup>;
  }

  onSaveSessionClick() {
    try {
      this.addSessionWithDetails()
      this.blockErrorMessage2 = true;
      this.areSubtasks = true;
    }catch (error) {
      this.errorMessage = 'Operation failed: ' + (error as Error).message;
      this.isErrorMessageVisible = true;

      setTimeout(() => {
        this.isErrorMessageVisible = false;
      }, 800);

      setTimeout(() => {
        this.errorMessage = null;
      }, 1500);
    }
  }

  startAddingSession() {
    this.isAddingNewSession = true;
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
      throw new Error("Sessions can't be in the Past");
    }

    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      throw new Error('Start or End Date not valid');
    }

    const durationInMilliseconds = end.getTime() - start.getTime();
    const durationInHours = durationInMilliseconds / (1000 * 60 * 60);
    const isExactMultipleOfHour = Number.isInteger(durationInHours);

    if (durationInHours <= 0 || !isExactMultipleOfHour) {
      throw new Error('a Session must last a multiple of an hour');
    }

    if (!this.controlTimeTable(start, end)) {
      throw new Error('the TimeSlot selected is Not Valid');
    }

    this.updateSubtaskButtonState();

    const sessionGroup = this.fb.group({
      startDate: [startValue, Validators.required],
      endDate: [endValue, Validators.required],
    });

    (this.taskForm.get('sessions') as FormArray).push(sessionGroup);

    this.newSessionForm.reset();
  }

  controlTimeTable(start: Date, end: Date): boolean {
    const timeTable = this.taskForm.get('timetable')!.value;
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


  generateSessionsFromWeeklySchedule() {
    const startDate = new Date(this.scheduleForm.value.startScheduleDate);
    const endDate = new Date(this.scheduleForm.value.endScheduleDate);
    const weekly = this.scheduleForm.value.weeklySchedule;

    if (startDate > endDate) {
      console.error("startDate is after endDate. Interrupting.");
      return;
    }
    for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
      const dayOfWeek = d.getDay();
      let index = dayOfWeek - 1;
      if (index < 0) index = 6;

      const daySchedule = weekly[index];

      if (daySchedule?.startTime && daySchedule?.endTime) {
        const [startH, startM] = daySchedule.startTime.split(':').map(Number);
        const [endH, endM] = daySchedule.endTime.split(':').map(Number);

        const sessionStart = new Date(d);
        sessionStart.setHours(startH, startM, 0, 0);

        const sessionEnd = new Date(d);
        sessionEnd.setHours(endH, endM, 0, 0);

        const sessionGroup = this.fb.group({
          startDate: [sessionStart.toISOString().slice(0,16)],
          endDate: [sessionEnd.toISOString().slice(0,16)]
        });
        this.sessions.push(sessionGroup);
      }
    }
    console.log("Sessioni generate:", this.sessions.value);
  }


  onSubmit() {
    if (!this.isProfileComplete) {
      this.showProfileWarning = true;
      setTimeout(() => this.showProfileWarning = false, 3000);
      return;
    }
    if (this.taskForm.valid && this.validateSessions()) {
      const username = localStorage.getItem('username');
      if (!username) {
        console.error('Error: Username not found in LocalStorage');
        return;
      }
      this.userService.getUserId().pipe(
        take(1),
        switchMap((id) => {
          if (id) return of(id);
          else {
            console.warn('ID utente non disponibile, recupero dal backend...');
            return this.userService.fetchUserIdByUsername(username).pipe(
              switchMap(() => this.userService.getUserId().pipe(take(1), filter(id => id !== null))),
              catchError(error => {
                console.error('Errore durante il recupero dell\'ID utente:', error);
                return of(null);
              })
            );
          }
        })
      ).subscribe((finalUserId) => {
        if (finalUserId) {
          console.log("ID utente recuperato correttamente:", finalUserId);
          this.processTask(finalUserId);
        } else {
          console.error('Errore: ID utente ancora non disponibile.');
        }
      });
    } else {
      console.error('Validation failed.');
    }
  }


  private processTask(id: string) {
    const taskWithUserId = {
      ...this.taskForm.value,
      userId: id  // ✅
    };
    console.log("📤 Task sent to the Backend:", taskWithUserId);
    this.backendService.sendDataPERSONAL(taskWithUserId).subscribe({
      next: (response: Response) => {console.log('✅Data sent with success:', response);},
      error: (error: Response) => {
        console.log('❌ Data not sent:', error);
        (this.taskForm.get('resources') as FormArray).clear();
        (this.taskForm.get('sessions') as FormArray).clear();
        (this.taskForm.get('strategies') as FormArray).clear();
        this.newStrategyForm.reset();
        this.newSessionForm.reset();
        this.newResourcesForm.reset({
          name: '',
          type: '',
          value: '',
          money: ''
        });
        this.errorOnSubmit = true;
        this.notPostPosedSelectionable = true;
        this.postPosed = true;
        this.eachStrategySelectionable = true;
        this.freezeTot = true;
        this.consecTot = true;
        this.taskForm.reset();

        setTimeout(() => {
          this.errorOnSubmit = false;
        }, 2900);
      }
    });
    this.taskForm.reset();
  }






  getSubtaskSessions(index: number): FormArray {
    const subtasks = this.taskForm.get('subtasks') as FormArray;
    let sessions = subtasks.at(index).get('subSessions') as FormArray
    console.log(sessions.at(0).get('startDate'));
    return subtasks.at(index).get('subSessions') as FormArray;
  }

  getSubtaskResources(index: number): FormArray {
    const subtasks = this.taskForm.get('subtasks') as FormArray;
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
        value: [this.newResourcesForm.get('value')?.value, resourceValueValidator],
        money: [this.newResourcesForm.get('money')?.value, moneyValueValidator],
      });
      (this.taskForm.get('resources') as FormArray).push(resourcesGroup);
      this.newResourcesForm.reset({
        name: '',
        type: '',
        value: '',
        money: ''
      });
    } else {
      console.error('All fields are required for the resource.');
    }
  }


  activateResourceInput(){
    this.showResourceInput = true;
  }

  get descriptionControl(): FormControl {
    return this.taskForm.get('description') as FormControl;
  }


  get priorityControl(): FormControl {
    return this.taskForm.get('priority') as FormControl;
  }

  get name(): String {
    const nameControl = this.taskForm.get('name') as FormControl;
    return nameControl.value;
  }

  get sessionCount(): number {
    return (this.taskForm.get('sessions') as FormArray).length;
  }


  get totalTimeControl(): FormControl {
    return this.taskForm.get('totalTime') as FormControl;
  }

  get timeTableControl(): FormControl {
    return this.taskForm.get('timetable') as FormControl;
  }

  get nameControl(): FormControl {
    return this.taskForm.get('name') as FormControl;
  }

  get deadline(): FormControl {
    return this.taskForm.get('deadline') as FormControl;
  }

  get topicControl(): FormControl {
    return this.taskForm.get('topic') as FormControl;
  }

  get subtasks(): FormArray {
    return this.taskForm.get('subtasks') as FormArray;
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






  // Metodo per rimuovere una sessione
  removeSession(index: number) {
    this.sessions.removeAt(index);
    this.updateSubtaskButtonState();
  }
  removeResources(index: number) {
    this.resources.removeAt(index);
  }

  // Metodo per validare le sessioni
  validateSessions(): boolean {
    if (this.subtasks.length != 0) {
      const taskSessions = this.taskForm.get('sessions') as FormArray;
      const subtaskSessions: string[] = [];

      for (let i = 0; i < this.subtasks.length; i++) {
        const child = this.subtasks.at(i) as FormGroup;
        const childSessions = child.get('subSessions') as FormArray;

        // Assicurati che le sessioni siano incluse nel task
        for (const session of childSessions.controls) {
          const sessionValue = session.value;

          const foundInTask = taskSessions.value.some((ts: any) =>
            ts.startDate === sessionValue.startDate && ts.endDate === sessionValue.endDate
          );
          if (!foundInTask) {
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

  get totalSessionsHours(): number {
    const sessionsArray = this.taskForm.get('sessions') as FormArray;
    let total = 0;
    sessionsArray.controls.forEach((control: AbstractControl) => {
      const session = control.value;
      const start = new Date(session.startDate);
      const end = new Date(session.endDate);
      // Differenza in millisecondi, convertita in ore
      const diffInMs = end.getTime() - start.getTime();
      const diffInHours = diffInMs / (1000 * 60 * 60);

      total += diffInHours;
    });
    return total;
  }

  get sessionsMismatchMessage(): string | null {
    let totalTime = this.taskForm.get('totalTime')?.value;
    const totalHours = this.totalSessionsHours;
    // Se totalTime è null/undefined, assegna 0
    if (totalTime == null) {
      totalTime = '0';
    }
    // Se non ci sono sessioni, nessun messaggio
    if (totalHours === 0) {
      return null;
    }
    // Se totalTime > somma delle durate, “X hours are missing”
    if (Number(totalTime) > totalHours) {
      const missing = Number(totalTime) - totalHours;
      return `${missing} hours are missing!`;
    }
    // Se totalTime < somma delle durate, “totalTime should be TOT”
    if (Number(totalTime) < totalHours) {
      return `totalTime should be ${totalHours}!`;
    }
    // Se totalTime === totalHours, nessun errore
    return null;
  }




  showInCalendar(sessions: any[]){
    console.log('Sessioni da passare al calendario:', sessions);
    const temporarySessions = sessions.map(session => ({
      ...session,
      taskId: -2,
      taskName: this.name ?? 'temporaryTask',
      type: 'personal',
      state:'PROGRAMMED'
    }));
    console.log('Temporary sessions:', temporarySessions);
    const actualSessions = this.dataFromFormService.calendarSessionsSubject.getValue();
    const viewSessions = [...actualSessions, ...temporarySessions];
    this.dataFromFormService.calendarSessionsSubject.next(viewSessions);


    this.taskStateService.setFormData(this.taskForm);

    this.router.navigate(['/navbar/calendar'], { queryParams: { showTemporarySessions: true } });
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



  protected readonly input = input;


}
