import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from './auth-service/auth.service';
import { Router } from '@angular/router';
import { HttpResponse } from '@angular/common/http';

@Component({
  selector: 'app-root',
  template: `
    <router-outlet></router-outlet>
  `,
  styles: []
})
export class AppComponent {
  title = 'webapp';
}

@Component({
  selector: 'app-default',
  template: `
    <h1> Tabular View </h1>
    <div>
      <app-tag-selector></app-tag-selector>
    </div>
    <br>
    <div>
      <a [routerLink]="['/stats']">Show all stats</a>
    <div>
  `,
  styles: []
})
export class DefaultComponent {
}

@Component({
  selector: 'app-rest',
  template: `
    <div style="width: 100%">
    <span style="width: 50%; display: inline-block;">
      Navigation: <a [routerLink]="['/']">Home</a> &gt;&gt; Stats
    </span>
    <span style="width: 50%; text-align: right; display: inline-block;">
      <a [routerLink]="['/logout']" (click)="logout()">Logout</a>
    </span>
    </div>
    <div>&nbsp;</div>
    <div>
      <app-stats-table></app-stats-table>
    </div>
  `,
  styles: []
})
export class StatsComponent {
  constructor(public auth: AuthenticationService, public router: Router) {}

  handleLogout(res: HttpResponse<any>): void {
    if (res.status != 200) {
       console.log("Logout failed");
    }
    this.router.navigateByUrl("/login")
  }

  logout(): void {
    this.auth.logout().subscribe(
	    res => this.handleLogout(res),
            err => this.handleLogout(err),
    );
  }
}
