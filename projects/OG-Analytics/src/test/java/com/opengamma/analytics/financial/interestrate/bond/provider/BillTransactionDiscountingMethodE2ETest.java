/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests related to the pricing of bills transactions by discounting.
 */
@Test(groups = TestGroup.UNIT)
public class BillTransactionDiscountingMethodE2ETest {

  // Data
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 1, 22);
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = StandardDataSetsMulticurveUSD.getCurvesUSDOisL3();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_PAIR.getSecond();
  private static final Calendar NYC = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final Currency USD = Currency.USD;

  // Issuer provider with Issuer "" priced from the OIS curve.
  private static final LegalEntityFilter<LegalEntity> SHORT_NAME_FILTER = new LegalEntityShortName();
  private final static String USGOVT_NAME = "Utd Sts Amer";
  private static final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> ISSUER_SPECIFIC = new LinkedHashMap<>();
  static {
    ISSUER_SPECIFIC.put(Pairs.of((Object) USGOVT_NAME, SHORT_NAME_FILTER), MULTICURVE.getCurve(USD));
  }
  private final static IssuerProviderDiscount ISSUER_MULTICURVE = new IssuerProviderDiscount(MULTICURVE, ISSUER_SPECIFIC);

  // ISIN: US912796DQ92 - 
  private static final DayCount ACT360 = DayCounts.ACT_360;
  private static final int SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION_DISCOUNT = YieldConventionFactory.INSTANCE.getYieldConvention("DISCOUNT");
  private final static ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2014, 4, 24);
  private final static double NOTIONAL = 1;
  private final static BillSecurityDefinition B140814_DEFINITION = new BillSecurityDefinition(USD, MATURITY_DATE, NOTIONAL, SETTLEMENT_DAYS, NYC,
      YIELD_CONVENTION_DISCOUNT, ACT360, USGOVT_NAME);

  // Trade 1
  private final static ZonedDateTime SETTLE_DATE_1 = DateUtils.getUTCDate(2014, 1, 23);
  private final static double QUANTITY_1 = 10000000;
  private final static double PREMIUM_1 = -9999000;
  private final static BillTransactionDefinition B140814_TRA_1_DEFINITION = new BillTransactionDefinition(B140814_DEFINITION, QUANTITY_1, SETTLE_DATE_1, PREMIUM_1);
  private final static BillTransaction B140814_TRA_1 = B140814_TRA_1_DEFINITION.toDerivative(REFERENCE_DATE);

  // Method and calculator
  //  private final static BillTransactionDiscountingMethod METHOD_TRANSACTION = BillTransactionDiscountingMethod.getInstance();
  private final static BillSecurityDiscountingMethod METHOD_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private final static PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();

  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterIssuerProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSIC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterIssuerProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final double TOLERANCE_PV = 1.0E-4;
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double TOLERANCE_PV_DELTA = 1.0E-4;
  private static final double BP1 = 1.0E-4;

  @Test
  /**
   * Tests the present value against hard-coded results for standard data sets.
   */
  public void presentValueOIS() {
    final MultipleCurrencyAmount pvComputed = B140814_TRA_1.accept(PVIC, ISSUER_MULTICURVE);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(USD, -1368.887395d);
    assertEquals("BillTransactionDiscountingMethodE2E: discounting method - present value", 1, pvComputed.size());
    assertEquals("BillTransactionDiscountingMethodE2E: discounting method - present value", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the yield against hard-coded results for standard data sets.
   */
  public void yieldFromCurvesOIS() {
    final double yieldComputed = METHOD_SECURITY.yieldFromCurves(B140814_TRA_1.getBillStandard(), ISSUER_MULTICURVE);
    final double yieldExpected = 0.0009371446;
    assertEquals("BillTransactionDiscountingMethodE2E: discounting method - yield from curves", yieldExpected, yieldComputed, TOLERANCE_RATE);
  }

  @Test
  /**
   * Test different results with a standard set of data against hardcoded values. Can be used for platform testing or regression testing.
   */
  public void BucketedPVOIS() {
    // Delta
    final double[] deltaDsc = {3.80024E-4, -2.7771, 0.0000, 0.0000, -249.8815, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    //    final ParameterSe
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(B140814_TRA_1, ISSUER_MULTICURVE, BLOCK).multipliedBy(BP1);
    AssertSensivityObjects.assertEquals("ForwardRateAgreementDiscountingMethod: bucketed delts from standard curves", pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

}
