import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupfinishedComponent } from './groupfinished.component';

describe('GroupfinishedComponent', () => {
  let component: GroupfinishedComponent;
  let fixture: ComponentFixture<GroupfinishedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupfinishedComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupfinishedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
