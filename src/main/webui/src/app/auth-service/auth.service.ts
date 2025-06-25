import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpResponse, HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  constructor(private _http: HttpClient,
	      private _router: Router) { }

  login(username: string, password: string) {
    const formData = new FormData();
    formData.append('j_username', username);
    formData.append('j_password', password);
    this._http.post('/j_security_check', formData)
               .subscribe( res => this.handleJSecurityResp(),
                           err => this.handleAuthFail(err));
  }
  
  handleJSecurityResp() {
    this._http.post<Token>('/api/tokens/create/r', null)
	                                 .subscribe( data =>  {
						   sessionStorage.setItem("token", data.token);
				                   this._router.navigateByUrl("/");
					 });
  }

  handleAuthFail(resp: HttpResponse<any>) {
    // j_security_check didn't work. Redirect with login fail.
    this._router.navigateByUrl("/login?error=Login failed");
  }

  logout(): Observable<any> {
    sessionStorage.removeItem("token");
    return of(new HttpResponse({ status: 200 }));
  }

  isUserLoggedIn(): boolean {
    if (this.token() != null) {
      return true;
    }
    return false;
  }

  token(): string | null {
    return sessionStorage.getItem("token");
  }
}

interface Token {
  token: string;
  message: string;
}
