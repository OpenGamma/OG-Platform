/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
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
 * 
 */
public class SABRInterpolatedTermStructureTest extends CapletStrippingSetup {

  private static double[] ALPHA_KNOTS = new double[] {1, 2, 3, 5, 7, 10 };
  private static double[] BETA_KNOTS = new double[] {1 };
  private static double[] RHO_KNOTS = new double[] {1, 3, 7 };
  private static double[] NU_KNOTS = new double[] {1, 2, 3, 5, 7, 10 };
  private static Interpolator1D BASE_INTERPOLATOR;
  private static ParameterLimitsTransform ALPHA_TRANSFORM;
  private static ParameterLimitsTransform BETA_TRANSFORM;
  private static ParameterLimitsTransform RHO_TRANSFORM;
  private static ParameterLimitsTransform NU_TRANSFORM;

  private static final DoubleMatrix1D START;

  static {
    BASE_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    ALPHA_TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    BETA_TRANSFORM = new DoubleRangeLimitTransform(0.1, 1);
    RHO_TRANSFORM = new DoubleRangeLimitTransform(-1, 1);
    NU_TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);

    final int nAlphaKnots = ALPHA_KNOTS.length;
    final int nBetaKnots = BETA_KNOTS.length;
    final int nRhoKnots = RHO_KNOTS.length;
    final int nNuKnots = NU_KNOTS.length;
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
  }

  @Test
  public void test() {

    final MulticurveProviderDiscount yc = getYieldCurves();
    final List<CapFloor> caps = getAllCaps();
    final double[] vols = getAllCapVols();

    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, yc);

    final InterpolatedVectorFunctionProvider alphaPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(BASE_INTERPOLATOR, ALPHA_TRANSFORM), ALPHA_KNOTS);
    final InterpolatedVectorFunctionProvider betaPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR), BETA_TRANSFORM), BETA_KNOTS);
    final InterpolatedVectorFunctionProvider rhoPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(BASE_INTERPOLATOR, RHO_TRANSFORM), RHO_KNOTS);
    final InterpolatedVectorFunctionProvider nuPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(BASE_INTERPOLATOR, NU_TRANSFORM), NU_KNOTS);

    final double[] t = pricer.getCapletExpiries();
    final int nExp = t.length;
    final VectorFunction alpha = alphaPro.from(t);
    final VectorFunction beta = betaPro.from(t);
    final VectorFunction rho = rhoPro.from(t);
    final VectorFunction nu = nuPro.from(t);

    final VectorFunction modelToSmileParms = new ConcatenatedVectorFunction(new VectorFunction[] {alpha, beta, rho, nu });

    final double[] errors = new double[caps.size()];
    Arrays.fill(errors, 0.0001); // 1bps
    final CapletStripper stripper = new CapletStripperSmileModel<SABRFormulaData>(pricer, new SABRHaganVolatilityFunction(), modelToSmileParms);
    final CapletStrippingResult res = stripper.solve(vols, MarketDataType.VOL, errors, START);
    System.out.println(res);

    res.printSurface(System.out, 101, 101);

  }
}
