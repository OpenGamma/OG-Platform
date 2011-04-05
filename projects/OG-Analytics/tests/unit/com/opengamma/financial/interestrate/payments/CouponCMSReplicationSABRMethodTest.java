package com.opengamma.financial.interestrate.payments;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

public class CouponCMSReplicationSABRMethodTest {
  //Swap 5Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2014, 3, 17);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, !FIXED_IS_PAYER);
  // CMS coupon construction
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2014, 6, 17); // Prefixed
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
  private static final ZonedDateTime ACCRUAL_START_DATE = SETTLEMENT_DATE;
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponCMSDefinition CMS_COUPON_RECEIVER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  private static final CouponCMSDefinition CMS_COUPON_PAYER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  // to derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final CouponCMS CMS_COUPON_RECEIVER = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponCMS CMS_COUPON_PAYER = (CouponCMS) CMS_COUPON_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculators
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  @Test
  public void testPriceReplicationPayerReceiver() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double priceReceiver = PVC.visit(CMS_COUPON_RECEIVER, sabrBundle);
    double pricePayer = PVC.visit(CMS_COUPON_PAYER, sabrBundle);
    assertEquals("Payer/receiver", priceReceiver, -pricePayer);
  }

  @Test
  public void testPresentValueSABRSensitivitySABRParameters() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    CouponCMSReplicationSABRMethod method = new CouponCMSReplicationSABRMethod();
    // Swaption sensitivity
    PresentValueSABRSensitivity pvsReceiver = method.presentValueSABRSensitivity(CMS_COUPON_RECEIVER, sabrBundle);
    PresentValueSABRSensitivity pvsPayer = method.presentValueSABRSensitivity(CMS_COUPON_PAYER, sabrBundle);
    // Long/short parity
    pvsPayer.multiply(-1.0);
    assertEquals(pvsPayer.getAlpha(), pvsReceiver.getAlpha());
    // SABR sensitivity vs finite difference
    double pvLongPayer = method.presentValue(CMS_COUPON_RECEIVER, sabrBundle);
    double shift = 0.0001;
    double shiftAlpha = 0.00001;
    DoublesPair expectedExpiryTenor = new DoublesPair(CMS_COUPON_RECEIVER.getFixingTime(), ANNUITY_TENOR_YEAR + 1.0 / 365.0);
    // Alpha sensitivity vs finite difference computation
    SABRInterestRateParameter sabrParameterAlphaBumped = TestsDataSets.createSABR1AlphaBumped(shiftAlpha);
    SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, curves);
    double pvLongPayerAlphaBumped = method.presentValue(CMS_COUPON_RECEIVER, sabrBundleAlphaBumped);
    double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsReceiver.getAlpha().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsReceiver.getAlpha().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsReceiver.getAlpha().get(expectedExpiryTenor), 150.0);
    // Rho sensitivity vs finite difference computation
    SABRInterestRateParameter sabrParameterRhoBumped = TestsDataSets.createSABR1RhoBumped();
    SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, curves);
    double pvLongPayerRhoBumped = method.presentValue(CMS_COUPON_RECEIVER, sabrBundleRhoBumped);
    double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsReceiver.getRho().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsReceiver.getRho().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsReceiver.getRho().get(expectedExpiryTenor), expectedRhoSensi, 2.0);
    // Alpha sensitivity vs finite difference computation
    SABRInterestRateParameter sabrParameterNuBumped = TestsDataSets.createSABR1NuBumped();
    SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, curves);
    double pvLongPayerNuBumped = method.presentValue(CMS_COUPON_RECEIVER, sabrBundleNuBumped);
    double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsReceiver.getNu().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsReceiver.getNu().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsReceiver.getNu().get(expectedExpiryTenor), expectedNuSensi, 15.0);
  }

  @Test(enabled = false)
  public void testPerformance() {
    // Used only to assess performance
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    CouponCMSReplicationSABRMethod replication = new CouponCMSReplicationSABRMethod();
    long startTime, endTime;
    int nbTest = 50;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      replication.presentValue(CMS_COUPON_RECEIVER, sabrBundle);
      replication.presentValueSensitivity(CMS_COUPON_RECEIVER, sabrBundle);
      replication.presentValueSABRSensitivity(CMS_COUPON_RECEIVER, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS swap by replication (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta: 04-Apr-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 175 ms for 50 coupon 5Y.
    // Performance note: price+delta+vega: 04-Apr-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 550 ms for 50 coupon 5Y.
  }
}
