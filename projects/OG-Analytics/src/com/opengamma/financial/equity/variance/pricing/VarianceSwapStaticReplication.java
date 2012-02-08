/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance.pricing;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.equity.variance.VarianceSwapDataBundle;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedStrikeFromDeltaFunction;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceVistor;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * We construct a model independent method to price variance as a static replication
 * of an (in)finite sum of call and put option prices on the underlying.
 * We assume the existence of a smooth function of these option prices / Implied volatilities.
 * The portfolio weighting is 1/k^2. As such, this method is especially sensitive to strike near zero,
 * so we allow the caller to override the Volatilities below a cutoff point (defined as a fraction of the forward rate).
 * We then fit a ShiftedLognormal model to the price of the linear (call) and digital (call spread) at the cutoff.
 * <p>
 * Note: This is not intended to handle large payment delays between last observation date and payment. No convexity adjustment has been applied.<p>
 * Note: Forward variance (forward starting observations) is intended to consider periods beginning more than A_FEW_WEEKS from trade inception
 */
public class VarianceSwapStaticReplication {

  //TODO CASE Review: Current treatment of forward vol attempts to disallow 'short' periods that may confuse intention of traders.
  // If the entire observation period is less than A_FEW_WEEKS, an error will be thrown.
  // If timeToFirstObs < A_FEW_WEEKS, the pricer will consider the volatility to be from now until timeToLastObs
  private static final double A_FEW_WEEKS = 0.05;
  protected static final double EPS = 1.0e-12;
  private static final double DEFULT_TOLERANCE = 1e-7;
  private static final Integrator1D<Double, Double> DEFAULT_INTEGRAL = new RungeKuttaIntegrator1D();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private final Integrator1D<Double, Double> _integrator;
  private double _tol;

  public VarianceSwapStaticReplication() {
    _tol = DEFULT_TOLERANCE;
    _integrator = DEFAULT_INTEGRAL;
  }

  public VarianceSwapStaticReplication(final double tolerance) {
    Validate.isTrue(tolerance > EPS && tolerance < 0.1, "Please specifiy tolerance in the range 1e-12 to 1e-1 or use other constructor");
    _tol = tolerance;
    _integrator = DEFAULT_INTEGRAL;
  }

  public VarianceSwapStaticReplication(final Integrator1D<Double, Double> integrator) {
    Validate.notNull(integrator, "null integrator");
    _tol = DEFULT_TOLERANCE;
    _integrator = integrator;
  }

  public VarianceSwapStaticReplication(final double tolerance, final Integrator1D<Double, Double> integrator) {
    Validate.isTrue(tolerance > EPS && tolerance < 0.1, "Please specifiy tolerance in the range 1e-12 to 1e-1 or use other constructor");
    Validate.notNull(integrator, "null integrator");
    _tol = tolerance;
    _integrator = integrator;
  }

  public double presentValue(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
    return presentValue(deriv, market, null);
  }

