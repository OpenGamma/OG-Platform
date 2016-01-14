/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.model.volatility.smile.function.SSVIVolatilityFunction;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackStirFuturesSsviPriceProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
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
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link InterestRateFutureOptionMarginSecurityBlackSsviPriceMethod}.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureOptionMarginSecurityBlackSsviPriceMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES =
      MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  
  private static final InterestRateFutureOptionMarginSecurityDefinition OPTION_ERU2_DEFINITION =
      InterestRateFutureOptionMarginBlackRateMethodTest.OPTION_ERU2_DEFINITION;
  private static final InterestRateFutureOptionMarginSecurity OPTION_ERU2 =
      InterestRateFutureOptionMarginBlackRateMethodTest.OPTION_ERU2;
  private static final IborIndex INDEX = OPTION_ERU2_DEFINITION.getUnderlyingFuture().getIborIndex();
  
  /* Methods */
  private static final InterestRateFutureOptionMarginSecurityBlackSsviPriceMethod METHOD_SSVI =
      InterestRateFutureOptionMarginSecurityBlackSsviPriceMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackPriceMethod METHOD_BLACK =
      InterestRateFutureOptionMarginSecurityBlackPriceMethod.getInstance(); 
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
  private static final BlackStirFuturesSsviPriceProvider SSVI_PROVIDER = 
      new BlackStirFuturesSsviPriceProvider(MULTICURVES, VOLATILITY_ATM, RHO, ETA, INDEX);
  /* Black equivalent */
  private static final double STRIKE_PRICE = OPTION_ERU2_DEFINITION.getStrike();
  private static final double FUTURES_PRICE = METHOD_BLACK.underlyingFuturesPrice(OPTION_ERU2, SSVI_PROVIDER);
  private static final double TIME_EXP = OPTION_ERU2.getExpirationTime();
  private static final double BLACK_IV = SSVI_PROVIDER
      .getVolatility(TIME_EXP, 0.0, STRIKE_PRICE, FUTURES_PRICE);
  private static final Surface<Double, Double, Double> BLACK_SURFACE = new ConstantDoublesSurface(BLACK_IV);
  private static final BlackSTIRFuturesProviderInterface BLACK_PROVIDER = 
      new BlackSTIRFuturesSmileProvider(MULTICURVES, BLACK_SURFACE, INDEX);

  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-6;
  private static final double TOLERANCE_PRICE_DELTA_RELATIVE = 1.0E-3;

  @Test
  public void price() {
    double priceBlack = METHOD_BLACK.price(OPTION_ERU2, BLACK_PROVIDER);
    double priceSsvi = METHOD_SSVI.price(OPTION_ERU2, SSVI_PROVIDER);
    assertEquals("SSVI formula: price", priceBlack, priceSsvi, TOLERANCE_PRICE);
  }

  @Test
  public void implied_volatility() {
    double ivSsvi = METHOD_SSVI.impliedVolatility(OPTION_ERU2, SSVI_PROVIDER);
    assertEquals("SSVI formula: implied volatility", ivSsvi, BLACK_IV, TOLERANCE_PRICE);
  }

  @Test
  public void price_curve_sensitivity() {
    MulticurveSensitivity pcsBlack = METHOD_BLACK.priceCurveSensitivity(OPTION_ERU2, BLACK_PROVIDER);
    MulticurveSensitivity pcsSsvi = METHOD_SSVI.priceCurveSensitivity(OPTION_ERU2, SSVI_PROVIDER);
    AssertSensitivityObjects.assertEquals("SSVI formula: sensitivity", pcsBlack, pcsSsvi, TOLERANCE_PRICE_DELTA);
  }

  @Test
  public void price_black_sensitivity() {
    SurfaceValue pbsBlack = METHOD_BLACK.priceBlackSensitivity(OPTION_ERU2, BLACK_PROVIDER);
    SurfaceValue pbsSsvi = METHOD_SSVI.priceBlackSensitivity(OPTION_ERU2, SSVI_PROVIDER);
    assertEquals("SSVI formula: price", pbsBlack.toSingleValue(), pbsSsvi.toSingleValue(), TOLERANCE_PRICE_DELTA);
  }

  @Test
  public void price_ssvi_sensitivity() {
    double vega = METHOD_SSVI.priceBlackSensitivity(OPTION_ERU2, BLACK_PROVIDER).toSingleValue();
    ValueDerivatives ssviPriceSensitivity = METHOD_SSVI.priceSsviSensitivity(OPTION_ERU2, SSVI_PROVIDER);
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
    ValueDerivatives ssviPriceSensitivity = METHOD_SSVI.priceSsviSensitivity(OPTION_ERU2, SSVI_PROVIDER);
    Function1D<DoubleMatrix1D, Double> function = new Function1D<DoubleMatrix1D, Double>() {
      private static final long serialVersionUID = 1L;
      @Override
      public Double evaluate(DoubleMatrix1D x) {
        Double[] vol = VOLATILITY_ATM.getYData().clone();
        for(int i=0; i<vol.length; i++){
          vol[i] += x.getEntry(0);
        }
        DoublesCurve volatilityAtm = new InterpolatedDoublesCurve(VOLATILITY_ATM.getXData(), vol, LINEAR_FLAT, true);
        BlackStirFuturesSsviPriceProvider ssviProvider =
            new BlackStirFuturesSsviPriceProvider(MULTICURVES, volatilityAtm, 
                RHO + x.getEntry(1), ETA + x.getEntry(2), INDEX);
        return METHOD_SSVI.price(OPTION_ERU2, ssviProvider);
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
