import { Component } from '@angular/core';
import {NgForOf, NgIf} from "@angular/common";
import {DataFromFormService} from '../services/data-from-form.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-sharedfinished',
    imports: [
        NgForOf,
        NgIf
    ],
  templateUrl: './sharedfinished.component.html',
  standalone: true,
  styleUrl: './sharedfinished.component.css'
})
export class SharedfinishedComponent {


  tasks: any[] = [];
  private tasksSubscription!: Subscription; // Subscription per gestire l'Observable

  constructor(private dataFromFormService: DataFromFormService) {}

  ngOnInit(): void {

    this.tasksSubscription = this.dataFromFormService
      .getTasks('Shared'/*, 'FINISHED'*/) // Ottieni l'Observable dei task
      .subscribe((tasks) => {
        this.tasks = tasks; // Aggiorna la lista dei task visualizzati
      });
  }

  ngOnDestroy(): void {
    // Annulla la sottoscrizione quando il componente viene distrutto
    if (this.tasksSubscription) {
      this.tasksSubscription.unsubscribe();
    }
  }
}
