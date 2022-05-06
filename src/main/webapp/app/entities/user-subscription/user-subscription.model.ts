import * as dayjs from 'dayjs';
import { IUser } from 'app/entities/user/user.model';

export interface IUserSubscription {
  id?: number;
  cancelled?: boolean | null;
  createdBy?: string;
  lastModifiedBy?: string;
  createdDate?: dayjs.Dayjs | null;
  lastModifiedDate?: dayjs.Dayjs | null;
  user?: IUser | null;
}

export class UserSubscription implements IUserSubscription {
  constructor(
    public id?: number,
    public cancelled?: boolean | null,
    public createdBy?: string,
    public lastModifiedBy?: string,
    public createdDate?: dayjs.Dayjs | null,
    public lastModifiedDate?: dayjs.Dayjs | null,
    public user?: IUser | null
  ) {
    this.cancelled = this.cancelled ?? false;
  }
}

export function getUserSubscriptionIdentifier(userSubscription: IUserSubscription): number | undefined {
  return userSubscription.id;
}
