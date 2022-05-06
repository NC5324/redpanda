import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { IUserSubscription } from '../user-subscription.model';
import { UserSubscriptionService } from '../service/user-subscription.service';

@Component({
  templateUrl: './user-subscription-delete-dialog.component.html',
})
export class UserSubscriptionDeleteDialogComponent {
  userSubscription?: IUserSubscription;

  constructor(protected userSubscriptionService: UserSubscriptionService, protected activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.userSubscriptionService.delete(id).subscribe(() => {
      this.activeModal.close('deleted');
    });
  }
}
