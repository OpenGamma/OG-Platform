/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.model.finitedifference.applications.PDEDataBundleProvider;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.surface.AbsoluteLocalVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.financial.model.volatility.surface.DupireLocalVolatilityCalculator;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurfaceConverter;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurfaceMoneyness;
import com.opengamma.financial.model.volatility.surface.LocalVolatilitySurfaceStrike;
import com.opengamma.financial.model.volatility.surface.PriceSurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@Test
@SuppressWarnings("unused")
public class SABRFiniteDifferenceTest {
  private static final boolean DEBUG = false; //set to false before commit

  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final PDEDataBundleProvider PDE_DATA_PROVIDER = new PDEDataBundleProvider();

  private static final BlackPriceFunction BLACK_PRICE_FUNCTION = new BlackPriceFunction();
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();
  private static final double SPOT = 0.04;
  private static final double STRIKE;
  private static final Function1D<Double, Double> ATM_VOL;
  private static final double BETA = 0.5;
  private static final double RHO = -0.6;
  // private static final double NU = 0.5;
  private static final Function1D<Double, Double> NU;
  private static final double RATE = 0.02;
  private static final double DRIFT = 0.07;
  private static final double T = 5.0;
  private static final ForwardCurve FORWARD_CURVE;
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final EuropeanVanillaOption OPTION;
  private static final ConvectionDiffusionPDEDataBundle DATA;

  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;

