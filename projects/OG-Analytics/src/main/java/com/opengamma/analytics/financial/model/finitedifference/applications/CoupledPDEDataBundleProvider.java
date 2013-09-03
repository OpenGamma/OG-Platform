/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoupledCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ExtendedCoupledPDEDataBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.local.AbsoluteLocalVolatilitySurface;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 *
 */
@SuppressWarnings("deprecation")
public class CoupledPDEDataBundleProvider {

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
   * @param maturity The option maturity (in years)
   * @param data Data relating to the Markov chain
   * @return a pair of convection diffusion data bundles
   */
  public ConvectionDiffusionPDE1DCoupledCoefficients[] getCoupledBackwardsPair(final ForwardCurve forward, final double maturity, final TwoStateMarkovChainDataBundle data) {
    final AbsoluteLocalVolatilitySurface localVolOverlay = new AbsoluteLocalVolatilitySurface(ConstantDoublesSurface.from(1.0));
    return getCoupledBackwardsPair(forward, maturity, data, localVolOverlay);
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
   * @param maturity The option maturity (in years)
   * @param data Data relating to the Markov chain
   * @param localVolOverlay The local vol term
   * @return a pair of convection diffusion data bundles
   */
  public ConvectionDiffusionPDE1DCoupledCoefficients[] getCoupledBackwardsPair(final ForwardCurve forward, final double maturity, final TwoStateMarkovChainDataBundle data,
      final AbsoluteLocalVolatilitySurface localVolOverlay) {
    Validate.notNull(forward, "null forward");
    Validate.notNull(data, "null data");
    Validate.notNull(localVolOverlay, "null localVolOverlay");

    final ConvectionDiffusionPDE1DCoupledCoefficients[] res = new ConvectionDiffusionPDE1DCoupledCoefficients[2];
    res[0] = getCoupledBackwardsPDE(forward, data.getVol1(), maturity, data.getLambda12(), data.getBeta1(), localVolOverlay);
    res[1] = getCoupledBackwardsPDE(forward, data.getVol2(), maturity, data.getLambda21(), data.getBeta2(), localVolOverlay);
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
  public ConvectionDiffusionPDE1DCoupledCoefficients[] getCoupledForwardPair(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data) {
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
  public ConvectionDiffusionPDE1DCoupledCoefficients[] getCoupledForwardPair(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data,
      final AbsoluteLocalVolatilitySurface localVolOverlay) {
    Validate.notNull(forward, "null forward");
    Validate.notNull(data, "null data");
    Validate.notNull(localVolOverlay, "null localVolOverlay");

    final ConvectionDiffusionPDE1DCoupledCoefficients[] res = new ConvectionDiffusionPDE1DCoupledCoefficients[2];
    res[0] = getCoupledForwardPDE(forward, data.getVol1(), data.getLambda12(), data.getLambda21(), data.getBeta1(), localVolOverlay);
    res[1] = getCoupledForwardPDE(forward, data.getVol2(), data.getLambda21(), data.getLambda12(), data.getBeta2(), localVolOverlay);
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

  private ConvectionDiffusionPDE1DCoupledCoefficients getCoupledBackwardsPDE(final ForwardCurve forward, final double vol, final double maturity, final double lambda, final double beta,
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

    return new ConvectionDiffusionPDE1DCoupledCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), -lambda);
  }

  private ConvectionDiffusionPDE1DCoupledCoefficients getCoupledForwardPDE(final ForwardCurve forward, final double vol, final double lambda1, final double lambda2,
      final double beta,
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

    return new ConvectionDiffusionPDE1DCoupledCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), -lambda2);
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

}
