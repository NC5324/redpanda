jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { UserSubscriptionService } from '../service/user-subscription.service';
import { IUserSubscription, UserSubscription } from '../user-subscription.model';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/user.service';

import { UserSubscriptionUpdateComponent } from './user-subscription-update.component';

describe('UserSubscription Management Update Component', () => {
  let comp: UserSubscriptionUpdateComponent;
  let fixture: ComponentFixture<UserSubscriptionUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let userSubscriptionService: UserSubscriptionService;
  let userService: UserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [UserSubscriptionUpdateComponent],
      providers: [FormBuilder, ActivatedRoute],
    })
      .overrideTemplate(UserSubscriptionUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(UserSubscriptionUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    userSubscriptionService = TestBed.inject(UserSubscriptionService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call User query and add missing value', () => {
      const userSubscription: IUserSubscription = { id: 456 };
      const user: IUser = { id: 39531 };
      userSubscription.user = user;

      const userCollection: IUser[] = [{ id: 78143 }];
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [user];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ userSubscription });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(userCollection, ...additionalUsers);
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const userSubscription: IUserSubscription = { id: 456 };
      const user: IUser = { id: 30157 };
      userSubscription.user = user;

      activatedRoute.data = of({ userSubscription });
      comp.ngOnInit();

      expect(comp.editForm.value).toEqual(expect.objectContaining(userSubscription));
      expect(comp.usersSharedCollection).toContain(user);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<UserSubscription>>();
      const userSubscription = { id: 123 };
      jest.spyOn(userSubscriptionService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ userSubscription });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: userSubscription }));
      saveSubject.complete();

      // THEN
      expect(comp.previousState).toHaveBeenCalled();
      expect(userSubscriptionService.update).toHaveBeenCalledWith(userSubscription);
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<UserSubscription>>();
      const userSubscription = new UserSubscription();
      jest.spyOn(userSubscriptionService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ userSubscription });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: userSubscription }));
      saveSubject.complete();

      // THEN
      expect(userSubscriptionService.create).toHaveBeenCalledWith(userSubscription);
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<UserSubscription>>();
      const userSubscription = { id: 123 };
      jest.spyOn(userSubscriptionService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ userSubscription });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(userSubscriptionService.update).toHaveBeenCalledWith(userSubscription);
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Tracking relationships identifiers', () => {
    describe('trackUserById', () => {
      it('Should return tracked User primary key', () => {
        const entity = { id: 123 };
        const trackResult = comp.trackUserById(0, entity);
        expect(trackResult).toEqual(entity.id);
      });
    });
  });
});
