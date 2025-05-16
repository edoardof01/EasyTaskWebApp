import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FoldersSideBarComponent } from './folders-side-bar.component';

describe('FoldersSideBarComponent', () => {
  let component: FoldersSideBarComponent;
  let fixture: ComponentFixture<FoldersSideBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FoldersSideBarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FoldersSideBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
