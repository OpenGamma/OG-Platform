/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
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
 * Tests the constructor of annuities of Ibor ratchets.
 */
@Test(groups = TestGroup.UNIT)
public class AnnuityCouponIborRatchetDefinitionTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Currency CUR = Currency.EUR;
  //Euribor 3m
  private static final int INDEX_TENOR_MONTH = 3;
  private static final Period INDEX_TENOR = Period.ofMonths(INDEX_TENOR_MONTH);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  //Annuity description
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final int ANNUITY_TENOR_YEAR = 2;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final int NB_COUPON = ANNUITY_TENOR_YEAR * 12 / INDEX_TENOR_MONTH;
  private static final boolean IS_PAYER = false;
  private static final double NOTIONAL = 100000000; // 100m
  private static final double[] MAIN_COEF = new double[] {0.4, 0.5, 0.0010 };
  private static final double[] FLOOR_COEF = new double[] {0.75, 0.00, 0.00 };
  private static final double[] CAP_COEF = new double[] {1.50, 1.00, 0.0050 };
  private static final double FIRST_CPN_RATE = 0.02;

  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAMES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 5);

  @Test
  public void constructorFixed() {
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, ANNUITY_TENOR, INDEX_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
    final CouponDefinition[] cpn = new CouponDefinition[NB_COUPON];
    cpn[0] = new CouponFixedDefinition(CUR, paymentDates[0], SETTLEMENT_DATE, paymentDates[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, paymentDates[0]), NOTIONAL, FIRST_CPN_RATE);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = new CouponIborRatchetDefinition(CUR, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], DAY_COUNT.getDayCountFraction(paymentDates[loopcpn - 1],
          paymentDates[loopcpn]), NOTIONAL, ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -SETTLEMENT_DAYS, CALENDAR), IBOR_INDEX, MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    }
    final AnnuityCouponIborRatchetDefinition annuity = new AnnuityCouponIborRatchetDefinition(cpn, CALENDAR);
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      assertEquals("Annuity Ratchet Ibor: constructor", cpn[loopcpn], annuity.getNthPayment(loopcpn));
    }
    final AnnuityCouponIborRatchetDefinition annuityFixed = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, FIRST_CPN_RATE,
        MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    assertEquals("Annuity Ratchet Ibor: constructor", annuity, annuityFixed);
  }

  @Test
  public void constructorIborGearing() {
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, ANNUITY_TENOR, INDEX_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
    final CouponDefinition[] cpn = new CouponDefinition[NB_COUPON];
    cpn[0] = CouponIborGearingDefinition.from(SETTLEMENT_DATE, paymentDates[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, paymentDates[0]), NOTIONAL, IBOR_INDEX, MAIN_COEF[2], MAIN_COEF[1],
        CALENDAR);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = new CouponIborRatchetDefinition(CUR, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], DAY_COUNT.getDayCountFraction(paymentDates[loopcpn - 1],
          paymentDates[loopcpn]), NOTIONAL, ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -SETTLEMENT_DAYS, CALENDAR), IBOR_INDEX, MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    }
    final AnnuityCouponIborRatchetDefinition annuity = new AnnuityCouponIborRatchetDefinition(cpn, CALENDAR);
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      assertEquals("Annuity Ratchet Ibor: constructor", cpn[loopcpn], annuity.getNthPayment(loopcpn));
    }
    final AnnuityCouponIborRatchetDefinition annuityGearing = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, MAIN_COEF,
        FLOOR_COEF, CAP_COEF, CALENDAR);
    assertEquals("Annuity Ratchet Ibor: constructor", annuity, annuityGearing);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when no fixing data is provided.
   */
  public void toDerivativesFixedNoFixingDeprecated() {
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    @SuppressWarnings("unchecked")
    final Annuity<Payment> annuity = (Annuity<Payment>) annuityDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    final Coupon[] cpn = new Coupon[NB_COUPON];
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, CURVES_NAMES);
    }
    final Annuity<Payment> annuity2 = new Annuity<Payment>(cpn);
    assertTrue("Annuity Ratchet Ibor: toDerivatives", annuity2.equals(annuity));
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when no fixing data is provided.
   */
  public void toDerivativesIborNoFixingDeprecated() {
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    @SuppressWarnings("unchecked")
    final Annuity<Payment> annuity = ((Annuity<Payment>) annuityDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES));
    final Coupon[] cpn = new Coupon[NB_COUPON];
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, CURVES_NAMES);
    }
    final Annuity<Payment> annuity2 = new Annuity<Payment>(cpn);
    assertTrue("Annuity Ratchet Ibor: toDerivatives", annuity2.equals(annuity));
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when fixing data is provided but not used.
   */
  public void toDerivativesFixingNotUsedDeprecated() {
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(REFERENCE_DATE, 0.0);
    final AnnuityCouponIborRatchet annuity = annuityDefinition.toDerivative(REFERENCE_DATE, fixingTS, CURVES_NAMES);
    final Coupon[] cpn = new Coupon[NB_COUPON];
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, CURVES_NAMES);
    }
    final AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    assertEquals("Annuity Ratchet Ibor: toDerivatives", annuity, annuity2);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when fixing data is provided but not used.
   */
  public void toDerivativesIborNotFixedDeprecated() {
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(REFERENCE_DATE.minusDays(1), 0.02);
    final AnnuityCouponIborRatchet annuity = annuityDefinition.toDerivative(REFERENCE_DATE, fixingTS, CURVES_NAMES);
    final Coupon[] cpn = new Coupon[NB_COUPON];
    cpn[0] = ((CouponIborGearingDefinition) annuityDefinition.getNthPayment(0)).toDerivative(REFERENCE_DATE, fixingTS, CURVES_NAMES);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, CURVES_NAMES);
    }
    final AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      assertEquals("Annuity Ratchet Ibor: toDerivatives " + loopcpn, annuity.getNthPayment(loopcpn), annuity2.getNthPayment(loopcpn));
    }
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when fixing data is used for Ibor.
   */
  public void toDerivativesIborFixedDeprecated() {
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(REFERENCE_DATE, 0.02);
    final AnnuityCouponIborRatchet annuity = annuityDefinition.toDerivative(REFERENCE_DATE, fixingTS, CURVES_NAMES);
    final Coupon[] cpn = new Coupon[NB_COUPON];
    cpn[0] = ((CouponIborGearingDefinition) annuityDefinition.getNthPayment(0)).toDerivative(REFERENCE_DATE, fixingTS, CURVES_NAMES);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, CURVES_NAMES);
    }
    final AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      assertEquals("Annuity Ratchet Ibor: toDerivatives " + loopcpn, annuity.getNthPayment(loopcpn), annuity2.getNthPayment(loopcpn));
    }
  }

  @SuppressWarnings("deprecation")
  @Test
  public void toDerivativesOneFixingDeprecated() {
    /**
     * Tests the toDerivatives for Ibor Ratchet when fixing data is provided and used for one coupon.
     */
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 9);
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    final double fixing = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(DateUtils.getUTCDate(2011, 12, 5), fixing);
    final Annuity<Coupon> annuity = annuityDefinition.toDerivative(referenceDate, fixingTS, CURVES_NAMES);
    final Coupon[] cpn = new Coupon[NB_COUPON - 1];
    double rate = MAIN_COEF[0] * FIRST_CPN_RATE + MAIN_COEF[1] * fixing + MAIN_COEF[2];
    rate = Math.max(rate, FLOOR_COEF[0] * FIRST_CPN_RATE + FLOOR_COEF[1] * fixing + FLOOR_COEF[2]);
    rate = Math.min(rate, CAP_COEF[0] * FIRST_CPN_RATE + CAP_COEF[1] * fixing + CAP_COEF[2]);
    cpn[0] = new CouponFixed(CUR, TimeCalculator.getTimeBetween(referenceDate, annuityDefinition.getNthPayment(1).getPaymentDate()), DISCOUNTING_CURVE_NAME, annuityDefinition.getNthPayment(1)
        .getPaymentYearFraction(), NOTIONAL, rate, annuityDefinition.getNthPayment(1).getAccrualStartDate(), annuityDefinition.getNthPayment(1).getAccrualEndDate());
    for (int loopcpn = 1; loopcpn < NB_COUPON - 1; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn + 1).toDerivative(referenceDate, CURVES_NAMES);
    }
    final AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    assertEquals("Annuity Ratchet Ibor: toDerivatives", annuity, annuity2);
  }

  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when no fixing data is provided.
   */
  public void toDerivativesFixedNoFixing() {
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    @SuppressWarnings("unchecked")
    final Annuity<Payment> annuity = (Annuity<Payment>) annuityDefinition.toDerivative(REFERENCE_DATE);
    final Coupon[] cpn = new Coupon[NB_COUPON];
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE);
    }
    final Annuity<Payment> annuity2 = new Annuity<Payment>(cpn);
    assertTrue("Annuity Ratchet Ibor: toDerivatives", annuity2.equals(annuity));
  }

  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when no fixing data is provided.
   */
  public void toDerivativesIborNoFixing() {
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    @SuppressWarnings("unchecked")
    final Annuity<Payment> annuity = (Annuity<Payment>) annuityDefinition.toDerivative(REFERENCE_DATE);
    final Coupon[] cpn = new Coupon[NB_COUPON];
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE);
    }
    final Annuity<Payment> annuity2 = new Annuity<Payment>(cpn);
    assertTrue("Annuity Ratchet Ibor: toDerivatives", annuity2.equals(annuity));
  }

  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when fixing data is provided but not used.
   */
  public void toDerivativesFixingNotUsed() {
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(REFERENCE_DATE, 0.0);
    final AnnuityCouponIborRatchet annuity = annuityDefinition.toDerivative(REFERENCE_DATE, fixingTS);
    final Coupon[] cpn = new Coupon[NB_COUPON];
    for (int loopcpn = 0; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE);
    }
    final AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    assertEquals("Annuity Ratchet Ibor: toDerivatives", annuity, annuity2);
  }

  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when fixing data is provided but not used.
   */
  public void toDerivativesIborNotFixed() {
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(REFERENCE_DATE.minusDays(1), 0.02);
    final AnnuityCouponIborRatchet annuity = annuityDefinition.toDerivative(REFERENCE_DATE, fixingTS);
    final Coupon[] cpn = new Coupon[NB_COUPON];
    cpn[0] = ((CouponIborGearingDefinition) annuityDefinition.getNthPayment(0)).toDerivative(REFERENCE_DATE, fixingTS);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE);
    }
    final AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      assertEquals("Annuity Ratchet Ibor: toDerivatives " + loopcpn, annuity.getNthPayment(loopcpn), annuity2.getNthPayment(loopcpn));
    }
  }

  @Test
  /**
   * Tests the toDerivatives for Ibor Ratchet when fixing data is used for Ibor.
   */
  public void toDerivativesIborFixed() {
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(REFERENCE_DATE, 0.02);
    final AnnuityCouponIborRatchet annuity = annuityDefinition.toDerivative(REFERENCE_DATE, fixingTS);
    final Coupon[] cpn = new Coupon[NB_COUPON];
    cpn[0] = ((CouponIborGearingDefinition) annuityDefinition.getNthPayment(0)).toDerivative(REFERENCE_DATE, fixingTS);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE);
    }
    final AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    for (int loopcpn = 1; loopcpn < NB_COUPON; loopcpn++) {
      assertEquals("Annuity Ratchet Ibor: toDerivatives " + loopcpn, annuity.getNthPayment(loopcpn), annuity2.getNthPayment(loopcpn));
    }
  }

  /**
   * Tests the toDerivatives for Ibor Ratchet when fixing data is provided and used for one coupon.
   */
  @Test
  public void toDerivativesOneFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 9);
    final AnnuityCouponIborRatchetDefinition annuityDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER,
        FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF, CALENDAR);
    final double fixing = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(DateUtils.getUTCDate(2011, 12, 5), fixing);
    final Annuity<Coupon> annuity = annuityDefinition.toDerivative(referenceDate, fixingTS);
    final Coupon[] cpn = new Coupon[NB_COUPON - 1];
    double rate = MAIN_COEF[0] * FIRST_CPN_RATE + MAIN_COEF[1] * fixing + MAIN_COEF[2];
    rate = Math.max(rate, FLOOR_COEF[0] * FIRST_CPN_RATE + FLOOR_COEF[1] * fixing + FLOOR_COEF[2]);
    rate = Math.min(rate, CAP_COEF[0] * FIRST_CPN_RATE + CAP_COEF[1] * fixing + CAP_COEF[2]);
    cpn[0] = new CouponFixed(CUR, TimeCalculator.getTimeBetween(referenceDate, annuityDefinition.getNthPayment(1).getPaymentDate()), annuityDefinition.getNthPayment(1)
        .getPaymentYearFraction(), NOTIONAL, rate, annuityDefinition.getNthPayment(1).getAccrualStartDate(), annuityDefinition.getNthPayment(1).getAccrualEndDate());
    for (int loopcpn = 1; loopcpn < NB_COUPON - 1; loopcpn++) {
      cpn[loopcpn] = (Coupon) annuityDefinition.getNthPayment(loopcpn + 1).toDerivative(referenceDate);
    }
    final AnnuityCouponIborRatchet annuity2 = new AnnuityCouponIborRatchet(cpn);
    assertEquals("Annuity Ratchet Ibor: toDerivatives", annuity, annuity2);
  }

  //TODO: two fixing

}
