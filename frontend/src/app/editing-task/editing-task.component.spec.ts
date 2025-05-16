import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditingTaskComponent } from './editing-task.component';

describe('EditingTaskComponent', () => {
  let component: EditingTaskComponent;
  let fixture: ComponentFixture<EditingTaskComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditingTaskComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditingTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
