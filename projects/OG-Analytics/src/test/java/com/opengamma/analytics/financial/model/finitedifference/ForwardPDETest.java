/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.applications.InitialConditionsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.test.TestGroup;

/**
 * Test the forward parabolic PDE for a option price - i.e. gives an option price surface for maturity and strike (for a fixed "now" time and
 * spot). Since all strikes and maturities are priced with a single pass of the solver, this is very useful for calibrating to market prices.
 * By contrast the backwards PDE gives the price surface for time-to-maturity and spot (for a fixed maturity and strike), so a separate solver
 * will need to be run for each maturity and strike. However the greeks (in particular, delta, gamma and theta) can be read straight off
 * the backwards PDE.
 */
@Test(groups = TestGroup.UNIT)
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

  private static Surface<Double, Double, Double> ZERO_SURFACE = ConstantDoublesSurface.from(0.0);

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

    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<>(PDE, INT_COND, LOWER, UPPER, grid);
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

  @Test
  (enabled = false)
  public void debugTest() {

    final Function<Double, Double> lvFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tm) {
        Validate.isTrue(tm.length == 2);
        final double t = tm[0];
        final double m = tm[1];
        final double x = Math.log(m);
        if (Math.abs(x) > Math.sqrt(t) * 1.2) {
          return 0.0;
        }
        return 0.4;
      }
    };
    final LocalVolatilitySurfaceMoneyness lv = new LocalVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(lvFunc), new ForwardCurve(1.0));

    final Function1D<Double, Double> initCon = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        return Math.max(0, 1 - Math.exp(x));
      }
    };

    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.50, true);
    final ConvectionDiffusionPDE1DStandardCoefficients pde = getForwardLocalVol(lv);
    final double xMin = -2.5;
    final double xMax = 2.5;

    PDEUtilityTools.printSurface("lv", lv.getSurface(), 0, 2.0, Math.exp(xMin), Math.exp(xMax));

    final DirichletBoundaryCondition lower = new DirichletBoundaryCondition(initCon.evaluate(xMin), xMin);
    final DirichletBoundaryCondition upper = new DirichletBoundaryCondition(initCon.evaluate(xMax), xMax);
    final MeshingFunction timeMesh = new ExponentialMeshing(0, 2.0, 12, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(xMin, xMax, 17, 0.0);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, initCon, lower, upper, grid);

    final PDEFullResults1D prices = (PDEFullResults1D) solver.solve(db);
    PDEUtilityTools.printSurface("prices", prices);
  }

  @Test
  public void flatVolTest() {

    final double spot = 10.0;
    final double r = 0.1;
    final double y = 0.0;
    final double vol = 0.3;
    final double expiry = 2.0;
    final double mult = Math.exp(Math.sqrt(expiry) * vol * 6.0);

    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, true);
    // ConvectionDiffusionPDESolver solver = new RichardsonExtrapolationFiniteDifference(base);

    final int tNodes = 51;
    final int xNodes = 101;

    final ConvectionDiffusionPDE1DStandardCoefficients pde = PDE_DATA_PROVIDER.getForwardBlackSholes(r, y, vol);
    final Function1D<Double, Double> intCon = INITIAL_COND_PROVIDER.getForwardCallPut(spot, true);
    //only true if y =0
    final BoundaryCondition lower = new DirichletBoundaryCondition(spot, 0);
    final BoundaryCondition upper = new DirichletBoundaryCondition(0, mult * spot);

    final MeshingFunction timeMesh = new ExponentialMeshing(0, expiry, tNodes, 3.0);
    //final MeshingFunction spaceMesh = new ExponentialMeshing(0, 5 * spot, xNodes, 0.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0, mult * spot, spot, xNodes, 0.05);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    //    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, intCon, lower, upper, grid);
    //    final PDEFullResults1D res = (PDEFullResults1D) solver.solve(db);

    final double[][] price = new double[tNodes][xNodes];
    for (int i = 0; i < tNodes; i++) {
      final double t = grid.getTimeNode(i);
      final double df = Math.exp(-r * t);
      final double fwd = spot * Math.exp((r - y) * t);
      for (int j = 0; j < xNodes; j++) {
        price[i][j] = df * BlackFormulaRepository.price(fwd, grid.getSpaceNode(j), t, vol, true);
      }
    }

    //    double k = grid.getSpaceNode(1);
    //    double pdePrice = res.getFunctionValue(1, 1);
    //    double analPrice = price[1][1];
    //    double t = grid.getTimeNode(1);
    //    double fPrice = pdePrice * Math.exp(r * t);
    //    double fwd = spot * Math.exp(r * t);
    //    double iv = BlackFormulaRepository.impliedVolatility(fPrice, fwd, k, t, true);
    //
    //    System.out.println("debug " + grid.getSpaceNode(1) + "\t" + price[1][1] + "\t" + res.getFunctionValue(1, 1) + "\t" + iv);

    //    PDEUtilityTools.printSurface("PDE res", res);
    //    PDEUtilityTools.printSurface("call price", price, grid.getSpaceNodes(), grid.getTimeNodes(), System.out);
    //
    //    Map<DoublesPair, Double> iv = PDEUtilityTools.priceToImpliedVol(new ForwardCurve(spot, r), new YieldCurve("", ConstantDoublesCurve.from(r)), res, 0, 2.0, 0.0, mult * spot);
    //    GridInterpolator2D gridIn = new GridInterpolator2D(Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE, Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE);
    //    Map<Double, Interpolator1DDataBundle> idb = gridIn.getDataBundle(iv);
    //    PDEUtilityTools.printSurface("iv", idb, 0.1, 2.0, 0.7, 3 * spot, 100, 100);
  }

  private ConvectionDiffusionPDE1DStandardCoefficients getForwardLocalVol(final LocalVolatilitySurfaceMoneyness localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        Validate.isTrue(tx.length == 2);
        final double t = tx[0];
        final double x = tx[1];

        final double vol = localVol.getVolatilityForMoneyness(t, Math.exp(x));
        return -0.5 * vol * vol;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        return -a.evaluate(tx);
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ZERO_SURFACE);
  }

}
