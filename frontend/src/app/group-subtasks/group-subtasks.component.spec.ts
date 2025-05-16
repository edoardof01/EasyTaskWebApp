import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupSubtasksComponent } from './group-subtasks.component';

describe('GroupSubtasksComponent', () => {
  let component: GroupSubtasksComponent;
  let fixture: ComponentFixture<GroupSubtasksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupSubtasksComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupSubtasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
