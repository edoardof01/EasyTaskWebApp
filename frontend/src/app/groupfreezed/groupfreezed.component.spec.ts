import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupfreezedComponent } from './groupfreezed.component';

describe('GroupfreezedComponent', () => {
  let component: GroupfreezedComponent;
  let fixture: ComponentFixture<GroupfreezedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupfreezedComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupfreezedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
