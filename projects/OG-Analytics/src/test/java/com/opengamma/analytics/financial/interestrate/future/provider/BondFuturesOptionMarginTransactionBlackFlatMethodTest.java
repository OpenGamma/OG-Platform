/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.BondFuturesDataSets;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.calculator.blackbondfutures.PresentValueBlackBondFuturesOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackbondfutures.PresentValueCurveSensitivityBlackBondFuturesOptionCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.StandardDataSetsBlack;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesFlatProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.blackbondfutures.ParameterSensitivityBlackBondFuturesFlatDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

public class BondFuturesOptionMarginTransactionBlackFlatMethodTest {

  /** Bond future option: Bobl */
  private static final BondFuturesSecurityDefinition BOBLM4_DEFINITION = BondFuturesDataSets.boblM4Definition();
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 3, 31);
  private static final double STRIKE_115 = 1.15;
  private static final ZonedDateTime EXPIRY_DATE_OPT = DateUtils.getUTCDate(2014, 6, 5);
  private static final ZonedDateTime LAST_TRADING_DATE_OPT = DateUtils.getUTCDate(2014, 6, 4);
  private static final boolean IS_CALL = true;
  private static final BondFuturesOptionMarginSecurityDefinition CALL_BOBLM4_125_SEC_DEFINITION = new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION,
      LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_115, IS_CALL);
  private static final int QUANTITY = 1234;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2014, 3, 31);
  private static final double TRADE_PRICE = 0.01;
  private static final BondFuturesOptionMarginTransactionDefinition CALL_BOBLM4_125_TRA_1_DEFINITION =
      new BondFuturesOptionMarginTransactionDefinition(CALL_BOBLM4_125_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);
  private static final BondFuturesOptionMarginTransactionDefinition CALL_BOBLM4_125_TRA_2_DEFINITION =
      new BondFuturesOptionMarginTransactionDefinition(CALL_BOBLM4_125_SEC_DEFINITION, QUANTITY, TRADE_DATE.minusDays(1), TRADE_PRICE);
  private static final double REFERENCE_PRICE = 0.02;
  private static final BondFuturesOptionMarginTransaction CALL_BOBLM4_125_TRA_1 = CALL_BOBLM4_125_TRA_1_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE);
  private static final BondFuturesOptionMarginTransaction CALL_BOBLM4_125_TRA_2 = CALL_BOBLM4_125_TRA_2_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE);
  /** Black surface expiry/delay */
  final private static InterpolatedDoublesSurface BLACK_SURFACE = StandardDataSetsBlack.blackSurfaceExpiryDelay();
  /** Curves for a specific issuer name */
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVES = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  /** The legal entity */
  private static final LegalEntity[] LEGAL_ENTITIES = IssuerProviderDiscountDataSets.getIssuers();
  private static final LegalEntity LEGAL_ENTITY_GERMANY = LEGAL_ENTITIES[2];
  /** The Black bond futures provider **/
  private static final BlackBondFuturesFlatProviderDiscount BLACK_FLAT_BNDFUT = new BlackBondFuturesFlatProviderDiscount(ISSUER_SPECIFIC_MULTICURVES,
      BLACK_SURFACE, LEGAL_ENTITY_GERMANY);
  /** Methods and calculators */
  private static final BondFuturesOptionMarginSecurityBlackBondFuturesMethod METHOD_OPT_SEC = BondFuturesOptionMarginSecurityBlackBondFuturesMethod.getInstance();
  private static final FuturesTransactionBlackBondFuturesMethod METHOD_OPT_TRA = new FuturesTransactionBlackBondFuturesMethod();
  private static final PresentValueBlackBondFuturesOptionCalculator PVBFC = PresentValueBlackBondFuturesOptionCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackBondFuturesOptionCalculator PVCSBFC = PresentValueCurveSensitivityBlackBondFuturesOptionCalculator.getInstance();
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<BlackBondFuturesProviderInterface> PSSFC = new ParameterSensitivityParameterCalculator<>(PVCSBFC);
  private static final ParameterSensitivityBlackBondFuturesFlatDiscountInterpolatedFDCalculator PSSFC_FD =
      new ParameterSensitivityBlackBondFuturesFlatDiscountInterpolatedFDCalculator(PVBFC, SHIFT);
  /** Tolerances */
  private static final double TOLERANCE_RATE = 1.0E-3;
  private static final double TOLERANCE_PV_DELTA = 1.0E-1;

  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed1 = METHOD_OPT_TRA.presentValue(CALL_BOBLM4_125_TRA_1, BLACK_FLAT_BNDFUT);
    final double priceOpt1 = METHOD_OPT_SEC.price(CALL_BOBLM4_125_TRA_1.getUnderlyingSecurity(), BLACK_FLAT_BNDFUT);
    final double pvExpected1 = (priceOpt1 - TRADE_PRICE) * BOBLM4_DEFINITION.getNotional() * QUANTITY;
    assertEquals("BondFuturesOptionMarginTransactionBlackFlatMethod: present value", pvExpected1, pvComputed1.getAmount(BOBLM4_DEFINITION.getCurrency()), TOLERANCE_RATE);
    assertTrue("BondFuturesOptionMarginTransactionBlackFlatMethod: present value", pvComputed1.size() == 1);
    final MultipleCurrencyAmount pvCalculator = CALL_BOBLM4_125_TRA_1.accept(PVBFC, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginTransactionBlackFlatMethod: present value", pvExpected1, pvCalculator.getAmount(BOBLM4_DEFINITION.getCurrency()), TOLERANCE_RATE);
    final MultipleCurrencyAmount pvComputed2 = METHOD_OPT_TRA.presentValue(CALL_BOBLM4_125_TRA_2, BLACK_FLAT_BNDFUT);
    final double priceOpt2 = METHOD_OPT_SEC.price(CALL_BOBLM4_125_TRA_2.getUnderlyingSecurity(), BLACK_FLAT_BNDFUT);
    final double pvExpected2 = (priceOpt2 - REFERENCE_PRICE) * BOBLM4_DEFINITION.getNotional() * QUANTITY;
    assertEquals("BondFuturesOptionMarginTransactionBlackFlatMethod: present value", pvExpected2, pvComputed2.getAmount(BOBLM4_DEFINITION.getCurrency()), TOLERANCE_RATE);
  }

  @Test
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsComputed = PSSFC.calculateSensitivity(CALL_BOBLM4_125_TRA_1, BLACK_FLAT_BNDFUT);
    final MultipleCurrencyParameterSensitivity pvpsFD = PSSFC_FD.calculateSensitivity(CALL_BOBLM4_125_TRA_1, BLACK_FLAT_BNDFUT);
    AssertSensivityObjects.assertEquals("BondFuturesOptionMarginTransactionBlackFlatMethod: presentValueCurveSensitivity", pvpsFD, pvpsComputed, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_OPT_TRA.presentValueCurveSensitivity(CALL_BOBLM4_125_TRA_1, BLACK_FLAT_BNDFUT);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CALL_BOBLM4_125_TRA_1.accept(PVCSBFC, BLACK_FLAT_BNDFUT);
    AssertSensivityObjects.assertEquals("BondFuturesOptionMarginTransactionBlackFlatMethod: presentValueCurveSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

}
