/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondDataSets;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsBondCurveGBP;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveGBP;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests on (fixed coupon) bonds with static data. Data available also in interface unit test and in snapshots.
 */
public class BondFixedTransactionDiscountingMethodE2ETest {

  private static final Currency GBP = Currency.GBP;
  // Curve calibrated on swaps (OIS)
  private static final ZonedDateTime REFERENCE_DATE_SWAPCURVE = DateUtils.getUTCDate(2014, 1, 22);
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_SWAP_PAIR = StandardDataSetsMulticurveGBP.getCurvesGBPSonia();
  private static final MulticurveProviderDiscount MULTICURVE_SWAP = MULTICURVE_SWAP_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_SWAP = MULTICURVE_SWAP_PAIR.getSecond();
  private static final Calendar LON = StandardDataSetsMulticurveGBP.calendarArray()[0];
  // Curve calibrated with bills and bonds.
  private static final ZonedDateTime REFERENCE_DATE_GOVTCURVE = DateUtils.getUTCDate(2014, 7, 11);
  private static final Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> ISSUER_GOVT_PAIR = StandardDataSetsBondCurveGBP.getCurvesGBPSoniaGovt();
  private static final IssuerProviderDiscount ISSUER_GOVT = ISSUER_GOVT_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_GOVT = ISSUER_GOVT_PAIR.getSecond();

