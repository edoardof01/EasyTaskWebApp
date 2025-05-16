import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SharedtodoComponent } from './sharedtodo.component';

describe('SharedtodoComponent', () => {
  let component: SharedtodoComponent;
  let fixture: ComponentFixture<SharedtodoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedtodoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SharedtodoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
