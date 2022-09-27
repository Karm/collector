import { HttpClient, HttpHeaders } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule, MatSelectChange } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../auth-service/auth.service';

@Component({
  selector: 'app-tag-selector',
  templateUrl: './tag-selector.component.html',
  styleUrls: [ './tag-selector.component.css' ]
})
export class TagSelectorComponent implements OnInit {

  default_selection: string = "<-- None -->";
  selected_tag : string = this.default_selection;
  tags: string[] = [ this.default_selection ];

  constructor(private _http: HttpClient,
              private _router: Router,
              private _auth: AuthenticationService) { }

  private getTagsFromApi() {
    let headers = new HttpHeaders();
    let myHeaders: HttpHeaders;
    let tok: string | null;
    if (this._auth.token() != null) {
      tok = this._auth.token();
      myHeaders = headers.set("token", tok ? tok : "");
    } else {
      myHeaders = headers;
    }
    return this._http.get<string[]>('/api/v1/image-stats/tags/distinct', { 'headers': myHeaders });
  }

  private goToStats(t: string) {
    // nativage to the stats table page with the selected tag
    // being used as filter
    this._router.navigate(['/stats'], {queryParams: { tag: t }});
  }

  // Called by drop-down selection widget on-change
  showStatsTable(e: MatSelectChange) {
    let t: string = (e.value as string);
    this.goToStats(t);
  }

  ngOnInit(): void {
    this.getTagsFromApi().subscribe( t => {
      t.forEach( v => {
        this.tags.push(v);
      });
    });
  }

}
