/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.BondFuturesDataSets;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.future.calculator.DeltaBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.GammaBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.ThetaBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.VegaBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.calculator.blackbondfutures.PresentValueBlackBondFuturesOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesExpStrikeProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * End-to-end test used for demo and integration checks. Previous run hard-coded numbers. 
 */
public class BondFuturesOptionPremiumBlackExpStrikeE2ETest {

  /** Bond future option: JGB */
  private static final Currency JPY = Currency.JPY;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2015, 5, 12);
  private static final BondFuturesSecurityDefinition JBM5_DEFINITION = BondFuturesDataSets.JBM5_DEFINITION;
  private static final BondFuturesSecurity JBM5 = JBM5_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesSecurityDefinition JBU5_DEFINITION = BondFuturesDataSets.JBU5_DEFINITION;
  private static final BondFuturesSecurity JBU5 = JBU5_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final double NOTIONAL = JBM5_DEFINITION.getNotional();
  private static final double STRIKE_147 = 1.47; // To be close to ATM for the data set used.
  private static final double STRIKE_146_5 = 1.465; // To be close to ATM for the data set used.
  private static final ZonedDateTime EXPIRY_DATE_M_OPT = DateUtils.getUTCDate(2015, 5, 31);
  private static final ZonedDateTime EXPIRY_DATE_N_OPT = DateUtils.getUTCDate(2015, 6, 30);
  private static final boolean IS_CALL = true;
  private static final BondFuturesOptionPremiumSecurityDefinition CALL_JBM_147_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(JBM5_DEFINITION, EXPIRY_DATE_M_OPT, STRIKE_147, IS_CALL);
  private static final BondFuturesOptionPremiumSecurityDefinition PUT_JBM_147_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(JBM5_DEFINITION, EXPIRY_DATE_M_OPT, STRIKE_147, !IS_CALL);
  private static final BondFuturesOptionPremiumSecurityDefinition PUT_JBN_146_5_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(JBU5_DEFINITION, EXPIRY_DATE_N_OPT, STRIKE_146_5, !IS_CALL);

  private static final int QUANTITY = 100;
  private static final ZonedDateTime PREMIUM_DATE = DateUtils.getUTCDate(2015, 5, 13);
  private static final ZonedDateTime PREMIUM_DATE_2 = DateUtils.getUTCDate(2015, 5, 6);
  private static final double PREMIUM_UNIT_CALL_JBM_147 = 0.0030;
  private static final double PREMIUM_UNIT_PUT_JBM_147 = 0.0046;
  
  private static final BondFuturesOptionPremiumTransactionDefinition CALL_JBM_147_TRA_DEFINITION =
      new BondFuturesOptionPremiumTransactionDefinition(CALL_JBM_147_DEFINITION, QUANTITY, PREMIUM_DATE,
          -QUANTITY * NOTIONAL * PREMIUM_UNIT_CALL_JBM_147);
  private static final BondFuturesOptionPremiumTransaction CALL_JBM_147_TRA =
      CALL_JBM_147_TRA_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesOptionPremiumTransactionDefinition PUT_JBM_147_TRA_DEFINITION =
      new BondFuturesOptionPremiumTransactionDefinition(PUT_JBM_147_DEFINITION, QUANTITY, PREMIUM_DATE,
          -QUANTITY * NOTIONAL * PREMIUM_UNIT_PUT_JBM_147);
  private static final BondFuturesOptionPremiumTransaction PUT_JBM_147_TRA =
      PUT_JBM_147_TRA_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesOptionPremiumTransactionDefinition PUT_JBN_146_5_TRA_DEFINITION =
      new BondFuturesOptionPremiumTransactionDefinition(PUT_JBN_146_5_DEFINITION, -QUANTITY, PREMIUM_DATE_2, 0);
  private static final BondFuturesOptionPremiumTransaction PUT_JBN_146_5_TRA =
      PUT_JBN_146_5_TRA_DEFINITION.toDerivative(REFERENCE_DATE);
  
  /** Black surface expiry/strike */
  final private static InterpolatedDoublesSurface BLACK_SURFACE_EXP_STRIKE = 
      BondFuturesOptionPremiumE2EDataSet.BLACK_SURFACE_BND_EXP_STRIKE;
  /** Curves for a specific issuer name */
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVES = 
      BondFuturesOptionPremiumE2EDataSet.ISSUER_SPECIFIC_MULTICURVE_JP;
  /** The legal entity */
  private static final LegalEntity LEGAL_ENTITY_JAPAN = BondFuturesOptionPremiumE2EDataSet.JP_GOVT;
  /** The Black bond futures provider **/
  private static final BlackBondFuturesExpStrikeProvider BLACK_EXP_STRIKE_BNDFUT =
      new BlackBondFuturesExpStrikeProvider(ISSUER_SPECIFIC_MULTICURVES, BLACK_SURFACE_EXP_STRIKE, LEGAL_ENTITY_JAPAN);
  /** Methods and calculators */
  private static final BondFuturesOptionPremiumSecurityBlackBondFuturesMethod METHOD_OPT_SEC = 
      BondFuturesOptionPremiumSecurityBlackBondFuturesMethod.getInstance();
  private static final BondFuturesOptionPremiumTransactionBlackBondFuturesMethod METHOD_OPT_TRA = 
      BondFuturesOptionPremiumTransactionBlackBondFuturesMethod.getInstance();
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURES = 
      BondFuturesSecurityDiscountingMethod.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);

  
  /** Tolerances */
  private static final double TOLERANCE_PRICE = 1.0E-6;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_APPROX = 1.0E+3;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;
  private static final double BP1 = 1.0E-4;

  @Test
  public void futurePrice() {
    double futuresPriceM5Expected = 1.46883113;
    double futuresPriceM5 = METHOD_FUTURES.price(JBM5, ISSUER_SPECIFIC_MULTICURVES);
    assertEquals(futuresPriceM5Expected, futuresPriceM5, TOLERANCE_PRICE);
    double futuresPriceU5Expected = 1.46480431;
    double futuresPriceU5 = METHOD_FUTURES.price(JBU5, ISSUER_SPECIFIC_MULTICURVES);
    assertEquals(futuresPriceU5Expected, futuresPriceU5, TOLERANCE_PV);
  }
  
  /* Present value for each option. */
  @Test
  public void presentValue() {
    double pvCallM147Expected = 5878108.4498; // PV includes premium and option value
    MultipleCurrencyAmount pvCallM147Computed = METHOD_OPT_TRA.presentValue(CALL_JBM_147_TRA, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(pvCallM147Expected, pvCallM147Computed.getAmount(JPY), TOLERANCE_PV);
    double pvPutM147Expected = 1566468.8540;
    MultipleCurrencyAmount pvPutM147Computed = METHOD_OPT_TRA.presentValue(PUT_JBM_147_TRA, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(pvPutM147Expected, pvPutM147Computed.getAmount(JPY), TOLERANCE_PV);    
    double pvPutU1465Expected = -81090395.9457; // Short the option
    MultipleCurrencyAmount pvPutU1465Computed = METHOD_OPT_TRA.presentValue(PUT_JBN_146_5_TRA, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(pvPutU1465Expected, pvPutU1465Computed.getAmount(JPY), TOLERANCE_PV);
    MultipleCurrencyAmount pvPutU1465Calculator = PUT_JBN_146_5_TRA
        .accept(PresentValueBlackBondFuturesOptionCalculator.getInstance(), BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(pvPutU1465Expected, pvPutU1465Calculator.getAmount(JPY), TOLERANCE_PV);
  }
  
  /* Sensitivity to the zero-coupon rates of the curves. */
  @Test
  public void presentValueParameterSensitivity() {
    MultipleCurrencyMulticurveSensitivity pvptsCallM147 = 
        METHOD_OPT_TRA.presentValueCurveSensitivity(CALL_JBM_147_TRA, BLACK_EXP_STRIKE_BNDFUT);
    MultipleCurrencyParameterSensitivity pvpsCallM147Computed = 
        PSC.pointToParameterSensitivity(pvptsCallM147, BLACK_EXP_STRIKE_BNDFUT).multipliedBy(BP1); // ZR sensi to 1 bp
    double[] deltaDscCM = {41391.172, 30092.939, 0.000, 0.000, 0.000, 0.000, 0.000};
    double[] deltaGovtCM = {-1699.265, -5064.843, -30751.921, -66818.039, -4351376.994, -159578.927, 0.000}; // Check with delta
    AssertSensitivityObjects.assertEquals("BondFuturesOptionPremiumBlackExpStrike - end-to-end test",
        pvpsCallM147Computed, ps(deltaDscCM, deltaGovtCM), TOLERANCE_PV_DELTA);
    MultipleCurrencyMulticurveSensitivity pvptsPutU1465 = 
        METHOD_OPT_TRA.presentValueCurveSensitivity(PUT_JBN_146_5_TRA, BLACK_EXP_STRIKE_BNDFUT);
    MultipleCurrencyParameterSensitivity pvpsPutU1465Computed = 
        PSC.pointToParameterSensitivity(pvptsPutU1465, BLACK_EXP_STRIKE_BNDFUT).multipliedBy(BP1); // ZR sensi to 1 bp
    double[] deltaDscPN = {510.164, 172102.284, 121028.501, 0.000, 0.000, 0.000, 0.000};
    double[] deltaGovtPN = {-1932.522, -5617.214, -34020.187, -73950.143, -5118826.798, -689566.008, 0.000};
    AssertSensitivityObjects.assertEquals("BondFuturesOptionPremiumBlackExpStrike - end-to-end test",
        pvpsPutU1465Computed, ps(deltaDscPN, deltaGovtPN), TOLERANCE_PV_DELTA);
  }
  
  /* Delta of the PV. */
  @Test
  public void presentValueDelta() {
    double deltaPutM147Expected = -5433547105.474;
    double deltaPutM147Computed = METHOD_OPT_TRA.presentValueDelta(PUT_JBM_147_TRA, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(deltaPutM147Expected, deltaPutM147Computed, TOLERANCE_PV);
  }
  
  /* Gamma of the PV. */
  @Test
  public void presentValueGamma() {
    double gammaPutM147Expected = 381729633582.165;
    double gammaPutM147Computed = METHOD_OPT_TRA.presentValueGamma(PUT_JBM_147_TRA, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(gammaPutM147Expected, gammaPutM147Computed, TOLERANCE_PV);
  }
  
  /* Vega of the PV. */
  @Test
  public void presentValueVega() {
    double vegaPutM147Expected = 1328991007.568;
    double vegaPutM147Computed = METHOD_OPT_TRA.presentValueVega(PUT_JBM_147_TRA, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(vegaPutM147Expected, vegaPutM147Computed, TOLERANCE_PV);
  }
  
  /* Check that the delta/gamma expansion approximate the change of PV */
  @Test
  public void presentValueDeltaGammaExpansion() {
    double priceFutures = METHOD_FUTURES.price(JBU5, ISSUER_SPECIFIC_MULTICURVES);
    double delta = METHOD_OPT_TRA.presentValueDelta(PUT_JBN_146_5_TRA, BLACK_EXP_STRIKE_BNDFUT);
    double gamma = METHOD_OPT_TRA.presentValueGamma(PUT_JBN_146_5_TRA, BLACK_EXP_STRIKE_BNDFUT);
    double pv0 = METHOD_OPT_TRA.presentValue(PUT_JBN_146_5_TRA, BLACK_EXP_STRIKE_BNDFUT).getAmount(JPY);
    double shift = 1.0E-3; // 10 bp
    double pvShifted = METHOD_OPT_TRA.presentValueFromUnderlyingPrice(
        PUT_JBN_146_5_TRA, BLACK_EXP_STRIKE_BNDFUT, priceFutures + shift).getAmount(JPY);
    assertEquals(pvShifted, pv0 + delta * shift + 0.5 * gamma * shift * shift, TOLERANCE_PV_APPROX);
  }
  
  /* Check that the delta/gamma expansion approximate the change of PV */
  @Test
  public void presentValueVegaExpansion() {
    double vega = METHOD_OPT_TRA.presentValueVega(PUT_JBN_146_5_TRA, BLACK_EXP_STRIKE_BNDFUT);
    double pv0 = METHOD_OPT_TRA.presentValue(PUT_JBN_146_5_TRA, BLACK_EXP_STRIKE_BNDFUT).getAmount(JPY);
    double shift = 5.0E-4; // 5bp out of ~3% vol
    InterpolatedDoublesSurface blackSurfaceShifted = BondFuturesOptionPremiumE2EDataSet.blackSurfaceBndExpStrike(shift);
    BlackBondFuturesExpStrikeProvider providerVolShifted =
        new BlackBondFuturesExpStrikeProvider(ISSUER_SPECIFIC_MULTICURVES, blackSurfaceShifted, LEGAL_ENTITY_JAPAN);
    double pvShifted = METHOD_OPT_TRA.presentValue(PUT_JBN_146_5_TRA, providerVolShifted).getAmount(JPY);
    assertEquals(pvShifted, pv0 + vega * shift, TOLERANCE_PV_APPROX);
  }
  
  @Test
  public void delta() {
    double deltaPutM147Expected = -0.54335471;
    double deltaPutM147Computed = 
        METHOD_OPT_SEC.delta(PUT_JBM_147_TRA.getUnderlyingOption(), BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(deltaPutM147Expected, deltaPutM147Computed, TOLERANCE_PRICE);    
    double deltaPutM147Calculator = PUT_JBM_147_TRA.getUnderlyingOption()
        .accept(DeltaBlackBondFuturesCalculator.getInstance(), BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(deltaPutM147Expected, deltaPutM147Calculator, TOLERANCE_PRICE);    
  }
  
  @Test
  public void gamma() {
    double gammaPutM147Expected = 38.17296336;
    double gammaPutM147Computed = 
        METHOD_OPT_SEC.gamma(PUT_JBM_147_TRA.getUnderlyingOption(), BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(gammaPutM147Expected, gammaPutM147Computed, TOLERANCE_PRICE);
    double gammaPutM147Calculator = PUT_JBM_147_TRA.getUnderlyingOption()
        .accept(GammaBlackBondFuturesCalculator.getInstance(), BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(gammaPutM147Expected, gammaPutM147Calculator, TOLERANCE_PRICE);
  }
  
  @Test
  public void vega() {
    double vegaPutM147Expected = 0.13289910;
    double vegaPutM147Computed = 
        METHOD_OPT_SEC.vega(PUT_JBM_147_TRA.getUnderlyingOption(), BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(vegaPutM147Expected, vegaPutM147Computed, TOLERANCE_PRICE);
    double vegaPutM147Calculator = PUT_JBM_147_TRA.getUnderlyingOption()
        .accept(VegaBlackBondFuturesCalculator.getInstance(), BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(vegaPutM147Expected, vegaPutM147Calculator, TOLERANCE_PRICE);
  }
  
  @Test
  public void theta() {
    double thetaPutM147Expected = -0.03957085;
    double thetaPutM147Computed = 
        METHOD_OPT_SEC.theta(PUT_JBM_147_TRA.getUnderlyingOption(), BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(thetaPutM147Expected, thetaPutM147Computed, TOLERANCE_PRICE);    
    double thetaPutM147Calculator = PUT_JBM_147_TRA.getUnderlyingOption()
        .accept(ThetaBlackBondFuturesCalculator.getInstance(), BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(thetaPutM147Expected, thetaPutM147Calculator, TOLERANCE_PRICE);
  }
  
  // create a parameter sensitivity from the arrays
  private MultipleCurrencyParameterSensitivity ps(double[] deltaDsc, double[] deltaGovt) {
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider().getName(JPY), JPY),
        new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(ISSUER_SPECIFIC_MULTICURVES.getName(LEGAL_ENTITY_JAPAN), JPY), 
        new DoubleMatrix1D( deltaGovt));
    return new MultipleCurrencyParameterSensitivity(sensitivity);
  }
  
}