  public double presentValue(final VarianceSwap deriv, final VarianceSwapDataBundle market, DoublesPair cutoff) {
    Validate.notNull(deriv, "VarianceSwap deriv");
    Validate.notNull(market, "VarianceSwapDataBundle market");

    if (deriv.getTimeToSettlement() < 0) {
      return 0.0; // All payments have been settled
    }

    // Compute contribution from past realizations
    double realizedVar = new RealizedVariance().evaluate(deriv); // Realized variance of log returns already observed
    // Compute contribution from future realizations
    double remainingVar = impliedVariance(deriv, market, cutoff); // Remaining variance implied by option prices

    // Compute weighting
    double nObsExpected = deriv.getObsExpected(); // Expected number as of trade inception
    double nObsDisrupted = deriv.getObsDisrupted(); // Number of observations missed due to market disruption
    double nObsActual = 0;

    if (deriv.getTimeToObsStart() <= 0) {
      Validate.isTrue(deriv.getObservations().length > 0, "presentValue requested after first observation date, yet no observations have been provided.");
      nObsActual = deriv.getObservations().length - 1; // From observation start until valuation
    }

    double totalVar = realizedVar * (nObsActual / nObsExpected) + remainingVar * (nObsExpected - nObsActual - nObsDisrupted) / nObsExpected;
    double finalPayment = deriv.getVarNotional() * (totalVar - deriv.getVarStrike());

    final double df = market.getDiscountCurve().getDiscountFactor(deriv.getTimeToSettlement());
    return df * finalPayment;

  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterized in 'variance' terms,
   * It is quoted as an annual variance value, hence 1/T * integral(0,T) {sigmaSquared dt} <p>
   * 
   * @param deriv VarianceSwap derivative to be priced
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  public double impliedVariance(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
    return impliedVariance(deriv, market, null);
  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterized in 'variance' terms,
   * It is quoted as an annual variance value, hence 1/T * integral(0,T) {sigmaSquared dt} <p>
   * 
   * @param deriv VarianceSwap derivative to be priced
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  public double impliedVariance(final VarianceSwap deriv, final VarianceSwapDataBundle market, DoublesPair cutoff) {

    validateData(deriv, market);

    final double timeToLastObs = deriv.getTimeToObsEnd();
    if (timeToLastObs <= 0) {//expired swap returns 0 variance
      return 0.0;
    }

    final double timeToFirstObs = deriv.getTimeToObsStart();

    // Compute Variance from spot until last observation
    final double varianceSpotEnd = impliedVarianceFromSpot(timeToLastObs, market, cutoff);

    // If timeToFirstObs < A_FEW_WEEKS, the pricer will consider the volatility to be from now until timeToLastObs
    final boolean forwardStarting = timeToFirstObs > A_FEW_WEEKS;
    if (!forwardStarting) {
      return varianceSpotEnd;
    }
    final double varianceSpotStart = impliedVarianceFromSpot(timeToFirstObs, market, cutoff);
    return (varianceSpotEnd * timeToLastObs - varianceSpotStart * timeToFirstObs) / (timeToLastObs - timeToFirstObs);
  }

  private void validateData(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
    Validate.notNull(deriv, "VarianceSwap deriv");
    Validate.notNull(market, "VarianceSwapDataBundle market");

    final double timeToLastObs = deriv.getTimeToObsEnd();
    final double timeToFirstObs = deriv.getTimeToObsStart();

    Validate.isTrue(timeToFirstObs + A_FEW_WEEKS < timeToLastObs, "timeToLastObs is not sufficiently longer than timeToFirstObs. "
        + "This method is not intended to handle very short periods of volatility." + (timeToLastObs - timeToFirstObs));
  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterized in 'variance' terms,
   * It is quoted as an annual variance value, hence 1/T * integral(0,T) {sigmaSquared dt} <p>
   * 
   * @param expiry Time from spot until last observation
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  protected double impliedVarianceFromSpot(final double expiry, final VarianceSwapDataBundle market) {
    return impliedVarianceFromSpot(expiry, market, null);
  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterized in 'variance' terms,
   * It is quoted as an annual variance value, hence 1/T * integral(0,T) {sigmaSquared dt} <p>
   * 
   * @param expiry Time from spot until last observation
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  protected double impliedVarianceFromSpot(final double expiry, final VarianceSwapDataBundle market, DoublesPair cutoff) {
    // 1. Unpack Market data
    final double fwd = market.getForwardCurve().getForward(expiry);
    final BlackVolatilitySurface<?> volSurf = market.getVolatilitySurface();

    varianceCalculator varCal;
    if (cutoff == null) {
      varCal = new varianceCalculator(fwd, expiry);
    } else {
      ExtrapolationParameters exParCal = new ExtrapolationParameters(fwd, expiry);
      Pair<double[], double[]> pars = exParCal.getparameters(volSurf, cutoff);
      double[] ks = pars.getFirst();
      double[] vols = pars.getSecond();
      final double res = getResidual(fwd, expiry, ks, vols);

      varCal = new varianceCalculator(fwd, expiry, res, cutoff.first);
    }

    return varCal.getVariance(volSurf);
  }

  class ExtrapolationParameters implements BlackVolatilitySurfaceVistor<DoublesPair, Pair<double[], double[]>> {

    private final double _t;
    private final double _f;

    public ExtrapolationParameters(final double forward, final double expiry) {
      _t = expiry;
      _f = forward;
    }

    public Pair<double[], double[]> getparameters(BlackVolatilitySurface<?> surf, DoublesPair cutoff) {
      return surf.accept(this, cutoff);
    }

    @Override
    public Pair<double[], double[]> visitDelta(BlackVolatilitySurfaceDelta surface, DoublesPair data) {
      final double[] deltas = new double[2];
      final double[] k = new double[2];
      final double[] vols = new double[2];
      deltas[0] = data.first;
      deltas[1] = data.second;
      vols[0] = surface.getVolatilityForDelta(_t, deltas[0]);
      vols[1] = surface.getVolatilityForDelta(_t, deltas[1]);
      k[0] = BlackFormulaRepository.strikeForDelta(_f, deltas[0], _t, vols[0], true);
      k[1] = BlackFormulaRepository.strikeForDelta(_f, deltas[1], _t, vols[1], true);
      Validate.isTrue(k[0] < k[1], "need first (cutoff) strike less than second");
      return new ObjectsPair<double[], double[]>(k, vols);
    }

    @Override
    public Pair<double[], double[]> visitStrike(BlackVolatilitySurfaceStrike surface, DoublesPair data) {
      final double[] k = new double[2];
      final double[] vols = new double[2];
      k[0] = data.first;
      k[1] = data.second;
      Validate.isTrue(k[0] < k[1], "need first (cutoff) strike less than second");
      vols[0] = surface.getVolatility(_t, k[0]);
      vols[1] = surface.getVolatility(_t, k[1]);
      return new ObjectsPair<double[], double[]>(k, vols);
    }

    @Override
    public Pair<double[], double[]> visitMoneyness(BlackVolatilitySurfaceMoneyness surface, DoublesPair data) {
      return null;
    }

    @Override
    public Pair<double[], double[]> visitDelta(BlackVolatilitySurfaceDelta surface) {
      return null;
    }

    @Override
    public Pair<double[], double[]> visitStrike(BlackVolatilitySurfaceStrike surface) {
      return null;
    }

    @Override
    public Pair<double[], double[]> visitMoneyness(BlackVolatilitySurfaceMoneyness surface) {
      return null;
    }

  }

  class varianceCalculator implements BlackVolatilitySurfaceVistor<DoublesPair, Double> {

    private final BracketRoot BRACKETER = new BracketRoot();
    private final BisectionSingleRootFinder ROOT_FINDER = new BisectionSingleRootFinder(1e-3);

    private final double _t;
    private final double _f;
    private final double _residual;
    private final double _lowStrikeCutoff;
    private final boolean _addResidual;

    public varianceCalculator(final double forward, final double expiry) {
      _f = forward;
      _t = expiry;
      _addResidual = false;
      _lowStrikeCutoff = 0.0;
      _residual = 0.0;
    }

    public varianceCalculator(final double forward, final double expiry, final double residual, final double lowStrikeCutoff) {
      _f = forward;
      _t = expiry;
      _addResidual = true;
      _lowStrikeCutoff = lowStrikeCutoff;
      _residual = residual;
    }

    public double getVariance(BlackVolatilitySurface<?> surf) {
      return surf.accept(this);
    }

    @Override
    public Double visitDelta(final BlackVolatilitySurfaceDelta surface, final DoublesPair data) {

      if (_t < 1e-4) {
        final double dnsVol = surface.getVolatilityForDelta(_t, 0.5);
        return dnsVol * dnsVol;
      }

      final double eps = 1e-5;

      Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
        @SuppressWarnings("synthetic-access")
        @Override
        public Double evaluate(final Double delta) {

          final double vol = surface.getVolatilityForDelta(_t, delta);
          final double strike = BlackImpliedStrikeFromDeltaFunction.impliedStrike(delta, true, _f, _t, vol);
          boolean isCall = strike >= _f;

          //TODO if should be the job of the vol surface to provide derivatives
          double dSigmaDDelta;
          if (delta < eps) {
            final double volUp = surface.getVolatilityForDelta(_t, delta + eps);
            dSigmaDDelta = (volUp - vol) / eps;
          } else if (delta > 1 - eps) {
            final double volDown = surface.getVolatilityForDelta(_t, delta - eps);
            dSigmaDDelta = (vol - volDown) / eps;
          } else {
            final double volUp = surface.getVolatilityForDelta(_t, delta + eps);
            final double volDown = surface.getVolatilityForDelta(_t, delta - eps);
            dSigmaDDelta = (volUp - volDown) / 2 / eps;
          }

          final double d1 = NORMAL.getInverseCDF(delta);
          final double rootT = Math.sqrt(_t);
          final double weight = (vol * rootT / NORMAL.getPDF(d1) + dSigmaDDelta * (d1 * rootT - vol * _t)) / strike;
          final double otmPrice = BlackFormulaRepository.price(_f, strike, _t, vol, isCall);
          return weight * otmPrice;
        }
      };

      //find the delta corresponding to the at-the-money-forward (NOTE this is not the DNS of delta = 0.5)
      final double atmfVol = surface.getVolatility(_t, _f);
      final double atmfDelta = BlackFormulaRepository.delta(_f, _f, _t, atmfVol, true);

      final double lowerLimit = data.first;
      final double upperLimit = data.second;

      //Do the call/k^2 integral - split up into the the put integral and the call integral because the function is not smooth at strike = forward
      double res = _integrator.integrate(integrand, atmfDelta, upperLimit);
      res += _integrator.integrate(integrand, lowerLimit, atmfDelta);

      if (_addResidual) {
        res += _residual;
      }
      return 2 * res / _t;
    }

    @Override
    public Double visitStrike(final BlackVolatilitySurfaceStrike surface, DoublesPair data) {

      if (_t < 1e-4) {
        final double atmVol = surface.getVolatility(_t, _f);
        return atmVol * atmVol;
      }

      final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
        @SuppressWarnings("synthetic-access")
        @Override
        public Double evaluate(final Double strike) {
          if (strike == 0) {
            return 0.0;
          }
          final boolean isCall = strike >= _f;
          final double vol = surface.getVolatility(_t, strike);
          final double otmPrice = BlackFormulaRepository.price(_f, strike, _t, vol, isCall);
          final double weight = 1.0 / (strike * strike);
          return otmPrice * weight;
        }
      };

      final double lowerLimit = data.first;
      final double upperLimit = data.second;

      //Do the call/k^2 integral - split up into the the put integral and the call integral because the function is not smooth at strike = forward
      double res = _integrator.integrate(integrand, _f, upperLimit);
      res += _integrator.integrate(integrand, lowerLimit, _f);

      if (_addResidual) {
        res += _residual;
      }
      return 2 * res / _t;
    }

    @Override
    public Double visitMoneyness(BlackVolatilitySurfaceMoneyness surface, DoublesPair data) {
      return null;
    }

    @Override
    public Double visitDelta(BlackVolatilitySurfaceDelta surface) {

      if (_t < 1e-4) {
        final double atmVol = surface.getVolatility(_t, _f);
        return atmVol * atmVol;
      }

      double lowerLimit, upperLimit;
      if (_addResidual) {
        upperLimit = _lowStrikeCutoff;
      } else {
        upperLimit = 1 - _tol;
      }
      lowerLimit = _tol;

      DoublesPair limits = new DoublesPair(lowerLimit, upperLimit);
      return visitDelta(surface, limits);
    }

    @Override
    public Double visitStrike(final BlackVolatilitySurfaceStrike surface) {

      final double atmVol = surface.getVolatility(_t, _f);
      if (_t < 1e-4) {
        return atmVol * atmVol;
      }

      final double a = _tol * atmVol * atmVol * _t / 2;
      //  final double logA = Math.log(a);

      double lowerLimit;
      if (_addResidual) {
        lowerLimit = _lowStrikeCutoff;
      } else {

        Function1D<Double, Double> putLimitFunc = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(Double x) {
            if (x == 0) {
              return -a;
            }
            final double vol = surface.getVolatility(_t, x);
            final double price = BlackFormulaRepository.price(_f, x, _t, vol, true);

            return price / x - a;
          }
        };
        double[] brackets = BRACKETER.getBracketedPoints(putLimitFunc, 0, 0.5 * _f, 0.0, _f);
        lowerLimit = ROOT_FINDER.getRoot(putLimitFunc, brackets[0], brackets[1]);
      }

      Function1D<Double, Double> callLimitFunc = new Function1D<Double, Double>() {
        @Override
        public Double evaluate(Double x) {

          final double vol = surface.getVolatility(_t, x);
          final double price = BlackFormulaRepository.price(_f, x, _t, vol, true);

          return price / x - a;
        }
      };

      double[] brackets = BRACKETER.getBracketedPoints(callLimitFunc, 1.0 * _f, 10 * _f, _f, Double.POSITIVE_INFINITY);
      final double upperLimit = ROOT_FINDER.getRoot(callLimitFunc, brackets[0], brackets[1]);

      DoublesPair limits = new DoublesPair(lowerLimit, upperLimit);
      return visitStrike(surface, limits);
    }

    @Override
    public Double visitMoneyness(BlackVolatilitySurfaceMoneyness surface) {
      return null;
    }

  }

  protected double getResidual(final double fwd, final double expiry, final double[] ks, final double[] vols) {

    // Check for trivial case where cutoff is so low that there's no effective value in the option
    double cutoffPrice = BlackFormulaRepository.price(fwd, ks[0], expiry, vols[0], ks[0] > fwd);
    if (CompareUtils.closeEquals(cutoffPrice, 0)) {
      return 0.0; //i.e. the tail function is never used
    }
    // The typical case - fit a  ShiftedLognormal to the two strike-vol pairs
    final ShiftedLognormalVolModel leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, ks[0], vols[0], ks[1], vols[1]);

    // Now, handle behaviour near zero strike. ShiftedLognormalVolModel has non-zero put price for zero strike.
    // What we do is to find the strike, k_min, at which f(k) = p(k)/k^2 begins to blow up, by finding the minimum of this function, k_min
    // then setting f(k) = f(k_min) for k < k_min. This ensures the implied volatility and the integrand are well behaved in the limit k -> 0.
    final Function1D<Double, Double> shiftedLnIntegrand = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double strike) {
        return leftExtrapolator.priceFromFixedStrike(strike) / (strike * strike);
      }
    };
    final double kMin = new BrentMinimizer1D().minimize(shiftedLnIntegrand, EPS, EPS, ks[0]);
    final double fMin = shiftedLnIntegrand.evaluate(kMin);
    double res = fMin * kMin; //the (hopefully) very small rectangular bit between zero and kMin

    res += _integrator.integrate(shiftedLnIntegrand, kMin, ks[0]);

    return res;
  }

  /**
   * Computes the fair value strike of a spot starting VarianceSwap parameterised in vol/vega terms.
   * This is an estimate of annual Lognormal (Black) volatility
   * 
   * @param deriv VarianceSwap derivative to be priced
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap.
   */
  public double impliedVolatility(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
    final double sigmaSquared = impliedVariance(deriv, market);
    return Math.sqrt(sigmaSquared);
  }

}
