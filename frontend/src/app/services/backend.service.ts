import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BackendService {

  baseUrl: string = 'http://localhost:8080/EasyTask-1.0-SNAPSHOT/api'
  constructor( private http: HttpClient) {}

  sendDataPERSONAL(data: any): Observable<any> {
    const url = `${this.baseUrl}/personal/`;
    return this.http.post<any>(url, data);
  }
  sendDataSHARED(data: any): Observable<any> {
    const url = `${this.baseUrl}/shared/`;
    return this.http.post<any>(url, data);
  }
  sendDataGROUP(data: any): Observable<any> {
    const url = `${this.baseUrl}/group/`;
    return this.http.post<any>(url, data);
  }
}
