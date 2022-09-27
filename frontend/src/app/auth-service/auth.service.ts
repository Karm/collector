import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpResponse, HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  fakeUsername: string = "mandrel";
  fakePassword: string = "123qwe";

  constructor(private _http: HttpClient,
	      private _router: Router) { }

  login(username: string, password: string) {
    const formData = new FormData();
    formData.append('j_username', username);
    formData.append('j_password', password);
    this._http.post('/j_security_check', formData)
               .subscribe( res => { this._http.post<Token>('/api/tokens/create/r', null)
	                                 .subscribe( data =>  {
						   sessionStorage.setItem("token", data.token);
				                   this._router.navigateByUrl("/");
					 }); },
                           err => { console.log("j_security_check login failed " + err.status);
				    this._router.navigateByUrl("/login?error");
			           });
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
