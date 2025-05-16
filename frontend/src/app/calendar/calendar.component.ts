import {Component, OnDestroy, OnInit} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {DatePipe, LowerCasePipe, NgClass, NgForOf, NgIf, TitleCasePipe} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import { DataFromFormService } from '../services/data-from-form.service';
import {timer, Subscription, switchMap, forkJoin} from 'rxjs';
import { ChangeDetectorRef } from '@angular/core';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {map} from 'rxjs/operators';


@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  standalone: true,
  imports: [
    FormsModule,
    DatePipe,
    NgForOf,
    NgIf,
    NgClass,
    LowerCasePipe,
    TitleCasePipe,
    TranslatePipe
  ],
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnInit, OnDestroy {

  private todoSubscription!: Subscription;
  private calendarSubscription!: Subscription;

  showSessionDetails = false;
  sessionOfInterest:any = null

  modalTopPosition: number = 0;

  taskOfInterest:any = null;

  allowAddSessions = false;


  // Vista selezionata ('day' o 'month')
  viewMode: 'day' | 'month' = 'day';

  isMonthView = false

  selectedDay = new Date().getDate();
  selectedMonth = new Date().getMonth() + 1;
  selectedYear = new Date().getFullYear();

  daysArray = Array.from({ length: 31 }, (_, i) => i + 1);
  monthsArray = [
    { value: 1,  label: 'Gennaio' },
    { value: 2,  label: 'Febbraio' },
    { value: 3,  label: 'Marzo' },
    { value: 4,  label: 'Aprile' },
    { value: 5,  label: 'Maggio' },
    { value: 6,  label: 'Giugno' },
    { value: 7,  label: 'Luglio' },
    { value: 8,  label: 'Agosto' },
    { value: 9,  label: 'Settembre' },
    { value: 10, label: 'Ottobre' },
    { value: 11, label: 'Novembre' },
    { value: 12, label: 'Dicembre' }
  ];
  yearsArray = Array.from({ length: 11 }, (_, i) => 2020 + i);

  hoursInDay: string[] = [];

  daysInMonth: Date[] = [];

  extendedSessions: any[] = [];

  temporary: boolean = false;



  colors = ['bg-red-200', 'bg-blue-200','bg-yellow-200','bg-purple-200','bg-orange-200'];
  taskColors: { [key: string]: string } = {};
  borderColors = ['border-red-300', 'border-blue-300','border-orange-300', 'border-yellow-300', 'border-purple-300'];

  getTaskColor(taskId: string): string {
    if (!this.taskColors[taskId]) {
      const colorIndex = Object.keys(this.taskColors).length % this.colors.length;
      this.taskColors[taskId] = this.colors[colorIndex];
    }
    return this.taskColors[taskId];
  }

  getTaskBorderColor(taskId: string): string {
    if (!this.taskColors[taskId]) {
      const colorIndex = Object.keys(this.taskColors).length % this.borderColors.length;
      this.taskColors[taskId] = this.borderColors[colorIndex];
    }
    return this.taskColors[taskId];
  }

  getCircularReplacer() {
    const seen = new WeakSet();
    return (_key: string, value: any) => {
      if (typeof value === 'object' && value !== null) {
        if (seen.has(value)) {
          return;
        }
        seen.add(value);
      }
      return value;
    };
  }

  constructor( private translate: TranslateService, private route: ActivatedRoute, private router: Router, private cdr: ChangeDetectorRef,
    private dataFromFormService: DataFromFormService) {}

  ngOnInit(): void {
    this.todoSubscription = this.route.queryParams.subscribe(params => {
      this.allowAddSessions = !!params['toCalendar'];
    });
    this.route.queryParams.subscribe(params => {
      this.temporary = (params['showTemporarySessions'] === 'true');
    });
    if (!this.temporary) {
      this.dataFromFormService.getAllSessions('personal')
        .pipe(
          switchMap((allSessions) => {
            const taskIds = Array.from(new Set(allSessions.map(s => s.taskId)));
            const tasksObs = taskIds.map(id => this.dataFromFormService.getTask('personal', id));
            return forkJoin(tasksObs).pipe(
              map((allTasks) => {
                const freezeds = new Set(allTasks
                  .filter(t => t.taskState === 'FREEZED')
                  .map(t => t.id)
                );
                return allSessions.filter(s => !freezeds.has(s.taskId));
              }));
          }))
        .subscribe((sessionsFiltrate) => {
          this.dataFromFormService.calendarSessionsSubject.next(sessionsFiltrate);
        });
    }
    this.calendarSubscription = this.dataFromFormService.calendarSessions$.subscribe((sessions) => {
      this.extendedSessions = sessions;
      console.log('Sessions in entrance are: ', sessions);
      this.cdr.detectChanges();
    });
    this.generateHoursInDay();
    this.generateDaysInMonth();
  }



  completeSession(session: any){
    console.log('completeSession in calendar is:'+session.id + '_'+session.startDate+'_' + session.state)
    this.dataFromFormService.completeSession(session.taskType,session.taskId,session.id).subscribe({
      next: () => {
        this.showSessionDetails = false;
      }
    })
  }

  deleteTaskOfInterest(task: any) {
    this.dataFromFormService.deleteTask('personal', task.id).subscribe({
      next: () => {
        if (task.sessions) {
          const sessionIds = new Set(task.sessions.map((s: any) => s.id));
          const updatedSessions = this.dataFromFormService.calendarSessionsSubject.value.filter(
            session => !sessionIds.has(session.id)
          );
          this.dataFromFormService.calendarSessionsSubject.next(updatedSessions);
          this.extendedSessions = updatedSessions;
          localStorage.setItem('calendarSessions', JSON.stringify(updatedSessions));
          this.showSessionDetails = false;
        }
      },
      error: (err) => {
        console.error('Errore durante l\'eliminazione del task:', err);
      }
    });
  }

  editTaskOfInterest(task:any){
    this.dataFromFormService.freezeTaskFromCalendar('personal',task.id).subscribe({
        next: ()=> console.log('task correctly freezed'),
        error: (err) => {console.error('Errore durante il freezing del task:', err);}
    })
    this.router.navigate(['/navbar/editingTask'], {queryParams: {taskId: task.id}});
  }


  ngOnDestroy() {
    if (this.allowAddSessions) {
      this.allowAddSessions = false;
      this.todoSubscription.unsubscribe();
      this.calendarSubscription.unsubscribe();
    }
    const temporarySessions = this.dataFromFormService.calendarSessionsSubject.getValue();
    const actualSessions = temporarySessions.filter((session: any) => session.taskId !== -2);
    this.temporary = false;
    this.dataFromFormService.calendarSessionsSubject.next(actualSessions);
    localStorage.setItem('calendarSessions', JSON.stringify(actualSessions, this.getCircularReplacer()));
  }

  switchView(view: 'day' | 'month') {
    this.viewMode = view;
    if(view === 'month') {
      this.isMonthView = true;
    }
  }

  updateDaysArray() {
    const totalDays = this.getDaysInMonthManual(this.selectedMonth, this.selectedYear);
    this.daysArray = Array.from({ length: totalDays }, (_, i) => i + 1);
    if (this.selectedDay > totalDays) {
      this.selectedDay = totalDays;
    }
  }


  updateMonth() {
    if (this.selectedMonth < 1) {
      this.selectedMonth = 1;
    } else if (this.selectedMonth > 12) {
      this.selectedMonth = 12;
    }
    this.updateDaysArray();
    this.generateDaysInMonth();
  }

  generateHoursInDay() {
    this.hoursInDay = [];
    for (let i = 0; i < 24; i++) {
      this.hoursInDay.push(`${String(i).padStart(2, '0')}:00`);
    }
  }

  generateDaysInMonth() {
    const totalDays = this.getDaysInMonthManual(this.selectedMonth, this.selectedYear);
    const days: Date[] = [];
    const monthIndex = this.selectedMonth - 1;
    for (let d = 1; d <= totalDays; d++) {
      days.push(new Date(this.selectedYear, monthIndex, d));
    }
    this.daysInMonth = days;
  }

  getDaysInMonthManual(m: number, y: number): number {
    switch (m) {
      case 2: // Febbraio
        return this.isLeapYear(y) ? 29 : 28;
      case 4:
      case 6:
      case 9:
      case 11:
        return 30;
      default:
        return 31;
    }
  }

  get displayDate(): string {
    return `${this.selectedDay}/${this.selectedMonth}/${this.selectedYear}`;
  }

  switchBackToTask(){
    let temporarySessions = this.dataFromFormService.calendarSessionsSubject.getValue();
    const actualSessions = temporarySessions.filter((session:any)=>{
      return session.taskId != -2;
    })
    this.temporary = false;
    this.dataFromFormService.calendarSessionsSubject.next(actualSessions);
    localStorage.setItem('calendarSessions', JSON.stringify(actualSessions, this.getCircularReplacer()));
    this.router.navigate(['/navbar/tasks'], { queryParams: { backToPersonal: true }});
  }


  get displayMonthYear(): string {
    const found = this.monthsArray.find(x => x.value === this.selectedMonth);
    const label = found ? found.label : '???';
    return `${label} ${this.selectedYear}`;
  }

  isLeapYear(year: number): boolean {
    return ((year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0));
  }


  getSessionsForHour(hour: string) {
    const h = parseInt(hour.split(':')[0], 10);
    return this.extendedSessions.filter(s => {
      if (!(s.startDate instanceof Date)) {
        s.startDate = new Date(s.startDate);
      }
      return s.startDate.getFullYear() === this.selectedYear &&
        (s.startDate.getMonth() + 1) === this.selectedMonth &&
        s.startDate.getDate() === this.selectedDay &&
        s.startDate.getHours() === h;
    });
  }


  getSessionsForDay(day: Date) {
    return this.extendedSessions.filter(s => {
      if (!(s.startDate instanceof Date)) {
        s.startDate = new Date(s.startDate);
      }
      return s.startDate.getFullYear() === day.getFullYear() &&
        s.startDate.getMonth() === day.getMonth() &&
        s.startDate.getDate() === day.getDate();
    });
  }


  onSessionClick(session: any): void {
    this.dataFromFormService.getTask('personal', session.taskId).subscribe({
      next: (task: any) => {
        this.modalTopPosition = window.scrollY - 550;
        this.taskOfInterest = task;
        this.sessionOfInterest = session;
        this.showSessionDetails = true;
      },
      error: (error) => {
        console.error(`❌ Errore durante il processo per il task:`, error);
      }
    });
  }

  onSessionClose(){
    this.showSessionDetails = false;
    this.taskOfInterest = null;
  }


  getSessionTop(session: any): number {
    let start: Date;
    if (session.startDate instanceof Date) {
      start = session.startDate;
    } else {
      start = new Date(session.startDate);
    }
    if (isNaN(start.getTime())) {
      console.error("Data di inizio non valida per la sessione:", session);
      return 0;
    }
    return start.getHours() * 60 + start.getMinutes();
  }


  getSessionHeight(session: any): number {
    let start: Date;
    let end: Date;
    if (session.startDate instanceof Date) {
      start = session.startDate;
    } else {
      start = new Date(session.startDate);
    }
    if (session.endDate instanceof Date) {
      end = session.endDate;
    } else {
      end = new Date(session.endDate);
    }
    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      console.error("Data non valida per la sessione:", session);
      return 0;
    }
    return (end.getTime() - start.getTime()) / (1000 * 60);
  }
}
