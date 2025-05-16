import {Component, OnDestroy, OnInit} from '@angular/core';
import {DatePipe, LowerCasePipe, NgForOf, NgIf, TitleCasePipe} from '@angular/common';
import {DataFromFormService} from '../services/data-from-form.service';
import {Subscription} from 'rxjs';
import {Router, RouterOutlet} from '@angular/router';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-freezed',
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    LowerCasePipe,
    RouterOutlet,
    TitleCasePipe,
    TranslatePipe
  ],
  templateUrl: './freezed.component.html',
  standalone: true,
  styleUrl: './freezed.component.css'
})
export class FreezedComponent implements OnInit, OnDestroy {

  tasks: any[] = [];
  selectedTaskId: number | null = null;
  isTaskCard = false;
  private tasksSubscription!: Subscription; // Subscription per gestire l'Observable


  constructor(private translate: TranslateService, private router: Router, private dataFromFormService: DataFromFormService) {}


  ngOnInit(): void {
    this.tasksSubscription = this.dataFromFormService.getTasks('personal').subscribe({
      next: (tasks) => {
        for (const task of tasks) {
          if(task.taskState==="FREEZED") {
            this.tasks.push(task);
          }
        }

      },
      error: (error) => {
        console.error('Errore nel recupero dei task:', error);
      }
    });
  }

  editTask(task: any){
    this.router.navigate(['/navbar/editingTask'], {queryParams: {taskId: task.id}})
  }

  goToCalendar(taskId: number) {
    this.dataFromFormService.moveToCalendarTask('personal', taskId).subscribe({
      next: (updatedTask) => {
        console.log(`✅ Task con ID ${taskId} spostato con successo. Risultato:`, updatedTask);

        this.router.navigate(['/navbar/calendar'], {
          queryParams: {
            toCalendar: true,
          }
        });
      },
      error: (err) => {
        console.error('❌ Errore durante lo spostamento del task:', err);
      }
    });
    this.tasks = this.tasks.filter(task => task.id != taskId);
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
    // Annulla la sottoscrizione quando il componente viene distrutto
    if (this.tasksSubscription) {
      this.tasksSubscription.unsubscribe();
    }
  }
}
