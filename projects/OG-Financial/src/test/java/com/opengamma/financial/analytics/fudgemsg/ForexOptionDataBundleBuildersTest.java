/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class ForexOptionDataBundleBuildersTest extends AnalyticsTestBase {

  @Test
  public void test1() {
    final double[] t = {0.0, 0.25, 0.50, 1.00, 2.00};
    final double[] atm = {0.175, 0.185, 0.18, 0.17, 0.16};
    final double[] delta = new double[] {0.10, 0.25};
    final double[][] rr = new double[][] { {-0.010, -0.0050}, {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090}};
    final double[][] strangle = new double[][] { {0.0300, 0.0100}, {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}};
    SmileDeltaTermStructureParametersStrikeInterpolation smiles = new SmileDeltaTermStructureParametersStrikeInterpolation(t, delta, atm, rr, strangle);
    assertEquals(smiles, cycleObject(SmileDeltaTermStructureParametersStrikeInterpolation.class, smiles));
    final Interpolator1D strikeInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final Interpolator1D timeInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    smiles = new SmileDeltaTermStructureParametersStrikeInterpolation(t, delta, atm, rr, strangle, strikeInterpolator, timeInterpolator);
    assertEquals(smiles, cycleObject(SmileDeltaTermStructureParametersStrikeInterpolation.class, smiles));
  }

  @Test
  public void test2() {
    final double[] t = {0., 0.5};
    final double[] deltas = {0.8, 0.2};
    final double[][] volatility = new double[][] {new double[] {0.45, 0.35, 0.3, 0.27, 0.22}, new double[] {0.25, 0.15, 0.12, 0.11, 0.1}};
    SmileDeltaTermStructureParameters smiles = new SmileDeltaTermStructureParameters(t, deltas, volatility);
    assertEquals(smiles, cycleObject(SmileDeltaTermStructureParameters.class, smiles));
    final Interpolator1D timeInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    smiles = new SmileDeltaTermStructureParameters(smiles.getVolatilityTerm(), timeInterpolator);
    assertEquals(smiles, cycleObject(SmileDeltaTermStructureParameters.class, smiles));
  }

  @Test
  public void test3() {
    final DoublesCurve volatility = InterpolatedDoublesCurve.fromSorted(new double[] {1, 2, 3, 4, 5}, new double[] {0.3, 0.4, 0.5, 0.6, 0.7}, Interpolator1DFactory.LINEAR_INSTANCE);
    final BlackForexTermStructureParameters termStructure = new BlackForexTermStructureParameters(volatility);
    assertEquals(termStructure, cycleObject(BlackForexTermStructureParameters.class, termStructure));
  }
}
