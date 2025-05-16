import { Component } from '@angular/core';
import {TasksComponent} from '../tasks/tasks.component';
import {NgIf} from '@angular/common';
import {SharedComponent} from '../shared/shared.component';
import {GroupComponent} from '../group/group.component';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-sidebar',
  imports: [
    TasksComponent,
    NgIf,
    SharedComponent,
    GroupComponent,
    RouterLink,
    RouterLinkActive,
    RouterOutlet
  ],
  templateUrl: './sidebar.component.html',
  standalone: true,
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  taskType: string = 'personal';

  isSidebarOpen = false;

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
  }



}
