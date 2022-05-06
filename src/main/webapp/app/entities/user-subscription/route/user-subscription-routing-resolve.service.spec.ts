jest.mock('@angular/router');

import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { IUserSubscription, UserSubscription } from '../user-subscription.model';
import { UserSubscriptionService } from '../service/user-subscription.service';

import { UserSubscriptionRoutingResolveService } from './user-subscription-routing-resolve.service';

describe('UserSubscription routing resolve service', () => {
  let mockRouter: Router;
  let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
  let routingResolveService: UserSubscriptionRoutingResolveService;
  let service: UserSubscriptionService;
  let resultUserSubscription: IUserSubscription | undefined;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [Router, ActivatedRouteSnapshot],
    });
    mockRouter = TestBed.inject(Router);
    mockActivatedRouteSnapshot = TestBed.inject(ActivatedRouteSnapshot);
    routingResolveService = TestBed.inject(UserSubscriptionRoutingResolveService);
    service = TestBed.inject(UserSubscriptionService);
    resultUserSubscription = undefined;
  });

  describe('resolve', () => {
    it('should return IUserSubscription returned by find', () => {
      // GIVEN
      service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
      mockActivatedRouteSnapshot.params = { id: 123 };

      // WHEN
      routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
        resultUserSubscription = result;
      });

      // THEN
      expect(service.find).toBeCalledWith(123);
      expect(resultUserSubscription).toEqual({ id: 123 });
    });

    it('should return new IUserSubscription if id is not provided', () => {
      // GIVEN
      service.find = jest.fn();
      mockActivatedRouteSnapshot.params = {};

      // WHEN
      routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
        resultUserSubscription = result;
      });

      // THEN
      expect(service.find).not.toBeCalled();
      expect(resultUserSubscription).toEqual(new UserSubscription());
    });

    it('should route to 404 page if data not found in server', () => {
      // GIVEN
      jest.spyOn(service, 'find').mockReturnValue(of(new HttpResponse({ body: null as unknown as UserSubscription })));
      mockActivatedRouteSnapshot.params = { id: 123 };

      // WHEN
      routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
        resultUserSubscription = result;
      });

      // THEN
      expect(service.find).toBeCalledWith(123);
      expect(resultUserSubscription).toEqual(undefined);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
    });
  });
});
