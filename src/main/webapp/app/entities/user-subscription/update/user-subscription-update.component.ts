import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import * as dayjs from 'dayjs';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';

import { IUserSubscription, UserSubscription } from '../user-subscription.model';
import { UserSubscriptionService } from '../service/user-subscription.service';
import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/user.service';

@Component({
  selector: 'jhi-user-subscription-update',
  templateUrl: './user-subscription-update.component.html',
})
export class UserSubscriptionUpdateComponent implements OnInit {
  isSaving = false;

  usersSharedCollection: IUser[] = [];

  editForm = this.fb.group({
    id: [],
    cancelled: [],
    user: [],
  });

  constructor(
    protected userSubscriptionService: UserSubscriptionService,
    protected userService: UserService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ userSubscription }) => {
      if (userSubscription.id === undefined) {
        const today = dayjs().startOf('day');
        userSubscription.createdDate = today;
        userSubscription.lastModifiedDate = today;
      }

      this.updateForm(userSubscription);

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const userSubscription = this.createFromForm();
    if (userSubscription.id !== undefined) {
      this.subscribeToSaveResponse(this.userSubscriptionService.update(userSubscription));
    } else {
      this.subscribeToSaveResponse(this.userSubscriptionService.create(userSubscription));
    }
  }

  trackUserById(index: number, item: IUser): number {
    return item.id!;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IUserSubscription>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(userSubscription: IUserSubscription): void {
    this.editForm.patchValue({
      id: userSubscription.id,
      cancelled: userSubscription.cancelled,
      user: userSubscription.user,
    });

    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing(this.usersSharedCollection, userSubscription.user);
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing(users, this.editForm.get('user')!.value)))
      .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
  }

  protected createFromForm(): IUserSubscription {
    return {
      ...new UserSubscription(),
      id: this.editForm.get(['id'])!.value,
      cancelled: this.editForm.get(['cancelled'])!.value,
      user: this.editForm.get(['user'])!.value,
    };
  }
}
