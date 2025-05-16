import { TestBed } from '@angular/core/testing';

import { DataFromFormService } from './data-from-form.service';

describe('DataFromFormService', () => {
  let service: DataFromFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DataFromFormService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
