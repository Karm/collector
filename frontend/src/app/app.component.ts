import { Component } from '@angular/core';
import { Router } from '@angular/router';

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
    <top-nav></top-nav>
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
    <div>
      <top-nav></top-nav>
    <div>
    <div>
      <app-stats-table></app-stats-table>
    </div>
  `,
  styles: []
})
export class StatsComponent {
}
