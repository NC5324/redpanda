import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { UserSubscriptionDetailComponent } from './user-subscription-detail.component';

describe('UserSubscription Management Detail Component', () => {
  let comp: UserSubscriptionDetailComponent;
  let fixture: ComponentFixture<UserSubscriptionDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UserSubscriptionDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ userSubscription: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(UserSubscriptionDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(UserSubscriptionDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load userSubscription on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.userSubscription).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
