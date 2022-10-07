import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MatTableDataSource } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { AfterViewInit, Component, Inject, LOCALE_ID, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { formatNumber } from '@angular/common';
import { AuthenticationService } from '../auth-service/auth.service';

class ImageStatsSubset {
  id: number;
  tag: string;
  img_name: string;
  total_classes: number;
  total_methods: number;
  total_fields: number;
  reflection_classes: number;
  reflection_methods: number;
  reflection_fields: number;
  reachable_classes: number;
  reachable_percent_cl: FormattedData;
  reachable_methods: number;
  reachable_fields: number;
  jni_classes: number;
  jni_methods: number;
  jni_fields: number;
  image_size: FormattedData;
  created_at: string;

  constructor() {
     this.img_name = "";
     this.total_classes = -1;
     this.total_methods = -1;
     this.total_fields = -1;
     this.jni_classes = -1;
     this.jni_methods = -1;
     this.jni_fields = -1;
     this.reachable_classes = -1;
     this.reachable_methods = -1;
     this.reachable_fields = -1;
     this.reachable_percent_cl = new FormattedData("", -1);
     this.reflection_classes = -1;
     this.reflection_methods = -1;
     this.reflection_fields = -1;
     this.created_at = "";
     this.image_size = new FormattedData("", -1);
     this.tag = "";
     this.id = -1;
  }

  setReachablePercentCl(locale: string) {
    let percent: number;
    let format_percent: string;
    percent = this.calculatePercent(this.reachable_classes, this.total_classes);
    format_percent = formatNumber(percent, locale, "0.2-2");
    this.reachable_percent_cl = new FormattedData(format_percent, percent);
  }

  private calculatePercent(n: number, div: number) : number {
    return (n/div) * 100;
  }
}

class FormattedData {
  formatted_value: string;
  data: number;
  constructor(fmt_v: string, d: number) {
    this.formatted_value = fmt_v;
    this.data = d;
  }
}

interface ClassStats {
  classes: number;
  fields: number;
  methods: number;
  id: number;
}

interface ImageSizeStats {
  id: number,
  total_bytes: number,
  code_cache_bytes: number,
  heap_bytes: number,
  other_bytes: number,
  debuginfo_bytes: number
}

@Component({
  selector: 'app-stats-table',
  templateUrl: './stats-table.component.html',
  styleUrls: [ './stats-table.component.css' ]
})
export class StatsTableComponent implements OnInit, AfterViewInit {

  displayedColumns: string[] = ['img_name', 'image_size', 'reachable_classes', 'reachable_percent_cl', 'reachable_methods', 'reachable_fields', 'reflection_classes', 'reflection_methods', 'reflection_fields', 'jni_classes', 'jni_fields', 'jni_methods', 'created_at'];
  dataSource = new MatTableDataSource<ImageStatsSubset>();
  tag : string | undefined;

  constructor(private _http: HttpClient,
              private _route: ActivatedRoute,
              private _router: Router,
              @Inject(LOCALE_ID) public _locale: string,
	      private _auth: AuthenticationService) {
    this.paginator = null;
  }

  @ViewChild(MatSort)
  sort: MatSort = new MatSort();

  @ViewChild(MatPaginator)
  paginator: MatPaginator | null;

  private getStatsFromApi(t: string | undefined) {
    let headers = new HttpHeaders();
    let myHeaders: HttpHeaders;
    let tok: string | null;
    if (this._auth.token() != null) {
      tok = this._auth.token();
      myHeaders = headers.set("token", tok ? tok : "");
    } else {
      myHeaders = headers;
    }
    if (!!t) {
      // get stats by tag
      return this._http.get<[]>('/api/v1/image-stats/tag/' + encodeURIComponent(t), { 'headers': myHeaders });
    } else {
      return this._http.get<[]>('/api/v1/image-stats', { 'headers': myHeaders });
    }
  }

  ngAfterViewInit() {
    this.sort.disableClear = true; // disable clear
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.dataSource.sortingDataAccessor = (row: ImageStatsSubset, columnName: string) : string | number => {
      switch (columnName) {
        case 'image_size': return row.image_size.data;
        case 'reachable_percent_cl': return row.reachable_percent_cl.data;
        case 'reachable_classes': return row.reachable_classes;
        case 'reachable_methods': return row.reachable_methods;
        case 'reachable_fields': return row.reachable_fields;
        default: return row[columnName as keyof ImageStatsSubset] as string;
      }
    }
  }

  private transform(stats: Object) {
     let k: keyof typeof stats;
     let st = new ImageStatsSubset();
     for (k in stats) {
        if (k.includes("img_name")) {
          let ob: Object;
          ob = stats[k];
          st.img_name = (ob as string);
        }
        if (k.includes("total_classes_stats")) {
          let ob: Object;
          let cl_st: ClassStats;
          ob = stats[k];
          cl_st = (ob as ClassStats);
          st.total_classes = cl_st.classes;
          st.total_methods = cl_st.methods;
          st.total_fields = cl_st.fields;
        }
        if (k.includes("reflection_stats")) {
          let ob: Object;
          let r_st: ClassStats;
          ob = stats[k];
          r_st = (ob as ClassStats);
          st.reflection_classes = r_st.classes;
          st.reflection_methods = r_st.methods;
          st.reflection_fields = r_st.fields;
        }
        if (k.includes("reachability_stats")) {
          let ob: Object;
          let r_st: ClassStats;
          ob = stats[k];
          r_st = (ob as ClassStats);
          st.reachable_classes = r_st.classes;
          st.reachable_methods = r_st.methods;
          st.reachable_fields = r_st.fields;
        }
        if (k.includes("jni_classes_stats")) {
          let ob: Object;
          let jni_st: ClassStats;
          ob = stats[k];
          jni_st = (ob as ClassStats);
          st.jni_classes = jni_st.classes;
          st.jni_methods = jni_st.methods;
          st.jni_fields = jni_st.fields;
        }
        if (k.includes("image_size_stats")) {
          let ob: Object;
          let im_st: ImageSizeStats;
          ob = stats[k];
          im_st = (ob as ImageSizeStats);
          st.image_size = new FormattedData(this.niceBytes(im_st.total_bytes), im_st.total_bytes);
        }
        if (k.includes("created_at")) {
          let ob: Object;
          ob = stats[k];
          st.created_at = (ob as string);
        }
        if (k.includes("tag")) {
          let ob: Object;
          let tag: string;
          ob = stats[k];
          tag = (ob as string);
          st.tag = tag;
        }
        if (k.includes("id")) {
          let ob: Object;
          ob = stats[k];
          st.id = (ob as number);
        }
     }
     return st;
  }


  private niceBytes(bytes: number, decimals: number = 2) {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

    const i = Math.floor(Math.log(bytes) / Math.log(k));

    const val = parseFloat((bytes / Math.pow(k, i)).toFixed(dm));
    return val.toFixed(dm) + ' ' + sizes[i];
  }

  // navigate to chart page for the given ID
  rowClicked(row: ImageStatsSubset) {
    // nativage to the stats charts page for the selected
    // stat
    this._router.navigate(['/charts'], {queryParams: { statId: row.id }});

  }

  ngOnInit(): void {
    // First get the tag from query params
    this._route.queryParams.subscribe( params => {
      this.tag = params['tag'];
    });
    this.getStatsFromApi(this.tag).subscribe( stats => {
      let dataStats: ImageStatsSubset[] = [];
      stats.forEach( item => {
        let s: ImageStatsSubset;
        s = this.transform(item);
        s.setReachablePercentCl(this._locale);
        dataStats.push(s);
      });
      this.dataSource.data = dataStats;
    });
  }

}
