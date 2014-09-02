/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.discrete.ParameterizedSABRModelDiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.DoublesVectorFunctionProvider;
import com.opengamma.analytics.math.function.InterpolatedVectorFunctionProvider;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;

/**
 * This tests {@link CapletStripperSABRModel} where the parameter term structures are represented at interpolated curves
 */
public class CapletStripperSABRModelTest extends CapletStrippingSetup {

  private static final double[] ALPHA_KNOTS = new double[] {1, 2, 3, 5, 7, 10 };
  private static final double[] BETA_KNOTS = new double[] {1 };
  private static final double[] RHO_KNOTS = new double[] {1, 3, 7 };
  private static final double[] NU_KNOTS = new double[] {1, 2, 3, 5, 7, 10 };
  private static final Interpolator1D BASE_INTERPOLATOR;
  private static final ParameterLimitsTransform ALPHA_TRANSFORM;
  private static final ParameterLimitsTransform BETA_TRANSFORM;
  private static final ParameterLimitsTransform RHO_TRANSFORM;
  private static final ParameterLimitsTransform NU_TRANSFORM;

  private static final DoubleMatrix1D START;
  private static final DoublesVectorFunctionProvider[] s_providers;

  static {
    BASE_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    ALPHA_TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    BETA_TRANSFORM = new DoubleRangeLimitTransform(0.1, 1);
    RHO_TRANSFORM = new DoubleRangeLimitTransform(-1, 1);
    NU_TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    int nAlphaKnots = ALPHA_KNOTS.length;
    int nBetaKnots = BETA_KNOTS.length;
    int nRhoKnots = RHO_KNOTS.length;
    int nNuKnots = NU_KNOTS.length;
    START = new DoubleMatrix1D(nAlphaKnots + +nBetaKnots + nRhoKnots + nNuKnots);
    double[] temp = new double[nAlphaKnots];
    Arrays.fill(temp, ALPHA_TRANSFORM.transform(0.2));
    System.arraycopy(temp, 0, START.getData(), 0, nAlphaKnots);
    temp = new double[nBetaKnots];
    Arrays.fill(temp, BETA_TRANSFORM.transform(0.7));
    System.arraycopy(temp, 0, START.getData(), nAlphaKnots, nBetaKnots);
    temp = new double[nRhoKnots];
    Arrays.fill(temp, RHO_TRANSFORM.transform(-0.2));
    System.arraycopy(temp, 0, START.getData(), nAlphaKnots + nBetaKnots, nRhoKnots);
    temp = new double[nNuKnots];
    Arrays.fill(temp, NU_TRANSFORM.transform(0.5));
    System.arraycopy(temp, 0, START.getData(), nAlphaKnots + nBetaKnots + nRhoKnots, nNuKnots);

    InterpolatedVectorFunctionProvider alphaPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(
        BASE_INTERPOLATOR, ALPHA_TRANSFORM), ALPHA_KNOTS);
    InterpolatedVectorFunctionProvider betaPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
            Interpolator1DFactory.FLAT_EXTRAPOLATOR), BETA_TRANSFORM), BETA_KNOTS);
    InterpolatedVectorFunctionProvider rhoPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(
        BASE_INTERPOLATOR, RHO_TRANSFORM), RHO_KNOTS);
    InterpolatedVectorFunctionProvider nuPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(
        BASE_INTERPOLATOR, NU_TRANSFORM), NU_KNOTS);

    s_providers = new DoublesVectorFunctionProvider[] {alphaPro, betaPro, rhoPro, nuPro };
  }

  /**
   * Fit all the cap (109 including ATM) using the SABR model with parameters given by interpolated term structures. The
   * fit is
   * poor (RMS error around 93bps), but is does produce a very smooth (caplet) volatility surface.
   */
  @Test
  public void allCapsTest() {
    double oneBP = 1e-4;

    MulticurveProviderDiscount yc = getYieldCurves();
    List<CapFloor> caps = getAllCaps();
    int nCaps = caps.size();
    double[] vols = getAllCapVols();

    MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, yc);

    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP); // 1bps
    CapletStripper stripper = new CapletStripperSABRModel(pricer, s_providers);
    CapletStrippingResult res = stripper.solve(vols, MarketDataType.VOL, errors, START);

    double expectedChi2 = 936380.0252991668; // this corresponds to a RMS errors of about 93bps
    assertEquals(expectedChi2, res.getChiSqr(), 1e-12 * expectedChi2);
  }

  /**
   * Fit all the cap prices (weighted by vega) (109 including ATM) using the SABR model with parameters given by
   * interpolated term structures. The fit is
   * poor (RMS error around 93bps), but is does produce a very smooth (caplet) volatility surface.
   */
  @Test
  public void allCapsPriceTest() {
    double oneBP = 1e-4;

    MulticurveProviderDiscount yc = getYieldCurves();
    List<CapFloor> caps = getAllCaps();
    int nCaps = caps.size();
    double[] vols = getAllCapVols();

    MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, yc);
    double[] pricers = pricer.price(vols);
    double[] vega = pricer.vega(vols);

    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP); // 1bps
    CapletStripper stripper = new CapletStripperSABRModel(pricer, s_providers);

    // Fit to price (weighted by vega)
    // scale vega
    for (int i = 0; i < nCaps; i++) {
      vega[i] *= oneBP;
    }

    CapletStrippingResult res = stripper.solve(pricers, MarketDataType.PRICE, vega, START);
    double expectedChi2 = 925326.9058053035;
    assertEquals(expectedChi2, res.getChiSqr(), 1e-12 * expectedChi2);
  }

  /**
   * Fit all the cap excluding the ATM (102) using the SABR model with parameters given by interpolated term structures.
   * The fit is
   * poor (RMS error around 82bps), but is does produce a very smooth (caplet) volatility surface.
   */
  @Test
  public void allCapsExATMTest() {

    MulticurveProviderDiscount yc = getYieldCurves();
    List<CapFloor> caps = getAllCapsExATM();
    double[] vols = getAllCapVolsExATM();

    MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, yc);

    CapletStripper stripper = new CapletStripperSABRModel(pricer, s_providers);
    // error is effectively 10,000bps
    CapletStrippingResult res = stripper.solve(vols, MarketDataType.VOL, START);

    double expectedChi2 = 0.006812970733200472; // this corresponds to a RMS errors of about 82bps
    assertEquals(expectedChi2, res.getChiSqr(), 1e-12 * expectedChi2);
  }

  @Test
  public void providerTest() {
    MulticurveProviderDiscount yc = getYieldCurves();
    List<CapFloor> caps = getAllCaps();
    double[] vols = getAllCapVols();

    MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, yc);
    double[] capletExpiries = pricer.getCapletExpiries();
    double[] fwds = pricer.getCapletForwardRates();
    // this interpolated forward curve that will only be hit at the knots, so don't need anything more than linear
    ForwardCurve fwdCurve = new ForwardCurve(InterpolatedDoublesCurve.from(capletExpiries, fwds,
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
            Interpolator1DFactory.FLAT_EXTRAPOLATOR)));

    ParameterizedSABRModelDiscreteVolatilityFunctionProvider dvfp = new ParameterizedSABRModelDiscreteVolatilityFunctionProvider(
        fwdCurve, s_providers);
    CapletStripper stripper = new CapletStripperSABRModel(pricer, dvfp);
    CapletStrippingResult res = stripper.solve(vols, MarketDataType.VOL, START);

    double expectedChi2 = 0.009363800283928515; // this corresponds to a RMS errors of about 93bps
    assertEquals(expectedChi2, res.getChiSqr(), 1e-12 * expectedChi2);
  }
}