  private static final PriceSurface SABR_PRICE_SURFACE;
  private static final BlackVolatilitySurfaceStrike SABR_VOL_SURFACE;
  private static final BlackVolatilitySurfaceMoneyness SABR_VOL_M_SURFACE;
  private static final LocalVolatilitySurfaceStrike SABR_LOCAL_VOL;
  private static final LocalVolatilitySurfaceMoneyness LV_M;
  private static final AbsoluteLocalVolatilitySurface SABR_ABS_LOCAL_VOL;
  /**
   * 
   */
  static {
    ATM_VOL = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double t) {
        return (0.05 + 0.1 * t) * Math.exp(-0.3 * t) + 0.2;
      }
    };

    NU = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        return 0.3 * Math.exp(-0.2 * t) + 0.2;
      }
    };

    Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double t) {
        return SPOT * Math.exp(t * DRIFT); //* (1 + 0.3 * (1 - Math.exp(-0.3 * t)));
      }
    };

    FORWARD_CURVE = new ForwardCurve(func);

    SABR_VOL_SURFACE = getSABRImpliedVolSurface(BETA, FORWARD_CURVE);

    final Function<Double, Double> sabrMSurface = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final double f = FORWARD_CURVE.getForward(t);
        final double k = x * f;
        final double alpha = ATM_VOL.evaluate(t) * Math.pow(f, 1 - BETA);
        final double nu = NU.evaluate(t);
        final SABRFormulaData sabrdata = new SABRFormulaData(alpha, BETA, RHO, nu);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        final Function1D<SABRFormulaData, Double> func1 = SABR.getVolatilityFunction(option, f);
        return func1.evaluate(sabrdata);
      }
    };

    SABR_VOL_M_SURFACE = new BlackVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(sabrMSurface), FORWARD_CURVE);

    SABR_PRICE_SURFACE = getSABRPriceSurface(BETA, FORWARD_CURVE, YIELD_CURVE);

    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();

    //    Function<Double, Double> dummySurface = new Function<Double, Double>() {
    //
    //      @Override
    //      public Double evaluate(Double... tk) {
    //        double t = tk[0];
    //        double k = tk[1];
    //        return 0.15 + 4.0 * FunctionUtils.square(Math.log(k / SPOT)) / Math.sqrt(1 + 10 * t);
    //      }
    //    };
    //
    //    SABR_LOCAL_VOL = new LocalVolatilitySurface(FunctionalDoublesSurface.from(dummySurface));
    SABR_LOCAL_VOL = getSABRLocalVolSurface(BETA, FORWARD_CURVE);

    LV_M = cal.getLocalVolatility(SABR_VOL_M_SURFACE);
    SABR_ABS_LOCAL_VOL = cal.getAbsoluteLocalVolatilitySurface(SABR_VOL_SURFACE, SPOT, RATE);

    STRIKE = SPOT / YIELD_CURVE.getDiscountFactor(T);

    OPTION = new EuropeanVanillaOption(STRIKE, T, true);

    LOWER = new DirichletBoundaryCondition(0, 0.0);
    UPPER = new NeumannBoundaryCondition(1.0, 5 * FORWARD_CURVE.getForward(T), false);

    DATA = PDE_DATA_PROVIDER.getBackwardsLocalVol(STRIKE, T, true, SABR_LOCAL_VOL, FORWARD_CURVE);
  }

  /**
   * Run a backwards PDE once for the example strike and maturity, using the local volatility derived from the SABR
   * implied volatility surface, and check the price agrees with SABR.
   */
  @Test
  public void testBackwardsSingalStrike() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final int tNodes = 20;
    final int xNodes = 101;
    final PDEGrid1D grid = new PDEGrid1D(tNodes, xNodes, T, LOWER.getLevel(), UPPER.getLevel());
    final PDEResults1D res = solver.solve(DATA, grid, LOWER, UPPER);

    double fwd0 = FORWARD_CURVE.getForward(T);
    final double df = YIELD_CURVE.getDiscountFactor(T);
    final int i = (int) (xNodes * fwd0 / UPPER.getLevel());
    final double fwd = res.getSpaceValue(i);
    final double price = df * res.getFunctionValue(i);

    assertEquals("forward", fwd0, fwd, 1e-9);
    assertEquals("price", SABR_PRICE_SURFACE.getPrice(T, STRIKE), price, price * 2e-3);

    final BlackFunctionData data = new BlackFunctionData(FORWARD_CURVE.getForward(T), df, 0.0);

    double impVol, impVol2;
    try {
      impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
    } catch (final Exception e) {
      impVol = 0.0;
    }
    assertEquals("vol", SABR_VOL_SURFACE.getVolatility(T, STRIKE), impVol, 1e-3);
  }

  /**
   * Run a classic (i.e. defined in terms of instantaneous short rates) backwards PDE once for the example strike and maturity, using the local volatility derived from the SABR
   * implied volatility surface, and check the price agrees with SABR.
   */
  @Test
  public void testClassicBackwardsSingalStrike() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final int tNodes = 20;
    final int xNodes = 101;
    BoundaryCondition lower = new DirichletBoundaryCondition(0, 0.0);
    BoundaryCondition upper = new NeumannBoundaryCondition(new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double tau) {
        return Math.exp(-tau * (RATE - DRIFT));
      }
    }, 5 * SPOT, false);

    final PDEGrid1D grid = new PDEGrid1D(tNodes, xNodes, T, lower.getLevel(), upper.getLevel());
    ConvectionDiffusionPDEDataBundle pdeData = PDE_DATA_PROVIDER.getBackwardsLocalVol(RATE, RATE - DRIFT, STRIKE, T, true, SABR_LOCAL_VOL);
    final PDEResults1D res = solver.solve(pdeData, grid, lower, upper);

    final double df = YIELD_CURVE.getDiscountFactor(T);
    final int i = (int) (xNodes * SPOT / upper.getLevel());
    final double s = res.getSpaceValue(i);
    final double price = res.getFunctionValue(i);

    assertEquals("spot", SPOT, s, 1e-9);
    assertEquals("price", SABR_PRICE_SURFACE.getPrice(T, STRIKE), price, price * 2e-3);

    final BlackFunctionData data = new BlackFunctionData(FORWARD_CURVE.getForward(T), df, 0.0);

    double impVol, impVol2;
    try {
      impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
    } catch (final Exception e) {
      impVol = 0.0;
    }
    assertEquals("vol", SABR_VOL_SURFACE.getVolatility(T, STRIKE), impVol, 1e-3);
  }

  /**
   * Run a backwards PDE multiple time at different strikes for maturity, using the local volatility derived from the
   * SABR implied volatility surface, and check the price agrees with SABR.
   */
  @Test
  public void testBackwardsMultipleStrikes() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final int tNodes = 50;
    final int xNodes = 101;

    final double forward = FORWARD_CURVE.getForward(T);
    final double maxForward = 5.0 * forward;

    BoundaryCondition lower = new DirichletBoundaryCondition(0.0, 0.0);

    MeshingFunction timeMesh = new ExponentialMeshing(0.0, T, tNodes, 5.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, maxForward, forward, xNodes, 0.1);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    System.out.println("backwards PDE");
    for (int j = 0; j < 50; j++) {
      double strike = forward * (0.5 + 1.5 * j / 49.);
      BoundaryCondition upper = new DirichletBoundaryCondition(maxForward - strike, maxForward);
      ConvectionDiffusionPDEDataBundle pdeData = PDE_DATA_PROVIDER.getBackwardsLocalVol(strike, T, true, SABR_LOCAL_VOL, FORWARD_CURVE);
      PDEResults1D res = solver.solve(pdeData, grid, lower, upper);

      //since the grid of forwards values may not contain the desired forward, we interpolate
      double[] tFwd = new double[xNodes];
      double[] tVol = new double[xNodes];
      int count = 0;
      for (int i = 0; i < xNodes; i++) {
        double fwd = grid.getSpaceNode(i);
        if (fwd > 0.8 * forward && fwd < 1.2 * forward) {
          double price = res.getFunctionValue(i);
          double vol = BlackFormulaRepository.impliedVolatility(price, fwd, strike, T, true);
          tFwd[count] = fwd;

          tVol[count] = vol;
          count++;
        }
      }
      double[] fwds = new double[count];
      double[] vols = new double[count];
      System.arraycopy(tFwd, 0, fwds, 0, count);
      System.arraycopy(tVol, 0, vols, 0, count);
      Interpolator1DDoubleQuadraticDataBundle db = INTERPOLATOR_1D.getDataBundle(fwds, vols);
      double intepVol = INTERPOLATOR_1D.interpolate(db, forward);
      double sabrVol = SABR_VOL_SURFACE.getVolatility(T, strike);
      if (DEBUG) {
        System.out.println(strike + "\t" + intepVol + "\t" + sabrVol);
      } else {
        assertEquals("Backwards PDE vols", sabrVol, intepVol, 1.5e-3); //15bps error TODO why so large
      }
    }
  }

  /**
   * Run a forwards PDE once, using the local volatility derived from the
   * SABR implied volatility surface, and check the prices at the example expiry and strikes agrees with SABR.
   */
  @Test
  public void testForwardPDE() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final int tNodes = 50;
    final int xNodes = 101;

    final double maxMoneyness = 6.0;

    ConvectionDiffusionPDEDataBundle pdeData = PDE_DATA_PROVIDER.getForwardLocalVol(SABR_LOCAL_VOL, FORWARD_CURVE, true);

    BoundaryCondition lower = new DirichletBoundaryCondition(1.0, 0.0);
    // BoundaryCondition upper = new DirichletBoundaryCondition(0.0, maxMoneyness);
    BoundaryCondition upper = new NeumannBoundaryCondition(0, maxMoneyness, false);

    MeshingFunction timeMesh = new ExponentialMeshing(0.0, T, tNodes, 4.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, maxMoneyness, 1.0, xNodes, 0.05);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    PDEResults1D res = solver.solve(pdeData, grid, lower, upper);
    double fwd = FORWARD_CURVE.getForward(T);

    for (int i = 0; i < xNodes; i++) {
      double x = grid.getSpaceNode(i);
      double k = fwd * x;
      if (k > 0.5 * fwd && k < 2.0 * fwd) {
        double modifiedPrice = res.getFunctionValue(i);
        double vol = BlackFormulaRepository.impliedVolatility(modifiedPrice, 1.0, x, T, true);
        double sabrVol = SABR_VOL_SURFACE.getVolatility(T, k);
        if (DEBUG) {
          System.out.println(k + "\t" + vol + "\t" + sabrVol);
        } else {
          assertEquals("testForwardPDE vols " + k, sabrVol, vol, 8e-4); //8bps error
        }
      }
    }

  }

  /**
   * Run a classic forwards PDE once, using the local volatility derived from the
   * SABR implied volatility surface, and check the prices at the example expiry and strikes agrees with SABR.
   */
  @Test
  public void testClassicForwardsPDE() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final int tNodes = 50;
    final int xNodes = 101;
    double fwd = FORWARD_CURVE.getForward(T);
    final double maxStrike = 4.0 * fwd;

    ConvectionDiffusionPDEDataBundle pdeData = PDE_DATA_PROVIDER.getForwardLocalVol(RATE, RATE - DRIFT, SPOT, true, SABR_LOCAL_VOL);

    BoundaryCondition lower = new DirichletBoundaryCondition(new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        return SPOT * Math.exp(-t * (RATE - DRIFT));
      }
    }, 0.0);
    BoundaryCondition upper = new DirichletBoundaryCondition(0.0, maxStrike);

    MeshingFunction timeMesh = new ExponentialMeshing(0.0, T, tNodes, 5.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0.0, maxStrike, SPOT, xNodes, 0.1);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    PDEResults1D res = solver.solve(pdeData, grid, lower, upper);
    double df = Math.exp(-RATE * T);
    //   System.out.println("Classic forward PDE");
    for (int i = 0; i < xNodes; i++) {
      double k = grid.getSpaceNode(i);
      if (k > 0.5 * fwd && k < 2.0 * fwd) {
        double price = res.getFunctionValue(i);
        double vol = BlackFormulaRepository.impliedVolatility(price / df, fwd, k, T, true);
        double sabrVol = SABR_VOL_SURFACE.getVolatility(T, k);
        if (DEBUG) {
          System.out.println(k + "\t" + vol + "\t" + sabrVol);
        } else {
          assertEquals("testClassicForwardsPDE. Strike = " + k, sabrVol, vol, 15e-4); //15bps error
        }
      }
    }

  }

  /**
   * For delta calculations we use the forward (or driftless) delta (pips forward delta in FX speek), which is the sensitivity of the forward option
   * price to a change in the relevant forward value of the underlying. In a Black-Scholes world this is just N(d1) (for a call) - i.e. no
   * exp(.) factors.
   * The delta under local volatility means the above sensitivity with the local volatility surface fixed, i.e. it is invariant
   * to a change of the forward curve. If the local volatility surface was allowed to change with the forward, then a different
   * delta value would be obtained. For example, we can calculated a delta for the SABR model by assuming that the SABR parameters
   * (alpha, beta, rho & nu) do not change when the forward moves - this delta will be different to what is calculated below.
   * Since there is no way of analytically calculating the delta for an arbitrary local volatility surface, we compare the results of running the
   * backwards and forwards PDE solver
   */
  @Test
  public void testLocalVolDelta() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final int tNodes = 50;
    final int xNodes = 101;

    final double forward = FORWARD_CURVE.getForward(T);
    final double maxForward = 5.0 * forward;
    final double maxMoneyness = 5.0;
    final double maxStrike = 5.0 * forward;
    final double shift = 1e-3;

    LocalVolatilitySurfaceMoneyness lvUp = LocalVolatilitySurfaceConverter.shiftForwardCurve(LV_M, shift);
    LocalVolatilitySurfaceMoneyness lvDown = LocalVolatilitySurfaceConverter.shiftForwardCurve(LV_M, -shift);

    ConvectionDiffusionPDEDataBundle pdeDataFwd = PDE_DATA_PROVIDER.getForwardLocalVol(LV_M, true);
    ConvectionDiffusionPDEDataBundle pdeDataCUp = PDE_DATA_PROVIDER.getForwardLocalVol(RATE, RATE - DRIFT, SPOT * (1 + shift), true, SABR_LOCAL_VOL);
    ConvectionDiffusionPDEDataBundle pdeDataCDown = PDE_DATA_PROVIDER.getForwardLocalVol(RATE, RATE - DRIFT, SPOT * (1 - shift), true, SABR_LOCAL_VOL);
    ConvectionDiffusionPDEDataBundle pdeDataUp = PDE_DATA_PROVIDER.getForwardLocalVol(lvUp, true);
    ConvectionDiffusionPDEDataBundle pdeDataDown = PDE_DATA_PROVIDER.getForwardLocalVol(lvDown, true);

    BoundaryCondition lowerFwd = new DirichletBoundaryCondition(1.0, 0.0);
    BoundaryCondition upperFwd = new DirichletBoundaryCondition(0.0, maxMoneyness);
    BoundaryCondition lowerBwd = new DirichletBoundaryCondition(0.0, 0.0);
    BoundaryCondition lowerUp = new DirichletBoundaryCondition(new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        return SPOT * (1 + shift) * Math.exp(-t * (RATE - DRIFT));
      }
    }, 0.0);
    BoundaryCondition lowerDown = new DirichletBoundaryCondition(new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        return SPOT * (1 - shift) * Math.exp(-t * (RATE - DRIFT));
      }
    }, 0.0);
    BoundaryCondition upperCFwd = new DirichletBoundaryCondition(0.0, maxStrike);

    MeshingFunction timeMesh = new ExponentialMeshing(0.0, T, tNodes, 5.0);
    MeshingFunction spaceMeshFwd = new HyperbolicMeshing(0.0, maxMoneyness, 1.0, xNodes, 0.1);
    MeshingFunction spaceMeshBwd = new HyperbolicMeshing(0.0, maxForward, forward, xNodes, 0.1);
    MeshingFunction spaceMeshCFwd = new HyperbolicMeshing(0.0, maxStrike, forward, xNodes, 0.1);
    PDEGrid1D gridFwd = new PDEGrid1D(timeMesh, spaceMeshFwd);
    PDEGrid1D gridCFwd = new PDEGrid1D(timeMesh, spaceMeshCFwd);
    PDEGrid1D gridBwd = new PDEGrid1D(timeMesh, spaceMeshBwd);

    PDEResults1D res = solver.solve(pdeDataFwd, gridFwd, lowerFwd, upperFwd);
    PDEResults1D resUp = solver.solve(pdeDataUp, gridFwd, lowerFwd, upperFwd);
    PDEResults1D resDown = solver.solve(pdeDataDown, gridFwd, lowerFwd, upperFwd);
    PDEResults1D resCUp = solver.solve(pdeDataCUp, gridCFwd, lowerUp, upperCFwd);
    PDEResults1D resCDown = solver.solve(pdeDataCDown, gridCFwd, lowerDown, upperCFwd);

    double q = Math.exp(-(RATE - DRIFT) * T);

    for (int i = 0; i < xNodes; i++) {
      double x = res.getSpaceValue(i);
      double k = x * forward;
      assertEquals("strikes", k, gridCFwd.getSpaceNode(i), 1e-6);
      double modelDD = res.getFirstSpatialDerivative(i);
      double fixedSurfaceDelta = res.getFunctionValue(i) - x * modelDD;
      double surfaceDelta = (resUp.getFunctionValue(i) - resDown.getFunctionValue(i)) / 2 / shift;
      double deltaFwd = fixedSurfaceDelta + surfaceDelta;

      double priceUp = resCUp.getFunctionValue(i);
      double priceDown = resCDown.getFunctionValue(i);

      double deltaCFwd = (priceUp - priceDown) / 2 / SPOT / shift / q;

      BoundaryCondition upperBwd = new DirichletBoundaryCondition(maxForward - k, maxForward);
      ConvectionDiffusionPDEDataBundle pdeDataBkd = PDE_DATA_PROVIDER.getBackwardsLocalVol(k, T, true, SABR_LOCAL_VOL, FORWARD_CURVE);
      PDEResults1D resBkd = solver.solve(pdeDataBkd, gridBwd, lowerBwd, upperBwd);

      //since the actual forward (i.e. F(0,T)) does not necessarily lie on the grid, we have to interpolate
      double[] tFwd = new double[xNodes];
      double[] tDelta = new double[xNodes];
      int count = 0;
      for (int j = 0; j < xNodes; j++) {
        double fwd = gridBwd.getSpaceNode(j);
        if (fwd > 0.9 * forward && fwd < 1.1 * forward) {
          tFwd[count] = fwd;
          tDelta[count] = resBkd.getFirstSpatialDerivative(j);
          count++;
        }
      }
      double[] fwds = new double[count];
      double[] deltas = new double[count];
      System.arraycopy(tFwd, 0, fwds, 0, count);
      System.arraycopy(tDelta, 0, deltas, 0, count);
      Interpolator1DDoubleQuadraticDataBundle db = INTERPOLATOR_1D.getDataBundle(fwds, deltas);
      double deltaBkd = INTERPOLATOR_1D.interpolate(db, forward);

      if (DEBUG) {
        System.out.println(k + "\t" + deltaFwd + "\t" + deltaCFwd + "\t" + deltaBkd);
      } else {
        assertEquals("testLocalVolDelta", deltaFwd, deltaBkd, 2e-2);
        assertEquals("testLocalVolDelta2", deltaFwd, deltaCFwd, 2e-2); //TODO this is not as accurate as one would like
      }
    }
  }

  @Test
  public void testLocalVolGamma() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final int tNodes = 50;
    final int xNodes = 101;

    final double forward = FORWARD_CURVE.getForward(T);
    final double maxForward = 5.0 * forward;
    final double maxMoneyness = 5.0;
    final double shift = 1e-3;

    LocalVolatilitySurfaceMoneyness lvUp = LocalVolatilitySurfaceConverter.shiftForwardCurve(LV_M, shift);
    LocalVolatilitySurfaceMoneyness lvDown = LocalVolatilitySurfaceConverter.shiftForwardCurve(LV_M, -shift);

    ConvectionDiffusionPDEDataBundle pdeDataFwd = PDE_DATA_PROVIDER.getForwardLocalVol(LV_M, true);
    ConvectionDiffusionPDEDataBundle pdeDataUp = PDE_DATA_PROVIDER.getForwardLocalVol(lvUp, true);
    ConvectionDiffusionPDEDataBundle pdeDataDown = PDE_DATA_PROVIDER.getForwardLocalVol(lvDown, true);

    BoundaryCondition lowerFwd = new DirichletBoundaryCondition(1.0, 0.0);
    BoundaryCondition upperFwd = new DirichletBoundaryCondition(0.0, maxMoneyness);
    BoundaryCondition lowerBwd = new DirichletBoundaryCondition(0, 0);

    MeshingFunction timeMesh = new ExponentialMeshing(0.0, T, tNodes, 5.0);
    MeshingFunction spaceMeshFwd = new HyperbolicMeshing(0.0, maxMoneyness, 1.0, xNodes, 0.1);
    MeshingFunction spaceMeshBwd = new HyperbolicMeshing(0.0, maxForward, forward, xNodes, 0.1);
    PDEGrid1D gridFwd = new PDEGrid1D(timeMesh, spaceMeshFwd);
    PDEGrid1D gridBwd = new PDEGrid1D(timeMesh, spaceMeshBwd);

    PDEResults1D res = solver.solve(pdeDataFwd, gridFwd, lowerFwd, upperFwd);
    PDEResults1D resUp = solver.solve(pdeDataUp, gridFwd, lowerFwd, upperFwd);
    PDEResults1D resDown = solver.solve(pdeDataDown, gridFwd, lowerFwd, upperFwd);

    double q = Math.exp(-(RATE - DRIFT) * T);

    for (int i = 0; i < xNodes; i++) {
      double x = res.getSpaceValue(i);
      double k = x * forward;

      double modelDD = res.getFirstSpatialDerivative(i);
      double fixedSurfaceDelta = res.getFunctionValue(i) - x * modelDD;
      double surfaceDelta = (resUp.getFunctionValue(i) - resDown.getFunctionValue(i)) / 2 / shift / forward;
      double deltaFwd = fixedSurfaceDelta + forward * surfaceDelta;
      double fixedSurfaceGamma = x * x / forward * res.getSecondSpatialDerivative(i);
      double surfaceVanna = (resUp.getFirstSpatialDerivative(i) - resDown.getFirstSpatialDerivative(i)) / 2 / forward / shift;
      double surfaceGamma = (resUp.getFunctionValue(i) + resDown.getFunctionValue(i) - 2 * res.getFunctionValue(i)) / forward / shift / shift;
      double gammaFwd = fixedSurfaceGamma + 2 * surfaceDelta - 2 * x * surfaceVanna + surfaceGamma;

      BoundaryCondition upperBwd = new DirichletBoundaryCondition(maxForward - k, maxForward);
      ConvectionDiffusionPDEDataBundle pdeDataBkd = PDE_DATA_PROVIDER.getBackwardsLocalVol(k, T, true, SABR_LOCAL_VOL, FORWARD_CURVE);
      PDEResults1D resBkd = solver.solve(pdeDataBkd, gridBwd, lowerBwd, upperBwd);

      //since the actual forward (i.e. F(0,T)) does not necessarily lie on the grid, we have to interpolate
      double[] tFwd = new double[xNodes];
      double[] tDelta = new double[xNodes];
      double[] tGamma = new double[xNodes];
      int count = 0;
      for (int j = 0; j < xNodes; j++) {
        double fwd = gridBwd.getSpaceNode(j);
        if (fwd > 0.9 * forward && fwd < 1.1 * forward) {
          tFwd[count] = fwd;
          tDelta[count] = resBkd.getFirstSpatialDerivative(j);
          tGamma[count] = resBkd.getSecondSpatialDerivative(j);
          count++;
        }
      }
      double[] fwds = new double[count];
      double[] deltas = new double[count];
      double[] gammas = new double[count];
      System.arraycopy(tFwd, 0, fwds, 0, count);
      System.arraycopy(tDelta, 0, deltas, 0, count);
      System.arraycopy(tGamma, 0, gammas, 0, count);
      Interpolator1DDoubleQuadraticDataBundle dbDelta = INTERPOLATOR_1D.getDataBundle(fwds, deltas);
      Interpolator1DDoubleQuadraticDataBundle dbGamma = INTERPOLATOR_1D.getDataBundle(fwds, gammas);
      double deltaBkd = INTERPOLATOR_1D.interpolate(dbDelta, forward);
      double gammaBkd = INTERPOLATOR_1D.interpolate(dbGamma, forward);

      if (DEBUG) {
        System.out.println(k + "\t" + deltaFwd + "\t" + deltaBkd + "\t" + gammaFwd + "\t" + gammaBkd + "\t" +
            fixedSurfaceGamma + "\t" + surfaceDelta + "\t" + surfaceVanna + "\t" + surfaceGamma);
      } else {
        assertEquals("testLocalVolDelta", gammaFwd, gammaBkd, 5e-1);//TODO still a large error here
      }
    }
  }

  /**
   * When the local volatility surface parametrised by moneyness (rather than strike) in made invariant to changes in the forward curve, the resulting
   * delta is that of a volatility model which is a function of moneyness only. An example of this is SABR with beta = 1.
   */
  @Test
  public void testSABRDelta() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final int tNodes = 50;
    final int xNodes = 101;

    final double forward = FORWARD_CURVE.getForward(T);
    final double maxMoneyness = 5.0;
    SABRFormulaData sabrData = new SABRFormulaData(ATM_VOL.evaluate(T), 1.0, RHO, NU.evaluate(T));
    LocalVolatilitySurfaceStrike locVol = getSABRLocalVolSurface(1.0, FORWARD_CURVE);

    ConvectionDiffusionPDEDataBundle pdeDataFwd = PDE_DATA_PROVIDER.getForwardLocalVol(locVol, FORWARD_CURVE, true);

    BoundaryCondition lowerFwd = new DirichletBoundaryCondition(1.0, 0.0);
    BoundaryCondition upperFwd = new DirichletBoundaryCondition(0.0, maxMoneyness);

    MeshingFunction timeMesh = new ExponentialMeshing(0.0, T, tNodes, 5.0);
    MeshingFunction spaceMeshFwd = new HyperbolicMeshing(0.0, maxMoneyness, 1.0, xNodes, 0.1);

    PDEGrid1D gridFwd = new PDEGrid1D(timeMesh, spaceMeshFwd);
    PDEResults1D res = solver.solve(pdeDataFwd, gridFwd, lowerFwd, upperFwd);

    for (int i = 1; i < xNodes - 1; i++) {
      double x = res.getSpaceValue(i);
      double k = x * forward;
      double modelDD = res.getFirstSpatialDerivative(i);
      double fixedSurfaceDelta = res.getFunctionValue(i) - x * modelDD;
      double[] volAdjoint = SABR.getVolatilityAdjoint(new EuropeanVanillaOption(k, T, true), forward, sabrData);
      double bsDelta = BlackFormulaRepository.delta(forward, k, T, volAdjoint[0], true);
      double bsVega = BlackFormulaRepository.vega(forward, k, T, volAdjoint[0]);
      double sabrDelta = bsDelta + bsVega * volAdjoint[1];

      if (DEBUG) {
        System.out.println(k + "\t" + fixedSurfaceDelta + "\t" + sabrDelta);
      } else {
        assertEquals("testSABRDelta", sabrDelta, fixedSurfaceDelta, 1e-2);

      }
    }
  }

  private static BlackVolatilitySurfaceStrike getSABRImpliedVolSurface(final double beta, final ForwardCurve forwardCurve) {

    final Function<Double, Double> sabrSurface = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        final double fwd = forwardCurve.getForward(t);
        final double alpha = ATM_VOL.evaluate(T) * Math.pow(fwd, 1 - beta);
        final double nu = NU.evaluate(t);
        final SABRFormulaData data = new SABRFormulaData(alpha, beta, RHO, nu);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        return SABR.getVolatility(option, fwd, data);
      }
    };

    return new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(sabrSurface));
  }

  private static LocalVolatilitySurfaceStrike getSABRLocalVolSurface(final double beta, final ForwardCurve forwardCurve) {
    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();
    final BlackVolatilitySurfaceStrike impVol = getSABRImpliedVolSurface(beta, forwardCurve);
    return cal.getLocalVolatility(impVol, forwardCurve);
  }

  private static PriceSurface getSABRPriceSurface(final double beta, final ForwardCurve forwardCurve, final YieldAndDiscountCurve discountCurve) {

    final BlackVolatilitySurfaceStrike impVol = getSABRImpliedVolSurface(beta, forwardCurve);
    final Function<Double, Double> priceSurface = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        double fwd = forwardCurve.getForward(t);
        double vol = impVol.getVolatility(t, k);
        return discountCurve.getDiscountFactor(t) * BlackFormulaRepository.price(fwd, k, t, vol, true);
      }
    };
    return new PriceSurface(FunctionalDoublesSurface.from(priceSurface));
  }

}
