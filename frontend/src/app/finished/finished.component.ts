import {Component, OnDestroy, OnInit} from '@angular/core';
import {DataFromFormService} from '../services/data-from-form.service';
import {Subscription} from 'rxjs';
import {DatePipe, LowerCasePipe, NgForOf, NgIf, TitleCasePipe} from '@angular/common';
import {Router, RouterOutlet} from '@angular/router';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-finished',
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    LowerCasePipe,
    RouterOutlet,
    TitleCasePipe,
    TranslatePipe
  ],
  templateUrl: './finished.component.html',
  standalone: true,
  styleUrl: './finished.component.css'
})
export class FinishedComponent implements OnInit,OnDestroy {

  tasks: any[] = [];
  selectedTaskId: number | null = null;
  isTaskCard = false;
  private tasksSubscription!: Subscription; // Subscription per gestire l'Observable


  constructor(private translate: TranslateService, private router: Router, private dataFromFormService: DataFromFormService) {}


  ngOnInit(): void {
    this.tasksSubscription = this.dataFromFormService.getTasks('personal').subscribe({
      next: (tasks) => {
        for (const task of tasks) {
          if(task.taskState==="FINISHED") {
            this.tasks.push(task);
          }
        }

      },
      error: (error) => {
        console.error('Errore nel recupero dei task:', error);
      }
    });
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
