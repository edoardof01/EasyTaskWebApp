import { Routes } from '@angular/router';
import {TasksComponent} from './tasks/tasks.component';
import {SharedComponent} from './shared/shared.component';
import {GroupComponent} from './group/group.component';
import {LoginComponent} from './login/login.component';
import {SidebarComponent} from './sidebar/sidebar.component';
import {RegisterComponent} from './register/register.component';
import {AuthGuardService} from './services/auth-guard.service';
import {NavbarComponent} from './navbar/navbar.component';
import {CalendarComponent} from './calendar/calendar.component';
import {ProfileComponent} from './profile/profile.component';
import {EditingTaskComponent} from './editing-task/editing-task.component';
import {InprogressComponent} from './inprogress/inprogress.component';
import {FreezedComponent} from './freezed/freezed.component';
import {FinishedComponent} from './finished/finished.component';
import {TodoComponent} from './todo/todo.component';
import {FoldersSideBarComponent} from './folders-side-bar/folders-side-bar.component';



export const routes: Routes = [
  {path: '', redirectTo: '/login', pathMatch: 'full'},
  {path: 'login', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  {path:'navbar',component: NavbarComponent, canActivate: [AuthGuardService], children: [
    {path: 'editingTask', component: EditingTaskComponent, canActivate: [AuthGuardService]},
    {path: 'calendar', component: CalendarComponent, canActivate: [AuthGuardService]},
    {path: 'profile', component: ProfileComponent, canActivate: [AuthGuardService]},
    {path: 'tasks', component: TasksComponent, canActivate: [AuthGuardService]},
    {path: 'folders', component: FoldersSideBarComponent,canActivate: [AuthGuardService],children: [
      {path: '',redirectTo: 'personal/todo', pathMatch: 'full'},
      {path: 'personal/todo', component: TodoComponent,canActivate: [AuthGuardService]},
      {path: 'personal/inprogress', component: InprogressComponent,canActivate: [AuthGuardService]},
      {path: 'personal/freezed', component: FreezedComponent,canActivate: [AuthGuardService]},
      {path: 'personal/finished', component: FinishedComponent,canActivate: [AuthGuardService]},
      ]},
  ]},
];
