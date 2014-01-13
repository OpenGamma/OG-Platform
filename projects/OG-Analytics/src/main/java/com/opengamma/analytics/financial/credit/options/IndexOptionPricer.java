/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadISDAFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.GaussHermiteQuadratureIntegrator1D;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class IndexOptionPricer {
  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1);
  private static final double ONE_OVER_ROOT_PI = 1 / Math.sqrt(Math.PI);
  private static final Integrator1D<Double, Double> RK = new RungeKuttaIntegrator1D();
  private static final GaussHermiteQuadratureIntegrator1D GHQ2 = new GaussHermiteQuadratureIntegrator1D(2);
  private static final GaussHermiteQuadratureIntegrator1D GHQ7 = new GaussHermiteQuadratureIntegrator1D(7);
  private static final BracketRoot BRACKER = new BracketRoot();
  private static final NewtonRaphsonSingleRootFinder ROOTFINDER = new NewtonRaphsonSingleRootFinder(1e-10);

  private final PortfolioSwapAdjustment _portfolioAdjustment = new PortfolioSwapAdjustment();

  private final AnnuityForSpreadFunction _annuityFunc;
  private final CDSAnalytic _fwdCDS;
  private final double _expiry;
  private final ISDACompliantYieldCurve _yieldCurve;
  private final double _coupon;

  public IndexOptionPricer(final CDSAnalytic fwdCDS, final ISDACompliantYieldCurve yieldCurve, final double coupon) {
    _annuityFunc = new AnnuityForSpreadISDAFunction(fwdCDS, yieldCurve);
    _fwdCDS = fwdCDS;
    _yieldCurve = yieldCurve;
    _expiry = _fwdCDS.getEffectiveProtectionStart(); //TODO should have explicit option expiry 
    _coupon = coupon;
  }

  public double price(final int initialIndexSize, final ISDACompliantCreditCurve[] adjCreditCurves, final double[] recoveryRates, final double defaultedValue, final double strike, final double vol) {

    final double df = _yieldCurve.getDiscountFactor(_expiry);
    final double accrued = _fwdCDS.getAccruedPremium(_coupon) * _yieldCurve.getDiscountFactor(_fwdCDS.getCashSettleTime()) / df;

    final double pvIndex = _portfolioAdjustment.indexPV(_fwdCDS, _coupon, initialIndexSize, _yieldCurve, adjCreditCurves, recoveryRates, PriceType.CLEAN);
    double sum = 0.0;
    final int n = adjCreditCurves.length; //number of non-defaulted names at t (observation time)
    for (int i = 0; i < n; i++) {
      final double q = adjCreditCurves[i].getSurvivalProbability(_expiry);
      final double lgd = 1 - recoveryRates[i];
      sum += lgd - (lgd + accrued) * q;
    }
    //this is the expected value (today) of the default adjusted forward portfolio swap - i.e. the discounted expected (full) value of the index and the LGD of the 
    //defaulted names, at the option expiry 
    final double pvDefaultAdjIndex = pvIndex + df * (sum / initialIndexSize + defaultedValue);

    final double x0 = calibrateX0(pvDefaultAdjIndex, vol); //mean of pseudo-spread 
    final double gK = (strike - _coupon) * _annuityFunc.evaluate(strike) - accrued; //the excise price 
    final double zStar = getZStar(x0, vol, gK); //critical value of z 

    final Function1D<Double, Double> integrand = getPriceIntegrand(x0, vol, gK);
    return RK.integrate(integrand, zStar, Math.max(8.0, zStar + 2));
  }

  /**
   * This calibrates X0 (the mean value of the pseudo-spread, X) for a given volatility (vol) such that the expected
   * value of $F_I(X)$ equals the calculated value of the default-adjusted forward portfolio swap
   * @param pvDefaultAdjIndex The calculated value of the default-adjusted forward portfolio swap
   * @param vol The volatility of the pseudo-spread, X
   * @return The calibrated value of X0
   */
  public double calibrateX0(final double pvDefaultAdjIndex, final double vol) {

    final DoubleFunction1D funcLowAccuracy = new DoubleFunction1D() {
      @Override
      public Double evaluate(final Double x0) {
        final Function1D<Double, Double> intergrand = getGaussHermiteIntegrand(x0, vol);
        return GHQ2.integrateFromPolyFunc(intergrand) - pvDefaultAdjIndex;
      }
    };

    final DoubleFunction1D funcHiAccuracy = new DoubleFunction1D() {
      @Override
      public final Double evaluate(final Double x0) {
        final Function1D<Double, Double> intergrand = getGaussHermiteIntegrand(x0, vol);
        return GHQ7.integrateFromPolyFunc(intergrand) - pvDefaultAdjIndex;
      }
    };

    //This uses a low accuracy (2 Gauss_hermite points) function to provide the gradient 
    //Gradient is calculated by finite-difference. TODO investigate computing this analytically  
    final double guess = pvDefaultAdjIndex / _annuityFunc.evaluate(_coupon) + _coupon;
    return ROOTFINDER.getRoot(funcHiAccuracy, funcLowAccuracy.derivative(), guess);
  }

  /**
   * The <em>default-adjusted forward portfolio swap</em> (Pedersen 2003, F_I, is the index 
   * plus the right to exchange bonds on defaulted entries for par at the option excise date. 
   * Its expected value today can be expressed as a function of a pseudo spread, x. 
   * @param coupon This index coupon
   * @return a function F_I(x), that maps a spread, x, to expected value of the default-adjusted
   * forward portfolio swap.
   */
  public Function1D<Double, Double> getDefaultAdjForwardForX(final double coupon) {
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        return (x - coupon) * _annuityFunc.evaluate(x);
      }
    };
  }

  /**
   * This is ultimately used to calibrate x0 for a given vol and $F_I$ (The default-adjusted forward portfolio swap).
   * It gives the integrand in a form for use by Gauss-Hermite (i.e. the $exp(-z^2)$ is <b>not</b> included) 
   * @param x0 The mean value of the pseudo-spread at expiry 
   * @param vol The volatility of the pseudo-spread
   * @return An integrand function to be used by <b>Gauss-Hermite only</b>
   */
  private Function1D<Double, Double> getGaussHermiteIntegrand(final double x0, final double vol) {
    //      final double sigmaRoot2T = vol * Math.sqrt(2 * _expiry);
    //      final double sigmaSqrTOver2 = vol * vol * _expiry / 2;
    final Function1D<Double, Double> func = getFiForZ(x0, vol);

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double zeta) {
        return func.evaluate(zeta * Math.sqrt(2)) * ONE_OVER_ROOT_PI;
        //          final double x = x0 * Math.exp(sigmaRoot2T * z - sigmaSqrTOver2);
        //          return (x - _coupon) * _annuityFunc.evaluate(x) * oneOverRootPi;
      }
    };
  }

  /**
   * This gives a function for the value of $F_I$ in terms of the Gaussian random variable Z, i.e. $F_I(Z)$
   * @param x0 The mean value of the pseudo-spread at expiry 
   * @param vol The volatility of the pseudo-spread
   * @return The function $F_I(Z | X0, \sigma)$ - terminal value of the default-adjusted forward portfolio swap as a function of 
   * the Gaussian random variable Z
   */
  private Function1D<Double, Double> getFiForZ(final double x0, final double vol) {
    final double sigmaRootT = vol * Math.sqrt(_expiry);
    final double sigmaSqrTOver2 = vol * vol * _expiry / 2;
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double z) {
        final double x = x0 * Math.exp(sigmaRootT * z - sigmaSqrTOver2);
        return (x - _coupon) * _annuityFunc.evaluate(x);
      }
    };
  }

  private Function1D<Double, Double> getPriceIntegrand(final double x0, final double vol, final double exciseAmt) {
    final Function1D<Double, Double> fi = getFiForZ(x0, vol);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double z) {
        return Math.max(0.0, fi.evaluate(z) - exciseAmt) * NORMAL.getPDF(z);
      }
    };
  }

  /**
   * Finds $Z^*$, the value of Z for which the option payoff becomes positive 
   * @param x0 The mean value of the pseudo-spread at expiry 
   * @param vol The volatility of the pseudo-spread
   * @param exciseAmt The excise amount, $G(K)$
   * @return The critical value, $Z^*$ where $F_I(Z^*) = G(K)$
   */
  private double getZStar(final double x0, final double vol, final double exciseAmt) {
    final Function1D<Double, Double> fi = getFiForZ(x0, vol);
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double z) {
        return fi.evaluate(z) - exciseAmt;
      }
    };

    return ROOTFINDER.getRoot(func, 0.0);
  }

  /**
   * This lets the default-adjusted forward portfolio spread, $F_I$ be a function of a pseudo-spread, x,
   * $F_I = (x-c)A(x)$ where $A(x)$ is the annuity for the spread level x.  
   * @param x the pseudo-spread
   * @return The default-adjusted forward portfolio spread, $F_I$
   */
  private double pedersenFunction(final double x) {
    return (x - _coupon) * _annuityFunc.evaluate(x);
  }

}
