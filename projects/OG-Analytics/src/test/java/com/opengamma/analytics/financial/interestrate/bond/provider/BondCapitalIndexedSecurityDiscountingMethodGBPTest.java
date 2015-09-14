/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CouponInflationZeroCouponMonthlyGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueInflationIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDecoratedMulticurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscountingDecoratedIssuer;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the present value of Capital inflation indexed bonds.
 */
@Test(groups = TestGroup.UNIT)
public class BondCapitalIndexedSecurityDiscountingMethodGBPTest {

  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MulticurveProviderDiscountDataSets.getPriceIndexes();
  private static final IndexPrice PRICE_INDEX_UKRPI = PRICE_INDEXES[1];
  private static final String[] ISSUER_NAMES = MulticurveProviderDiscountDataSets.getIssuerNames();
  private static final String ISSUER_UK_GOVT = ISSUER_NAMES[1];

  private static final double SHIFT_FD = 1.0E-9;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 8);
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_BOND_INFLATION = new BondCapitalIndexedSecurityDiscountingMethod();
  private static final CouponInflationZeroCouponMonthlyGearingDiscountingMethod METHOD_INFLATION_ZC_MONTHLY = new CouponInflationZeroCouponMonthlyGearingDiscountingMethod();
  //  private static final CouponInflationZeroCouponInterpolationGearingDiscountingMethod METHOD_INFLATION_ZC_INTERPOLATION = new CouponInflationZeroCouponInterpolationGearingDiscountingMethod();
  private static final PresentValueDiscountingInflationCalculator PVDIC = PresentValueDiscountingInflationCalculator.getInstance();
  //  private static final NetAmountInflationCalculator NADIC = NetAmountInflationCalculator.getInstance();
  private static final PresentValueInflationIssuerDiscountingCalculator PVDIIC = PresentValueInflationIssuerDiscountingCalculator.getInstance();
  private static final ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = 
      new ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator(PVDIC, SHIFT_FD);
  //  private static final ParameterSensitivityIssuerInflationMulticurveDiscountInterpolatedFDCalculator PS_PV_FDIC = new ParameterSensitivityIssuerInflationMulticurveDiscountInterpolatedFDCalculator(
  //      PVDIIC, SHIFT_FD);
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSDC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationProviderInterface> PSC = new ParameterSensitivityInflationParameterCalculator<>(PVCSDC);
  //  private static final PresentValueCurveSensitivityIssuerDiscountingInflationCalculator PVCSDIC = PresentValueCurveSensitivityIssuerDiscountingInflationCalculator.getInstance();
  //  private static final ParameterInflationSensitivityParameterCalculator<InflationIssuerProviderInterface> PSIC = new ParameterInflationSensitivityParameterCalculator<>(PVCSDIC);

  // Index-Lined Gilt 2% Index-linked Treasury Stock 2035 - GB0031790826
  // UK Old convention (8m delay, semi-annual)
  private static final Calendar CALENDAR_GBP = new MondayToFridayCalendar("GBP");
  private static final BusinessDayConvention BUSINESS_DAY_GBP = BusinessDayConventions.FOLLOWING;
  private static final DayCount DAY_COUNT_GILT_1 = DayCounts.ACT_ACT_ISDA;
  private static final boolean IS_EOM_GILT_1 = false;
  private static final ZonedDateTime START_DATE_GILT_1 = DateUtils.getUTCDate(2002, 7, 11);
  private static final ZonedDateTime FIRST_COUPON_DATE_GILT_1 = DateUtils.getUTCDate(2003, 1, 26);
  private static final ZonedDateTime MATURITY_DATE_GILT_1 = DateUtils.getUTCDate(2035, 1, 26);
  private static final YieldConvention YIELD_CONVENTION_GILT_1 = SimpleYieldConvention.INDEX_LINKED_FLOAT;
  private static final int MONTH_LAG_GILT_1 = 8;
  private static final double INDEX_START_GILT_1 = 173.60; // November 2001
  private static final double NOTIONAL_GILT_1 = 1.00;
  private static final double REAL_RATE_GILT_1 = 0.02;
  private static final Period COUPON_PERIOD_GILT_1 = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS_GILT_1 = 2;
  // TODO: ex-coupon 7 days
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> BOND_SECURITY_GILT_1_DEFINITION = BondCapitalIndexedSecurityDefinition.fromMonthly(
      PRICE_INDEX_UKRPI, MONTH_LAG_GILT_1, START_DATE_GILT_1, INDEX_START_GILT_1, FIRST_COUPON_DATE_GILT_1, MATURITY_DATE_GILT_1, COUPON_PERIOD_GILT_1, NOTIONAL_GILT_1, REAL_RATE_GILT_1,
      BUSINESS_DAY_GBP, SETTLEMENT_DAYS_GILT_1, CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_GILT_1, IS_EOM_GILT_1, ISSUER_UK_GOVT);
  private static final DoubleTimeSeries<ZonedDateTime> UK_RPI = MulticurveProviderDiscountDataSets.ukRpiFrom2010();
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY_GILT_1 = BOND_SECURITY_GILT_1_DEFINITION.toDerivative(PRICING_DATE, UK_RPI);

  private static final double TOLERANCE_ACCRUED = 1.0E-5;
  private static final double TOLERANCE_PRICE = 2.0E-4;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_YIELD = 1.0E-4;
  private static final double TOLERANCE_YIELD_2 = 1.0E-8;

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValueGilt1() {
    final InflationProviderDiscount marketUKGovt = new InflationProviderDiscount();
    marketUKGovt.setCurve(BOND_SECURITY_GILT_1.getCurrency(), MARKET.getCurve(BOND_SECURITY_GILT_1.getIssuerEntity()));
    marketUKGovt.setCurve(PRICE_INDEX_UKRPI, MARKET.getCurve(PRICE_INDEX_UKRPI));
    final MultipleCurrencyAmount pvNominal = 
        METHOD_INFLATION_ZC_MONTHLY.presentValue(
            (CouponInflationZeroCouponMonthlyGearing) BOND_SECURITY_GILT_1.getNominal().getNthPayment(0), marketUKGovt);
    MultipleCurrencyAmount pvCoupon = MultipleCurrencyAmount.of(BOND_SECURITY_GILT_1.getCurrency(), 0.0);
    for (int loopcpn = 0; loopcpn < BOND_SECURITY_GILT_1.getCoupon().getNumberOfPayments(); loopcpn++) {
      pvCoupon = pvCoupon.plus(BOND_SECURITY_GILT_1.getCoupon().getNthPayment(loopcpn).accept(PVDIC, marketUKGovt));
    }
    final MultipleCurrencyAmount pvExpectd = pvNominal.plus(pvCoupon);
    final MultipleCurrencyAmount pv = METHOD_BOND_INFLATION.presentValue(BOND_SECURITY_GILT_1, MARKET);
    assertEquals("Inflation Capital Indexed bond: present value", 
        pvExpectd.getAmount(BOND_SECURITY_GILT_1.getCurrency()), 
        pv.getAmount(BOND_SECURITY_GILT_1.getCurrency()), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_BOND_INFLATION.presentValue(BOND_SECURITY_GILT_1, MARKET);
    final MultipleCurrencyAmount pvCalculator = BOND_SECURITY_GILT_1.accept(PVDIIC, MARKET);
    assertEquals("Inflation Capital Indexed bond: present value", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Test the present value parameter curves sensitivity.
   */
  public void presentValueParameterCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvicsFD = PS_PV_FDC.calculateSensitivity(BOND_SECURITY_GILT_1.getCoupon(), MARKET.getInflationProvider());
    final MultipleCurrencyParameterSensitivity pvicsExact = PSC.calculateSensitivity(BOND_SECURITY_GILT_1.getCoupon(), MARKET.getInflationProvider(), MARKET.getAllNames());
    AssertSensitivityObjects.assertEquals("Bond capital indexed security: presentValueParameterCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value curves sensitivity.
   */
  public void presentValueCurveSensitivity() {
    MulticurveProviderInterface multicurveDecorated = new MulticurveProviderDiscountingDecoratedIssuer(
        MARKET.getIssuerProvider(), BOND_SECURITY_GILT_1.getCurrency(), BOND_SECURITY_GILT_1.getIssuerEntity());
    InflationProviderInterface creditDiscounting = new InflationProviderDecoratedMulticurve(
        MARKET.getInflationProvider(), multicurveDecorated);
    final MultipleCurrencyInflationSensitivity sensitivityNominal = BOND_SECURITY_GILT_1.getNominal().accept(PVCSDC, creditDiscounting);
    final MultipleCurrencyInflationSensitivity sensitivityCoupon = BOND_SECURITY_GILT_1.getCoupon().accept(PVCSDC, creditDiscounting);
    final MultipleCurrencyInflationSensitivity pvcisCalculated = sensitivityNominal.plus(sensitivityCoupon);
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD_BOND_INFLATION.presentValueCurveSensitivity(BOND_SECURITY_GILT_1, MARKET);
    AssertSensitivityObjects.assertEquals("Bond capital indexed security: presentValueCurveSensitivity ", pvcisCalculated, pvcisMethod, TOLERANCE_PV_DELTA);
  }

  private static final ZonedDateTime PRICING_DATE_2 = DateUtils.getUTCDate(2014, 6, 9);
  private static final double PRICE_GILT = 2.00; // Nominal price
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY_GILT_2 = BOND_SECURITY_GILT_1_DEFINITION.toDerivative(PRICING_DATE_2, UK_RPI);

  @Test
  /**
   * Accrued interest
   */
  public void accruedInterest() {
    double aiComputed = BOND_SECURITY_GILT_2.getAccruedInterest(); // Accrued interest real
    double indexRatio = 252.1d / 173.6d;
    double aiExpected = 0.01083124; // Accrued interest nominal.
    assertEquals("Inflation Capital Indexed bond: present value", aiComputed, aiExpected / indexRatio, TOLERANCE_ACCRUED);
  }

  @Test
  /**
   * Price to and from yield
   */
  public void priceYield() {
    double yieldExpected = 0.00132482;
    double priceComputed = METHOD_BOND_INFLATION.cleanPriceFromYield(BOND_SECURITY_GILT_2, yieldExpected);
    assertEquals("Inflation Capital Indexed bond: yield - price", priceComputed, PRICE_GILT, TOLERANCE_PRICE);
    double yieldComputed = METHOD_BOND_INFLATION.yieldRealFromCleanPrice(BOND_SECURITY_GILT_2, PRICE_GILT);
    assertEquals("Inflation Capital Indexed bond: present value", yieldComputed, yieldExpected, TOLERANCE_YIELD);
    double yieldComputed2 = METHOD_BOND_INFLATION.yieldRealFromCleanPrice(BOND_SECURITY_GILT_2, priceComputed);
    assertEquals("Inflation Capital Indexed bond: present value", yieldComputed2, yieldExpected, TOLERANCE_YIELD_2);
  }

  // Index-Lined TOYOTA 2.413% Index-linked XS0302263214
  // UK New convention (3m delay, semi-annual)
  private static final ZonedDateTime PRICING_DATE_3 = DateUtils.getUTCDate(2014, 6, 10);
  private static final String ISSUER_TOYOTA = "TOYOTA";
  private static final ZonedDateTime START_DATE_CORP_1 = DateUtils.getUTCDate(2007, 5, 30);
  private static final ZonedDateTime FIRST_COUPON_DATE_CORP_1 = DateUtils.getUTCDate(2007, 11, 30);
  private static final ZonedDateTime MATURITY_DATE_CORP_1 = DateUtils.getUTCDate(2017, 5, 30);
  private static final YieldConvention YIELD_CONVENTION_CORP_1 = SimpleYieldConvention.UK_IL_BOND;
  private static final int MONTH_LAG_CORP_1 = 3;
  private static final double INDEX_START_CORP_1 = 204.31613;
  private static final double NOTIONAL_CORP_1 = 1.00;
  private static final double REAL_RATE_CORP_1 = 0.02413;
  private static final Period COUPON_PERIOD_CORP_1 = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS_CORP_1 = 3;
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> BOND_SECURITY_CORP_1_DEFINITION = BondCapitalIndexedSecurityDefinition.fromMonthly(
      PRICE_INDEX_UKRPI, MONTH_LAG_CORP_1, START_DATE_CORP_1, INDEX_START_CORP_1, FIRST_COUPON_DATE_CORP_1, MATURITY_DATE_CORP_1, COUPON_PERIOD_CORP_1, NOTIONAL_CORP_1, REAL_RATE_CORP_1,
      BUSINESS_DAY_GBP, SETTLEMENT_DAYS_CORP_1, CALENDAR_GBP, DAY_COUNT_GILT_1, YIELD_CONVENTION_CORP_1, IS_EOM_GILT_1, ISSUER_TOYOTA);
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY_CORP_1 = BOND_SECURITY_CORP_1_DEFINITION.toDerivative(PRICING_DATE_3, UK_RPI);

  private static final double PRICE_CLEAN_CORP = 1.20; // Real price
  private static final double YIELD_CORP = -0.03891519; // Real Yield 

  @Test
  /**
   * Price to and from yield
   */
  public void priceYieldUKILBond() {
    double priceComputed = METHOD_BOND_INFLATION.cleanPriceFromYield(BOND_SECURITY_CORP_1, YIELD_CORP);
    double yieldComputed = METHOD_BOND_INFLATION.yieldRealFromCleanPrice(BOND_SECURITY_CORP_1, PRICE_CLEAN_CORP);
    double yieldComputed3 = METHOD_BOND_INFLATION.yieldRealFromCleanPrice(BOND_SECURITY_CORP_1, priceComputed);
    assertEquals("Inflation Capital Indexed bond: yield - price", priceComputed, PRICE_CLEAN_CORP, TOLERANCE_PRICE);
    assertEquals("Inflation Capital Indexed bond: present value", yieldComputed, YIELD_CORP, TOLERANCE_YIELD);
    assertEquals("Inflation Capital Indexed bond: present value", yieldComputed3, YIELD_CORP, TOLERANCE_YIELD_2);
  }

}
