import { Component, OnInit, ViewChild } from '@angular/core';
import { ChartData, ChartEvent, ChartType } from 'chart.js';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { AuthenticationService } from '../auth-service/auth.service';

interface ImageStats {
  id: number;
  tag: string;
  img_name: string;
  generator_version: string;
  image_size_stats: ImageSizeStats;
  jni_classes_stats: ClassStats;
  reflection_stats: ClassStats;
  build_perf_stats: BuildPerfStats;
  total_classes_stats: ClassStats;
  reachability_stats: ClassStats;
  created_at: string;
}

interface BuildPerfStats {
  id: number;
  total_build_time_sec: number;
  num_cpu_cores: number;
  total_machine_memory: number;
  peak_rss_bytes: number;
  cpu_load: number;
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

interface DataBag {
  data: [number, number, number, number];
}

@Component({
  selector: 'app-charts-component',
  templateUrl: './stats-chart.component.html',
  styleUrls: [ './stats-chart.component.css' ]
})
export class StatsChartComponent implements OnInit {

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  stat_id: number = -1;
  total_size: number = -1;
  img_name: string = "N/A";
  stat : ImageStats | undefined;

  constructor(private _http: HttpClient,
              private _route: ActivatedRoute,
              private _auth: AuthenticationService) {
  }

  // Doughnut
  public imageSizeLabels: string[] = [ 'Heap Bytes', 'Code Cache Bytes', 'Other Bytes', 'Debuginfo Bytes' ];
  public doughnutChartData: ChartData<'doughnut'> = {
    labels: this.imageSizeLabels,
    datasets: [ ]
  };
  public doughnutChartType: ChartType = 'doughnut';

  // events
  public chartClicked({ event, active }: { event: ChartEvent, active: {}[] }): void {
    // nothing
  }

  public chartHovered({ event, active }: { event: ChartEvent, active: {}[] }): void {
    // nothing
  }

  private getDataSets(s: ImageStats) : DataBag {
    return { data: [ s.image_size_stats.heap_bytes,
                     s.image_size_stats.code_cache_bytes,
                     s.image_size_stats.other_bytes,
                     s.image_size_stats.debuginfo_bytes ]};
  }

  getStatsFromApi(id: number) {
    let headers = new HttpHeaders();
    let myHeaders: HttpHeaders;
    let tok: string | null;
    if (this._auth.token() != null) {
      tok = this._auth.token();
      myHeaders = headers.set("token", tok ? tok : "");
    } else {
      myHeaders = headers;
    }
    return this._http.get<ImageStats>('/api/v1/image-stats/' + id, { 'headers': myHeaders });
  }

  ngOnInit(): void {
    // First get the tag from query params
    this._route.queryParams.subscribe( params => {
      this.stat_id = params['statId'];
    });
    this.getStatsFromApi(this.stat_id).subscribe( s => {
      this.stat = s;
      this.total_size = s.image_size_stats.total_bytes;
      this.img_name = s.img_name;
      this.doughnutChartData.datasets.push(this.getDataSets(this.stat));
      this.chart?.update();
    });
  }
}
