/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.finitedifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.financial.model.finitedifference.CoupledPDEDataBundle;
import com.opengamma.financial.model.finitedifference.ExtendedConvectionDiffusionPDEDataBundle;
import com.opengamma.financial.model.finitedifference.ExtendedCoupledPDEDataBundle;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.AbsoluteLocalVolatilitySurface;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class PDEDataBundleProvider {

  /**
   * Sets up a standard Black-Scholes PDE
   *  $$\frac{\partial V}{\partial \tau} - \frac{\sigma^2 s^2}{2} \frac{\partial^2 V}{\partial s^2} -rs \frac{\partial V}{\partial s} + rV = 0$$
   *  where the 'time' term $\tau$ is time to maturity, so the initial state is at $\tau = 0$ is a standard option payoff
   * @param vol The volatility
   * @param rate The rate
   * @param strike The strike
   * @param isCall is a call
   * @return a convection data bundle
   */
  public ConvectionDiffusionPDEDataBundle getBackwardsBlackScholes(final double vol, final double rate, final double strike, final boolean isCall) {

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
        return -s * rate;
      }
    };

    return new ConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(rate), new EuropeanPayoff(strike, isCall));
  }

  public ConvectionDiffusionPDEDataBundle getBackwardsBlackScholesSpecial(final double vol, final double rate, final double strike, final boolean isCall) {

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
        final double t = ts[0];
        final double s = ts[1];
        if (s == 0) {
          return 0.0;
        }
        final double df = Math.exp(-t * rate);
        final double forward = s / df;
        final double price = df * BlackFormulaRepository.price(forward, strike, t, vol, true);
        double dOverP;
        if (price == 0.0) {
          final double sigmaRootT = vol * Math.sqrt(t);
          final double d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
          final double d2 = d1 - sigmaRootT;
          dOverP = (1 + d2 * d2) / sigmaRootT / forward / (sigmaRootT - 2 * d1);
        } else {
          final double delta = BlackFormulaRepository.delta(forward, strike, t, vol, true);
          dOverP = delta / price;
        }
        final double temp = s * vol;
        return -(temp * temp * dOverP + s * rate);
      }
    };

    final Function1D<Double, Double> constant = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return 1.0;
      }
    };

    return new ConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(0), constant);
  }

  public ConvectionDiffusionPDEDataBundle getBackwardsBlackScholesSpecial2(final double vol, final double rate, final double strike, final boolean isCall) {
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
        return -s * rate;
      }
    };

    final Function1D<Double, Double> constant = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return 0.0;
      }
    };

    return new ConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(rate), constant);

  }

  /**
   * Set up a Black-Scholes PDE where the space variable is the log of the spot $x=\ln(s)$
   * $$\frac{\partial V}{\partial \tau} - \frac{\sigma^2}{2} \frac{\partial^2 V}{\partial x^2} -(\frac{\sigma^2}{2}+r) \frac{\partial V}{\partial x} + rV = 0$$
   * @param vol  The volatility
   * @param rate The rate
   * @param strike The strike
   * @param isCall is a call
   * @return a convection data bundle
   */
  public ConvectionDiffusionPDEDataBundle getBackwardsLogBlackScholes(final double vol, final double rate, final double strike, final boolean isCall) {

    final double a = -vol * vol / 2;
    final double b = -a - rate;

    final Function1D<Double, Double> payoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        final double s = Math.exp(x);
        if (isCall) {
          return Math.max(s - strike, 0);
        }
        return Math.max(strike - s, 0);

      }
    };

    return new ConvectionDiffusionPDEDataBundle(ConstantDoublesSurface.from(a), ConstantDoublesSurface.from(b), ConstantDoublesSurface.from(rate), payoff);
  }

  /**
   * This models the forward process $df = \sigma f^\beta dW$ where $f(t,T) = \mathbb{E^T}[s_T|\mathcal{F}_t]$ and is a Martingale.
   * The corresponding PDE for the option price is $$\frac{\partial V}{\partial \tau} - \frac{(\sigma^*)^2 f^{2\beta}}{2} \frac{\partial^2 V}{\partial f^2} + rV = 0$$
   * The term $r$ is yield to maturity - it can be set to zero and a discount factor applied to the option price instead.
   * @param vol The volatility
   * @param rate The yield to maturity
   * @param strike The strike
   * @param beta The beta
   * @param isCall Is a call
   * @return A convection data bundle
   */
  public ConvectionDiffusionPDEDataBundle getBackwardsCEV(final double vol, final double rate, final double strike, final double beta, final boolean isCall) {
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

    return new ConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), ConstantDoublesSurface.from(0.0), ConstantDoublesSurface.from(rate), new EuropeanPayoff(strike, isCall));
  }

  /**
   * Set up a backwardsPDE for a model where the underlying follows the SDE $ ds = \mu(t)sdt + \sigma(t,s) s^{\beta} dW$, with local volatility
   *  $\sigma(t,s)$. The resulting PDE is $$\frac{\partial V}{\partial \tau} - \sigma(t,s)\frac{s^{2\beta}}{2} \frac{\partial^2 V}{\partial s^2}
   *  -\mu(t)s \frac{\partial V}{\partial s} + \mu(t) V = 0$$
   *  As usual with the backwards PDEs, 'time' is time-to-maturity ($\tau$) and thus starts at zero (i.e. at maturity). The initial condition is a European option payoff
   *  (so $V(0,s) = (\omega(s-k))^+$), where $\omega$ is +1 for call and -1 for put). Real (i.e. calendar) time, $t$, appears in the local volatility and drift terms with
   *  $t = T - \tau$ where $T$ is the maturity. The solution at $t = 0$ $(\tau=T)$ is the option price for a particular spot.
   * @param forward The forward curve TODO the assumption here is that the drift $\mu(t)$ is the same as the short rate used for discounting (both of which are taken off
   * the forward curve)- this is fine for non-dividend paying equity but not for FX
   * @param strike The strike
   * @param maturity The maturity (in years)
   * @param beta The CEV parameter
   * @param isCall True for call
   * @param localVol Local Volatility
   * @return A convection diffusion data bundle
   */
  public ConvectionDiffusionPDEDataBundle getBackwardsLocalVol(final ForwardCurve forward, final double strike, final double maturity, final double beta, final boolean isCall,
      final AbsoluteLocalVolatilitySurface localVol) {
    Validate.isTrue(beta >= 0.0, "Need beta >=0");
    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double tau = ts[0];
        final double s = ts[1];
        final double t = maturity - tau;
        final double temp = Math.pow(s, beta) * localVol.getVolatility(t, s);
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
        return -s * forward.getDrift(t);
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double tau = ts[0];
        final double t = maturity - tau;
        return forward.getDrift(t);
      }
    };

    final Function1D<Double, Double> payoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        if (isCall) {
          return Math.max(0, x - strike);
        }
        return Math.max(0, strike - x);
      }
    };

    return new ConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), payoff);
  }

  public ConvectionDiffusionPDEDataBundle getForwardLocalVol(final ForwardCurve forward, final double beta, final boolean isCall,
      final AbsoluteLocalVolatilitySurface localVol) {
    Validate.isTrue(beta >= 0.0, "Need beta >=0");
    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double t = tk[0];
        final double k = tk[1];

        final double temp = Math.pow(k, beta) * localVol.getVolatility(t, k);
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double t = tk[0];
        final double k = tk[1];
        return k * forward.getDrift(t);
      }
    };

    final Function1D<Double, Double> payoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double k) {
        if (isCall) {
          return Math.max(0, forward.getSpot() - k);
        }
        return Math.max(0, k - forward.getSpot());
      }
    };

    return new ConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), ConstantDoublesSurface.from(0), payoff);
  }

  public ExtendedConvectionDiffusionPDEDataBundle getFokkerPlank(final ForwardCurve forward, final double beta, final AbsoluteLocalVolatilitySurface localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return -1.0;
      }
    };

    final Function<Double, Double> aStar = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double temp = localVol.getVolatility(t, s) * Math.pow(s, beta);

        return 0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        return s * forward.getDrift(t);
      }
    };

    final Function<Double, Double> bStar = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return 1.0;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];

        return forward.getDrift(t);
      }
    };

    //using a log-normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {
      private final double _volRootTOffset = 0.01;

      @Override
      public Double evaluate(final Double s) {
        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward.getSpot());
        final NormalDistribution dist = new NormalDistribution(0, _volRootTOffset);
        return dist.getPDF(x) / s;
      }
    };

    return new ExtendedConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), FunctionalDoublesSurface.from(aStar),
        FunctionalDoublesSurface.from(bStar), initialCondition);

  }

  /**
   * Set up a backwards coupled PDE for a model where the underlying follows the SDE $ds = \mu(t)sdt + \sigma_i s^{\beta} dW$, with local volatility overlay
   *  $\sigma(t,s)$ and level $\sigma_i $ which takes on one of two values depending on the state of the continuous 2-state hidden Markov chain. The
   *  resulting pair of PDEs is $$\begin{eqnarray}
   *  \frac{\partial V_1}{\partial \tau} - \frac{\sigma_1^2 s^{2\beta}}{2} \frac{\partial^2 V_1}{\partial s^2}
   *  -\mu(t)s \frac{\partial V_1}{\partial s} + (\mu(t) + \lambda_{12})V_1 - \lambda_{12} V_2= 0 \\
   *  \frac{\partial V_2}{\partial \tau} - \frac{\sigma_2^2 s^{2\beta}}{2} \frac{\partial^2 V_2}{\partial s^2} -\mu(t)s \frac{\partial V_2}{\partial s}
   *  + (\mu(t) + \lambda_{21})V_2 - \lambda_{21} V_1= 0
   *  \end{eqnarray}$$
   *  As usual with the backwards PDEs, 'time' is time-to-maturity ($\tau$) and thus starts at zero (i.e. at maturity). The initial condition is a call payoff (so
   *  $V_1(0,s) = V_2(0,s) = (s-k)^+$). Real (i.e. calendar) time, $t$, appears in the drift term with $t = T - \tau$ where $T$
   *  is the maturity. The solution at $t=0$ $(\tau=T)$ for $V_1$ and $V_2$ is the option price if the Markov chain started in state 1 or 2. If the
   *  initial state of the Markov chain is unknown (but the probability of being in a state is known), then the option price is the probability weighting of the two solutions.
   * @param forward The forward curve
   * @param strike The strike
   * @param maturity The option maturity (in years)
   * @param data Data relating to the Markov chain
   * @return a pair of convection diffusion data bundles
   */
  public CoupledPDEDataBundle[] getCoupledBackwardsPair(final ForwardCurve forward, final double strike, final double maturity, final TwoStateMarkovChainDataBundle data) {
    final AbsoluteLocalVolatilitySurface localVolOverlay = new AbsoluteLocalVolatilitySurface(ConstantDoublesSurface.from(1.0));
    return getCoupledBackwardsPair(forward, strike, maturity, data, localVolOverlay);
  }

  /**
   * Set up a backwards coupled PDE for a model where the underlying follows the SDE $ ds = \mu(t)sdt + \sigma(t,s)\sigma_i s^{\beta} dW$, with local volatility overlay
   *  $\sigma(t,s)$ and level $\sigma_i $ which takes on one of two values depending on the state of the continuous 2-state hidden Markov chain. The
   *  resulting pair of PDEs is $$\begin{eqnarray}
   *  \frac{\partial V_1}{\partial \tau} - \sigma(t,s)\frac{\sigma_1^2 s^{2\beta}}{2} \frac{\partial^2 V_1}{\partial s^2}
   *  -\mu(t)s \frac{\partial V_1}{\partial s} + (\mu(t) + \lambda_{12})V_1 - \lambda_{12} V_2= 0 \\
   * \frac{\partial V_2}{\partial \tau} - \sigma(t,s)\frac{\sigma_2^2 s^{2\beta}}{2} \frac{\partial^2 V_2}{\partial s^2} -\mu(t)s \frac{\partial V_2}{\partial s}
   * + (\mu(t) + \lambda_{21})V_2 - \lambda_{21} V_1= 0
   * \end{eqnarray}$$
   *  As usual with the backwards PDEs, 'time' is time-to-maturity ($\tau$) and thus starts at zero (i.e. at maturity). The initial condition is a call payoff (so
   *  $V_1(0,s) = V_2(0,s) = (s-k)^+$). Real (i.e. calendar) time, $t$, appears in the local volatility and drift terms with $t = T - \tau$ where $T$
   *  is the maturity. The solution at $t = 0$ $(\tau=T)$ for $V_1$ and $V_2$ is the option price if the Markov chain started in state 1 or 2. If the
   *  initial state of the Markov chain is unknown (but the probability of being in a state is know), then the option price is the probability weighting of the two solutions.
   * @param forward The forward curve
   * @param strike The strike
   * @param maturity The option maturity (in years)
   * @param data Data relating to the Markov chain
   * @param localVolOverlay The local vol term
   * @return a pair of convection diffusion data bundles
   */
  public CoupledPDEDataBundle[] getCoupledBackwardsPair(final ForwardCurve forward, final double strike, final double maturity, final TwoStateMarkovChainDataBundle data,
      final AbsoluteLocalVolatilitySurface localVolOverlay) {
    Validate.notNull(forward, "null forward");
    Validate.notNull(data, "null data");
    Validate.notNull(localVolOverlay, "null localVolOverlay");

    final CoupledPDEDataBundle[] res = new CoupledPDEDataBundle[2];
    res[0] = getCoupledBackwardsPDE(forward, data.getVol1(), strike, maturity, data.getLambda12(), data.getBeta1(), localVolOverlay);
    res[1] = getCoupledBackwardsPDE(forward, data.getVol2(), strike, maturity, data.getLambda21(), data.getBeta2(), localVolOverlay);
    return res;
  }

  /**
   *  Set up a forward coupled PDE for a model where the underlying follows the SDE $ds = \mu(t)sdt + \sigma_i s^{\beta} dW$, with
   *  level  $\sigma_i$ which takes on one of two values depending on the state of the continuous 2-state hidden Markov chain,
   *  and the payoff is a standard European call. The coupled PDEs are in maturity ($T$) and strike ($k$) coordinates and read
   *   $$\begin{eqnarray}\frac{\partial V_1}{\partial T}  - \frac{\sigma_1^2 k^{2\beta}}{2}
   *   \frac{\partial^2 V_1}{\partial k^2} -\mu(T)k \frac{\partial V_1}{\partial k} + \lambda_{12}V_1 - \lambda_{21} V_2= 0 \\
   *   \frac{\partial V_2}{\partial T}  - \frac{\sigma_2^2 k^{2\beta}}{2}
   *  \frac{\partial^2 V_2}{\partial k^2} -\mu(T)k \frac{\partial V_2}{\partial k} + \lambda_{21}V_2 - \lambda_{12} V_1= 0
   *  \end{eqnarray}$$
   *  The initial condition is $V_1(0,k) = p_1(0)(s_0-k)^+$ and $V_2(0,k) = p_2(0)(s_0-k)^+$ where $p_1(0)$ and $p_2(0)$ are the probabilities
   *  of the Markov chain starting in a given state and $s_0$ is the spot. The option value at a particular maturity ($T$) and strike ($k$) is given by
   *   $V(T,k) = V_1(T,k) + V_2(T,k)$
   * @param forward  The forward curve
   * @param data Data relating to the Markov chain
   * @return a pair of convection diffusion data bundles
   */
  public CoupledPDEDataBundle[] getCoupledForwardPair(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data) {
    final AbsoluteLocalVolatilitySurface localVolOverlay = new AbsoluteLocalVolatilitySurface(ConstantDoublesSurface.from(1.0));
    return getCoupledForwardPair(forward, data, localVolOverlay);
  }

  /**
   *  Set up a forward coupled PDE for a model where the underlying follows the SDE $ ds = \mu(t)sdt + \sigma(t,s)\sigma_i s^{\beta} dW$, with local volatility overlay
   *  $\sigma(t,s)$ and level $\sigma_i $ which takes on one of two values depending on the state of the continuous 2-state hidden Markov chain,
   *  and the payoff is a standard European call. The coupled PDEs are in maturity (T) and strike (k) coordinates and read
   *   $$\begin{eqnarray}\frac{\partial V_1}{\partial T}  - \sigma(T,k)\frac{\sigma_1^2 k^{2\beta}}{2}
   *   \frac{\partial^2 V_1}{\partial k^2} -\mu(T)k \frac{\partial V_1}{\partial k} + \lambda_{12}V_1 - \lambda_{21} V_2= 0 \\
   *   \frac{\partial V_2}{\partial T}  - \sigma(T,k)\frac{\sigma_2^2 k^{2\beta}}{2}
   *  \frac{\partial^2 V_2}{\partial k^2} -\mu(T)k \frac{\partial V_2}{\partial k} + \lambda_{21}V_2 - \lambda_{12} V_1= 0 \end{eqnarray}$$
   *  The initial condition is $V_1(0,k) = p_1(0)(s_0-k)^+$ and $V_2(0,k) = p_2(0)(s_0-k)^+$ where $p_1(0)$ and $p_2(0)$ are the probabilities
   *  of the Markov chain starting in a given state and $s_0$ is the spot. The option value at a particular maturity ($T$) and strike ($k$) is given by
   *   $V(T,k) = V_1(T,k) + V_2(T,k)$
   * @param forward  The forward curve
   * @param data Data relating to the Markov chain
   * @param localVolOverlay The local vol term
   * @return a pair of convection diffusion data bundles
   */
  public CoupledPDEDataBundle[] getCoupledForwardPair(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data, final AbsoluteLocalVolatilitySurface localVolOverlay) {
    Validate.notNull(forward, "null forward");
    Validate.notNull(data, "null data");
    Validate.notNull(localVolOverlay, "null localVolOverlay");

    final CoupledPDEDataBundle[] res = new CoupledPDEDataBundle[2];
    res[0] = getCoupledForwardPDE(forward, data.getVol1(), data.getLambda12(), data.getLambda21(), data.getP0(), data.getBeta1(), localVolOverlay);
    res[1] = getCoupledForwardPDE(forward, data.getVol2(), data.getLambda21(), data.getLambda12(), 1.0 - data.getP0(), data.getBeta2(), localVolOverlay);
    return res;
  }

  /**
   * Set up a coupled Fokker-Plank PDE for a model where the underlying follows the SDE $ ds = \mu(t)sdt + \sigma_i s^{\beta} dW$, with  level
   *  $\sigma_i$ which takes on one of two values depending on the state of the continuous 2-state hidden Markov chain. The resultant PDE is
   *  $$\begin{eqnarray}\frac{\partial \rho_1}{\partial t}  -  \frac{\partial^2 }{\partial s^2}\left[\frac{\sigma_1^2 s^{2\beta}}{2}\rho_1 \right] -
   *  \frac{\partial}{\partial s}\left[\mu(t)s\rho_1\right] + \lambda_{12}\rho_1 - \lambda_{21}\rho_2= 0 \\
   *  \frac{\partial \rho_2}{\partial t}  - \frac{\partial^2 }{\partial s^2}\left[\frac{\sigma_2^2 s^{2\beta}}{2}\rho_2 \right] -
   *  \frac{\partial }{\partial s}\left[\mu(t)s\rho_2\right] + \lambda_{21}\rho_2 - \lambda_{12} \rho_1= 0
   *  \end{eqnarray}$$ where $\rho_i(t,s)$ is the probabilty density
   *  of being in state $i$ with a value of the underlying $s$ at time $t$. The inital state is $\rho_i(0,s) = p_i(0)\delta(s-s_0)$ where $p_i(0)$
   *  is the probability of the Markov chain starting in state $i$ and $s_0$ is the spot. Since a delta function cannot be used directly it is replaced with a
   *  log-normal distrubution (of $s/s_0$) with a standard deviation of 0.01;
   * @param forward The forward curve
   * @param data Data relating to the Markov chain
   * @return a pair of extended convection diffusion data bundles
   */
  public ExtendedCoupledPDEDataBundle[] getCoupledFokkerPlankPair(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data) {
    final AbsoluteLocalVolatilitySurface localVolOverlay = new AbsoluteLocalVolatilitySurface(ConstantDoublesSurface.from(1.0));
    return getCoupledFokkerPlankPair(forward, data, localVolOverlay);
  }

  /**
   * Set up a coupled Fokker-Plank PDE for a model where the underlying follows the SDE $ ds = \mu(t)sdt + \sigma(t,s)\sigma_i s^{\beta} dW$, with local volatility overlay
   *  $\sigma(t,s)$ and level $\sigma_i $ which takes on one of two values depending on the state of the continuous 2-state hidden Markov chain.
   *  The resultant PDE is
   *  $$\begin{eqnarray}\frac{\partial \rho_1}{\partial t} - \frac{\partial^2 }{\partial s^2}\left[ \sigma(t,s)\frac{\sigma_1^2 s^{2\beta}}{2}\rho_1 \right] -
   *  \frac{\partial}{\partial s}\left[\mu(t)s\rho_1\right] + \lambda_{12}\rho_1 - \lambda_{21}\rho_2= 0 \\
   *  \frac{\partial \rho_2}{\partial t} - \frac{\partial^2 }{\partial s^2}\left[ \sigma(t,s)\frac{\sigma_2^2 s^{2\beta}}{2}\rho_2 \right] -
   *  \frac{\partial }{\partial s}\left[\mu(t)s\rho_2\right] + \lambda_{21}\rho_2 - \lambda_{12} \rho_1= 0
   *  \end{eqnarray}$$ where $\rho_i(t,s)$ is the probabilty density
   *  of being in state $i$ with a value of the underlying $s$ at time $t$. The initial state is $\rho_i(0,s) = p_i(0)\delta(s-s_0)$ where $p_i(0)$
   *  is the probability of the Markov chain starting in state $i$ and $s_0$ is the spot. Since a delta function cannot be used directly it is replaced with a
   *  log-normal distrubution (of $s/s_0$) with a standard deviation of 0.01;
   * @param forward The forward curve
   * @param data Data relating to the Markov chain
   * @param localVolOverlay  The local vol term
   * @return a pair of extended convection diffusion data bundles
   */
  public ExtendedCoupledPDEDataBundle[] getCoupledFokkerPlankPair(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data, final AbsoluteLocalVolatilitySurface localVolOverlay) {
    Validate.notNull(forward, "null forward");
    Validate.notNull(data, "null data");
    Validate.notNull(localVolOverlay, "null localVolOverlay");

    final ExtendedCoupledPDEDataBundle[] res = new ExtendedCoupledPDEDataBundle[2];
    res[0] = getCoupledFokkerPlank(forward, data.getVol1(), data.getLambda12(), data.getLambda21(), data.getP0(), data.getBeta1(), localVolOverlay);
    res[1] = getCoupledFokkerPlank(forward, data.getVol2(), data.getLambda21(), data.getLambda12(), 1.0 - data.getP0(), data.getBeta2(), localVolOverlay);

    return res;
  }

  private CoupledPDEDataBundle getCoupledBackwardsPDE(final ForwardCurve forward, final double vol, final double strike, final double maturity, final double lambda, final double beta,
      final AbsoluteLocalVolatilitySurface localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double tau = ts[0];
        final double s = ts[1];
        final double t = maturity - tau;
        final double temp = vol * Math.pow(s, beta) * localVol.getVolatility(t, s);
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
        return -s * forward.getDrift(t);
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double tau = ts[0];
        final double t = maturity - tau;
        return forward.getDrift(t) + lambda;
      }
    };

    final Function1D<Double, Double> payoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return Math.max(0, x - strike);
      }
    };

    return new CoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), -lambda, payoff);
  }

  private CoupledPDEDataBundle getCoupledForwardPDE(final ForwardCurve forward, final double vol, final double lambda1, final double lambda2, final double initialProb, final double beta,
      final AbsoluteLocalVolatilitySurface localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double t = tk[0];
        final double k = tk[1];
        final double temp = vol * Math.pow(k, beta) * localVol.getVolatility(t, k);
        return -0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        final double k = tk[1];
        return k * forward.getDrift(beta);
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return lambda1;
      }
    };

    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double k) {
        return initialProb * Math.max(0, forward.getSpot() - k);
      }
    };

    return new CoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), -lambda2, initialCondition);
  }

  private ExtendedCoupledPDEDataBundle getCoupledFokkerPlank(final ForwardCurve forward, final double vol, final double lambda1, final double lambda2, final double initialProb, final double beta,
      final AbsoluteLocalVolatilitySurface localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return -1.0;
      }
    };

    final Function<Double, Double> aStar = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double temp = localVol.getVolatility(t, s) * vol * Math.pow(s, beta);

        return 0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        return s * forward.getDrift(t);
      }
    };

    final Function<Double, Double> bStar = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return 1.0;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];

        return forward.getDrift(t) + lambda1;
      }
    };

    //using a log-normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {
      private final double _volRootTOffset = 0.01;

      @Override
      public Double evaluate(final Double s) {
        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward.getSpot());
        final NormalDistribution dist = new NormalDistribution(0, _volRootTOffset);
        return initialProb * dist.getPDF(x) / s;
      }
    };

    return new ExtendedCoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), FunctionalDoublesSurface.from(aStar),
        FunctionalDoublesSurface.from(bStar), -lambda2, initialCondition);

  }

  private class EuropeanPayoff extends Function1D<Double, Double> {
    private final double _k;
    private final boolean _isCall;

    public EuropeanPayoff(final double strike, final boolean isCall) {
      _k = strike;
      _isCall = isCall;
    }

    @Override
    public Double evaluate(final Double x) {
      if (_isCall) {
        return Math.max(0, x - _k);
      }
      return Math.max(0, _k - x);
    }
  }

}
