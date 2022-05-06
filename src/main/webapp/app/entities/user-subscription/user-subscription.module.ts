import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { UserSubscriptionComponent } from './list/user-subscription.component';
import { UserSubscriptionDetailComponent } from './detail/user-subscription-detail.component';
import { UserSubscriptionUpdateComponent } from './update/user-subscription-update.component';
import { UserSubscriptionDeleteDialogComponent } from './delete/user-subscription-delete-dialog.component';
import { UserSubscriptionRoutingModule } from './route/user-subscription-routing.module';

@NgModule({
  imports: [SharedModule, UserSubscriptionRoutingModule],
  declarations: [
    UserSubscriptionComponent,
    UserSubscriptionDetailComponent,
    UserSubscriptionUpdateComponent,
    UserSubscriptionDeleteDialogComponent,
  ],
  entryComponents: [UserSubscriptionDeleteDialogComponent],
})
export class UserSubscriptionModule {}
