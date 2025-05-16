import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupinprogressComponent } from './groupinprogress.component';

describe('GroupinprogressComponent', () => {
  let component: GroupinprogressComponent;
  let fixture: ComponentFixture<GroupinprogressComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupinprogressComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupinprogressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
