import {Component, OnInit} from '@angular/core';
import {GroupComponent} from '../group/group.component';
import {NgClass, NgIf} from '@angular/common';
import {SharedComponent} from '../shared/shared.component';
import {TasksComponent} from '../tasks/tasks.component';
import {TodoComponent} from '../todo/todo.component';
import {InprogressComponent} from '../inprogress/inprogress.component';
import {FreezedComponent} from '../freezed/freezed.component';
import {FinishedComponent} from '../finished/finished.component';
import {SharedtodoComponent} from '../sharedtodo/sharedtodo.component';
import {SharedinprogressComponent} from '../sharedinprogress/sharedinprogress.component';
import {SharedfreezedComponent} from '../sharedfreezed/sharedfreezed.component';
import {SharedfinishedComponent} from '../sharedfinished/sharedfinished.component';
import {GrouptodoComponent} from '../grouptodo/grouptodo.component';
import {GroupinprogressComponent} from '../groupinprogress/groupinprogress.component';
import {GroupfreezedComponent} from '../groupfreezed/groupfreezed.component';
import {GroupfinishedComponent} from '../groupfinished/groupfinished.component';
import {Router, RouterOutlet} from '@angular/router';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-folders-side-bar',
  imports: [
    GroupComponent,
    NgIf,
    SharedComponent,
    TasksComponent,
    TodoComponent,
    InprogressComponent,
    FreezedComponent,
    FinishedComponent,
    SharedtodoComponent,
    SharedinprogressComponent,
    SharedfreezedComponent,
    SharedfinishedComponent,
    GrouptodoComponent,
    GroupinprogressComponent,
    GroupfreezedComponent,
    GroupfinishedComponent,
    NgClass,
    RouterOutlet,
    TranslatePipe
  ],
  templateUrl: './folders-side-bar.component.html',
  standalone: true,
  styleUrl: './folders-side-bar.component.css'
})/*
export class FoldersSideBarComponent {
  taskType: string = '';
  stateType: string = '';
  isMenu1Open = false;
  isMenu2Open = false;
  isMenu3Open = false;

  clickedStates: Record<string, string> = {
    Personal: '',
    Shared: '',
    Group: ''
  };

  constructor(private router: Router) {}

  toggleMenu1(): void {
    this.resetPreviousState(); // Reset dello stato precedente
    this.closeAllMenus();
    this.isMenu1Open = !this.isMenu1Open;
    this.taskType = 'Personal';

  }

  toggleMenu2(): void {
    this.resetPreviousState(); // Reset dello stato precedente
    this.closeAllMenus();
    this.isMenu2Open = !this.isMenu2Open;
    this.taskType = 'Shared';
  }

  toggleMenu3(): void {
    this.resetPreviousState(); // Reset dello stato precedente
    this.closeAllMenus();
    this.isMenu3Open = !this.isMenu3Open;
    this.taskType = 'Group';
  }

  closeAllMenus(): void {
    this.isMenu1Open = false;
    this.isMenu2Open = false;
    this.isMenu3Open = false;
  }

  resetPreviousState(): void {
    if (this.taskType) {
      this.clickedStates[this.taskType] = ''; // Resetta lo stato del menu precedente
    }
  }

  setShow(state: string): void {
    this.stateType = state;
    this.clickedStates[this.taskType] = state;
    // Costruisci il percorso corretto in base alla selezione
    if (this.taskType === 'Personal') {
      this.router.navigate([`/navbar/folders/personal/${state.toLowerCase()}`]);
    } else if (this.taskType === 'Shared') {
      this.router.navigate([`/navbar/folders/shared/${state.toLowerCase()}`]);
    } else if (this.taskType === 'Group') {
      this.router.navigate([`/navbar/folders/group/${state.toLowerCase()}`]);
    }
  }
}

*/

export class FoldersSideBarComponent implements OnInit {
  taskType: string = 'Personal';
  stateType: string = '';

  // Considerando che gestiamo solo il task "Personal", manteniamo solo questa voce
  clickedStates: Record<string, string> = {
    Personal: ''
  };

  constructor(private translate: TranslateService, private router: Router) {}


  ngOnInit() {
    this.stateType = "TODO";
  }

  // Ora, non sono necessari i metodi di toggle né il reset dello stato
  setShow(state: string): void {
    this.stateType = state;
    this.clickedStates[this.taskType] = state;
    // Naviga al percorso relativo al task Personal
    this.router.navigate([`/navbar/folders/personal/${state.toLowerCase()}`]);
  }
}
