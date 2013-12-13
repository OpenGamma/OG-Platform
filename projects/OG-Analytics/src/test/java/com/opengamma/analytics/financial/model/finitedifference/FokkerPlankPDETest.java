/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.applications.InitialConditionsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Project forward the density for a stock price using the Fokker-Plank equation and use the result to price a call option
 */
@Test(groups = TestGroup.UNIT)
public class FokkerPlankPDETest {

  private static final PDE1DCoefficientsProvider PDE_DATA_PROVIDER = new PDE1DCoefficientsProvider();
  private static final InitialConditionsProvider INITIAL_CONDITION_PROVIDER = new InitialConditionsProvider();
  //private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final EuropeanVanillaOption OPTION;

  // private static NormalDistribution NORMAL;
  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;

  private static final double SPOT = 100;
  private static final double STRIKE = 110;
  //private static final double FORWARD;
  private static final double T = 5.0;
  private static final double RATE = 0.05;
  private static final YieldAndDiscountCurve YIELD_CURVE = YieldCurve.from(ConstantDoublesCurve.from(RATE));
  private static final double ATM_VOL = 0.20;
  private static final Function1D<Double, Double> INITAL_CONDITION = INITIAL_CONDITION_PROVIDER.getLogNormalDensity(SPOT, 0.001, ATM_VOL);
  private static final ConvectionDiffusionPDE1DFullCoefficients DATA;
  private static final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> PDE_DATA_BUNDLE;
  private static final int T_NODES = 100;/*TODO this needs more time steps (up from 30) to pass time now using ExtendedThetaMethodFiniteDifference. Can be better to express problem in terms 
                                         in terms of ThetaMethodFiniteDifference (with ConvectionDiffusionPDEDataBundle)*/
  private static final int X_NODES = 101;

  static {

    OPTION = new EuropeanVanillaOption(STRIKE, T, true);
    DATA = PDE_DATA_PROVIDER.getFokkerPlank(ConstantDoublesCurve.from(RATE), new LocalVolatilitySurfaceStrike(ConstantDoublesSurface.from(ATM_VOL)));
    LOWER = new DirichletBoundaryCondition(0.0, 0.0);
    UPPER = new DirichletBoundaryCondition(0.0, 10.0 * SPOT);

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, T_NODES, 5.0);
    //MeshingFunction spaceMesh = new ExponentalMeshing(LOWER.getLevel(), UPPER.getLevel(), xNodes, 0.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), SPOT, X_NODES, 0.01);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    PDE_DATA_BUNDLE = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(DATA, INITAL_CONDITION, LOWER, UPPER, grid);
  }

  @Test
  public void test() {
    final ThetaMethodFiniteDifference solver = new ThetaMethodFiniteDifference(1.0, true);

    final PDEFullResults1D res = (PDEFullResults1D) solver.solve(PDE_DATA_BUNDLE);

    // PDEUtilityTools.printSurface("", res);

    final NormalDistribution dist = new NormalDistribution((RATE - ATM_VOL * ATM_VOL / 2) * T, ATM_VOL * Math.sqrt(T));
    double pdf;
    double s;
    for (int i = 0; i < X_NODES; i++) {
      s = res.getSpaceValue(i);
      if (s == 0.0) {
        pdf = 0.0;
      } else {
        final double x = Math.log(s / SPOT);
        pdf = dist.getPDF(x) / s;
      }
      //  System.out.println(res.getSpaceValue(i) + "\t" + pdf + "\t" + res.getFunctionValue(i));
      assertEquals("PDF test", pdf, res.getFunctionValue(i), 1e-4);
    }

    final double k = STRIKE;
    final double df = YIELD_CURVE.getDiscountFactor(T);

    double sum = 0.0;
    double s1, s2, rho1, rho2;
    s1 = res.getSpaceValue(0);
    rho1 = res.getFunctionValue(0);
    for (int i = 1; i < X_NODES; i++) {
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

    final double price = df * sum;

    final BlackFunctionData data = new BlackFunctionData(SPOT / df, df, ATM_VOL);
    final Function1D<BlackFunctionData, Double> pricer = BLACK_FUNCTION.getPriceFunction(OPTION);
    final double bs_price = pricer.evaluate(data);
    assertEquals("Option price test", bs_price, price, 2e-2 * bs_price);//TODO This is not very accurate 

  }
}
