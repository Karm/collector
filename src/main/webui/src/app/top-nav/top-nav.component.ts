import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthenticationService } from '../auth-service/auth.service';
import { HttpResponse } from '@angular/common/http';

@Component({
  selector: 'top-nav',
  templateUrl: './top-nav.component.html',
  styleUrls: ['./top-nav.component.css']
})
export class TopNavComponent implements OnInit {

  public currPath: string | null;

  constructor(public auth: AuthenticationService,
	      public router: Router,
	      public route: ActivatedRoute) {
    this.currPath = null;
  }

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

  ngOnInit() {
    this.route.url.subscribe( data =>  {
       if (data.length > 0) {
          this.currPath = data[0].path;
       }
    });
  }
}
