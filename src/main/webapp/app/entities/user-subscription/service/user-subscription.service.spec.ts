import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import * as dayjs from 'dayjs';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IUserSubscription, UserSubscription } from '../user-subscription.model';

import { UserSubscriptionService } from './user-subscription.service';

describe('UserSubscription Service', () => {
  let service: UserSubscriptionService;
  let httpMock: HttpTestingController;
  let elemDefault: IUserSubscription;
  let expectedResult: IUserSubscription | IUserSubscription[] | boolean | null;
  let currentDate: dayjs.Dayjs;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(UserSubscriptionService);
    httpMock = TestBed.inject(HttpTestingController);
    currentDate = dayjs();

    elemDefault = {
      id: 0,
      cancelled: false,
      createdBy: 'AAAAAAA',
      lastModifiedBy: 'AAAAAAA',
      createdDate: currentDate,
      lastModifiedDate: currentDate,
    };
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = Object.assign(
        {
          createdDate: currentDate.format(DATE_TIME_FORMAT),
          lastModifiedDate: currentDate.format(DATE_TIME_FORMAT),
        },
        elemDefault
      );

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(elemDefault);
    });

    it('should create a UserSubscription', () => {
      const returnedFromService = Object.assign(
        {
          id: 0,
          createdDate: currentDate.format(DATE_TIME_FORMAT),
          lastModifiedDate: currentDate.format(DATE_TIME_FORMAT),
        },
        elemDefault
      );

      const expected = Object.assign(
        {
          createdDate: currentDate,
          lastModifiedDate: currentDate,
        },
        returnedFromService
      );

      service.create(new UserSubscription()).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a UserSubscription', () => {
      const returnedFromService = Object.assign(
        {
          id: 1,
          cancelled: true,
          createdBy: 'BBBBBB',
          lastModifiedBy: 'BBBBBB',
          createdDate: currentDate.format(DATE_TIME_FORMAT),
          lastModifiedDate: currentDate.format(DATE_TIME_FORMAT),
        },
        elemDefault
      );

      const expected = Object.assign(
        {
          createdDate: currentDate,
          lastModifiedDate: currentDate,
        },
        returnedFromService
      );

      service.update(expected).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a UserSubscription', () => {
      const patchObject = Object.assign(
        {
          lastModifiedBy: 'BBBBBB',
          lastModifiedDate: currentDate.format(DATE_TIME_FORMAT),
        },
        new UserSubscription()
      );

      const returnedFromService = Object.assign(patchObject, elemDefault);

      const expected = Object.assign(
        {
          createdDate: currentDate,
          lastModifiedDate: currentDate,
        },
        returnedFromService
      );

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of UserSubscription', () => {
      const returnedFromService = Object.assign(
        {
          id: 1,
          cancelled: true,
          createdBy: 'BBBBBB',
          lastModifiedBy: 'BBBBBB',
          createdDate: currentDate.format(DATE_TIME_FORMAT),
          lastModifiedDate: currentDate.format(DATE_TIME_FORMAT),
        },
        elemDefault
      );

      const expected = Object.assign(
        {
          createdDate: currentDate,
          lastModifiedDate: currentDate,
        },
        returnedFromService
      );

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toContainEqual(expected);
    });

    it('should delete a UserSubscription', () => {
      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult);
    });

    describe('addUserSubscriptionToCollectionIfMissing', () => {
      it('should add a UserSubscription to an empty array', () => {
        const userSubscription: IUserSubscription = { id: 123 };
        expectedResult = service.addUserSubscriptionToCollectionIfMissing([], userSubscription);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(userSubscription);
      });

      it('should not add a UserSubscription to an array that contains it', () => {
        const userSubscription: IUserSubscription = { id: 123 };
        const userSubscriptionCollection: IUserSubscription[] = [
          {
            ...userSubscription,
          },
          { id: 456 },
        ];
        expectedResult = service.addUserSubscriptionToCollectionIfMissing(userSubscriptionCollection, userSubscription);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a UserSubscription to an array that doesn't contain it", () => {
        const userSubscription: IUserSubscription = { id: 123 };
        const userSubscriptionCollection: IUserSubscription[] = [{ id: 456 }];
        expectedResult = service.addUserSubscriptionToCollectionIfMissing(userSubscriptionCollection, userSubscription);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(userSubscription);
      });

      it('should add only unique UserSubscription to an array', () => {
        const userSubscriptionArray: IUserSubscription[] = [{ id: 123 }, { id: 456 }, { id: 4634 }];
        const userSubscriptionCollection: IUserSubscription[] = [{ id: 123 }];
        expectedResult = service.addUserSubscriptionToCollectionIfMissing(userSubscriptionCollection, ...userSubscriptionArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const userSubscription: IUserSubscription = { id: 123 };
        const userSubscription2: IUserSubscription = { id: 456 };
        expectedResult = service.addUserSubscriptionToCollectionIfMissing([], userSubscription, userSubscription2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(userSubscription);
        expect(expectedResult).toContain(userSubscription2);
      });

      it('should accept null and undefined values', () => {
        const userSubscription: IUserSubscription = { id: 123 };
        expectedResult = service.addUserSubscriptionToCollectionIfMissing([], null, userSubscription, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(userSubscription);
      });

      it('should return initial array if no UserSubscription is added', () => {
        const userSubscriptionCollection: IUserSubscription[] = [{ id: 123 }];
        expectedResult = service.addUserSubscriptionToCollectionIfMissing(userSubscriptionCollection, undefined, null);
        expect(expectedResult).toEqual(userSubscriptionCollection);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
