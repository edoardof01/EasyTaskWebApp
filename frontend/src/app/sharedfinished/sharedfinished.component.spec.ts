import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SharedfinishedComponent } from './sharedfinished.component';

describe('SharedfinishedComponent', () => {
  let component: SharedfinishedComponent;
  let fixture: ComponentFixture<SharedfinishedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedfinishedComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SharedfinishedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
