/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ZZConvectionDiffusionPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class LogPayoffWithDividendsTest {

  private static final Integrator1D<Double, Double> DEFAULT_INTEGRATOR = new RungeKuttaIntegrator1D();

  private static final Interpolator1D INTEPOLATOR1D = Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE;

  private static final double EXPIRY = 1.5;
  private static final double DIVIDEND_DATE = 0.85;
  private static final double ALPHA = 5.0;
  private static final double BETA = 0.06;
  private static final double PURE_VOL = 0.3;
  private static final double VOL = 0.4;
  private static final double SPOT = 100.0;
  private static final double DRIFT = 0.1;
  private static final LocalVolatilitySurfaceMoneyness PURE_LOCAL_VOL_FLAT;
  private static final LocalVolatilitySurfaceStrike LOCAL_VOL;
  private static final LocalVolatilitySurfaceStrike LOCAL_VOL_FLAT;
  private static final LocalVolatilitySurfaceMoneyness PURE_LOCAL_VOL;
  private static final ForwardCurve FORWARD_CURVE;

  private static final Surface<Double, Double, Double> PURE_PRICE_SURFACE;
  private static final Surface<Double, Double, Double> PRICE_SURFACE;

  private static final Function1D<Double, Double> R;
  private static final Function1D<Double, Double> D;
  private static final Function1D<Double, Double> F;

  private static final Function1D<Double, Double> PURE_LOG_PAY_OFF;

  static {
    R = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        double prod = Math.exp(t * DRIFT);
        if (t >= DIVIDEND_DATE) {
          prod *= (1 - BETA);
        }
        return prod;
      }
    };

    D = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        final double r_t = R.evaluate(t);
        double sum = 0.0;
        if (DIVIDEND_DATE > t) {
          sum += ALPHA / R.evaluate(DIVIDEND_DATE);
        }
        return sum * r_t;
      }
    };

    F = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        final double r_t = R.evaluate(t);
        double sum = 0.0;
        if (DIVIDEND_DATE <= t) {
          sum += ALPHA / R.evaluate(DIVIDEND_DATE);
        }
        return r_t * (SPOT - sum);
      }
    };

    PURE_LOG_PAY_OFF = new Function1D<Double, Double>() {
      final double fT = F.evaluate(EXPIRY);
      final double dT = D.evaluate(EXPIRY);

      @Override
      public Double evaluate(Double x) {
        final double s = (fT - dT) * Math.exp(x) + dT;
        return Math.log(s / fT);
      }
    };

    FORWARD_CURVE = new ForwardCurve(F);

    Function<Double, Double> localVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... ts) {
        double t = ts[0];
        double s = ts[1];
        double d = D.evaluate(t);
        return PURE_VOL * (s - d) / s;
      }
    };

    LOCAL_VOL = new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(localVol));

    Function<Double, Double> pureLocalVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tx) {
        double t = tx[0];
        double x = tx[1];
        double f = F.evaluate(t);
        double d = D.evaluate(t);
        return VOL * ((f - d) * x + d) / (f - d) / x;
      }
    };

    PURE_LOCAL_VOL = new LocalVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(pureLocalVol), new ForwardCurve(1.0));

    PURE_LOCAL_VOL_FLAT = new LocalVolatilitySurfaceMoneyness(ConstantDoublesSurface.from(PURE_VOL), new ForwardCurve(1.0));
    LOCAL_VOL_FLAT = new LocalVolatilitySurfaceStrike(ConstantDoublesSurface.from(VOL));

    final Function<Double, Double> pPrice = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final boolean isCall = x > 1;

        final double pp = BlackFormulaRepository.price(1.0, x, t, PURE_VOL, isCall);
        return pp;
      }
    };

    final Function<Double, Double> price = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        final double f = F.evaluate(t);
        final double d = D.evaluate(t);
        final double x = (k - d) / (f - d);
        final double p = (f - d) * pPrice.evaluate(t, x);
        return p;
      }
    };

    PURE_PRICE_SURFACE = FunctionalDoublesSurface.from(pPrice);
    PRICE_SURFACE = FunctionalDoublesSurface.from(price);

  }

  /**
   * check the value of the log-contract is correctly valued by static replication using 'pure' call and put prices,
   * and real call and put prices 
   */
  @Test
  public void staticReplicationTest() {

    final double fT = F.evaluate(EXPIRY);
    final double dT = D.evaluate(EXPIRY);
    final double dStar = dT / (fT - dT);
    final Function1D<Double, Double> integral1 = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        final double price = PURE_PRICE_SURFACE.getZValue(EXPIRY, x);
        final double xStar = x + dStar;
        return price / xStar / xStar;
      }
    };

    final Function1D<Double, Double> integral2 = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        final double price = PRICE_SURFACE.getZValue(EXPIRY, k);
        return price / k / k;
      }
    };

    double val1 = -DEFAULT_INTEGRATOR.integrate(integral1, 0.01, 10.0);
    double val2 = -DEFAULT_INTEGRATOR.integrate(integral2, 0.01 * fT, 10.0 * fT);
    assertEquals(val1, val2, 1e-9);
    assertEquals(PURE_VOL, Math.sqrt(-2 * val1 / EXPIRY), 1e-9);
    // System.out.println(val1 + "\t" + val2 + "\t" + Math.sqrt(-2 * val1 / EXPIRY));
  }

  /**
   * Check the the log-contract is correctly prices using a backwards PDE expressed in terms of (the log of) the 'pure' stock price 
   * - this avoids having jumps conditions in the PDE 
   */
  @Test
  public void backwardsPDEtest() {
    final double fT = F.evaluate(EXPIRY);
    final double dT = D.evaluate(EXPIRY);
    final double dStar = dT / (fT - dT);

    ZZConvectionDiffusionPDEDataBundle pdeBundle = getBackwardsPDEDataBundle(EXPIRY, PURE_LOCAL_VOL_FLAT, PURE_LOG_PAY_OFF);

    double theta = 0.5;
    double yL = -0.5;
    double yH = 0.5;
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower = new NeumannBoundaryCondition(1 / (1 + dStar * Math.exp(-yL)), yL, true);
    BoundaryCondition upper = new NeumannBoundaryCondition(1.0, yH, false);

    final MeshingFunction timeMesh = new ExponentialMeshing(0.0, EXPIRY, 100, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(yL, yH, 101, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDEResults1D res = solver.solve(pdeBundle, grid, lower, upper);

    final int n = res.getNumberSpaceNodes();

    double val = res.getFunctionValue(n / 2);

    assertEquals(PURE_VOL, Math.sqrt(-2 * val / EXPIRY), 1e-6);
    //System.out.println(val + "\t" + Math.sqrt(-2 * val / EXPIRY));
  }

  /**
   * Check the the log-contract is correctly prices using a backwards PDE expressed in terms of (the log of) the real stock price 
   * - this requires having jumps conditions in the PDE 
   */
  @Test
  public void backwardsPDEtest2() {
    final double fT = F.evaluate(EXPIRY);
    final double lnFT = Math.log(fT);

    final Function1D<Double, Double> payoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double y) {
        return y - lnFT;
      }
    };

    ZZConvectionDiffusionPDEDataBundle pdeBundle1 = getBackwardsPDEDataBundle(EXPIRY, LOCAL_VOL, payoff);

    double theta = 0.5;
    double yL = Math.log(SPOT / 6);
    double yH = Math.log(6 * SPOT);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower1 = new NeumannBoundaryCondition(1.0, yL, true);
    BoundaryCondition upper1 = new NeumannBoundaryCondition(1.0, yH, false);

    final MeshingFunction timeMesh1 = new ExponentialMeshing(0, EXPIRY - DIVIDEND_DATE - 1e-6, 50, 0.0);
    final MeshingFunction timeMesh2 = new ExponentialMeshing(EXPIRY - DIVIDEND_DATE + 1e-6, EXPIRY, 50, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(yL, yH, 101, 0.0);

    final PDEGrid1D grid1 = new PDEGrid1D(timeMesh1, spaceMesh);
    final double[] sNodes1 = grid1.getSpaceNodes();
    final PDETerminalResults1D res1 = (PDETerminalResults1D) solver.solve(pdeBundle1, grid1, lower1, upper1);
    final int nSNodes = sNodes1.length;

    double val1 = res1.getFunctionValue(nSNodes / 2);
    //   System.out.println(val1 + "\t" + Math.sqrt(-2 * val1 / (EXPIRY)));

    final double[] sNodes2 = new double[nSNodes];
    final double lnBeta = Math.log(1 - BETA);
    for (int i = 0; i < nSNodes; i++) {
      double temp = sNodes1[i];
      if (temp < 0) {
        sNodes2[i] = Math.log(Math.exp(temp) + ALPHA) - lnBeta;
      }
      else {
        sNodes2[i] = temp + Math.log(1 + ALPHA * Math.exp(-temp)) - lnBeta;
      }
    }

    PDEGrid1D grid2 = new PDEGrid1D(timeMesh2.getPoints(), sNodes2);
    BoundaryCondition lower2 = new NeumannBoundaryCondition(1.0, sNodes2[0], true);
    BoundaryCondition upper2 = new NeumannBoundaryCondition(1.0, sNodes2[nSNodes - 1], false);

    final Interpolator1DDataBundle interpolDB1 = INTEPOLATOR1D.getDataBundle(sNodes2, res1.getFinalTimePrices());
    final Function1D<Double, Double> pricesAtDividend = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return INTEPOLATOR1D.interpolate(interpolDB1, x);
      }
    };

    ZZConvectionDiffusionPDEDataBundle pdeBundle2 = getBackwardsPDEDataBundle(EXPIRY, LOCAL_VOL, pricesAtDividend);
    final PDETerminalResults1D res2 = (PDETerminalResults1D) solver.solve(pdeBundle2, grid2, lower2, upper2);

    final Interpolator1DDataBundle interpolDB2 = INTEPOLATOR1D.getDataBundle(sNodes2, res2.getFinalTimePrices());
    double val2 = INTEPOLATOR1D.interpolate(interpolDB2, Math.log(SPOT));
    assertEquals(PURE_VOL, Math.sqrt(-2 * val2 / EXPIRY), 1e-6);
    //  System.out.println(val2 + "\t" + Math.sqrt(-2 * val2 / EXPIRY));
  }

  /**
   * Check the the log-contract is correctly prices using a backwards PDE expressed in terms of (the log of) the real stock price 
   * - this requires having jumps conditions in the PDE 
   */
  @Test
  public void backwardsPDEtest3() {
    final double fT = F.evaluate(EXPIRY);
    final double lnFT = Math.log(fT);

    final Function1D<Double, Double> payoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double y) {
        return y - lnFT;
      }
    };

    ZZConvectionDiffusionPDEDataBundle pdeBundle1 = getBackwardsPDEDataBundle(EXPIRY, LOCAL_VOL_FLAT, payoff);

    double theta = 0.5;
    double yL = Math.log(SPOT / 6);
    double yH = Math.log(6 * SPOT);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower1 = new NeumannBoundaryCondition(1.0, yL, true);
    BoundaryCondition upper1 = new NeumannBoundaryCondition(1.0, yH, false);

    final MeshingFunction timeMesh1 = new ExponentialMeshing(0, EXPIRY - DIVIDEND_DATE - 1e-6, 50, 0.0);
    final MeshingFunction timeMesh2 = new ExponentialMeshing(EXPIRY - DIVIDEND_DATE + 1e-6, EXPIRY, 50, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(yL, yH, 101, 0.0);

    final PDEGrid1D grid1 = new PDEGrid1D(timeMesh1, spaceMesh);
    final double[] sNodes1 = grid1.getSpaceNodes();
    final PDETerminalResults1D res1 = (PDETerminalResults1D) solver.solve(pdeBundle1, grid1, lower1, upper1);
    final int nSNodes = sNodes1.length;

    double val1 = res1.getFunctionValue(nSNodes / 2);
    System.out.println(val1 + "\t" + Math.sqrt(-2 * val1 / (EXPIRY - DIVIDEND_DATE)));

    double[] sNodes2 = new double[nSNodes];
    final double lnBeta = Math.log(1 - BETA);
    //    sNodes2 = Arrays.copyOf(sNodes1, nSNodes);
    for (int i = 0; i < nSNodes; i++) {
      double temp = sNodes1[i];
      if (temp < 0) {
        sNodes2[i] = Math.log(Math.exp(temp) + ALPHA) - lnBeta;
      }
      else {
        sNodes2[i] = temp + Math.log(1 + ALPHA * Math.exp(-temp)) - lnBeta;
      }
    }

    PDEGrid1D grid2 = new PDEGrid1D(timeMesh2.getPoints(), sNodes2);
    BoundaryCondition lower2 = new NeumannBoundaryCondition(1.0, sNodes2[0], true);
    BoundaryCondition upper2 = new NeumannBoundaryCondition(1.0, sNodes2[nSNodes - 1], false);

    final Interpolator1DDataBundle interpolDB1 = INTEPOLATOR1D.getDataBundle(sNodes2, res1.getFinalTimePrices());
    final Function1D<Double, Double> pricesAtDividend = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return INTEPOLATOR1D.interpolate(interpolDB1, x);
      }
    };

    ZZConvectionDiffusionPDEDataBundle pdeBundle2 = getBackwardsPDEDataBundle(EXPIRY, LOCAL_VOL_FLAT, pricesAtDividend);
    final PDETerminalResults1D res2 = (PDETerminalResults1D) solver.solve(pdeBundle2, grid2, lower2, upper2);

    final Interpolator1DDataBundle interpolDB2 = INTEPOLATOR1D.getDataBundle(sNodes2, res2.getFinalTimePrices());
    double val2 = INTEPOLATOR1D.interpolate(interpolDB2, Math.log(SPOT));
    //assertEquals(PURE_VOL, Math.sqrt(-2 * val2 / EXPIRY), 1e-6);
    System.out.println(val2 + "\t" + Math.sqrt(-2 * val2 / EXPIRY));
  }

  /**
   * Check the the log-contract is correctly prices using a backwards PDE expressed in terms of (the log of) the 'pure' stock price 
   * - this avoids having jumps conditions in the PDE 
   */
  @Test
  public void backwardsPDEtest4() {
    final double fT = F.evaluate(EXPIRY);
    final double dT = D.evaluate(EXPIRY);
    final double dStar = dT / (fT - dT);

    ZZConvectionDiffusionPDEDataBundle pdeBundle = getBackwardsPDEDataBundle(EXPIRY, PURE_LOCAL_VOL, PURE_LOG_PAY_OFF);

    double theta = 0.5;
    double yL = -0.5;
    double yH = 0.5;
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower = new NeumannBoundaryCondition(1 / (1 + dStar * Math.exp(-yL)), yL, true);
    BoundaryCondition upper = new NeumannBoundaryCondition(1.0, yH, false);

    final MeshingFunction timeMesh = new ExponentialMeshing(0.0, EXPIRY, 100, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(yL, yH, 101, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDEResults1D res = solver.solve(pdeBundle, grid, lower, upper);

    final int n = res.getNumberSpaceNodes();

    double val = res.getFunctionValue(n / 2);

    //assertEquals(PURE_VOL, Math.sqrt(-2 * val / EXPIRY), 1e-6);
    System.out.println(val + "\t" + Math.sqrt(-2 * val / EXPIRY));
  }

  private ZZConvectionDiffusionPDEDataBundle getBackwardsPDEDataBundle(final double maturity, final LocalVolatilitySurfaceMoneyness localVol,
      final Function1D<Double, Double> payoff) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        Validate.isTrue(tx.length == 2);
        final double tau = tx[0];
        final double x = tx[1];
        final double t = maturity - tau;
        final double m = Math.exp(x);
        final double temp = localVol.getVolatilityForMoneyness(t, m);
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        Validate.isTrue(tx.length == 2);
        final double tau = tx[0];
        final double x = tx[1];
        final double t = maturity - tau;
        final double m = Math.exp(x);
        final double temp = localVol.getVolatilityForMoneyness(t, m);
        return 0.5 * temp * temp;
      }
    };

    return new ZZConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b),
        ConstantDoublesSurface.from(0.0), payoff);
  }

  private ZZConvectionDiffusionPDEDataBundle getBackwardsPDEDataBundle(final double maturity, final LocalVolatilitySurfaceStrike localVol,
      final Function1D<Double, Double> payoff) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        Validate.isTrue(tx.length == 2);
        final double tau = tx[0];
        final double x = tx[1];
        final double t = maturity - tau;
        final double s = Math.exp(x);
        final double temp = localVol.getVolatility(t, s);
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        Validate.isTrue(tx.length == 2);
        final double tau = tx[0];
        final double x = tx[1];
        final double t = maturity - tau;
        final double s = Math.exp(x);
        final double temp = localVol.getVolatility(t, s);
        return 0.5 * temp * temp - DRIFT;
      }
    };

    return new ZZConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b),
        ConstantDoublesSurface.from(0), payoff);
  }

}
