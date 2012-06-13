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
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Test the forward parabolic PDE for a option price - i.e. gives an option price surface for maturity and strike (for a fixed "now" time and
 * spot). Since all strikes and maturities are priced with a single pass of the solver, this is very useful for calibrating to market prices.
 * By contrast the backwards PDE gives the price surface for time-to-maturity and spot (for a fixed maturity and strike), so a separate solver 
 * will need to be run for each maturity and strike. However the greeks (in particular, delta, gamma and theta) can be read straight off
 * the backwards PDE. 
 */
public class ForwardPDETest {

  private static final PDE1DCoefficientsProvider PDE_DATA_PROVIDER = new PDE1DCoefficientsProvider();
  private static final InitialConditionsProvider INITIAL_COND_PROVIDER = new InitialConditionsProvider();

  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;

  private static final double SPOT = 100;
  private static final double T = 5.0;
  private static final double RATE = 0.05;// TODO change back to 5%
  private static final ForwardCurve FORWARD = new ForwardCurve(SPOT, RATE);

  private static final double ATM_VOL = 0.20;
  //private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  // private static final ZZConvectionDiffusionPDEDataBundle DATA;
  private static final ConvectionDiffusionPDE1DCoefficients PDE;
  private static final Function1D<Double, Double> INT_COND;

  @SuppressWarnings("unused")
  private static Surface<Double, Double, Double> ZERO_SURFACE;

  private static boolean ISCALL = true;

  static {

    final Function1D<Double, Double> strikeZeroPrice = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double t) {
        if (ISCALL) {
          return 1.0;
        }
        return 0.0;
      }
    };

    LOWER = new DirichletBoundaryCondition(strikeZeroPrice, 0.0);

    if (ISCALL) {
      UPPER = new DirichletBoundaryCondition(0, 10.0);
      // UPPER = new NeumannBoundaryCondition(0.0, 10.0 * SPOT * Math.exp(T * RATE), false);
    } else {
      UPPER = new NeumannBoundaryCondition(1.0, 10.0, false);
    }
    //  DATA = PDE_DATA_PROVIDER.getForwardLocalVol(FORWARD, 1.0, ISCALL, new AbsoluteLocalVolatilitySurface(ConstantDoublesSurface.from(ATM_VOL)));
    PDE = PDE_DATA_PROVIDER.getForwardLocalVol(FORWARD, new LocalVolatilitySurfaceStrike(ConstantDoublesSurface.from(ATM_VOL)));
    INT_COND = INITIAL_COND_PROVIDER.getForwardCallPut(ISCALL);
  }

  @Test
  public void testBlackScholes() {

    final boolean print = false;
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.55, true);
    // ConvectionDiffusionPDESolver solver = new RichardsonExtrapolationFiniteDifference(base);

    final int tNodes = 51;
    final int xNodes = 101;

    final MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 5.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), 1.0, xNodes, 0.01);
    // MeshingFunction spaceMesh = new ExponentalMeshing(LOWER.getLevel(), UPPER.getLevel(), xNodes, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(PDE, INT_COND, LOWER, UPPER, grid);
    final PDEFullResults1D res = (PDEFullResults1D) solver.solve(db);

    double t, x;
    double price;
    double impVol;
    double lowerX, upperX;
    final double tMin = 0.02;

    if (print) {
      for (int i = 0; i < xNodes; i++) {
        System.out.print("\t" + res.getSpaceValue(i));
      }
      System.out.print("\n");
    }

    for (int j = 0; j < tNodes; j++) {
      t = res.getTimeValue(j);
      lowerX = Math.exp((RATE - ATM_VOL * ATM_VOL / 2) * t - ATM_VOL * Math.sqrt(t) * 3);
      upperX = Math.exp((RATE - ATM_VOL * ATM_VOL / 2) * t + ATM_VOL * Math.sqrt(t) * 3);
      if (print) {
        System.out.print(t);
      }
      for (int i = 0; i < xNodes; i++) {
        x = res.getSpaceValue(i);
        price = res.getFunctionValue(i, j);
        if (x > lowerX && x < upperX && t > tMin) {
          try {
            impVol = BlackFormulaRepository.impliedVolatility(price, 1.0, x, t, ISCALL);
          } catch (final Exception e) {
            impVol = 0.0;
          }
          assertEquals(ATM_VOL, impVol, 1e-2);
          if (print) {
            System.out.print("\t" + impVol);
          }
        } else {
          if (print) {
            System.out.print("\t" + "");
          }
        }

      }
      if (print) {
        System.out.print("\n");
      }
    }
  }

}
