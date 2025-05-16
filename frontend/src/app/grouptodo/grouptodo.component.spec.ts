import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GrouptodoComponent } from './grouptodo.component';

describe('GrouptodoComponent', () => {
  let component: GrouptodoComponent;
  let fixture: ComponentFixture<GrouptodoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GrouptodoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GrouptodoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
