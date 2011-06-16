/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.time.DateUtil;

/**
 * Test the forward parabolic PDE for a option price - i.e. gives an option price surface for maturity and strike (for a fixed "now" time and
 * spot). Since all strikes and maturities are priced with a single pass of the solver, this is very useful for calibrating to market prices.
 * By contrast the backwards PDE gives the price surface for time-to-maturity and spot (for a fixed maturity and strike), so a separate solver 
 * will need to be run for each maturity and strike. However the greeks (in particular, delta, gamma and theta) can be read straight off
 * the backwards PDE. 
 */
public class ForwardPDETest {

  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BS_MODEL = new BlackScholesMertonModel();

  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;

  private static final double SPOT = 100;
  private static final double T = 5.0;
  private static final double RATE = 0.05;// TODO change back to 5%
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double ATM_VOL = 0.20;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final ConvectionDiffusionPDEDataBundle DATA;

  private static Surface<Double, Double, Double> A;
  private static Surface<Double, Double, Double> B;
  private static Surface<Double, Double, Double> C;
  @SuppressWarnings("unused")
  private static Surface<Double, Double, Double> ZERO_SURFACE;
  private static VolatilitySurface VOL_SURFACE;

  private static boolean ISCALL = true;

  static {

    VOL_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(ATM_VOL));

    Function1D<Double, Double> strikeZeroPrice = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        if (ISCALL) {
          return SPOT;
        } else {
          return 0.0;
        }
      }
    };

    LOWER = new DirichletBoundaryCondition(strikeZeroPrice, 0.0);

    if (ISCALL) {
      UPPER = new DirichletBoundaryCondition(0, 10.0 * SPOT * Math.exp(T * RATE));
      // UPPER = new NeumannBoundaryCondition(0.0, 10.0 * SPOT * Math.exp(T * RATE), false);
    } else {
      UPPER = new NeumannBoundaryCondition(1.0, 10.0 * SPOT * Math.exp(T * RATE), false);
    }

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        double k = tk[1];
        return -k * k * ATM_VOL * ATM_VOL / 2;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        double k = tk[1];
        return k * RATE;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return 0.0;
      }
    };

    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double k) {
        if (ISCALL) {
          return Math.max(0, SPOT - k);
        } else {
          return Math.max(0, k - SPOT);
        }
      }
    };

    A = FunctionalDoublesSurface.from(a);
    B = FunctionalDoublesSurface.from(b);
    C = FunctionalDoublesSurface.from(c);

    DATA = new ConvectionDiffusionPDEDataBundle(A, B, C, initialCondition);

  }

  @Test
  public void testBlackScholes() {

    final boolean print = false;
    ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, true);
    // ConvectionDiffusionPDESolver solver = new RichardsonExtrapolationFiniteDifference(base);

    int tNodes = 51;
    int xNodes = 101;

    MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 5.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), SPOT, xNodes, 0.01);
    // MeshingFunction spaceMesh = new ExponentalMeshing(LOWER.getLevel(), UPPER.getLevel(), xNodes, 0.0);

    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    PDEFullResults1D res = (PDEFullResults1D) solver.solve(DATA, grid, LOWER, UPPER);

    double t, k;
    double price;
    double impVol;
    double lowerK, upperK;
    double tMin = 0.02;

    if (print) {
      for (int i = 0; i < xNodes; i++) {
        System.out.print("\t" + res.getSpaceValue(i));
      }
      System.out.print("\n");
    }

    for (int j = 0; j < tNodes; j++) {
      t = res.getTimeValue(j);
      double df = YIELD_CURVE.getDiscountFactor(t);
      BlackFunctionData data = new BlackFunctionData(SPOT / df, df, 0.0);
      lowerK = SPOT * Math.exp((RATE - ATM_VOL * ATM_VOL / 2) * t - ATM_VOL * Math.sqrt(t) * 3);
      upperK = SPOT * Math.exp((RATE - ATM_VOL * ATM_VOL / 2) * t + ATM_VOL * Math.sqrt(t) * 3);
      if (print) {
        System.out.print(t);
      }
      for (int i = 0; i < xNodes; i++) {
        k = res.getSpaceValue(i);
        price = res.getFunctionValue(i, j);
        EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, ISCALL);
        if (k > lowerK && k < upperK && t > tMin) {
          try {
            impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
          } catch (Exception e) {
            impVol = 0.0;
          }
          assertEquals(ATM_VOL, impVol, 1e-2);
          if (print) {
            System.out.print("\t" + impVol);
          }
        } else {
          if (print) {
            System.out.print("\t" + ATM_VOL);
          }
        }

      }
      if (print) {
        System.out.print("\n");
      }
    }
  }

}
