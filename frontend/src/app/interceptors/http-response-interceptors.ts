import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class HttpResponseInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap(event => {
        // 🔹 Intercetta le risposte HTTP con status 201 e le converte in successi
        if (event instanceof HttpResponse && event.status === 201) {
          console.log("✅ Interceptor: Trattando HTTP 201 come successo.");
        }
      })
    );
  }
}
