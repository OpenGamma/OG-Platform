/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DFullCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TwoStateMarkovChainSABRFitterTest {

  private static final PDE1DCoefficientsProvider PDE_PROVIDER = new PDE1DCoefficientsProvider();
  private static final InitialConditionsProvider INITAL_CONDITION_PROVIDER = new InitialConditionsProvider();
  private static final Function1D<Double, Double> ALPHA;
  private static final double BETA = 0.5;
  private static final double RHO = -0.0;
  private static final Function1D<Double, Double> NU;
  private static final double T = 5.0;
  private static final double SPOT = 1.0;
  private static final ForwardCurve FORWARD_CURVE;
  //private static final YieldCurve YIELD_CURVE;
  private static final double RATE = 0.0;
  private static final Function<Double, Double> SABR_VOL_FUNCTION;
  private static final List<double[]> EXPIRY_AND_STRIKES = new ArrayList<>();
  private static final List<Pair<double[], Double>> SABR_VOLS;

  static {
    EXPIRY_AND_STRIKES.add(new double[] {1. / 12, 0.925 });
    EXPIRY_AND_STRIKES.add(new double[] {1. / 12, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {1. / 12, 1.075 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 0.95 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 1.05 });
    EXPIRY_AND_STRIKES.add(new double[] {0.5, 1.1 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 0.8 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {1, 1.1 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 0.7 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 1.2 });
    EXPIRY_AND_STRIKES.add(new double[] {2, 1.4 });
    EXPIRY_AND_STRIKES.add(new double[] {3, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {4, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 0.4 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 0.6 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 0.8 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 0.9 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 1.0 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 2.0 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 2.5 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 3.0 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 3.5 });
    EXPIRY_AND_STRIKES.add(new double[] {5, 4.0 });

    final Function1D<Double, Double> fwd = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double t) {
        return SPOT * Math.exp(t * RATE);
      }
    };

    FORWARD_CURVE = new ForwardCurve(fwd);
    //    YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));

    ALPHA = new Function1D<Double, Double>() {
      double a = 0.0;
      double b = 0.0;
      double c = 0.0;
      double d = 0.2;

      @Override
      public Double evaluate(final Double t) {
        final double atmVol = (a + b * t) * Math.exp(-c * t) + d;
        return atmVol * Math.pow(SPOT, 1 - BETA);
      }
    };

    NU = new Function1D<Double, Double>() {
      double a = 0.6;
      double b = 0.0;
      double c = 1.0;
      double d = 0.3;

      @Override
      public Double evaluate(final Double t) {
        return (a + b * t) * Math.exp(-c * t) + d;
      }
    };

    SABR_VOL_FUNCTION = new Function<Double, Double>() {
      SABRHaganVolatilityFunction hagan = new SABRHaganVolatilityFunction();

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        final Function1D<SABRFormulaData, Double> func = hagan.getVolatilityFunction(option, FORWARD_CURVE.getForward(t));
        final SABRFormulaData data = new SABRFormulaData(ALPHA.evaluate(t), BETA, RHO, NU.evaluate(t));
        return func.evaluate(data);
      }
    };

    SABR_VOLS = new ArrayList<>(EXPIRY_AND_STRIKES.size());
    for (int i = 0; i < EXPIRY_AND_STRIKES.size(); i++) {
      final double[] tk = EXPIRY_AND_STRIKES.get(i);
      final Pair<double[], Double> pair = Pairs.of(tk, SABR_VOL_FUNCTION.evaluate(tk[0], tk[1]));
      SABR_VOLS.add(pair);
    }

  }

  @Test(enabled = false)
  public void dumpSurfaceTest() {

    PDEUtilityTools.printSurface("dumpSurfaceTest", FunctionalDoublesSurface.from(SABR_VOL_FUNCTION), 0, 5.0, SPOT / 4.0, 4.0 * SPOT);
  }

  @Test
  (enabled = false)
  public void test() {
    final DoubleMatrix1D initialGuess = new DoubleMatrix1D(new double[] {0.2, 0.3, 0.2, 2.0, 0.95, 0.8 });
    final TwoStateMarkovChainFitter fitter = new TwoStateMarkovChainFitter();
    final LeastSquareResultsWithTransform res = fitter.fit(FORWARD_CURVE, SABR_VOLS, initialGuess);
    System.out.println("chi^2:" + res.getChiSq() + "\n params: " + res.getModelParameters().toString());
  }

  //TODO this test does not work for the ConvectionDiffusionPDE1DStandardCoefficients case
  @Test
  (enabled = false)
  public void fokkerPlankTest() {
    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator(1e-4);

    final BlackVolatilitySurfaceStrike volSurface = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(SABR_VOL_FUNCTION));

    final LocalVolatilitySurfaceStrike localVol = cal.getLocalVolatility(volSurface, new ForwardCurve(SPOT, RATE));

    //    final ZZConvectionDiffusionPDEDataBundle db1 = LocalVolDensity.getConvectionDiffusionPDEDataBundle(FORWARD_CURVE, localVol);
    //    final ExtendedConvectionDiffusionPDEDataBundle db2 = LocalVolDensity.getExtendedConvectionDiffusionPDEDataBundle(FORWARD_CURVE, localVol);

    final int tNodes = 50;
    final int xNodes = 100;
    final BoundaryCondition lower = new DirichletBoundaryCondition(0.0, 0.0);
    final BoundaryCondition upper = new DirichletBoundaryCondition(0.0, 10 * FORWARD_CURVE.getForward(T));
    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 5.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, upper.getLevel(), SPOT, xNodes, 0.01);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    final ConvectionDiffusionPDE1DFullCoefficients pde1 = PDE_PROVIDER.getFokkerPlank(ConstantDoublesCurve.from(RATE), localVol);
    final ConvectionDiffusionPDE1DStandardCoefficients pde2 = PDE_PROVIDER.getFokkerPlankInStandardCoefficients(ConstantDoublesCurve.from(RATE), localVol);
    final Function1D<Double, Double> initalCondition = INITAL_CONDITION_PROVIDER.getLogNormalDensity(SPOT, 0.01, 0.2);

    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db1 = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde1, initalCondition, lower, upper, grid);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db2 = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde2, initalCondition, lower, upper, grid);

    final ThetaMethodFiniteDifference thetaMethod = new ThetaMethodFiniteDifference(0.5, true);

    final PDEFullResults1D res1 = (PDEFullResults1D) thetaMethod.solve(db1);
    final PDEFullResults1D res2 = (PDEFullResults1D) thetaMethod.solve(db2);

    PDEUtilityTools.printSurface("State 1 density", res1);
    PDEUtilityTools.printSurface("State 2 density", res2);
  }

  //TODO need to have real tests here rather than print a lot of surfaces
  @Test
  (enabled = false)
  public void localVolFitTest() {
    final DoubleMatrix1D initialGuess = new DoubleMatrix1D(new double[] {0.2, 0.3, 0.2, 2.0, 0.95, 0.8 });
    final TwoStateMarkovChainLocalVolFitter fitter = new TwoStateMarkovChainLocalVolFitter(true);
    fitter.fit(FORWARD_CURVE, new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(SABR_VOL_FUNCTION)), SABR_VOLS, initialGuess);
    // System.out.println("chi^2:" + res.getChiSq() + "\n params: " + res.getParameters().toString());
  }

  @Test(enabled = false)
  public void localVolTest() {
    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();

    final BlackVolatilitySurfaceStrike volSurface = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(SABR_VOL_FUNCTION));
    final LocalVolatilitySurfaceStrike localVol = cal.getLocalVolatility(volSurface, new ForwardCurve(SPOT, RATE));

    PDEUtilityTools.printSurface("localVolTest", localVol.getSurface(), 0, 5.0, SPOT / 4.0, SPOT * 4.0);
  }

}
