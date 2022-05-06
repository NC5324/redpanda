import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IUserSubscription, UserSubscription } from '../user-subscription.model';
import { UserSubscriptionService } from '../service/user-subscription.service';

@Injectable({ providedIn: 'root' })
export class UserSubscriptionRoutingResolveService implements Resolve<IUserSubscription> {
  constructor(protected service: UserSubscriptionService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IUserSubscription> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((userSubscription: HttpResponse<UserSubscription>) => {
          if (userSubscription.body) {
            return of(userSubscription.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new UserSubscription());
  }
}
