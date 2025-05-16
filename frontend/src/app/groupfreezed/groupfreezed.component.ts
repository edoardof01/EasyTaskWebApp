import { Component } from '@angular/core';
import {DataFromFormService} from '../services/data-from-form.service';
import {Subscription} from 'rxjs';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-groupfreezed',
  imports: [
    NgForOf,
    NgIf
  ],
  templateUrl: './groupfreezed.component.html',
  standalone: true,
  styleUrl: './groupfreezed.component.css'
})
export class GroupfreezedComponent {

  tasks: any[] = []; // Contiene solo i task Personal e TODO
  private tasksSubscription!: Subscription; // Subscription per gestire l'Observable

  constructor(private dataFromFormService: DataFromFormService) {}

  ngOnInit(): void {
    // Sottoscriviti ai task di tipo 'Personal' e stato 'TODO'
    this.tasksSubscription = this.dataFromFormService
      .getTasks('Group'/*, 'FREEZED'*/) // Ottieni l'Observable dei task
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
