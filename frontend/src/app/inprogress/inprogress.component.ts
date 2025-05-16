import {Component, OnDestroy, OnInit} from '@angular/core';
import {DataFromFormService} from '../services/data-from-form.service';
import {Subscription} from 'rxjs';
import {DatePipe, LowerCasePipe, NgClass, NgForOf, NgIf, TitleCasePipe} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';


@Component({
  selector: 'app-inprogress',
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    LowerCasePipe,
    TitleCasePipe,
    NgClass,
    TranslatePipe
  ],
  templateUrl: './inprogress.component.html',
  standalone: true,
  styleUrl: './inprogress.component.css'
})
export class InprogressComponent implements OnInit,OnDestroy {

  tasks: any[] = [];
  selectedTaskId: number | null = null;
  isTaskCard = false;
  private tasksSubscription!: Subscription; // Subscription per gestire l'Observable

  private pollingTimer: any;

  constructor(private translate: TranslateService, private router: Router, private dataFromFormService: DataFromFormService) {}

  ngOnInit(): void {
    // 1. Caricamento iniziale
    this.dataFromFormService.getTasks('personal').subscribe({
      next: (tasks) => {
        // Salvi i task nel tuo array locale
        this.tasks = tasks.filter(t => t.taskState === 'INPROGRESS');
      },
      error: (err) => console.error('Errore:', err),
    });

    // 2. Avvio polling
    this.pollingTimer = setInterval(() => {
      this.dataFromFormService.getTasks('personal').subscribe({
        next: (tasks) => {
          this.tasks = tasks.filter(t => t.taskState === 'INPROGRESS');
        },
        error: (err) => console.error('Errore:', err),
      });
    }, 30000); // ogni 30 secondi
  }



  editTask(task: any){
    this.router.navigate(['/navbar/editingTask'], {queryParams: {taskId: task.id}});
    this.tasks.filter(oldTask => task.id != oldTask.id);
  }

  goToCalendar(taskId: number) {
    this.router.navigate(['/navbar/calendar'],);
  }

  deleteTask(taskId: number): void {
    this.dataFromFormService.deleteInProgressSessionsFromCalendar(taskId,'personal').subscribe({
      next: () => {
        console.log('rimozione sessioni di task inprogress avvenuta con successo');
      },
      error: (error) => {
        console.error('Errore nella rimozione delle sessioni: ',error);
      }
    })
    this.dataFromFormService.deleteTask('personal', taskId).subscribe({
      next: () => {
        this.tasks = this.tasks.filter(task => task.id !== taskId); // Rimuove il task dalla UI
      },
      error: (error) => {
        console.error('❌ Errore durante l\'eliminazione del task:', error);
      }
    });
  }

  expandTask(taskId: number){
    this.selectedTaskId = this.selectedTaskId === taskId ? null : taskId;
    this.isTaskCard = !this.isTaskCard;
  }


  ngOnDestroy(): void {
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
    }
    // Annulla la sottoscrizione quando il componente viene distrutto
    if (this.tasksSubscription) {
      this.tasksSubscription.unsubscribe();
    }
  }
}
