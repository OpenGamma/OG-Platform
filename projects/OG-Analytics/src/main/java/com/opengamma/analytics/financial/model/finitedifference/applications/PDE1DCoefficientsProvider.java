/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DFullCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class PDE1DCoefficientsProvider {

  private static final Surface<Double, Double, Double> ZERO_SURFACE = ConstantDoublesSurface.from(0.0);
  private static final Surface<Double, Double, Double> UNITY_SURFACE = ConstantDoublesSurface.from(1.0);

  // ******************************************************************************************************************************
  // Backwards PDEs - the initial condition is the option payoff at expiry, T, and the PDE is solved backwards in time, t.
  // In this setup, the time coordinate is time-to-expiry tau = T-t, so this initial condition is at tau = 0
  // ******************************************************************************************************************************

  /**
   * Sets up a standard Black-Scholes PDE
   *  $$\frac{\partial V}{\partial \tau} - \frac{\sigma^2 s^2}{2} \frac{\partial^2 V}{\partial s^2} -(r-y)s \frac{\partial V}{\partial s} + rV = 0$$
   *  where the 'time' term $\tau$ is time to maturity
   * @param rate The rate, $r$
   * @param yield The yield $y$ <b>Note</b> this is NOT the cost-of-carry $b$, they are related by $b=r-y$
   * @param vol The volatility
   * @return a ConvectionDiffusionPDE1DStandardCofficients
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getBlackScholes(final double rate, final double yield, final double vol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];
        final double temp = s * vol;
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];
        return -s * (rate - yield);
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(rate));
  }

  /**
   * Sets up a  Black-Scholes PDE with term structure of rates, yield and volatility
   *  $$\frac{\partial V}{\partial \tau} - \frac{\sigma^2 s^2}{2} \frac{\partial^2 V}{\partial s^2} -(r-y)s \frac{\partial V}{\partial s} + rV = 0$$
   *  where the 'time' term $\tau$ is time to maturity
   * @param rate The rate, $r$
   * @param yield The yield (or cost-of-carry), $y$
   * @param vol The volatility
   * @return a ConvectionDiffusionPDE1DStandardCofficients
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getBlackScholes(final Curve<Double, Double> rate, final Curve<Double, Double> yield, final Curve<Double, Double> vol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double temp = s * vol.getYValue(t);
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        return -s * (rate.getYValue(t) - yield.getYValue(t));
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        return rate.getYValue(t);
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c));
  }

  /**
   * Set up a Black-Scholes PDE where the space variable is the log of the spot $x=\ln(s)$
   * $$\frac{\partial V}{\partial \tau} - \frac{\sigma^2}{2} \frac{\partial^2 V}{\partial x^2} -(\frac{\sigma^2}{2}+r-y) \frac{\partial V}{\partial x} + rV = 0$$
   * @param rate The rate, $r$
   * @param yield The yield (or cost-of-carry), $y$
   * @param vol  The volatility
   * @return a ConvectionDiffusionPDE1DStandardCofficients
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getLogBlackScholes(final double rate, final double yield, final double vol) {

    final double a = -vol * vol / 2;
    final double b = -a - (rate - yield);

    return new ConvectionDiffusionPDE1DStandardCoefficients(ConstantDoublesSurface.from(a), ConstantDoublesSurface.from(b), ConstantDoublesSurface.from(rate));
  }

  /**
   * This models the CEV process - the forward follows the SDE $df = \sigma f^\beta dW$ where $f(t,T) = \mathbb{E^T}[s_T|\mathcal{F}_t]$ and
   * is a Martingale. The corresponding PDE for the option price is
   * $$\frac{\partial V}{\partial \tau} - \frac{(\sigma^*)^2 f^{2\beta}}{2} \frac{\partial^2 V}{\partial f^2} + rV = 0$$
   * The term $r$ is yield to maturity - it can be set to zero and a discount factor applied to the option price instead.
   * @param zeroRate The zero rate
   * @param beta The beta
   * @param vol The volatility
   * @return A ConvectionDiffusionPDE1DStandardCofficients
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getCEV(final double zeroRate, final double beta, final double vol) {
    Validate.isTrue(beta >= 0.0, "Need beta >=0");

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];
        final double temp = vol * Math.pow(s, beta);
        return -0.5 * temp * temp;
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), ZERO_SURFACE, ConstantDoublesSurface.from(zeroRate));
  }

  /**
   * Classic (i.e. formulated in terms of constant instantaneous short rates) backwards PDE setup for option price under
   * a local volatility. The state variables are time-to-maturity and (spot) value of the underlying<p>
   * <b>Note</b> The local volatility is a function of calendar time, t, and spot, but since the time variable in this PDE is
   * $\tau = T-t$ where $T$ is the expiry, we must specify $T$ is get the correct local volatility at a given $\tau$.
   * @param rate The risk free rate (domestic risk free rate in FX case)
   * @param yield The dividend yield (for equity) or foreign risk free (for FX)
   * @param maturity the expiry (time-to-maturity)
   * @param localVol A local volatility surface - gives the instantaneous (log-normal) volatility as a function of time and
   * the value of the underlying at that time
   * @return The ConvectionDiffusionPDE1DStandardCofficients  to run through a PDE solver that will give the time-zero option price as a
   *  function of the time-zero value of the underlying
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getBackwardsLocalVol(final double rate, final double yield, final double maturity, final LocalVolatilitySurfaceStrike localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double tau = ts[0];
        final double s = ts[1];
        final double t = maturity - tau;
        final double temp = s * localVol.getVolatility(t, s);
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double s = ts[1];

        return -s * (rate - yield);
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(rate));
  }

  /**
   * Backwards PDE setup for option price under a local volatility. The state variables are time-to-maturity and
   * forward value of the underlying. <b>Note</b> the option price will be the forward (non-discounted) price. The PDE is
   * $$
   * \frac{\partial V}{\partial \tau} - \frac{1}{2}\sigma\left(t,f_{\tau}\frac{f(0,t)}{f(0,T)}\right)^2f(t,T)^2\frac{\partial^2 V}{\partial f_{\tau}^2}=0\\
   * \text{where} \quad f_{\tau} \equiv f(T-\tau,T)
   * $$
   * @param forwardCurve the time-zero forward curve, F(0,T), for the underlying
   * @param maturity the time-to-maturity
   * @param localVol A local volatility surface - gives the instantaneous (log-normal) volatility as a function of time and
   * the value of the underlying at that time<p>
   * <b> Note </b> Even though the PDE is expressed in terms of the forward rate, the local volatility MUST be specified in terms of
   * the spot, hence the term $f_{\tau}\frac{f(0,t)}{f(0,T)} = s_{tau}$ in the local volatility arguments
   * @return The data to run through a PDE solver that will give the time-zero forward option price as a function of the time-zero
   * value of the underlying
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getBackwardsLocalVol(final ForwardCurve forwardCurve, final double maturity, final LocalVolatilitySurfaceStrike localVol) {

    final double fT = forwardCurve.getForward(maturity);

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tf) {
        Validate.isTrue(tf.length == 2);
        final double tau = tf[0];
        final double f = tf[1];
        final double t = maturity - tau;
        final double ft = forwardCurve.getForward(t);

        final double temp = f * localVol.getVolatility(t, f * ft / fT); // NOTE: f * ft / fT = s, the spot
        return -0.5 * temp * temp;
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), ZERO_SURFACE, ZERO_SURFACE);
  }

  /**
   * Backwards PDE setup for option price under a local volatility. The state variables are time-to-maturity and
   * value of the underlying. The PDE is
   * @param instRiskFreeRate the instantaneous risk free rate, r_t
   * @param instCostOfCarry the instantaneous cost-of-carry, b_t = r_t - q_t, where q_t is the (instantaneous) yield
   * @param maturity the time-to-maturity
   * @param localVol A local volatility surface - gives the instantaneous (log-normal) volatility as a function of time and
   * the value of the underlying at that time
   * @return The data to run through a PDE solver that will give the time-zero option price as a function of the time-zero
   * value of the underlying
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getBackwardsLocalVol(final Curve<Double, Double> instRiskFreeRate, final Curve<Double, Double> instCostOfCarry, final double maturity,
      final LocalVolatilitySurfaceStrike localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double tau = ts[0];
        final double s = ts[1];
        final double t = maturity - tau;
        final double temp = s * localVol.getVolatility(t, s);
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double tau = ts[0];
        final double s = ts[1];
        final double t = maturity - tau;
        return -s * instCostOfCarry.getYValue(t);
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double tau = ts[0];
        final double t = maturity - tau;
        return instRiskFreeRate.getYValue(t);
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b),
        FunctionalDoublesSurface.from(c));
  }

  /**
   * Backwards PDE setup for option price under a local volatility parameterised by moneyness. The state variables are time-to-maturity and
   * forward value of the underlying. <b>Note</b> the option price will be the forward (non-discounted) price. The PDE is
   * $$
   * \frac{\partial V}{\partial \tau} - \frac{1}{2}\sigma\left(t,\frac{f_{\tau}}{f(0,T)}\right)^2f(t,T)^2\frac{\partial^2 V}{\partial f_{\tau}^2}=0\\
   * \text{where} \quad f_{\tau} \equiv f(T-\tau,T)
   * $$
   * @param maturity the time-to-maturity
   * @param localVol A local volatility surface - gives the instantaneous (log-normal) volatility as a function of time and
   * the moneyness at that time
   * @return The data to run through a PDE solver that will give the time-zero forward option price as a function of the time-zero
   * value of the underlying
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getBackwardsLocalVol(final double maturity, final LocalVolatilitySurfaceMoneyness localVol) {

    final ForwardCurve fc = localVol.getForwardCurve();
    final double f0 = fc.getForward(maturity);
    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tf) {
        Validate.isTrue(tf.length == 2);
        final double tau = tf[0];
        final double f = tf[1];
        final double t = maturity - tau;
        final double x = f / f0;

        final double temp = f * localVol.getVolatilityForMoneyness(t, x);
        return -0.5 * temp * temp;
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), ZERO_SURFACE, ZERO_SURFACE);
  }

  /**
   * Backwards PDE setup for option price under a local volatility parameterised by spot. The state variables are time-to-maturity, $\tau$, and
   * log-spot , $x = \log(s)$. <b>Note</b> the option price will be the forward (non-discounted) price. <P>The PDE is
   * $$
   * \frac{\partial V}{\partial \tau} - \frac{1}{2}\sigma\left(T-\tau,e^x\right)^2\frac{\partial^2 V}{\partial x^2} +
   * \left(\frac{1}{2}\sigma\left(T-\tau,e^x\right)^2 -r + y \right)\frac{\partial V}{\partial x} + rV=0\\
   * \text{where} \quad x \equiv \log S
   * $$
   * @param rate The risk free rate (domestic risk free rate in FX case)
   * @param yield The dividend yield (for equity) or foreign risk free (for FX)
   * @param maturity the time-to-maturity
   * @param localVol A local volatility surface - gives the instantaneous (log-normal) volatility as a function of time and spot
   * @return The data to run through a PDE solver that will give the time-zero option price as a function of the time-zero
   * value of the log-spot
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getLogBackwardsLocalVol(final double rate, final double yield, final double maturity, final LocalVolatilitySurfaceStrike localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        Validate.isTrue(tx.length == 2);
        final double tau = tx[0];
        final double x = tx[1];
        final double s = Math.exp(x);
        final double t = maturity - tau;

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
        return 0.5 * temp * temp - (rate - yield);
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(rate));
  }

  /**
   * Backwards PDE setup for option price under a local volatility parameterised by moneyness. The state variables are time-to-maturity, $\tau$, and
   * log-forward value of the underlying, $x$. <b>Note</b> the option price will be the forward (non-discounted) price. <P>The PDE is
   * $$
   * \frac{\partial V}{\partial \tau} - \frac{1}{2}\sigma\left(t,\frac{e^x}{f(0,T)}\right)^2\frac{\partial^2 V}{\partial x^2} +
   * \frac{1}{2}\sigma\left(t,\frac{e^x}{f(0,T)}\right)^2\frac{\partial V}{\partial x}=0\\
   * \text{where} \quad x \equiv \log f(T-\tau,T)
   * $$
   * @param maturity the time-to-maturity
   * @param localVol A local volatility surface - gives the instantaneous (log-normal) volatility as a function of time and moneyness
   * @return The data to run through a PDE solver that will give the time-zero <b>forward</b> option price as a function of the time-zero
   * value of the log-forward
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getLogBackwardsLocalVol(final double maturity, final LocalVolatilitySurfaceMoneyness localVol) {

    final ForwardCurve fc = localVol.getForwardCurve();
    final double f0T = fc.getForward(maturity);
    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        Validate.isTrue(tx.length == 2);
        final double tau = tx[0];
        final double x = tx[1];
        final double t = maturity - tau;
        final double ftT = Math.exp(x);
        final double temp = localVol.getVolatilityForMoneyness(t, ftT / f0T);
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
        final double ftT = Math.exp(x);
        final double temp = localVol.getVolatilityForMoneyness(t, ftT / f0T);
        return 0.5 * temp * temp;
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ZERO_SURFACE);
  }

  /**
   * Backwards PDE setup for option price under a local volatility parameterised by spot. The state variables are time-to-maturity, $\tau$, and
   * log-forward value of the underlying, $x$. <b>Note</b> the option price will be the forward (non-discounted) price. <P>The PDE is
   * $$
   * \frac{\partial V}{\partial \tau} - \frac{1}{2}\sigma\left(t,\frac{e^xf(0,T-\tau)}{f(0,T)}\right)^2\frac{\partial^2 V}{\partial x^2} +
   * \frac{1}{2}\sigma\left(t,\frac{e^xf(0,T-\tau)}{f(0,T)}\right)^2\frac{\partial V}{\partial x}=0\\
   * \text{where} \quad x \equiv \log f(T-\tau,T)
   * $$
   * @param forwardCurve Forward Curve
   * @param maturity the time-to-maturity
   * @param localVol A local volatility surface - gives the instantaneous (log-normal) volatility as a function of time and spot
   * @return The data to run through a PDE solver that will give the time-zero <b>forward</b> option price as a function of the time-zero
   * value of the log of the forward
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getLogBackwardsLocalVol(final ForwardCurve forwardCurve, final double maturity, final LocalVolatilitySurfaceStrike localVol) {

    final double f0T = forwardCurve.getForward(maturity);

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        Validate.isTrue(tx.length == 2);
        final double tau = tx[0];
        final double x = tx[1];
        final double ftT = Math.exp(x);
        final double t = maturity - tau;
        final double f0t = forwardCurve.getForward(t);

        final double temp = localVol.getVolatility(t, ftT * f0t / f0T); // NOTE: f * ft / fT = s, the spot
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
        final double ftT = Math.exp(x);
        final double f0t = forwardCurve.getForward(t);
        final double temp = localVol.getVolatility(t, ftT * f0t / f0T);
        return 0.5 * temp * temp;
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ZERO_SURFACE);
  }

  // ******************************************************************************************************************************
  // Forward PDEs - the initial condition is at expiry, T = 0, and the PDE is solved forward in expiry time, T (calendar time t remains zero).
  // In this setup the coordinates are T and strike, k, so solving the PDE gives a set rather that a single option price
  // (NOTE: only option prices with European call and put payoffs can be transformed into a forward PDE)
  // ******************************************************************************************************************************

  /**
   * Classic (i.e. formulated in terms of constant instantaneous short rates) forward PDE setup for option price under a BlackSholes framework.
   * The state variables are time-to-maturity, $T$ and strike, $k$. The PDE is
   * $$
   * \frac{\partial V(T,k)}{\partial T} - \frac{1}{2}\sigma^2 k^2 \frac{\partial^2 V(T,k)}{\partial k^2} + k(r-y) \frac{\partial V(T,k)}{\partial k} +yV(T,k)=0
   * $$
   * @param rate $r$ The risk free rate (domestic risk free rate in FX case)
   * @param yield $y$ The dividend yield (for equity) or foreign risk free (for FX)
   * @param vol $\sigma$ The volatility
   * @return The data to run through a PDE solver that will give the option price as a function of strike and expiry
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getForwardBlackSholes(final double rate, final double yield, final double vol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double k = tk[1];
        final double temp = k * vol;
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double k = tk[1];
        return k * (rate - yield);
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(yield));
  }

  /**
   * Classic (i.e. formulated in terms of constant instantaneous short rates) forwards  PDE setup for option price under a local volatility.
   * The state variables are time-to-maturity, $T$ and strike, $k$. The PDE is
   * $$
   * \frac{\partial V(T,k)}{\partial T} - \frac{1}{2}\sigma(T,k)^2 k^2 \frac{\partial^2 V(T,k)}{\partial k^2} + k(r-y) \frac{\partial V(T,k)}{\partial k} +yV(T,k)=0
   * $$
   * @param rate $r$ The risk free rate (domestic risk free rate in FX case)
   * @param yield $y$ The dividend yield (for equity) or foreign risk free (for FX)
   * @param localVol $\sigma(T,k)$ The local volatility
   * @return The data to run through a PDE solver that will give the option price as a function of strike and expiry
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getForwardLocalVolatility(final double rate, final double yield, final LocalVolatilitySurfaceStrike localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double t = tk[0];
        final double k = tk[1];
        final double temp = k * localVol.getVolatility(t, k);
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double k = tk[1];
        return k * (rate - yield);
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(yield));
  }

  /**
   * Set up for running forward PDE with local volatility parameterised by moneyness (i.e. m = strike/forward). The option
   * prices will be in a normalised form, and will have to be multiplied by the discount factor AND the forward to recover
   * the actual option price. The PDE is
   * $$
   * \frac{\partial V(T,m)}{\partial T} - \frac{1}{2}\sigma\left(T,m\right)^2 m^2 \frac{\partial^2 V(T,m)}{\partial m^2}=0\\
   * \text{where}\quad m \equiv \frac{k}{f(0,T)}
   * $$
   * @param localVol A local volatility surface parametrised by expiry and moneyness (=strike/forward)
   * @return The data to run through a PDE solver that will give the modified option price (the true option price is this
   * multiplied by the discount factor AND the forward) as a function of expiry and <b>moneyness</b>
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getForwardLocalVol(final LocalVolatilitySurfaceMoneyness localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tm) {
        Validate.isTrue(tm.length == 2);
        final double t = tm[0];
        final double m = tm[1];

        final double vol = localVol.getVolatilityForMoneyness(t, m);
        final double temp = m * vol;
        return -0.5 * temp * temp;
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), ZERO_SURFACE, ZERO_SURFACE);
  }

  /**
   * Set up for running forward PDE with local volatility parameterised by strike. The option
   * prices will be in a normalised form, and will have to be multiplied by the discount factor AND the forward to recover
   * the actual option price. <P> The PDE is
   * $$
   * \frac{\partial V(T,m)}{\partial T} - \frac{1}{2}\sigma\left(T,mf(0,T)\right)^2 m^2 \frac{\partial^2 V(T,m)}{\partial m^2}=0\\
   * \text{where}\quad m \equiv \frac{k}{f(0,T)}
   * $$ <p>
   * <b>Note</b> The coordinates used in the PDE are expiry (T) and <b>moneyness</b> (m), hence the solution will be in these coordinates
   * @param forwardCurve the forward curve
   * @param localVol A local volatility surface
   * @return The data to run through a PDE solver that will give the modified option price (the true option price is this
   * multiplied by the discount factor AND the forward) as a function of expiry and <b>moneyness</b>
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getForwardLocalVol(final ForwardCurve forwardCurve, final LocalVolatilitySurfaceStrike localVol) {
    final LocalVolatilitySurfaceMoneyness lvm = LocalVolatilitySurfaceConverter.toMoneynessSurface(localVol, forwardCurve);
    return getForwardLocalVol(lvm);
  }

  // ******************************************************************************************************************************
  // Forward PDEs for the transition density $P(t,S_t,T,S_T)$
  // ******************************************************************************************************************************

  /**
   * The Fokker-Plank equation with a deterministic (time dependent) short-rate and a local volatility (i.e. a instantaneous volatility that is
   * a function of time and spot). If the SDE for the underlying is $\frac{dS_t}{S_t}=r_t dt + \sigma(t,S_t)^2dW_t$, then the PDE for the transition
   * density $P(t,S_t;T,S_T)$ is
   * $$
   *  \frac{\partial P}{\partial T} - \frac{1}{2}\frac{\partial^2[ \sigma(T,S)^2S^2P]}{\partial S^2} +
   *   r_T \frac{\partial[ S^2 P]}{\partial S}
   * $$
   * @param shortRate Deterministic (time dependent) short-rate
   * @param localVol Local Volatility
   * @return Description of Fokker-Plank PDE
   */
  public ConvectionDiffusionPDE1DFullCoefficients getFokkerPlank(final Curve<Double, Double> shortRate, final LocalVolatilitySurfaceStrike localVol) {

    final Function<Double, Double> alpha = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double temp = localVol.getVolatility(t, s) * s;

        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> beta = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        return shortRate.getYValue(t) * s;
      }
    };

    return new ConvectionDiffusionPDE1DFullCoefficients(UNITY_SURFACE, UNITY_SURFACE, ZERO_SURFACE, FunctionalDoublesSurface.from(alpha), FunctionalDoublesSurface.from(beta));

  }

  /**
   * The Fokker-Plank equation with a deterministic (time dependent) short-rate and a local volatility (i.e. a instantaneous volatility that is
   * a function of time and spot), but written in the form.<P>
   * $$
   * \frac{\partial P}{\partial t}+ a(t,x)\frac{\partial^2 P}{\partial x^2}+b(t,x)\frac{\partial P}{\partial x2}+c(t,x)V=0
   * $$
   * @param shortRate Deterministic (time dependent) short-rate
   * @param localVol Local Volatility
   * @return Description of Fokker-Plank PDE
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getFokkerPlankInStandardCoefficients(final Curve<Double, Double> shortRate, final LocalVolatilitySurfaceStrike localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double sigma = localVol.getVolatility(t, s) * s;
        return -0.5 * sigma * sigma;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double lvDiv = getLocalVolFirstDiv(localVol, t, s);
        final double lv = localVol.getVolatility(t, s);
        return s * (shortRate.getYValue(t) - 2 * lv * (s * lvDiv + lv));
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double lv1Div = getLocalVolFirstDiv(localVol, t, s);
        final double lv2Div = getLocalVolSecondDiv(localVol, t, s);
        final double lv = localVol.getVolatility(t, s);
        final double temp1 = (lv + s * lv1Div);
        final double temp2 = lv * s * (s * lv2Div + 2 * lv1Div);

        return shortRate.getYValue(t) - temp1 * temp1 - temp2;
      }
    };

    return new ConvectionDiffusionPDE1DStandardCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c));

  }

  // TODO handle with a central calculator
  private static double getLocalVolFirstDiv(final LocalVolatilitySurfaceStrike localVol, final double t, final double s) {
    final double eps = 1e-4;
    final double up = localVol.getVolatility(t, s + eps);
    final double down = localVol.getVolatility(t, s - eps);
    return (up - down) / 2 / eps;
  }

  private static double getLocalVolSecondDiv(final LocalVolatilitySurfaceStrike localVol, final double t, final double s) {
    final double eps = 1e-4;
    final double up = localVol.getVolatility(t, s + eps);
    final double mid = localVol.getVolatility(t, s);
    final double down = localVol.getVolatility(t, s - eps);
    return (up + down - 2 * mid) / eps / eps;
  }
}
