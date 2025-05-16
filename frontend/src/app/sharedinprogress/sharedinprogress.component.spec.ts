import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SharedinprogressComponent } from './sharedinprogress.component';

describe('SharedinprogressComponent', () => {
  let component: SharedinprogressComponent;
  let fixture: ComponentFixture<SharedinprogressComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedinprogressComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SharedinprogressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
