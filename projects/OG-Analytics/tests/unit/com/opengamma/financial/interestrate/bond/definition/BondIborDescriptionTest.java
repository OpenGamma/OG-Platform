package com.opengamma.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.financial.instrument.bond.BondIborDescriptionDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

public class BondIborDescriptionTest {

  //Quarterly Libor6m 2Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final int IBOR_SPOT_LAG = 2;
  private static final BusinessDayConvention IBOR_BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IBOR_IS_EOM = false;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SPOT_LAG, CALENDAR, IBOR_DAY_COUNT, IBOR_BUSINESS_DAY, IBOR_IS_EOM);
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 3; // Standard for euro-bonds.
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtil.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final BondIborDescriptionDefinition BOND_DESCRIPTION_DEFINITION = BondIborDescriptionDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT,
      BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition COUPON_DEFINITION = AnnuityCouponIborDefinition.fromAccrualUnadjusted(START_ACCRUAL_DATE, MATURITY_DATE, 1.0, IBOR_INDEX, false);
  private static final AnnuityPaymentFixedDefinition NOMINAL_DEFINITION = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, BUSINESS_DAY.adjustDate(
      CALENDAR, MATURITY_DATE), 1.0)});
  // to derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2011, 8, 18);
  private static final double FIRST_FIXING = 0.02;
  static {
    BOND_DESCRIPTION_DEFINITION.getCoupon().getNthPayment(0).fixingProcess(FIRST_FIXING); // First coupon has fixed.
    COUPON_DEFINITION.getNthPayment(0).fixingProcess(FIRST_FIXING);
  }
  private static final ZonedDateTime REFERENCE_DATE_2 = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC), BUSINESS_DAY, CALENDAR, SETTLEMENT_DAYS);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final double SPOT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_2, SPOT_DATE);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};

  private static final AnnuityPaymentFixed NOMINAL = NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  @SuppressWarnings("unchecked")
  private static final GenericAnnuity<Payment> COUPON = (GenericAnnuity<Payment>) COUPON_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final BondIborDescription BOND_DESCRIPTION = new BondIborDescription(NOMINAL, COUPON, SPOT_TIME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominal() {
    new BondIborDescription(null, COUPON, SPOT_TIME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    new BondIborDescription(NOMINAL, null, SPOT_TIME);
  }

  @Test
  public void testGetters() {
    assertEquals(NOMINAL, BOND_DESCRIPTION.getNominal());
    assertEquals(COUPON, BOND_DESCRIPTION.getCoupon());
  }
}
