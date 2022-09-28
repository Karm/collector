import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthenticationService } from '../auth-service/auth.service';
import { HttpClient } from '@angular/common/http';

interface CVersion {
  version: string;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm = new FormGroup({
    username: new FormControl('', Validators.required),
    password: new FormControl('', [ Validators.required, Validators.minLength(4)])
  });

  version: string | null;

  error: string | null;

  constructor(private _authenticationService: AuthenticationService,
              private _http: HttpClient,
	      private _route: ActivatedRoute) {
    this.error = null;
    this.version = null;
  }

  ngOnInit(): void {
    // First get the tag from query params
    this._route.queryParams.subscribe( params => {
      this.error = params['error'];
    });
    // Clear error marker on value changes
    this.loginForm.valueChanges.subscribe(x => { this.error = null });
    // Get the collector version
    this._http.get<CVersion>('/public/version').subscribe( v => {
      this.version = v.version;
    });
  }

  login(): void {
    let user = this.username?.value;
    let pass = this.password?.value;
    this._authenticationService.login(user, pass);
  }

  get username() { return this.loginForm.get('username'); }
  get password() { return this.loginForm.get('password'); }

}
