import { Component } from '@angular/core';
import {NgForOf, NgIf} from "@angular/common";
import {DataFromFormService} from '../services/data-from-form.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-sharedinprogress',
    imports: [
        NgForOf,
        NgIf
    ],
  templateUrl: './sharedinprogress.component.html',
  standalone: true,
  styleUrl: './sharedinprogress.component.css'
})
export class SharedinprogressComponent {


  tasks: any[] = [];
  private tasksSubscription!: Subscription; // Subscription per gestire l'Observable

  constructor(private dataFromFormService: DataFromFormService) {}

  ngOnInit(): void {

    this.tasksSubscription = this.dataFromFormService
      .getTasks('Shared'/*, 'INPROGRESS'*/) // Ottieni l'Observable dei task
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
