import {Component, OnInit, OnDestroy, TemplateRef} from '@angular/core';
import { Subscription } from 'rxjs';
import { DataFromFormService } from '../services/data-from-form.service';
import {DatePipe, LowerCasePipe, NgClass, NgForOf, NgIf, TitleCasePipe} from '@angular/common';
import {Router, RouterOutlet} from '@angular/router';
import {MatProgressBar} from '@angular/material/progress-bar';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-todo',
  templateUrl: './todo.component.html',
  standalone: true,
  imports: [
    NgIf,
    NgForOf,
    DatePipe,
    NgClass,
    TitleCasePipe,
    LowerCasePipe,
    RouterOutlet,
    MatProgressBar,
    TranslatePipe
  ],
  styleUrls: ['./todo.component.css']
})
export class TodoComponent implements OnInit, OnDestroy {

  tasks: any[] = [];
  selectedTaskId: number | null = null;
  isTaskCard = false;

  private tasksSubscription!: Subscription;

  constructor(private translate: TranslateService, private router: Router ,private dataFromFormService: DataFromFormService) {}

  ngOnInit(): void {
    // Sottoscriviti direttamente al BehaviorSubject per avere aggiornamenti live
    this.tasksSubscription = this.dataFromFormService.tasksCache['personal'].asObservable().subscribe({
      next: (tasks) => {
        // Sovrascrivi l'array dei task con quelli aggiornati, filtrando per taskState "TODO"
        this.tasks = tasks.filter(task => task.taskState === "TODO");

      },
      error: (error) => {
        console.error('Errore nel recupero dei task:', error);
      }
    });
    // Triggera il caricamento iniziale dei task (questo farà in modo che il BehaviorSubject venga aggiornato)
    this.dataFromFormService.getTasks('personal').subscribe();
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

  editTask(task: any) {
    this.router.navigate(['/navbar/editingTask'], {queryParams: {taskId: task.id}})
  }


  deleteTask(taskId: number): void {
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
    if (this.tasksSubscription) {
      this.tasksSubscription.unsubscribe();
    }
  }
}
