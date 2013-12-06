/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondIborSecurityTest {

  //Quarterly Libor6m 2Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final int IBOR_SPOT_LAG = 2;
  private static final BusinessDayConvention IBOR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IBOR_IS_EOM = false;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SPOT_LAG, IBOR_DAY_COUNT, IBOR_BUSINESS_DAY, IBOR_IS_EOM, "Ibor");
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 3; // Standard for euro-bonds.
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final AnnuityCouponIborDefinition COUPON_DEFINITION = AnnuityCouponIborDefinition.fromAccrualUnadjusted(START_ACCRUAL_DATE, MATURITY_DATE, 1.0, IBOR_INDEX, false, CALENDAR);
  private static final AnnuityPaymentFixedDefinition NOMINAL_DEFINITION = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, BUSINESS_DAY.adjustDate(
      CALENDAR, MATURITY_DATE), 1.0)}, CALENDAR);
  // to derivatives
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final ZonedDateTime STANDARD_SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double STANDARD_SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, STANDARD_SETTLEMENT_DATE);
  private static final double FIRST_FIXING = 0.02;

  private static final AnnuityPaymentFixed NOMINAL = NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS;
  private static final Annuity<Coupon> COUPON;
  private static final BondIborSecurity BOND_DESCRIPTION;

  static {
    final List<ZonedDateTime> fixingDates = new ArrayList<>();
    final List<Double> fixingRates = new ArrayList<>();
    for (int i = 0; i < COUPON_DEFINITION.getNumberOfPayments(); i++) {
      if (COUPON_DEFINITION.getNthPayment(i).getFixingDate().isBefore(REFERENCE_DATE)) {
        fixingDates.add(COUPON_DEFINITION.getNthPayment(i).getFixingDate());
        fixingRates.add(FIRST_FIXING);
      }
    }
    FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(fixingDates, fixingRates);
    COUPON = (Annuity<Coupon>) COUPON_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS);
    BOND_DESCRIPTION = new BondIborSecurity(NOMINAL, COUPON, STANDARD_SETTLEMENT_TIME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominal() {
    new BondIborSecurity(null, COUPON, STANDARD_SETTLEMENT_TIME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    new BondIborSecurity(NOMINAL, null, STANDARD_SETTLEMENT_TIME);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetDiscounting() {
    BOND_DESCRIPTION.getDiscountingCurveName();
  }

  @Test
  public void testGetters() {
    assertEquals(NOMINAL, BOND_DESCRIPTION.getNominal());
    assertEquals(COUPON, BOND_DESCRIPTION.getCoupon());
  }

  @Test
  public void testHashCodeEquals() {
    final BondIborSecurity bond = new BondIborSecurity(NOMINAL, COUPON, STANDARD_SETTLEMENT_TIME);
    BondIborSecurity other = new BondIborSecurity(NOMINAL, COUPON, STANDARD_SETTLEMENT_TIME);
    assertEquals(bond, other);
    assertEquals(bond.hashCode(), other.hashCode());
    other = new BondIborSecurity(NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE.minusDays(1)), COUPON, STANDARD_SETTLEMENT_TIME);
    assertFalse(other.equals(bond));
    other = new BondIborSecurity(NOMINAL, (Annuity<Coupon>) COUPON_DEFINITION.toDerivative(REFERENCE_DATE.minusDays(1), FIXING_TS), STANDARD_SETTLEMENT_TIME);
    assertFalse(other.equals(bond));
  }
}
