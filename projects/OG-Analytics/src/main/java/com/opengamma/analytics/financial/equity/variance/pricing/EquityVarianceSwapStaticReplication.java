/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;

/**
 * Class to calculate the expected variance (NOT annualised) of an equity variance swap when a discount curve, affine dividends and either a PURE implied volatility surface or
 * (classic) implied volatility surface is specified. See White (2012), Equity Variance Swap with Dividends, for details of the model
 */
public class EquityVarianceSwapStaticReplication {
  /** The integrator */
  private static final Integrator1D<Double, Double> INTEGRATOR = new RungeKuttaIntegrator1D();

  /**
   * Computes the computes the expected variance with and without adjustments for the dividend payments, by computing the price of a log-contract with expiry coinciding with that
   *  of the variance swap, and the value of the dividend corrections at all dividend dates, using the method of static replication from pure option prices obtained from the
   *  <b>pure</b> implied volatility surface (The pure< implied volatility is a number that put into Black formula (with unit forward) gives the price of puts and calls of the pure
   *  stock).
   * @param spot The current level of the stock or index
   * @param discountCurve The risk free interest rate curve
   * @param dividends The dividends structure
   * @param expiry The expiry of the variance swap
   * @param volSurface A <b>pure</b> implied volatility surface -
   * @return The expected variance (<b>not</b> annualised) with and without adjustments for the dividend payments (the former is usually the case for single stock and the latter for indices)
   */
  public double[] expectedVariance(final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends, final double expiry,
      final PureImpliedVolatilitySurface volSurface) {

    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);

    final double terminalFwd = divCurves.getF(expiry);
    final double logNoDivFwd = Math.log(spot) + discountCurve.getInterestRate(expiry) * expiry;
    final double logContract = integrate(getLogPayoffIntegrand(expiry, volSurface, divCurves)) + Math.log(terminalFwd);

    double corrDivAdj = 0;
    double uncorrDivAdj = 0;
    int index = 0;
    final int n = dividends.getNumberOfDividends();
    while (n > 0 && index < n && dividends.getTau(index) <= expiry) {
      final double f = divCurves.getF(dividends.getTau(index));
      corrDivAdj += integrate(getCorrectedDividendAdjustmentIntegrand(index, volSurface, divCurves, dividends)) + getCorrectedDividendAdjustment(f, index, dividends);
      uncorrDivAdj += integrate(getUncorrectedDividendAdjustmentIntegrand(index, volSurface, divCurves, dividends)) + getUncorrectedDividendAdjustment(f, index, dividends);
      index++;
    }

