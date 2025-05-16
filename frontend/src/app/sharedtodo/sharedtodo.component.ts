import { Component } from '@angular/core';
import {NgForOf, NgIf} from "@angular/common";
import {DataFromFormService} from '../services/data-from-form.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-sharedtodo',
    imports: [
        NgForOf,
        NgIf
    ],
  templateUrl: './sharedtodo.component.html',
  standalone: true,
  styleUrl: './sharedtodo.component.css'
})
export class SharedtodoComponent {

  tasks: any[] = []; // Contiene solo i task Personal e TODO
  private tasksSubscription!: Subscription; // Subscription per gestire l'Observable

  constructor(private dataFromFormService: DataFromFormService) {}

  ngOnInit(): void {
    // Sottoscriviti ai task di tipo 'Personal' e stato 'TODO'
    this.tasksSubscription = this.dataFromFormService
      .getTasks('Personal'/*, 'TODO'*/) // Ottieni l'Observable dei task
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
