/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class FokkerPlankPDETest {

  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final EuropeanVanillaOption OPTION;

  private static NormalDistribution NORMAL;
  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;

  private static final double SPOT = 100;
  private static final double STRIKE = 110;
  //private static final double FORWARD;
  private static final double T = 5.0;
  private static final double RATE = 0.05;// TODO change back to 5%
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double ATM_VOL = 0.20;

  private static final ConvectionDiffusionPDEDataBundle DATA;

  private static Surface<Double, Double, Double> A;
  private static Surface<Double, Double, Double> B;

  private static Surface<Double, Double, Double> C;

  static {

    OPTION = new EuropeanVanillaOption(STRIKE, T, true);

    NORMAL = new NormalDistribution(0, 1);

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        // return -0.5 * 100;
        return -s * s * ATM_VOL * ATM_VOL / 2;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        //return RATE;
        return (RATE - 2 * ATM_VOL * ATM_VOL) * s;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        //return 0.0;
        return RATE - ATM_VOL * ATM_VOL;
      }
    };

    //using a normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {

      // double eta = SPOT / 100;
      double tOffset = 0.05;

      @Override
      public Double evaluate(Double s) {

        double x = Math.log(s / SPOT);
        NormalDistribution dist = new NormalDistribution((RATE - ATM_VOL * ATM_VOL / 2) * tOffset, ATM_VOL * Math.sqrt(tOffset));
        return dist.getPDF(x) / s;

        // return NORMAL.getPDF((x - SPOT) / eta) / eta;
        //        if (x < 90 || x > 110) {
        //          return 0.0;
        //        }
        //        if (x < 100) {
        //          return x - 90;
        //        }
        //        return 110 - x;
      }
    };

    A = FunctionalDoublesSurface.from(a);
    B = FunctionalDoublesSurface.from(b);
    C = FunctionalDoublesSurface.from(c);

    DATA = new ConvectionDiffusionPDEDataBundle(A, B, C, initialCondition);

    LOWER = new DirichletBoundaryCondition(0.0, 0.0);
    UPPER = new DirichletBoundaryCondition(0.0, 10.0 * SPOT);

  }

  @Test
  public void test() {
    ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(1.0, true);
    int tNodes = 30;
    int xNodes = 101;
    MeshingFunction timeMesh = new ExponentialMeshing(0, T - 0.01, tNodes, 0.0);
    //MeshingFunction spaceMesh = new ExponentalMeshing(LOWER.getLevel(), UPPER.getLevel(), xNodes, 0.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), SPOT, xNodes, 0.1);

    double[] timeGrid = new double[tNodes];
    for (int n = 0; n < tNodes; n++) {
      timeGrid[n] = timeMesh.evaluate(n);
    }

    double[] spaceGrid = new double[xNodes];
    for (int i = 0; i < xNodes; i++) {
      spaceGrid[i] = spaceMesh.evaluate(i);
    }

    PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);
    PDEFullResults1D res = (PDEFullResults1D) solver.solve(DATA, grid, LOWER, UPPER);

    for (int i = 0; i < xNodes; i++) {
      double x = Math.log(res.getSpaceValue(i) / SPOT);

      NormalDistribution dist = new NormalDistribution((RATE - ATM_VOL * ATM_VOL / 2) * T, ATM_VOL * Math.sqrt(T));
      double pdf = dist.getPDF(x) / res.getSpaceValue(i);
      //System.out.println(res.getSpaceValue(i) + "\t" + pdf + "\t" + res.getFunctionValue(i));
      assertEquals(pdf, res.getFunctionValue(i), 1e-4);
    }

    double k = STRIKE;
    double df = YIELD_CURVE.getDiscountFactor(T - 0.01);

    double sum = 0.0;
    double s1, s2, rho1, rho2;
    s1 = res.getSpaceValue(0);
    rho1 = res.getFunctionValue(0);
    for (int i = 1; i < xNodes; i++) {
      s2 = res.getSpaceValue(i);
      rho2 = res.getFunctionValue(i);
      if (s2 > k) {
        if (s1 > k) {
          sum += ((s1 - k) * rho1 + (s2 - k) * rho2) * (s2 - s1) / 2.0;
        } else {
          sum += rho2 / 2.0;
        }
      }
      s1 = s2;
      rho1 = rho2;
    }
    double price = df * sum;

    BlackFunctionData data = new BlackFunctionData(SPOT / df, df, ATM_VOL);
    Function1D<BlackFunctionData, Double> pricer = BLACK_FUNCTION.getPriceFunction(OPTION);
    double bs_price = pricer.evaluate(data);
    assertEquals(bs_price, price, 2e-2 * bs_price);

    //    for (int i = 0; i < xNodes; i++) {
    //      System.out.print("\t" + res.getSpaceValue(i));
    //    }
    //    System.out.print("\n");
    //
    //    for (int j = 0; j < tNodes; j++) {
    //      System.out.print(res.getTimeValue(j));
    //      for (int i = 0; i < xNodes; i++) {
    //        System.out.print("\t" + res.getFunctionValue(i, j));
    //      }
    //      System.out.print("\n");
    //    }
  }
}
