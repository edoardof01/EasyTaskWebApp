import { Component } from '@angular/core';
import {RouterLink, RouterOutlet} from '@angular/router';
import {MatTab, MatTabGroup, MatTabLabel} from '@angular/material/tabs';
import {MatIcon} from '@angular/material/icon';
import {CalendarComponent} from './calendar/calendar.component';
import {TasksComponent} from './tasks/tasks.component';
import {NgIf} from '@angular/common';
import {SidebarComponent} from './sidebar/sidebar.component';
import {FoldersSideBarComponent} from './folders-side-bar/folders-side-bar.component';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, MatTabGroup, MatTab, MatIcon, MatTabLabel, CalendarComponent, TasksComponent, NgIf, SidebarComponent, FoldersSideBarComponent, RouterLink],
  templateUrl: './app.component.html',
  standalone: true,
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'angular_EasyTask';

  constructor(private translate: TranslateService) {
    this.translate.addLangs(['en', 'it']); // Lingue disponibili
    this.translate.setDefaultLang('en'); // Lingua predefinita

    const browserLang = this.translate.getBrowserLang();
    this.translate.use(browserLang?.match(/en|it/) ? browserLang : 'en');
  }

}

