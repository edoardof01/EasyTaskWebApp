import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InprogressComponent } from './inprogress.component';

describe('InprogressComponent', () => {
  let component: InprogressComponent;
  let fixture: ComponentFixture<InprogressComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InprogressComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InprogressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
