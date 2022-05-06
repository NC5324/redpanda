import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { UserSubscriptionComponent } from '../list/user-subscription.component';
import { UserSubscriptionDetailComponent } from '../detail/user-subscription-detail.component';
import { UserSubscriptionUpdateComponent } from '../update/user-subscription-update.component';
import { UserSubscriptionRoutingResolveService } from './user-subscription-routing-resolve.service';

const userSubscriptionRoute: Routes = [
  {
    path: '',
    component: UserSubscriptionComponent,
    data: {
      defaultSort: 'id,asc',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: UserSubscriptionDetailComponent,
    resolve: {
      userSubscription: UserSubscriptionRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: UserSubscriptionUpdateComponent,
    resolve: {
      userSubscription: UserSubscriptionRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: UserSubscriptionUpdateComponent,
    resolve: {
      userSubscription: UserSubscriptionRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(userSubscriptionRoute)],
  exports: [RouterModule],
})
export class UserSubscriptionRoutingModule {}
