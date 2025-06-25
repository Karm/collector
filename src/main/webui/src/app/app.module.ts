import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent, DefaultComponent, StatsComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginatorModule } from '@angular/material/paginator';
import { NgChartsModule } from 'ng2-charts';

import { StatsTableComponent } from './stats-table/stats-table.component';
import { TagSelectorComponent } from './tag-selector/tag-selector.component';
import { StatsChartComponent } from './stats-chart/stats-chart.component';
import { LoginComponent } from './login/login.component';
import { TopNavComponent } from './top-nav/top-nav.component';
import {
  AuthGuardService as AuthGuard
} from './auth-guard/auth-guard.service';

@NgModule({
  declarations: [
    AppComponent,
    DefaultComponent,
    StatsComponent,
    StatsTableComponent,
    TagSelectorComponent,
    StatsChartComponent,
    LoginComponent,
    TopNavComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    MatTableModule,
    FormsModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatFormFieldModule,
    MatTooltipModule,
    MatSortModule,
    MatPaginatorModule,
    NoopAnimationsModule,
    NgChartsModule
  ],
  providers: [AuthGuard],
  bootstrap: [AppComponent]
})
export class AppModule { }
