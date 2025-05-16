import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SharedfreezedComponent } from './sharedfreezed.component';

describe('SharedfreezedComponent', () => {
  let component: SharedfreezedComponent;
  let fixture: ComponentFixture<SharedfreezedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedfreezedComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SharedfreezedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
