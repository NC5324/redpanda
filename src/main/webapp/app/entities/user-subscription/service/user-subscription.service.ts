import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as dayjs from 'dayjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IUserSubscription, getUserSubscriptionIdentifier } from '../user-subscription.model';

export type EntityResponseType = HttpResponse<IUserSubscription>;
export type EntityArrayResponseType = HttpResponse<IUserSubscription[]>;

@Injectable({ providedIn: 'root' })
export class UserSubscriptionService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/user-subscriptions');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(userSubscription: IUserSubscription): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(userSubscription);
    return this.http
      .post<IUserSubscription>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(userSubscription: IUserSubscription): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(userSubscription);
    return this.http
      .put<IUserSubscription>(`${this.resourceUrl}/${getUserSubscriptionIdentifier(userSubscription) as number}`, copy, {
        observe: 'response',
      })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  partialUpdate(userSubscription: IUserSubscription): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(userSubscription);
    return this.http
      .patch<IUserSubscription>(`${this.resourceUrl}/${getUserSubscriptionIdentifier(userSubscription) as number}`, copy, {
        observe: 'response',
      })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IUserSubscription>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IUserSubscription[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addUserSubscriptionToCollectionIfMissing(
    userSubscriptionCollection: IUserSubscription[],
    ...userSubscriptionsToCheck: (IUserSubscription | null | undefined)[]
  ): IUserSubscription[] {
    const userSubscriptions: IUserSubscription[] = userSubscriptionsToCheck.filter(isPresent);
    if (userSubscriptions.length > 0) {
      const userSubscriptionCollectionIdentifiers = userSubscriptionCollection.map(
        userSubscriptionItem => getUserSubscriptionIdentifier(userSubscriptionItem)!
      );
      const userSubscriptionsToAdd = userSubscriptions.filter(userSubscriptionItem => {
        const userSubscriptionIdentifier = getUserSubscriptionIdentifier(userSubscriptionItem);
        if (userSubscriptionIdentifier == null || userSubscriptionCollectionIdentifiers.includes(userSubscriptionIdentifier)) {
          return false;
        }
        userSubscriptionCollectionIdentifiers.push(userSubscriptionIdentifier);
        return true;
      });
      return [...userSubscriptionsToAdd, ...userSubscriptionCollection];
    }
    return userSubscriptionCollection;
  }

  protected convertDateFromClient(userSubscription: IUserSubscription): IUserSubscription {
    return Object.assign({}, userSubscription, {
      createdDate: userSubscription.createdDate?.isValid() ? userSubscription.createdDate.toJSON() : undefined,
      lastModifiedDate: userSubscription.lastModifiedDate?.isValid() ? userSubscription.lastModifiedDate.toJSON() : undefined,
    });
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.createdDate = res.body.createdDate ? dayjs(res.body.createdDate) : undefined;
      res.body.lastModifiedDate = res.body.lastModifiedDate ? dayjs(res.body.lastModifiedDate) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((userSubscription: IUserSubscription) => {
        userSubscription.createdDate = userSubscription.createdDate ? dayjs(userSubscription.createdDate) : undefined;
        userSubscription.lastModifiedDate = userSubscription.lastModifiedDate ? dayjs(userSubscription.lastModifiedDate) : undefined;
      });
    }
    return res;
  }
}
