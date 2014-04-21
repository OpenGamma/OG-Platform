/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.applications.InitialConditionsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.PriceSurface;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DupireLocalVolatilityTest {

  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final CombinedInterpolatorExtrapolator EXTRAPOLATOR_1D = new CombinedInterpolatorExtrapolator(INTERPOLATOR_1D, new FlatExtrapolator1D());
  @SuppressWarnings("unused")
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(EXTRAPOLATOR_1D, EXTRAPOLATOR_1D);

  private static final DupireLocalVolatilityCalculator DUPIRE = new DupireLocalVolatilityCalculator();
  private static final SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();
  private static final double SPOT = 0.04;
  private static final double STRIKE = 0.05;
  private static final double EXPIRY = 5.0;
  private static final double ATM_VOL = 0.2;
  private static final double ALPHA;
  private static final double BETA = 0.5;
  private static final double RHO = -0.2;
  private static final double NU = 0.3;
  //  private static final SABRFormulaData SABR_DATA;
  private static final double RATE = 0.05; //turn back to 5%
  private static final double YIELD = 0.02;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, RATE - YIELD);

  private static final PriceSurface PRICE_SURFACE;
  private static final BlackVolatilitySurfaceStrike SABR_SURFACE;
  private static AbsoluteLocalVolatilitySurface ABS_LOCAL_VOL;
  private static LocalVolatilitySurfaceStrike LOCAL_VOL;
  /**
   *
   */
  static {
    ALPHA = ATM_VOL * Math.pow(SPOT, 1 - BETA);
    //   SABR_DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);

    final Function<Double, Double> sabrSurface = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        final SABRFormulaData sabrdata = new SABRFormulaData(ALPHA, BETA, RHO, NU);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        final Function1D<SABRFormulaData, Double> func = SABR.getVolatilityFunction(option, FORWARD_CURVE.getForward(t));
        return func.evaluate(sabrdata);
      }
    };

    SABR_SURFACE = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(sabrSurface));

    final Function<Double, Double> priceSurface = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        final double sigma = sabrSurface.evaluate(tk);
        final double df = Math.exp(-RATE * t);
        @SuppressWarnings("synthetic-access")
        final double price = BlackFormulaRepository.price(FORWARD_CURVE.getForward(t), k, t, sigma, true);
        if (Double.isNaN(price)) {
          System.out.println("Error");
        }
        return price * df;
      }
    };

    PRICE_SURFACE = new PriceSurface(FunctionalDoublesSurface.from(priceSurface));

    LOCAL_VOL = DUPIRE.getLocalVolatility(SABR_SURFACE, FORWARD_CURVE);
    ABS_LOCAL_VOL = DUPIRE.getAbsoluteLocalVolatilitySurface(SABR_SURFACE, SPOT, RATE);

  }

  @Test
  public void testImpliedVolCal() {
    final LocalVolatilitySurfaceStrike lv = DUPIRE.getLocalVolatility(PRICE_SURFACE, SPOT, RATE, YIELD);
    final double vol1 = lv.getVolatility(EXPIRY, STRIKE);
    final double vol2 = LOCAL_VOL.getVolatility(EXPIRY, STRIKE);
    assertEquals(vol1, vol2, 1e-6);
  }

  @Test
  public void testImpliedVolMoneynessCal() {
    final LocalVolatilitySurfaceStrike lv = DUPIRE.getLocalVolatility(PRICE_SURFACE, SPOT, RATE, YIELD);
    final double vol1 = lv.getVolatility(EXPIRY, STRIKE);
    final BlackVolatilitySurfaceMoneyness miv = BlackVolatilitySurfaceConverter.toMoneynessSurface(SABR_SURFACE, FORWARD_CURVE);
    final LocalVolatilitySurfaceMoneyness lvm = DUPIRE.getLocalVolatility(miv);
    final double vol2 = lvm.getVolatility(EXPIRY, STRIKE);
    assertEquals(vol1, vol2, 1e-6);
  }

  @Test(enabled = false)
  public void printSurfaces() {
    final SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    final double k = 0.01;
    for (int i = 0; i < 10; i++) {
      final double t = 0.5 + i * 20 / 9.;
      final double vol1 = SABR_SURFACE.getVolatility(t, k);
      final double vol2 = sabr.getVolatility(SPOT, k, t, ALPHA, BETA, RHO, NU);
      System.out.println(t + "\t" + vol1 + "\t" + vol2);
    }
    PDEUtilityTools.printSurface("Imp Vol", SABR_SURFACE.getSurface(), 0., 5., 0.1 * SPOT, 3 * SPOT);
    PDEUtilityTools.printSurface("Loc Vol", LOCAL_VOL.getSurface(), 0., 5., 0.1 * SPOT, 3 * SPOT);
    PDEUtilityTools.printSurface("ABs Loc Vol", ABS_LOCAL_VOL.getSurface(), 0., 5., 0.1 * SPOT, 3 * SPOT);
  }

  @Test
  public void pdePriceTest() {
    final PDE1DCoefficientsProvider pde_provider = new PDE1DCoefficientsProvider();
    final InitialConditionsProvider int_provider = new InitialConditionsProvider();
    //final ZZConvectionDiffusionPDEDataBundle db = provider.getBackwardsLocalVol(STRIKE, EXPIRY, true, LOCAL_VOL, FORWARD_CURVE);
    final ConvectionDiffusionPDE1DCoefficients pde = pde_provider.getBackwardsLocalVol(FORWARD_CURVE, EXPIRY, LOCAL_VOL);
    final Function1D<Double, Double> payoff = int_provider.getEuropeanPayoff(STRIKE, true);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final double forward = FORWARD_CURVE.getForward(EXPIRY);

    final int nTimeNodes = 50;
    final int nSpotNodes = 100;
    final double upperLevel = 3.5 * forward;

    final BoundaryCondition lower = new DirichletBoundaryCondition(0, 0);
    final BoundaryCondition upper = new NeumannBoundaryCondition(1.0, upperLevel, false);
    final MeshingFunction timeMesh = new ExponentialMeshing(0.0, EXPIRY, nTimeNodes, 6.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(0, upperLevel, STRIKE, nSpotNodes, 0.05);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDEResults1D res = solver.solve(new PDE1DDataBundle<>(pde, payoff, lower, upper, grid));

    final int fwdIndex = grid.getLowerBoundIndexForSpace(forward);
    final double[] fwd = new double[4];
    final double[] vol = new double[4];
    for (int i = 0; i < 4; i++) {
      fwd[i] = grid.getSpaceNode(i + fwdIndex - 1);
      final double price = res.getFunctionValue(i + fwdIndex - 1);
      vol[i] = BlackFormulaRepository.impliedVolatility(price, fwd[i], STRIKE, EXPIRY, true);
    }
    final Interpolator1DDoubleQuadraticDataBundle idb = INTERPOLATOR_1D.getDataBundle(fwd, vol);

    final double sabrVol = SABR_SURFACE.getVolatility(EXPIRY, STRIKE);
    final double modelVol = INTERPOLATOR_1D.interpolate(idb, forward);
    assertEquals("Volatility test", sabrVol, modelVol, 1e-4); //1bps error
  }

  @Test(enabled = false)
  public void printPriceTest() {

    double t;
    double k;
    double price;

    for (int j = 0; j < 101; j++) {
      t = 0.01 + 5.0 * j / 100.0;
      System.out.print("\t" + t);
    }
    System.out.print("\n");

    for (int i = 0; i < 101; i++) {
      k = 0.001 + 0.15 * i / 100.0;
      System.out.print(k);
      for (int j = 0; j < 101; j++) {
        t = 0.01 + 5.0 * j / 100.0;
        price = PRICE_SURFACE.getPrice(t, k);
        System.out.print("\t" + price);
      }
      System.out.print("\n");
    }
  }

  @Test(enabled = false)
  public void priceTest() {
    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();
    final LocalVolatilitySurfaceStrike locVol = cal.getLocalVolatility(PRICE_SURFACE, SPOT, RATE, 0.0);
    double t;
    double f;
    double vol;

    for (int j = 0; j < 101; j++) {
      t = 0.01 + 5.0 * j / 100.0;
      System.out.print("\t" + t);
    }
    System.out.print("\n");

    for (int i = 0; i < 101; i++) {
      f = 0.001 + 0.15 * i / 100.0;
      System.out.print(f);
      for (int j = 0; j < 101; j++) {
        t = 0.01 + 5.0 * j / 100.0;
        vol = locVol.getVolatility(t, f);
        System.out.print("\t" + vol);
      }
      System.out.print("\n");
    }
  }

  @SuppressWarnings("deprecation")
  @Test(enabled = false)
  public void volTest() {
    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();
    final LocalVolatilitySurfaceStrike locVol = cal.getLocalVolatility(SABR_SURFACE, SPOT, RATE);
    double t;
    double f;
    double vol;

    for (int j = 0; j < 101; j++) {
      t = 0.01 + 5.0 * j / 100.0;
      System.out.print("\t" + t);
    }
    System.out.print("\n");

    for (int i = 0; i < 101; i++) {
      f = 0.001 + 0.15 * i / 100.0;
      System.out.print(f);
      for (int j = 0; j < 101; j++) {
        t = 0.01 + 5.0 * j / 100.0;
        vol = locVol.getVolatility(t, f);
        System.out.print("\t" + vol);
      }
      System.out.print("\n");
    }
  }

}
