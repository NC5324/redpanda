import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IUserSubscription } from '../user-subscription.model';

@Component({
  selector: 'jhi-user-subscription-detail',
  templateUrl: './user-subscription-detail.component.html',
})
export class UserSubscriptionDetailComponent implements OnInit {
  userSubscription: IUserSubscription | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ userSubscription }) => {
      this.userSubscription = userSubscription;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
