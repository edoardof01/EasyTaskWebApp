import { HttpInterceptorFn } from '@angular/common/http';

export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', ` ${token}`), /*aggiungi eventualmente prima: Bearer */
    });
    return next(cloned);
  }
  return next(req);
};
