import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationService } from '../auth-service/auth.service';
import { HttpResponse } from '@angular/common/http';

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

  error: string;

  constructor(private authenticationService: AuthenticationService,
              private router: Router) {
    this.error = "";
  }

  ngOnInit(): void {
  }

  login(): void {
    let user = this.username?.value;
    let pass = this.password?.value;
    this.authenticationService.login(user, pass);
  }

  get username() { return this.loginForm.get('username'); }
  get password() { return this.loginForm.get('password'); }

}
