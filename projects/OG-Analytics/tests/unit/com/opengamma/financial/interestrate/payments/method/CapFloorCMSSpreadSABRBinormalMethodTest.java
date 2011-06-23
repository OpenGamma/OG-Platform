/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

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
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.financial.model.volatility.NormalImpliedVolatilityFormula;
import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.RealPolynomialFunction1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests the pricing of CMS spread option in binormal with correlation by strike approach.
 */
public class CapFloorCMSSpreadSABRBinormalMethodTest {

  //Swaps
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 17);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final boolean FIXED_IS_PAYER = true; // Irrelevant for the underlying
  private static final double RATE = 0.0; // Irrelevant for the underlying
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  // Swap 10Y
  private static final Period ANNUITY_TENOR_1 = Period.ofYears(10);
  private static final CMSIndex CMS_INDEX_1 = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR_1);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_1 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_1, 1.0, RATE, FIXED_IS_PAYER);
  // Swap 2Y
  private static final Period ANNUITY_TENOR_2 = Period.ofYears(2);
  private static final CMSIndex CMS_INDEX_2 = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR_2);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_2 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_2, 1.0, RATE, FIXED_IS_PAYER);
  // CMS spread coupon
  private static final double NOTIONAL = 100000000;
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double PAYMENT_ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double STRIKE = 0.0010; // 10 bps
  private static final boolean IS_CAP = true;
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final FixedCouponSwap<? extends Payment> SWAP_1 = SWAP_DEFINITION_1.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final FixedCouponSwap<? extends Payment> SWAP_2 = SWAP_DEFINITION_2.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, SWAP_DEFINITION_1.getFixedLeg().getNthPayment(0).getAccrualStartDate());

  private static final CapFloorCMSSpread CMS_SPREAD = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2,
      SETTLEMENT_TIME, STRIKE, IS_CAP, FUNDING_CURVE_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNotNullCorrelation() {
    new CapFloorCMSSpreadSABRBinormalMethod(null);
  }

  @Test
  public void getter() {
    final double correlation = 0.80;
    final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation}); // Constant function
    final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(correlationFunction);
    assertEquals("CMS spread binormal method: correlation function getter", correlationFunction, method.getCorrelation());
  }

  @Test
  /**
   * Tests the present value against the price explicitly computed for constant correlation. 
   */
  public void presentValue() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double correlation = 0.80;
    final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation}); // Constant function
    final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(correlationFunction);
    final double cmsSpreadPrice = method.presentValue(CMS_SPREAD, sabrBundle);
    final double discountFactorPayment = curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(PAYMENT_TIME);
    final CouponCMSSABRReplicationMethod methodCms = CouponCMSSABRReplicationMethod.getDefaultInstance();
    final CapFloorCMSSABRReplicationMethod methodCmsCap = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
    final NormalImpliedVolatilityFormula impliedVolatility = new NormalImpliedVolatilityFormula();
    final NormalPriceFunction normalPrice = new NormalPriceFunction();
    final ParRateCalculator parRate = ParRateCalculator.getInstance();
    final CouponCMS cmsCoupon1 = CouponCMS.from(CMS_SPREAD, SWAP_1, SETTLEMENT_TIME);
    final CouponCMS cmsCoupon2 = CouponCMS.from(CMS_SPREAD, SWAP_2, SETTLEMENT_TIME);
    final double cmsCoupon1Price = methodCms.presentValue(cmsCoupon1, sabrBundle);
    final double cmsCoupon2Price = methodCms.presentValue(cmsCoupon2, sabrBundle);
    final double expectedRate1 = cmsCoupon1Price / discountFactorPayment / cmsCoupon1.getNotional() / cmsCoupon1.getPaymentYearFraction();
    final double expectedRate2 = cmsCoupon2Price / discountFactorPayment / cmsCoupon2.getNotional() / cmsCoupon2.getPaymentYearFraction();
    final double forward1 = parRate.visit(SWAP_1, curves);
    final double forward2 = parRate.visit(SWAP_2, curves);
    final CapFloorCMS cmsCap1 = CapFloorCMS.from(cmsCoupon1, forward1, true);
    final CapFloorCMS cmsCap2 = CapFloorCMS.from(cmsCoupon2, forward2, true);
    final double cmsCap1Price = methodCmsCap.presentValue(cmsCap1, sabrBundle).getAmount();
    final double cmsCap2Price = methodCmsCap.presentValue(cmsCap2, sabrBundle).getAmount();
    final EuropeanVanillaOption optionCap1 = new EuropeanVanillaOption(forward1, FIXING_TIME, true);
    final BlackFunctionData dataCap1 = new BlackFunctionData(expectedRate1, 1.0, 0.0);
    final double cmsCap1IV = impliedVolatility.getImpliedVolatility(dataCap1, optionCap1, cmsCap1Price / discountFactorPayment / cmsCoupon1.getNotional() / cmsCoupon1.getPaymentYearFraction());
    final EuropeanVanillaOption optionCap2 = new EuropeanVanillaOption(forward2, FIXING_TIME, true);
    final BlackFunctionData dataCap2 = new BlackFunctionData(expectedRate2, 1.0, 0.0);
    final double cmsCap2IV = impliedVolatility.getImpliedVolatility(dataCap2, optionCap2, cmsCap2Price / discountFactorPayment / cmsCoupon2.getNotional() / cmsCoupon2.getPaymentYearFraction());
    double spreadVol = cmsCap1IV * cmsCap1IV - 2 * correlation * cmsCap1IV * cmsCap2IV + cmsCap2IV * cmsCap2IV;
    spreadVol = Math.sqrt(spreadVol);
    final EuropeanVanillaOption optionSpread = new EuropeanVanillaOption(STRIKE, FIXING_TIME, IS_CAP);
    final BlackFunctionData dataSpread = new BlackFunctionData(expectedRate1 - expectedRate2, 1.0, spreadVol);
    final Function1D<BlackFunctionData, Double> priceFunction = normalPrice.getPriceFunction(optionSpread);
    final double cmsSpreadPriceExpected = discountFactorPayment * priceFunction.evaluate(dataSpread) * CMS_SPREAD.getNotional() * CMS_SPREAD.getPaymentYearFraction();
    assertEquals("CMS spread: price with constant correlation", cmsSpreadPriceExpected, cmsSpreadPrice, 1.0E-2);
  }

  @Test
  /**
   * Tests the implied correlation computation for a range of correlations.
   */
  public void impliedCorrelation() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double[] correlation = new double[] {-0.50, 0.00, 0.50, 0.75, 0.80, 0.85, 0.90, 0.95, 0.99};
    final int nbCor = correlation.length;
    final double[] impliedCorrelation = new double[nbCor];
    for (int loopcor = 0; loopcor < nbCor; loopcor++) {
      final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation[loopcor]}); // Constant function
      final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(correlationFunction);
      final double cmsSpreadPrice = method.presentValue(CMS_SPREAD, sabrBundle);
      impliedCorrelation[loopcor] = method.impliedCorrelation(CMS_SPREAD, sabrBundle, cmsSpreadPrice);
      assertEquals("CMS spread cap/floor: implied correlation", correlation[loopcor], impliedCorrelation[loopcor], 1.0E-12);
    }
  }

}
