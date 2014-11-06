/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondIborSecurityDefinitionTest {
  //Quarterly Libor6m 2Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String ISSUER_NAME = "Issuer";
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ISDA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final int IBOR_SPOT_LAG = 2;
  private static final BusinessDayConvention IBOR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IBOR_IS_EOM = false;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SPOT_LAG, IBOR_DAY_COUNT, IBOR_BUSINESS_DAY, IBOR_IS_EOM, "Ibor");
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 3; // Standard for euro-bonds.
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);

  private static final BondIborSecurityDefinition FRN_DEFINITION = BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS,
      DAY_COUNT, BUSINESS_DAY, IS_EOM, ISSUER_NAME, CALENDAR);

  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String DSC_CURVE_NAME = "Discounting";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, DSC_CURVE_NAME };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturity() {
    BondIborSecurityDefinition.from(null, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, ISSUER_NAME, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    BondIborSecurityDefinition.from(MATURITY_DATE, null, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, ISSUER_NAME, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, null, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, ISSUER_NAME, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, null, BUSINESS_DAY, IS_EOM, ISSUER_NAME, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDay() {
    BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, null, IS_EOM, ISSUER_NAME, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveNominal() {
    final AnnuityCouponIborDefinition coupon = AnnuityCouponIborDefinition.fromAccrualUnadjusted(START_ACCRUAL_DATE, MATURITY_DATE, 1.0, IBOR_INDEX, false, CALENDAR);
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, -1.0) }, CALENDAR);
    new BondIborSecurityDefinition(nominal, coupon, 0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, ISSUER_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveCoupon() {
    final AnnuityCouponIborDefinition coupon = AnnuityCouponIborDefinition.fromAccrualUnadjusted(START_ACCRUAL_DATE, MATURITY_DATE, 1.0, IBOR_INDEX, false, CALENDAR);
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, -1.0) }, CALENDAR);
    new BondIborSecurityDefinition(nominal, coupon, 0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, ISSUER_NAME);
  }

  @Test
  public void testGetters() {
    //    BondIborSecurityDefinition bond = BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertEquals(SETTLEMENT_DAYS, FRN_DEFINITION.getSettlementDays());
    assertEquals(DAY_COUNT, FRN_DEFINITION.getDayCount());
    assertEquals(0, FRN_DEFINITION.getExCouponDays()); //Default
    final AnnuityCouponIborDefinition coupon = AnnuityCouponIborDefinition.fromAccrualUnadjusted(START_ACCRUAL_DATE, MATURITY_DATE, 1.0, IBOR_INDEX, false, CALENDAR);
    assertEquals(coupon, FRN_DEFINITION.getCoupons());
    final AnnuityDefinition<PaymentFixedDefinition> nominal = new AnnuityDefinition<>(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR,
        BUSINESS_DAY.adjustDate(CALENDAR, MATURITY_DATE), 1.0) }, CALENDAR);
    assertEquals(nominal.getCurrency(), FRN_DEFINITION.getNominal().getCurrency());
    assertEquals(nominal.getNthPayment(0).getPaymentDate(), FRN_DEFINITION.getNominal().getNthPayment(0).getPaymentDate());
    assertEquals(nominal.getNthPayment(0).getReferenceAmount(), FRN_DEFINITION.getNominal().getNthPayment(0).getReferenceAmount());
  }

  @Test
  public void testDatesVsFixed() {
    final BondIborSecurityDefinition bondIbor = BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY,
        IS_EOM, ISSUER_NAME, CALENDAR);
    final YieldConvention yield = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
    final BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, IBOR_TENOR, 0.0, SETTLEMENT_DAYS, CALENDAR,
        DAY_COUNT, BUSINESS_DAY, yield, IS_EOM, ISSUER_NAME);
    assertEquals(bondIbor.getNominal(), bondFixed.getNominal());
    for (int loopcpn = 0; loopcpn < bondIbor.getCoupons().getNumberOfPayments(); loopcpn++) {
      assertEquals("Payment " + loopcpn, bondIbor.getCoupons().getNthPayment(loopcpn).getPaymentDate(), bondFixed.getCoupons().getNthPayment(loopcpn).getPaymentDate());
    }
  }

  @Test
  public void toDerivativeSettleBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 7, 7);
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(referenceDate, SETTLEMENT_DAYS, CALENDAR);
    final BondIborSecurity frn = FRN_DEFINITION.toDerivative(referenceDate);
    final AnnuityPaymentFixed nominal = ((AnnuityPaymentFixedDefinition) FRN_DEFINITION.getNominal()).toDerivative(referenceDate);
    final Annuity<Coupon> coupon = (Annuity<Coupon>) FRN_DEFINITION.getCoupons().toDerivative(referenceDate);
    final double settlementTime = TimeCalculator.getTimeBetween(referenceDate, settlementDate);
    final BondIborSecurity frnExpected = new BondIborSecurity(nominal, coupon, settlementTime);
    assertEquals("FRN: toDerivative", frnExpected, frn);
  }

  @Test
  public void toDerivativeSettleMiddleFirstCoupon() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 16);
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(referenceDate, SETTLEMENT_DAYS, CALENDAR);
    final DoubleTimeSeries<ZonedDateTime> fixingUSDLibor3M = ImmutableZonedDateTimeDoubleTimeSeries.of(
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 7, 11),
            DateUtils.getUTCDate(2011, 7, 12), DateUtils.getUTCDate(2011, 7, 13), DateUtils.getUTCDate(2011, 8, 16) },
            new double[] {0.01, 0.05, 0.05, 0.05 }, ZoneOffset.UTC);
    final BondIborSecurity frn = FRN_DEFINITION.toDerivative(referenceDate, fixingUSDLibor3M);
    final AnnuityPaymentFixed nominal = ((AnnuityPaymentFixedDefinition) FRN_DEFINITION.getNominal()).toDerivative(referenceDate);
    final Annuity<Coupon> coupon = (Annuity<Coupon>) FRN_DEFINITION.getCoupons().toDerivative(referenceDate, fixingUSDLibor3M);
    final double settlementTime = TimeCalculator.getTimeBetween(referenceDate, settlementDate);
    final BondIborSecurity frnExpected = new BondIborSecurity(nominal, coupon, settlementTime);
    assertEquals("FRN: toDerivative", frnExpected, frn);
  }
}
