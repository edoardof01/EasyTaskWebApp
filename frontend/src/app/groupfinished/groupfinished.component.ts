import { Component } from '@angular/core';
import {NgForOf, NgIf} from '@angular/common';
import {DataFromFormService} from '../services/data-from-form.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-groupfinished',
  imports: [
    NgForOf,
    NgIf
  ],
  templateUrl: './groupfinished.component.html',
  standalone: true,
  styleUrl: './groupfinished.component.css'
})
export class GroupfinishedComponent {


  tasks: any[] = [];
  private tasksSubscription!: Subscription; // Subscription per gestire l'Observable

  constructor(private dataFromFormService: DataFromFormService) {}

  ngOnInit(): void {
    this.tasksSubscription = this.dataFromFormService
      .getTasks('Group'/*, 'FINISHED'*/) // Ottieni l'Observable dei task
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
