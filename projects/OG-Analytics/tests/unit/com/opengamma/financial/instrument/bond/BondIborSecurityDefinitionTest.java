/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

public class BondIborSecurityDefinitionTest {
  //Quarterly Libor6m 2Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final int IBOR_SPOT_LAG = 2;
  private static final BusinessDayConvention IBOR_BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IBOR_IS_EOM = false;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SPOT_LAG, CALENDAR, IBOR_DAY_COUNT, IBOR_BUSINESS_DAY, IBOR_IS_EOM);
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 3; // Standard for euro-bonds.
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);

  private static final BondIborSecurityDefinition FRN_DEFINITION = BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);

  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String DSC_CURVE_NAME = "Discounting";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, DSC_CURVE_NAME};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturity() {
    BondIborSecurityDefinition.from(null, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    BondIborSecurityDefinition.from(MATURITY_DATE, null, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, null, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, null, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDay() {
    BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, null, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveNominal() {
    AnnuityCouponIborDefinition coupon = AnnuityCouponIborDefinition.fromAccrualUnadjusted(START_ACCRUAL_DATE, MATURITY_DATE, 1.0, IBOR_INDEX, false);
    AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, -1.0)});
    new BondIborSecurityDefinition(nominal, coupon, 0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveCoupon() {
    AnnuityCouponIborDefinition coupon = AnnuityCouponIborDefinition.fromAccrualUnadjusted(START_ACCRUAL_DATE, MATURITY_DATE, 1.0, IBOR_INDEX, false);
    AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, -1.0)});
    new BondIborSecurityDefinition(nominal, coupon, 0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT);
  }

  @Test
  public void testGetters() {
    //    BondIborSecurityDefinition bond = BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertEquals(SETTLEMENT_DAYS, FRN_DEFINITION.getSettlementDays());
    assertEquals(DAY_COUNT, FRN_DEFINITION.getDayCount());
    assertEquals(0, FRN_DEFINITION.getExCouponDays()); //Default
    AnnuityCouponIborDefinition coupon = AnnuityCouponIborDefinition.fromAccrualUnadjusted(START_ACCRUAL_DATE, MATURITY_DATE, 1.0, IBOR_INDEX, false);
    assertEquals(coupon, FRN_DEFINITION.getCoupon());
    AnnuityDefinition<PaymentFixedDefinition> nominal = new AnnuityDefinition<PaymentFixedDefinition>(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, BUSINESS_DAY.adjustDate(CALENDAR,
        MATURITY_DATE), 1.0)});
    assertEquals(nominal.getCurrency(), FRN_DEFINITION.getNominal().getCurrency());
    assertEquals(nominal.getNthPayment(0).getPaymentDate(), FRN_DEFINITION.getNominal().getNthPayment(0).getPaymentDate());
    assertEquals(nominal.getNthPayment(0).getAmount(), FRN_DEFINITION.getNominal().getNthPayment(0).getAmount());
  }

  @Test
  public void testDatesVsFixed() {
    BondIborSecurityDefinition bondIbor = BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    YieldConvention yield = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition
        .from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, IBOR_TENOR, 0.0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, yield, IS_EOM);
    assertEquals(bondIbor.getNominal(), bondFixed.getNominal());
    for (int loopcpn = 0; loopcpn < bondIbor.getCoupon().getNumberOfPayments(); loopcpn++) {
      assertEquals("Payment " + loopcpn, bondIbor.getCoupon().getNthPayment(loopcpn).getPaymentDate(), bondFixed.getCoupon().getNthPayment(loopcpn).getPaymentDate());
    }
  }

  @Test
  public void toDerivativeSettleBeforeFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 7, 7);
    ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(referenceDate, SETTLEMENT_DAYS, CALENDAR);
    BondIborSecurity frn = FRN_DEFINITION.toDerivative(referenceDate, CURVES_NAME);
    AnnuityPaymentFixed nominal = ((AnnuityPaymentFixedDefinition) FRN_DEFINITION.getNominal()).toDerivative(referenceDate, CURVES_NAME[0]);
    @SuppressWarnings("unchecked")
    GenericAnnuity<Coupon> coupon = (GenericAnnuity<Coupon>) FRN_DEFINITION.getCoupon().toDerivative(referenceDate, CURVES_NAME);
    double settlementTime = TimeCalculator.getTimeBetween(referenceDate, settlementDate);
    BondIborSecurity frnExpected = new BondIborSecurity(nominal, coupon, settlementTime, DSC_CURVE_NAME);
    assertEquals("FRN: toDerivative", frnExpected, frn);
  }

  @Test
  public void toDerivativeSettleMiddleFirstCoupon() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 16);
    ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(referenceDate, SETTLEMENT_DAYS, CALENDAR);
    DoubleTimeSeries<ZonedDateTime> fixingUSDLibor3M = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 7, 11), DateUtils.getUTCDate(2011, 7, 12),
        DateUtils.getUTCDate(2011, 7, 13), DateUtils.getUTCDate(2011, 8, 16)}, new double[] {0.01, 0.05, 0.05, 0.05});
    BondIborSecurity frn = FRN_DEFINITION.toDerivative(referenceDate, fixingUSDLibor3M, CURVES_NAME);
    AnnuityPaymentFixed nominal = ((AnnuityPaymentFixedDefinition) FRN_DEFINITION.getNominal()).toDerivative(referenceDate, CURVES_NAME[0]);
    @SuppressWarnings("unchecked")
    GenericAnnuity<Coupon> coupon = (GenericAnnuity<Coupon>) FRN_DEFINITION.getCoupon().toDerivative(referenceDate, fixingUSDLibor3M, CURVES_NAME);
    double settlementTime = TimeCalculator.getTimeBetween(referenceDate, settlementDate);
    BondIborSecurity frnExpected = new BondIborSecurity(nominal, coupon, settlementTime, DSC_CURVE_NAME);
    assertEquals("FRN: toDerivative", frnExpected, frn);
  }

}
