import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'user-subscription',
        data: { pageTitle: 'UserSubscriptions' },
        loadChildren: () => import('./user-subscription/user-subscription.module').then(m => m.UserSubscriptionModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