  // Bond description
  private static final BondFixedSecurityDefinition UKT_800_20210607_SEC_DEF = BondDataSets.bondUKT800_20210607(1.0);
  private static final String NAME_ISSUER = UKT_800_20210607_SEC_DEF.getIssuer();
  private static final IssuerProviderDiscount ISSUER_SWAP = new IssuerProviderDiscount(MULTICURVE_SWAP);
  static {
    ISSUER_SWAP.setCurve(Pairs.of((Object) NAME_ISSUER, (LegalEntityFilter<LegalEntity>) new LegalEntityShortName()), MULTICURVE_SWAP.getCurve(GBP));
  }
  private static final double QUANTITY_UKT_800_20210607 = 10000000; // 10m
  private static final ZonedDateTime SETTLE_DATE_UKT_800_20210607_FWD = DateUtils.getUTCDate(2014, 2, 7);
  private static final ZonedDateTime SETTLE_DATE_UKT_800_20210607_PAST = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_SWAPCURVE, -1, LON);
  private static final double TRADE_PRICE_800_20210607 = 0.99;
  private static final ZonedDateTime SETTLE_DATE_UKT_800_20210607_3 = DateUtils.getUTCDate(2014, 8, 11);
  private static final BondFixedTransactionDefinition UKT_800_20210607_TRA_DEF = new BondFixedTransactionDefinition(UKT_800_20210607_SEC_DEF,
      QUANTITY_UKT_800_20210607, SETTLE_DATE_UKT_800_20210607_FWD, TRADE_PRICE_800_20210607);
  private static final BondFixedTransactionDefinition UKT_800_20210607_TRA_PAST_DEF = new BondFixedTransactionDefinition(UKT_800_20210607_SEC_DEF,
      QUANTITY_UKT_800_20210607, SETTLE_DATE_UKT_800_20210607_PAST, TRADE_PRICE_800_20210607);
  private static final BondFixedTransactionDefinition UKT_800_20210607_TRA_3_DEF = new BondFixedTransactionDefinition(UKT_800_20210607_SEC_DEF,
      QUANTITY_UKT_800_20210607, SETTLE_DATE_UKT_800_20210607_3, TRADE_PRICE_800_20210607);
  private static final BondFixedTransaction UKT_800_20210607_TRA_1 = UKT_800_20210607_TRA_DEF.toDerivative(REFERENCE_DATE_SWAPCURVE);
  private static final BondFixedTransaction UKT_800_20210607_TRA_2 = UKT_800_20210607_TRA_PAST_DEF.toDerivative(REFERENCE_DATE_SWAPCURVE);
  private static final BondFixedTransaction UKT_800_20210607_TRA_3 = UKT_800_20210607_TRA_3_DEF.toDerivative(REFERENCE_DATE_GOVTCURVE);

  // Calculator and methods
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final BondTransactionDiscountingMethod METHOD_BOND_TRA = BondTransactionDiscountingMethod.getInstance();
  private static final BondSecurityDiscountingMethod METHOD_BOND_SEC = BondSecurityDiscountingMethod.getInstance();
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterIssuerProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSIC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterIssuerProviderInterface> MQSBC =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final double BOND_QUOTED_CLEAN_PRICE = 1.40;
  private static final double BOND_QUOTED_YIELD = 0.018;

  private static final double TOLERANCE_PV = 1.0E-4;
  private static final double TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double TOLERANCE_YIELD = 1.0E-6;
  private static final double TOLERANCE_PRICE = 1.0E-6;
  private static final double BP1 = 1.0E-4;

  /** Curve calibrated on swaps (OIS) */
  @Test
  public void presentValueCurveOis() {
    double pvExpected1 = 4257244.16539445;
    MultipleCurrencyAmount pvComputed1 = UKT_800_20210607_TRA_1.accept(PVIC, ISSUER_SWAP);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", pvComputed1.getAmount(GBP), pvExpected1, TOLERANCE_PV);
    double pvExpected2 = 14291647.145374725;
    MultipleCurrencyAmount pvComputed2 = UKT_800_20210607_TRA_2.accept(PVIC, ISSUER_SWAP);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", pvComputed2.getAmount(GBP), pvExpected2, TOLERANCE_PV);
  }

  @Test
  public void yieldCurveOis() {
    double yieldExpected = 0.01886776;
    double yieldComputed = METHOD_BOND_SEC.yieldFromCurves(UKT_800_20210607_TRA_1.getBondStandard(), ISSUER_SWAP);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", yieldExpected, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void priceCurveOis() {
    double priceExpected = 1.41864835; // Clean price
    double priceComputed = METHOD_BOND_SEC.cleanPriceFromCurves(UKT_800_20210607_TRA_1.getBondStandard(), ISSUER_SWAP);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void presentValuePriceOis() {
    double pvExpected = 4070764.9831047133;
    MultipleCurrencyAmount pvComputed = METHOD_BOND_TRA.presentValueFromCleanPrice(UKT_800_20210607_TRA_1, ISSUER_SWAP, BOND_QUOTED_CLEAN_PRICE);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", pvComputed.getAmount(GBP), pvExpected, TOLERANCE_PV);
  }

  @Test
  public void yieldPrice() {
    double yieldExpected = 0.02109311;
    double yieldComputed = METHOD_BOND_SEC.yieldFromCleanPrice(UKT_800_20210607_TRA_1.getBondStandard(), BOND_QUOTED_CLEAN_PRICE);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", yieldExpected, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void presentValueYieldOis() {
    double pvExpected = 4330776.44270899;
    MultipleCurrencyAmount pvComputed = METHOD_BOND_TRA.presentValueFromYield(UKT_800_20210607_TRA_1, ISSUER_SWAP, BOND_QUOTED_YIELD);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", pvComputed.getAmount(GBP), pvExpected, TOLERANCE_PV);
  }

  @Test
  public void priceYield() {
    double priceExpected = 1.42600175; // Clean price
    double priceComputed = METHOD_BOND_SEC.cleanPriceFromYield(UKT_800_20210607_TRA_1.getBondStandard(), BOND_QUOTED_YIELD);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void BucketedPV01Ois() {
    final double[] deltaDsc = {18.3278, 6.1094, 19.5136, 0.8342, -8.5984, -7.0835, -18.0924, -35.6778, -109.0175, -164.1035,
      -219.2045, -276.5186, -318.6262, -4698.4808, -2631.1414, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(ISSUER_SWAP.getName(UKT_800_20210607_SEC_DEF.getIssuerEntity()), GBP), new DoubleMatrix1D(deltaDsc));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(UKT_800_20210607_TRA_1, ISSUER_SWAP, BLOCK_SWAP).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("BondFixedTransactionDiscountingMethodE2ETest: bucketed deltas from standard curves", pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

  /** Curve calibrated with bills and bonds. */
  @Test
  public void presentValueCurveGovt() {
    double pvExpected = 3821202.2688594256;
    MultipleCurrencyAmount pvComputed = UKT_800_20210607_TRA_3.accept(PVIC, ISSUER_GOVT);
    assertEquals("BondFixedTransactionDiscountingMethodE2ETest", pvComputed.getAmount(GBP), pvExpected, TOLERANCE_PV);
  }

  @Test
  public void BucketedPV01Govt() {
    final double[] deltaDsc = {-12.6245, -4.0078, 85.2275, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000,
      0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000 };
    final double[] deltaGovt = {-4.8697, -24.8628, 185.7734, 874.1115, 413.1247 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(ISSUER_GOVT.getMulticurveProvider().getName(GBP), GBP), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(ISSUER_GOVT.getName(UKT_800_20210607_SEC_DEF.getIssuerEntity()), GBP), new DoubleMatrix1D(deltaGovt));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(UKT_800_20210607_TRA_3, ISSUER_GOVT, BLOCK_GOVT).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("BondFixedTransactionDiscountingMethodE2ETest: bucketed deltas from standard curves", pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

}
