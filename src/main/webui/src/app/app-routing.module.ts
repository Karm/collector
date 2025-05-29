import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DefaultComponent, StatsComponent } from './app.component'
import { StatsChartComponent } from './stats-chart/stats-chart.component'
import { LoginComponent } from './login/login.component'
import {
  AuthGuardService as AuthGuard
} from './auth-guard/auth-guard.service';

const routes: Routes = [
  {path: 'login', component: LoginComponent},
  {path: 'logout', component: LoginComponent},
  {path: '', pathMatch: 'full', canActivate: [AuthGuard], component: DefaultComponent},
  {path: 'stats', canActivate: [AuthGuard], component: StatsComponent},
  {path: 'charts', canActivate: [AuthGuard], component: StatsChartComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