    final double rvNoDivs = -2 * (logContract - logNoDivFwd);
    final double rvCorrDivs = rvNoDivs + 2 * corrDivAdj;
    final double rvUncorrDivs = rvNoDivs + 2 * uncorrDivAdj;
    return new double[] {rvCorrDivs, rvUncorrDivs };
  }

  /**
   * Computes the computes the expected variance with and without adjustments for the dividend payments, by computing the price of a log-contract with expiry coinciding with that
   *  of the variance swap, and the value of the dividend corrections at all dividend dates, using the method of static replication from option prices obtained from the
   * implied volatility surface<p> <b>NOTE</b> For finite cash dividends, there is a arbitrage if the implied volatility remains smooth across the dividend date, hence simple interpolation
   * from market option prices to form a (smooth) implied volatility surface WILL introduce arbitrage. It is therefore better to work with a pure implied volatility surface.
   * @param spot The current level of the stock or index
   * @param discountCurve The risk free interest rate curve
   * @param dividends The dividends structure
   * @param expiry The expiry of the variance swap
   * @param volSurfaceStrike A implied volatility surface
   * @return The expected variance (<b>not</b> annualised) with and without adjustments for the dividend payments (the former is usually the case for single stock and the latter for indices)
   */
  public double[] expectedVariance(final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends, final double expiry,
      final BlackVolatilitySurfaceStrike volSurfaceStrike) {

    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    final BlackVolatilitySurfaceMoneyness volSurface = BlackVolatilitySurfaceConverter.toMoneynessSurface(volSurfaceStrike, new ForwardCurve(divCurves.getF()));

    final double terminalFwd = divCurves.getF(expiry);
    final double logNoDivFwd = Math.log(spot) + discountCurve.getInterestRate(expiry) * expiry;
    final double logContract = integrate(getLogPayoffIntegrand(expiry, volSurface)) + Math.log(terminalFwd);

    double corrDivAdj = 0;
    double uncorrDivAdj = 0;
    int index = 0;
    final int n = dividends.getNumberOfDividends();
    while (n > 0 && index < n && dividends.getTau(index) <= expiry) {
      final double f = divCurves.getF(dividends.getTau(index));
      corrDivAdj += integrate(getCorrectedDividendAdjustmentIntegrand(index, volSurface, dividends)) + getCorrectedDividendAdjustment(f, index, dividends);
      uncorrDivAdj += integrate(getUncorrectedDividendAdjustmentIntegrand(index, volSurface, dividends)) + getUncorrectedDividendAdjustment(f, index, dividends);
      index++;
    }

    final double rvNoDivs = -2 * (logContract - logNoDivFwd);
    final double rvCorrDivs = rvNoDivs + 2 * corrDivAdj;
    final double rvUncorrDivs = rvNoDivs + 2 * uncorrDivAdj;
    return new double[] {rvCorrDivs, rvUncorrDivs };
  }

  private double integrate(final Function1D<Double, Double> func) {
    final double put = INTEGRATOR.integrate(func, 0.0, 1.0);
    final double call = INTEGRATOR.integrate(func, 1.0, 50.0); //TODO set upper limit from tolerance
    return put + call;
  }

  /**
   * The (non-discounted) value of the log-payoff, $\mathbb{E}[S_T]$, can be computed as the log of the forward, $\log F_T$ plus the integral of this function from
   * 0 to infinity (because of the non-smoothness when switching from puts at calls at x = 1, it is better to split the integral in two, 0 to 1 & 1 to infinity).
   * @param expiry log-payoff expiry
   * @param volSurface pure implied volatility surface
   * @param divCurves dividend curves
   * @return A function that integrates the log payoff
   */
  private Function1D<Double, Double> getLogPayoffIntegrand(final double expiry, final PureImpliedVolatilitySurface volSurface, final EquityDividendsCurvesBundle divCurves) {
    final double f = divCurves.getF(expiry);
    final double d = divCurves.getD(expiry);

    final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        if (x == 0.0) {
          return 0.0;
        }
        final boolean isCall = x >= 1.0;
        final double vol = volSurface.getVolatility(expiry, x);
        final double price = BlackFormulaRepository.price(1.0, x, expiry, vol, isCall);
        final double weight = -FunctionUtils.square((f - d) / ((f - d) * x + d));
        return price * weight;
      }
    };
    return integrand;
  }

  private Function1D<Double, Double> getLogPayoffIntegrand(final double expiry, final BlackVolatilitySurfaceMoneyness volSurface) {

    final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        if (x == 0) {
          return 0.0;
        }
        final boolean isCall = x >= 1.0;
        final double vol = volSurface.getVolatilityForMoneyness(expiry, x);
        final double otmPrice = BlackFormulaRepository.price(1.0, x, expiry, vol, isCall);
        final double weight = -1 / x / x;
        return otmPrice * weight;
      }
    };
    return integrand;
  }

  private Function1D<Double, Double> getCorrectedDividendAdjustmentIntegrand(final int dividendIndex, final PureImpliedVolatilitySurface volSurface,
      final EquityDividendsCurvesBundle divCurves, final AffineDividends dividends) {
    final double tau = dividends.getTau(dividendIndex);
    final double f = divCurves.getF(tau);
    final double d = divCurves.getD(tau);
    final double alpha = dividends.getAlpha(dividendIndex);
    final double fMd2 = FunctionUtils.square(f - d);

    final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        if (x == 0) {
          return 0.0;
        }
        final boolean isCall = x >= 1.0;
        final double vol = volSurface.getVolatility(tau, x);
        final double otmPurePrice = BlackFormulaRepository.price(1.0, x, tau, vol, isCall);
        final double s = (f - d) * x + d;
        final double weight = -fMd2 * alpha * (2 * s + alpha) / s / s / FunctionUtils.square(s + alpha);
        return otmPurePrice * weight;
      }
    };
    return integrand;
  }

  private Function1D<Double, Double> getCorrectedDividendAdjustmentIntegrand(final int dividendIndex, final BlackVolatilitySurfaceMoneyness volSurface,
      final AffineDividends dividends) {
    final double tau = dividends.getTau(dividendIndex);
    final double alpha = dividends.getAlpha(dividendIndex);
    final double f = volSurface.getForwardCurve().getForward(tau);

    final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        if (x == 0) {
          return 0.0;
        }
        final boolean isCall = x >= 1.0;
        final double vol = volSurface.getVolatilityForMoneyness(tau, x);
        final double otmPrice = BlackFormulaRepository.price(1.0, x, tau, vol, isCall);

        final double weight = -alpha * (2 * x * f + alpha) / x / x / FunctionUtils.square(x * f + alpha);
        return otmPrice * weight;
      }
    };
    return integrand;
  }

  private Function1D<Double, Double> getUncorrectedDividendAdjustmentIntegrand(final int dividendIndex, final PureImpliedVolatilitySurface volSurface,
      final EquityDividendsCurvesBundle divCurves, final AffineDividends dividends) {
    final double tau = dividends.getTau(dividendIndex);
    final double f = divCurves.getF(tau);
    final double d = divCurves.getD(tau);
    final double alpha = dividends.getAlpha(dividendIndex);
    final double beta = dividends.getBeta(dividendIndex);
    final double fMd2 = FunctionUtils.square(f - d);

    final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        if (x == 0) {
          return 0.0;
        }
        final boolean isCall = x >= 1.0;
        final double vol = volSurface.getVolatility(tau, x);
        final double otmPurePrice = BlackFormulaRepository.price(1.0, x, tau, vol, isCall);
        final double s = (f - d) * x + d;
        final double sPalpha = s + alpha;
        final double h = Math.log(s * (1 - beta) / sPalpha);
        final double dH = alpha / s / sPalpha;
        final double ddH = -alpha * (s + sPalpha) / s / s / sPalpha / sPalpha;
        final double weight = fMd2 * ((1 + h) * ddH + dH * dH);
        return otmPurePrice * weight;
      }
    };
    return integrand;
  }

  private Function1D<Double, Double> getUncorrectedDividendAdjustmentIntegrand(final int dividendIndex, final BlackVolatilitySurfaceMoneyness volSurface,
      final AffineDividends dividends) {
    final double tau = dividends.getTau(dividendIndex);
    final double alpha = dividends.getAlpha(dividendIndex);
    final double beta = dividends.getBeta(dividendIndex);
    final double f = volSurface.getForwardCurve().getForward(tau);

    final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        if (x == 0) {
          return 0.0;
        }
        final boolean isCall = x >= 1.0;
        final double vol = volSurface.getVolatilityForMoneyness(tau, x);
        final double otmPrice = BlackFormulaRepository.price(1.0, x, tau, vol, isCall);
        final double s = x * f;
        final double sPalpha = s + alpha;
        final double h = Math.log(s * (1 - beta) / sPalpha);
        final double dH = alpha / x / sPalpha;
        final double ddH = -alpha * (s + sPalpha) / x / x / sPalpha / sPalpha;
        final double weight = ((1 + h) * ddH + dH * dH);
        return otmPrice * weight;
      }
    };
    return integrand;
  }

  private double getCorrectedDividendAdjustment(final double s, final int dividendIndex, final AffineDividends dividends) {
    final double alpha = dividends.getAlpha(dividendIndex);
    final double beta = dividends.getBeta(dividendIndex);
    return Math.log(s * (1 - beta) / (s + alpha));
  }

  private double getUncorrectedDividendAdjustment(final double s, final int dividendIndex, final AffineDividends dividends) {
    final double alpha = dividends.getAlpha(dividendIndex);
    final double beta = dividends.getBeta(dividendIndex);
    final double h = Math.log(s * (1 - beta) / (s + alpha));
    return h + 0.5 * h * h;
  }

}
