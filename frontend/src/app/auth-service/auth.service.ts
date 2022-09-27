import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  fakeUsername: string = "mandrel";
  fakePassword: string = "123qwe";

  constructor() { }

  login(username: string, password: string): Observable<any> {
    // Mock a successful call to an API server.
    if (username == this.fakeUsername && password == this.fakePassword) {
      sessionStorage.setItem("token", "my-super-secret-token-from-server");
      return of(new HttpResponse({ status: 200 }));
    } else {
      return of(new HttpResponse({ status: 401 }));
    }
  }

  logout(): Observable<any> {
    sessionStorage.removeItem("token");
    console.log(sessionStorage.getItem("token"));
    return of(new HttpResponse({ status: 200 }));
  }

  isUserLoggedIn(): boolean {
    if (sessionStorage.getItem("token") != null) {
      return true;
    }
    return false;
  }
}
