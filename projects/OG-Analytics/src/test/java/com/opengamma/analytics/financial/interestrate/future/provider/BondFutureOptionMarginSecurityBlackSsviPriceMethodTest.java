/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.model.volatility.smile.function.SSVIVolatilityFunction;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesFlatProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSsviPriceProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.analytics.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.differentiation.ValueDerivatives;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.test.TestGroup;
/**
 * Tests {@link BondFutureOptionMarginSecurityBlackSsviPriceMethod}.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureOptionMarginSecurityBlackSsviPriceMethodTest {
  
  /** Curves for a specific issuer name */
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVES = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  /** The legal entity */
  private static final LegalEntity[] LEGAL_ENTITIES = IssuerProviderDiscountDataSets.getIssuers();
  private static final LegalEntity LEGAL_ENTITY_GERMANY = LEGAL_ENTITIES[2];
  

  private static final BondFuturesOptionMarginSecurity CALL_BOBL_116 = 
      BondFuturesOptionMarginSecurityBlackExpLogMoneynessMethodTest.CALL_BOBL_116;
  private static final BondFuturesOptionMarginSecurity PUT_BOBL_116 = 
      BondFuturesOptionMarginSecurityBlackExpLogMoneynessMethodTest.PUT_BOBL_116;
  
  /* Methods */
  private static final BondFutureOptionMarginSecurityBlackSsviPriceMethod METHOD_SSVI =
      BondFutureOptionMarginSecurityBlackSsviPriceMethod.DEFAULT;
  private static final BondFutureOptionMarginSecurityBlackPriceMethod METHOD_BLACK =
      BondFutureOptionMarginSecurityBlackPriceMethod.getInstance(); 
  private static final ScalarFieldFirstOrderDifferentiator DIFFERENTIATOR =
      new ScalarFieldFirstOrderDifferentiator(FiniteDifferenceType.CENTRAL, 1.0E-5);
  
  /* SSVI data */
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory
      .getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double RHO = 0.25;
  private static final double ETA = 0.50;
  private static final DoublesCurve VOLATILITY_ATM;
  static{
    double[] times = {0.0, 0.5, 1.0, 5.0};
    double[] vol = {0.01, 0.011, 0.012, 0.01};
    VOLATILITY_ATM = new InterpolatedDoublesCurve(times, vol, LINEAR_FLAT, true);
  }
  private static final BlackBondFuturesSsviPriceProvider SSVI_PROVIDER = 
      new BlackBondFuturesSsviPriceProvider(ISSUER_SPECIFIC_MULTICURVES, VOLATILITY_ATM, RHO, ETA, LEGAL_ENTITY_GERMANY);
  /* Black equivalent */
  private static final double STRIKE_PRICE = CALL_BOBL_116.getStrike();
  private static final double FUTURES_PRICE = 
      METHOD_BLACK.getMethodFutures().price(CALL_BOBL_116.getUnderlyingFuture(), SSVI_PROVIDER);
  private static final double TIME_EXP = CALL_BOBL_116.getExpirationTime();
  private static final double BLACK_IV = SSVI_PROVIDER
      .getVolatility(TIME_EXP, 0.0, STRIKE_PRICE, FUTURES_PRICE);
  private static final Surface<Double, Double, Double> BLACK_SURFACE = new ConstantDoublesSurface(BLACK_IV);
  private static final BlackBondFuturesFlatProvider BLACK_PROVIDER = 
      new BlackBondFuturesFlatProvider(ISSUER_SPECIFIC_MULTICURVES, BLACK_SURFACE, LEGAL_ENTITY_GERMANY);

  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-6;
  private static final double TOLERANCE_PRICE_DELTA_RELATIVE = 1.0E-3;

  @Test
  public void price() {
    double priceBlack = METHOD_BLACK.price(CALL_BOBL_116, BLACK_PROVIDER);
    double priceSsvi = METHOD_SSVI.price(CALL_BOBL_116, SSVI_PROVIDER);
    assertEquals("SSVI formula: price", priceBlack, priceSsvi, TOLERANCE_PRICE);
  }
  
  @Test
  public void price_put_call_parity() {
    double priceCall = METHOD_SSVI.price(CALL_BOBL_116, SSVI_PROVIDER);
    double pricePut = METHOD_SSVI.price(PUT_BOBL_116, SSVI_PROVIDER);
    double priceFuture = METHOD_SSVI.underlyingFuturePrice(CALL_BOBL_116, ISSUER_SPECIFIC_MULTICURVES);
    assertEquals("SSVI formula: price", priceCall - pricePut, priceFuture - STRIKE_PRICE, TOLERANCE_PRICE);
  }

  @Test
  public void implied_volatility() {
    double ivSsvi = METHOD_SSVI.impliedVolatility(CALL_BOBL_116, SSVI_PROVIDER);
    assertEquals("SSVI formula: implied volatility", ivSsvi, BLACK_IV, TOLERANCE_PRICE);
  }

  @Test
  public void price_curve_sensitivity() {
    MulticurveSensitivity pcsBlack = METHOD_BLACK.priceCurveSensitivity(CALL_BOBL_116, BLACK_PROVIDER);
    MulticurveSensitivity pcsSsvi = METHOD_SSVI.priceCurveSensitivity(CALL_BOBL_116, SSVI_PROVIDER);
    AssertSensitivityObjects.assertEquals("SSVI formula: sensitivity", pcsBlack, pcsSsvi, TOLERANCE_PRICE_DELTA);
  }

  @Test
  public void price_black_sensitivity() {
    double vegaBlack = METHOD_BLACK.vega(CALL_BOBL_116, BLACK_PROVIDER);
    double vegaSsvi = METHOD_SSVI.vega(CALL_BOBL_116, SSVI_PROVIDER);
    assertEquals("SSVI formula: price Black sensitivity", vegaBlack, vegaSsvi, TOLERANCE_PRICE_DELTA);
  }

  @Test
  public void price_ssvi_sensitivity() {
    double vega = METHOD_SSVI.vega(CALL_BOBL_116, BLACK_PROVIDER);
    ValueDerivatives ssviPriceSensitivity = METHOD_SSVI.priceSsviSensitivity(CALL_BOBL_116, SSVI_PROVIDER);
    ValueDerivatives ssviVolSensitivity = SSVIVolatilityFunction
        .volatilityAdjoint(FUTURES_PRICE, STRIKE_PRICE, TIME_EXP, VOLATILITY_ATM.getYValue(TIME_EXP), RHO, ETA);
    for (int i = 0; i < 3; i++) {
      assertEquals("SSVI formula: price SSVI parameters sensitivity",
          ssviPriceSensitivity.getDerivatives(i), ssviVolSensitivity.getDerivatives(i+3) * vega,
          TOLERANCE_PRICE_DELTA);
    }
  }

  @Test
  public void price_ssvi_sensitivity_fd() {
    ValueDerivatives ssviPriceSensitivity = METHOD_SSVI.priceSsviSensitivity(CALL_BOBL_116, SSVI_PROVIDER);
    Function1D<DoubleMatrix1D, Double> function = new Function1D<DoubleMatrix1D, Double>() {
      private static final long serialVersionUID = 1L;
      @Override
      public Double evaluate(DoubleMatrix1D x) {
        Double[] vol = VOLATILITY_ATM.getYData().clone();
        for(int i=0; i<vol.length; i++){
          vol[i] += x.getEntry(0);
        }
        DoublesCurve volatilityAtm = new InterpolatedDoublesCurve(VOLATILITY_ATM.getXData(), vol, LINEAR_FLAT, true);
        BlackBondFuturesSsviPriceProvider ssviProvider =
            new BlackBondFuturesSsviPriceProvider(ISSUER_SPECIFIC_MULTICURVES, volatilityAtm, 
                RHO + x.getEntry(1), ETA + x.getEntry(2), LEGAL_ENTITY_GERMANY);
        return METHOD_SSVI.price(CALL_BOBL_116, ssviProvider);
      }
    };
    Function1D<DoubleMatrix1D, DoubleMatrix1D> d = DIFFERENTIATOR.differentiate(function);
    DoubleMatrix1D fd = d.evaluate(new DoubleMatrix1D(0.0, 0.0, 0.0));
    for (int j = 0; j < 3; j++) {
      assertEquals("SSVI formula: price SSVI parameters sensitivity",
          fd.getEntry(j), ssviPriceSensitivity.getDerivatives(j), TOLERANCE_PRICE_DELTA);
      assertEquals("SSVI formula: price SSVI parameters sensitivity",
          (fd.getEntry(j) - ssviPriceSensitivity.getDerivatives(j)) / ssviPriceSensitivity.getDerivatives()[j], 0.0,
          TOLERANCE_PRICE_DELTA_RELATIVE);
    }
  }
  
}
