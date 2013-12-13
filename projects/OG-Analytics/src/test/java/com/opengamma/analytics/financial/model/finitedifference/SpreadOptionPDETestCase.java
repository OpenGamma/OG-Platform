/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.cube.Cube;
import com.opengamma.analytics.math.cube.FunctionalDoublesCube;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SpreadOptionPDETestCase {

  private static BoundaryCondition2D A_LOWER;
  private static BoundaryCondition2D A_UPPER;
  private static BoundaryCondition2D B_LOWER;
  private static BoundaryCondition2D B_UPPER;

  private static final double SPOT_A = 100;
  private static final double SPOT_B = 100;

  private static final double T = 1.0;
  private static final double RATE = 0.05;
  private static final double VOL_A = 0.20;
  private static final double VOL_B = 0.30;
  private static final double RHO = -0.5;// used to be -0.5

  private static final ConvectionDiffusion2DPDEDataBundle DATA;

  private static Cube<Double, Double, Double, Double> A;
  private static Cube<Double, Double, Double, Double> B;
  private static Cube<Double, Double, Double, Double> C;
  private static Cube<Double, Double, Double, Double> D;
  private static Cube<Double, Double, Double, Double> E;
  private static Cube<Double, Double, Double, Double> F;

  static {

    final Function<Double, Double> bZeroBoundary = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        Validate.isTrue(tx.length == 2);
        final double x = tx[1];
        return x;
      }
    };

    A_LOWER = new DirichletBoundaryCondition2D(0.0, 0.0);
    // A_UPPER = new DirichletBoundaryCondition2D(0.0, 5 * SPOT_A);
    // B_LOWER = new DirichletBoundaryCondition2D(0.0, 0.0);
    // B_UPPER = new DirichletBoundaryCondition2D(0.0, 5 * SPOT_B);

    A_UPPER = new SecondDerivativeBoundaryCondition2D(0.0, 5 * SPOT_A);
    // B_LOWER = new SecondDerivativeBoundaryCondition2D(0.0, 0);
    B_LOWER = new DirichletBoundaryCondition2D(FunctionalDoublesSurface.from(bZeroBoundary), 0.0);// option value = Spot_A when Spot_B = 0
    B_UPPER = new SecondDerivativeBoundaryCondition2D(0.0, 5 * SPOT_B);

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        final double x = txy[1];
        return -x * x * VOL_A * VOL_A / 2;
      }
    };
    A = FunctionalDoublesCube.from(a);

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        final double x = txy[1];
        return -x * RATE;
      }
    };
    B = FunctionalDoublesCube.from(b);

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        return RATE;
      }
    };
    C = FunctionalDoublesCube.from(c);

    final Function<Double, Double> d = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        final double y = txy[2];
        return -y * y * VOL_B * VOL_B / 2;
      }
    };
    D = FunctionalDoublesCube.from(d);

    final Function<Double, Double> e = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        final double x = txy[1];
        final double y = txy[2];

        return -x * y * VOL_A * VOL_B * RHO;
      }
    };
    E = FunctionalDoublesCube.from(e);

    final Function<Double, Double> f = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        final double y = txy[2];
        return -y * RATE;
      }
    };
    F = FunctionalDoublesCube.from(f);

    final Function<Double, Double> payoff = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... xy) {
        Validate.isTrue(xy.length == 2);
        final double x = xy[0];
        final double y = xy[1];
        return Math.max(x - y, 0);// debug
        // return Math.max(x - SPOT_A, 0);
      }
    };

    DATA = new ConvectionDiffusion2DPDEDataBundle(A, B, C, D, E, F, FunctionalDoublesSurface.from(payoff));
  }

  public void testAgaintBSPrice(final ConvectionDiffusionPDESolver2D solver, final int timeSteps, final int spotASteps, final int spotBSteps) {

    final double[][] res = solver.solve(DATA, timeSteps, spotASteps, spotBSteps, T, A_LOWER, A_UPPER, B_LOWER, B_UPPER);

    // for (int i = 0; i <= spotASteps; i++) {
    // for (int j = 0; j <= spotBSteps; j++) {
    // System.out.print(res[i][j] + "\t");
    // }
    // System.out.print("\n");
    // }

    final double vol = Math.sqrt(VOL_A * VOL_A + VOL_B * VOL_B - 2 * RHO * VOL_A * VOL_B);
    final double forward = SPOT_A / SPOT_B;
    final double strike = 1.0;
    final BlackFunctionData data = new BlackFunctionData(forward, SPOT_B, vol);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, T, true);
    final BlackPriceFunction pricer = new BlackPriceFunction();
    final Function1D<BlackFunctionData, Double> func = pricer.getPriceFunction(option);
    final double price = func.evaluate(data);

    final double pdfPrice = res[(int) (SPOT_A * spotASteps / (A_UPPER.getLevel() - A_LOWER.getLevel()))][(int) (SPOT_B * spotBSteps / (B_UPPER.getLevel() - B_LOWER.getLevel()))];

    // System.out.println(price+"\t"+pdfPrice);

    assertEquals(price, pdfPrice, 1e-1);

  }

}
