/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests the constructor of annuities of Ibor ratchets.
 */
public class AnnuityCouponIborRatchetDefinitionTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Currency CUR = Currency.EUR;
  //Euribor 3m
  private static final int INDEX_TENOR_MONTH = 3;
  private static final Period INDEX_TENOR = Period.ofMonths(INDEX_TENOR_MONTH);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  //Annuity description
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final int ANNUITY_TENOR_YEAR = 2;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final int NB_COUPON = ANNUITY_TENOR_YEAR * 12 / INDEX_TENOR_MONTH;
  private static final boolean IS_PAYER = false;
  private static final double NOTIONAL = 100000000; // 100m
  private static final double[] MAIN_COEF = new double[] {0.4, 0.5, 0.0010};
  private static final double[] FLOOR_COEF = new double[] {0.75, 0.00, 0.00};
  private static final double[] CAP_COEF = new double[] {1.50, 1.00, 0.0050};
  private static final double FIRST_CPN_RATE = 0.02;

  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAMES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 5);

  @Test
  public void constructorFixed() {
    ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, ANNUITY_TENOR, INDEX_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
    CouponDefinition[] cpn = new CouponDefinition[NB_COUPON];
    cpn[0] = new CouponFixedDefinition(CUR, paymentDates[0], SETTLEMENT_DATE, paymentDates[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, paymentDates[0]), NOTIONAL, FIRST_CPN_RATE);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = new CouponIborRatchetDefinition(CUR, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], DAY_COUNT.getDayCountFraction(paymentDates[loopcpn - 1],
          paymentDates[loopcpn]), NOTIONAL, ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -SETTLEMENT_DAYS, CALENDAR), IBOR_INDEX, MAIN_COEF, FLOOR_COEF, CAP_COEF);
    }
    AnnuityCouponIborRatchetDefinition annuity = new AnnuityCouponIborRatchetDefinition(cpn);
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      assertEquals("Annuity Ratchet Ibor: constructor", cpn[loopcpn], annuity.getNthPayment(loopcpn));
    }
    AnnuityCouponIborRatchetDefinition annuityFixed = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, FIRST_CPN_RATE,
        MAIN_COEF, FLOOR_COEF, CAP_COEF);
    assertEquals("Annuity Ratchet Ibor: constructor", annuity, annuityFixed);
  }

  @Test
  public void constructorIborGearing() {
    ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, ANNUITY_TENOR, INDEX_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
    CouponDefinition[] cpn = new CouponDefinition[NB_COUPON];
    cpn[0] = CouponIborGearingDefinition.from(SETTLEMENT_DATE, paymentDates[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, paymentDates[0]), NOTIONAL, IBOR_INDEX, MAIN_COEF[2], MAIN_COEF[1]);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = new CouponIborRatchetDefinition(CUR, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], DAY_COUNT.getDayCountFraction(paymentDates[loopcpn - 1],
          paymentDates[loopcpn]), NOTIONAL, ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -SETTLEMENT_DAYS, CALENDAR), IBOR_INDEX, MAIN_COEF, FLOOR_COEF, CAP_COEF);
    }
    AnnuityCouponIborRatchetDefinition annuity = new AnnuityCouponIborRatchetDefinition(cpn);
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      assertEquals("Annuity Ratchet Ibor: constructor", cpn[loopcpn], annuity.getNthPayment(loopcpn));
    }
    AnnuityCouponIborRatchetDefinition annuityGearing = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, MAIN_COEF,
        FLOOR_COEF, CAP_COEF);
    assertEquals("Annuity Ratchet Ibor: constructor", annuity, annuityGearing);
  }

  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when no fixing data is provided.
   */
  public void toDerivativesFixedNoFixing() {
    AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, FIRST_CPN_RATE,
        MAIN_COEF, FLOOR_COEF, CAP_COEF);
    @SuppressWarnings("unchecked")
    GenericAnnuity<Payment> annuity = (GenericAnnuity<Payment>) annuityDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    Coupon[] cpn = new Coupon[NB_COUPON];
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, CURVES_NAMES);
    }
    GenericAnnuity<Payment> annuity2 = new GenericAnnuity<Payment>(cpn);
    assertTrue("Annuity Ratchet Ibor: toDerivatives", annuity2.equals(annuity));
  }

  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when no fixing data is provided.
   */
  public void toDerivativesIborNoFixing() {
    AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, MAIN_COEF,
        FLOOR_COEF, CAP_COEF);
    @SuppressWarnings("unchecked")
    GenericAnnuity<Payment> annuity = (GenericAnnuity<Payment>) annuityDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    Coupon[] cpn = new Coupon[NB_COUPON];
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, CURVES_NAMES);
    }
    GenericAnnuity<Payment> annuity2 = new GenericAnnuity<Payment>(cpn);
    assertTrue("Annuity Ratchet Ibor: toDerivatives", annuity2.equals(annuity));
  }

  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when fixing data is provided but not used.
   */
  public void toDerivativesFixingNotUsed() {
    AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, FIRST_CPN_RATE,
        MAIN_COEF, FLOOR_COEF, CAP_COEF);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {REFERENCE_DATE}, new double[] {0.0});
    AnnuityCouponIborRatchet annuity = annuityDefinition.toDerivative(REFERENCE_DATE, fixingTS, CURVES_NAMES);
    Coupon[] cpn = new Coupon[NB_COUPON];
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, CURVES_NAMES);
    }
    AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    assertEquals("Annuity Ratchet Ibor: toDerivatives", annuity, annuity2);
  }

  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when fixing data is provided but not used.
   */
  public void toDerivativesIborNotFixed() {
    AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, MAIN_COEF,
        FLOOR_COEF, CAP_COEF);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {REFERENCE_DATE.minusDays(1)}, new double[] {0.02});
    AnnuityCouponIborRatchet annuity = annuityDefinition.toDerivative(REFERENCE_DATE, fixingTS, CURVES_NAMES);
    Coupon[] cpn = new Coupon[NB_COUPON];
    cpn[0] = ((CouponIborGearingDefinition) annuityDefinition.getNthPayment(0)).toDerivative(REFERENCE_DATE, fixingTS, CURVES_NAMES);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, CURVES_NAMES);
    }
    AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      assertEquals("Annuity Ratchet Ibor: toDerivatives " + loopcpn, annuity.getNthPayment(loopcpn), annuity2.getNthPayment(loopcpn));
    }
  }

  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when fixing data is used for Ibor.
   */
  public void toDerivativesIborFixed() {
    AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, MAIN_COEF,
        FLOOR_COEF, CAP_COEF);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {REFERENCE_DATE}, new double[] {0.02});
    AnnuityCouponIborRatchet annuity = annuityDefinition.toDerivative(REFERENCE_DATE, fixingTS, CURVES_NAMES);
    Coupon[] cpn = new Coupon[NB_COUPON];
    cpn[0] = ((CouponIborGearingDefinition) annuityDefinition.getNthPayment(0)).toDerivative(REFERENCE_DATE, fixingTS, CURVES_NAMES);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, CURVES_NAMES);
    }
    AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      assertEquals("Annuity Ratchet Ibor: toDerivatives " + loopcpn, annuity.getNthPayment(loopcpn), annuity2.getNthPayment(loopcpn));
    }
  }

  @Test
  public void toDerivativesOneFixing() {
    /**
     * Tests the toDerivatives for Ibor Ratchet when fixing data is provided and used for one coupon.
     */
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 9);
    AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, FIRST_CPN_RATE,
        MAIN_COEF, FLOOR_COEF, CAP_COEF);
    double fixing = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 12, 5)}, new double[] {fixing});
    GenericAnnuity<Coupon> annuity = annuityDefinition.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
    Coupon[] cpn = new Coupon[NB_COUPON - 1];
    double rate = MAIN_COEF[0] * FIRST_CPN_RATE + MAIN_COEF[1] * fixing + MAIN_COEF[2];
    rate = Math.max(rate, FLOOR_COEF[0] * FIRST_CPN_RATE + FLOOR_COEF[1] * fixing + FLOOR_COEF[2]);
    rate = Math.min(rate, CAP_COEF[0] * FIRST_CPN_RATE + CAP_COEF[1] * fixing + CAP_COEF[2]);
    cpn[0] = new CouponFixed(CUR, TimeCalculator.getTimeBetween(referenceDate, annuityDefinition.getNthPayment(1).getPaymentDate()), DISCOUNTING_CURVE_NAME, annuityDefinition.getNthPayment(1)
        .getPaymentYearFraction(), NOTIONAL, rate, annuityDefinition.getNthPayment(1).getAccrualStartDate(), annuityDefinition.getNthPayment(1).getAccrualEndDate());
    for (int loopcpn = 1; loopcpn < NB_COUPON - 1; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn + 1).toDerivative(referenceDate, CURVES_NAMES);
    }
    AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    assertEquals("Annuity Ratchet Ibor: toDerivatives", annuity, annuity2);
  }

  //TODO: two fixing

}
