import { Component } from '@angular/core';
import {DataFromFormService} from '../services/data-from-form.service';
import {Subscription} from 'rxjs';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-grouptodo',
  imports: [
    NgForOf,
    NgIf
  ],
  templateUrl: './grouptodo.component.html',
  standalone: true,
  styleUrl: './grouptodo.component.css'
})
export class GrouptodoComponent {

  tasks: any[] = [];
  private tasksSubscription!: Subscription; // Subscription per gestire l'Observable

  constructor(private dataFromFormService: DataFromFormService) {}

  ngOnInit(): void {
    this.tasksSubscription = this.dataFromFormService
      .getTasks('Group'/*, 'TODO'*/) // Ottieni l'Observable dei task
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
