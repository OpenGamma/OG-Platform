/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance.pricing;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.equity.variance.VarianceSwapDataBundle;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedStrikeFromDeltaFunction;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceConverter;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceLogMoneyness;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceVisitor;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.minimization.BrentMinimizer1D;
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
  private static final double EPS = 1.0e-12;
  private static final double DEFULT_TOLERANCE = 1e-7;
  private static final Integrator1D<Double, Double> DEFAULT_INTEGRAL = new RungeKuttaIntegrator1D();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private final Integrator1D<Double, Double> _integrator;
  private final double _tol;

  public VarianceSwapStaticReplication() {
    _tol = DEFULT_TOLERANCE;
    _integrator = DEFAULT_INTEGRAL;
  }

  public VarianceSwapStaticReplication(final double tolerance) {
    Validate.isTrue(tolerance >= EPS && tolerance < 0.1, "Please specifiy tolerance in the range 1e-12 to 1e-1 or use other constructor");
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

  public double presentValue(final VarianceSwap deriv, final VarianceSwapDataBundle market, final DoublesPair cutoff) {
    Validate.notNull(deriv, "VarianceSwap deriv");
    Validate.notNull(market, "VarianceSwapDataBundle market");

    if (deriv.getTimeToSettlement() < 0) {
      return 0.0; // All payments have been settled
    }

    // Compute contribution from past realizations
    final double realizedVar = new RealizedVariance().evaluate(deriv); // Realized variance of log returns already observed
    // Compute contribution from future realizations
    final double remainingVar = impliedVariance(deriv, market, cutoff); // Remaining variance implied by option prices

    // Compute weighting
    final double nObsExpected = deriv.getObsExpected(); // Expected number as of trade inception
    final double nObsDisrupted = deriv.getObsDisrupted(); // Number of observations missed due to market disruption
    double nObsActual = 0;

    if (deriv.getTimeToObsStart() <= 0) {
      Validate.isTrue(deriv.getObservations().length > 0, "presentValue requested after first observation date, yet no observations have been provided.");
      nObsActual = deriv.getObservations().length - 1; // From observation start until valuation
    }

    final double totalVar = realizedVar * (nObsActual / nObsExpected) + remainingVar * (nObsExpected - nObsActual - nObsDisrupted) / nObsExpected;
    final double finalPayment = deriv.getVarNotional() * (totalVar - deriv.getVarStrike());

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
   * @param cutoff The cutoff
   * @return presentValue of the *remaining* variance in the swap.
   */
  public double impliedVariance(final VarianceSwap deriv, final VarianceSwapDataBundle market, final DoublesPair cutoff) {

    validateData(deriv, market);

    final double timeToLastObs = deriv.getTimeToObsEnd();
    if (timeToLastObs <= 0) { //expired swap returns 0 variance
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
   * @param cutoff The cutoff
   * @return presentValue of the *remaining* variance in the swap.
   */
  protected double impliedVarianceFromSpot(final double expiry, final VarianceSwapDataBundle market, final DoublesPair cutoff) {
    // 1. Unpack Market data
    final double fwd = market.getForwardCurve().getForward(expiry);
    final BlackVolatilitySurface<?> volSurf = market.getVolatilitySurface();

    VarianceCalculator varCal;
    if (cutoff == null) {
      varCal = new VarianceCalculator(fwd, expiry);
    } else {
      final ExtrapolationParameters exParCal = new ExtrapolationParameters(fwd, expiry);
      final Pair<double[], double[]> pars = exParCal.getparameters(volSurf, cutoff);
      final double[] ks = pars.getFirst();
      final double[] vols = pars.getSecond();
      final double res = getResidual(fwd, expiry, ks, vols);

      varCal = new VarianceCalculator(fwd, expiry, res, ks[0]);
    }

    return varCal.getVariance(volSurf);
  }

  protected double getResidual(final double fwd, final double expiry, final double[] ks, final double[] vols) {

    // Check for trivial case where cutoff is so low that there's no effective value in the option
    final double cutoffPrice = BlackFormulaRepository.price(fwd, ks[0], expiry, vols[0], ks[0] > fwd);
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
      public Double evaluate(final Double strike) {
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

  /**
   * Converts from cutoff limit and spread expressed in the same units as the volatility surface (e.g. moneyness, delta etc) and returns (absolute strike points and volatilities)
   */
  private class ExtrapolationParameters implements BlackVolatilitySurfaceVisitor<DoublesPair, Pair<double[], double[]>> {

    private final double _t;
    private final double _f;

    public ExtrapolationParameters(final double forward, final double expiry) {
      _t = expiry;
      _f = forward;
    }

    public Pair<double[], double[]> getparameters(final BlackVolatilitySurface<?> surf, final DoublesPair cutoff) {
      return surf.accept(this, cutoff);
    }

    @Override
    public Pair<double[], double[]> visitDelta(final BlackVolatilitySurfaceDelta surface, final DoublesPair data) {
      Validate.isTrue(data.first > 0.0 && data.first < 1.0, "cut off must be a valide delta (0,1)");
      Validate.isTrue(data.second > 0.0 && data.second < data.first, "spread must be positive and numerically less than cutoff");
      final double[] deltas = new double[2];
      final double[] k = new double[2];
      final double[] vols = new double[2];
      deltas[0] = data.first;
      deltas[1] = data.first - data.second;
      vols[0] = surface.getVolatilityForDelta(_t, deltas[0]);
      vols[1] = surface.getVolatilityForDelta(_t, deltas[1]);
      k[0] = BlackFormulaRepository.strikeForDelta(_f, deltas[0], _t, vols[0], true);
      k[1] = BlackFormulaRepository.strikeForDelta(_f, deltas[1], _t, vols[1], true);
      Validate.isTrue(k[0] < k[1], "need first (cutoff) strike less than second");
      return new ObjectsPair<double[], double[]>(k, vols);
    }

    @Override
    public Pair<double[], double[]> visitStrike(final BlackVolatilitySurfaceStrike surface, final DoublesPair data) {
      Validate.isTrue(data.first > 0.0, "cut off must be greater than zero");
      Validate.isTrue(data.second > 0.0, "spread must be positive");
      final double[] k = new double[2];
      final double[] vols = new double[2];
      k[0] = data.first;
      k[1] = data.first + data.second;
      vols[0] = surface.getVolatility(_t, k[0]);
      vols[1] = surface.getVolatility(_t, k[1]);
      return new ObjectsPair<double[], double[]>(k, vols);
    }

    @Override
    public Pair<double[], double[]> visitMoneyness(final BlackVolatilitySurfaceMoneyness surface, final DoublesPair data) {
      Validate.isTrue(data.first > 0.0, "cut off must be greater than zero");
      Validate.isTrue(data.second > 0.0, "spread must be positive");
      final double[] k = new double[2];
      final double[] vols = new double[2];
      k[0] = _f * data.first;
      k[1] = _f * (data.first + data.second);
      vols[0] = surface.getVolatilityForMoneyness(_t, data.first);
      vols[1] = surface.getVolatilityForMoneyness(_t, data.first + data.second);
      return new ObjectsPair<double[], double[]>(k, vols);
    }

    @Override
    public Pair<double[], double[]> visitLogMoneyness(final BlackVolatilitySurfaceLogMoneyness surface, final DoublesPair data) {
      Validate.isTrue(data.second > 0.0, "spread must be positive");
      final double[] k = new double[2];
      final double[] vols = new double[2];
      k[0] = _f * Math.exp(data.first);
      k[1] = _f * Math.exp(data.first + data.second);
      vols[0] = surface.getVolatilityForLogMoneyness(_t, data.first);
      vols[1] = surface.getVolatilityForLogMoneyness(_t, data.first + data.second);
      return new ObjectsPair<double[], double[]>(k, vols);
    }

    @Override
    public Pair<double[], double[]> visitDelta(final BlackVolatilitySurfaceDelta surface) {
      throw new NotImplementedException();
    }

    @Override
    public Pair<double[], double[]> visitStrike(final BlackVolatilitySurfaceStrike surface) {
      throw new NotImplementedException();
    }

    @Override
    public Pair<double[], double[]> visitMoneyness(final BlackVolatilitySurfaceMoneyness surface) {
      throw new NotImplementedException();
    }

    @Override
    public Pair<double[], double[]> visitLogMoneyness(final BlackVolatilitySurfaceLogMoneyness surface) {
      throw new NotImplementedException();
    }

  }

  private class VarianceCalculator implements BlackVolatilitySurfaceVisitor<DoublesPair, Double> {

    private final double _t;
    private final double _f;
    private final double _residual;
    private final double _lowStrikeCutoff;
    private final boolean _addResidual;

    public VarianceCalculator(final double forward, final double expiry) {
      _f = forward;
      _t = expiry;
      _addResidual = false;
      _lowStrikeCutoff = 0.0;
      _residual = 0.0;
    }

    public VarianceCalculator(final double forward, final double expiry, final double residual, final double lowStrikeCutoff) {
      _f = forward;
      _t = expiry;
      _addResidual = true;
      _lowStrikeCutoff = lowStrikeCutoff;
      _residual = residual;
    }

    public double getVariance(final BlackVolatilitySurface<?> surf) {
      return surf.accept(this);
    }

    //    public double getVariance(final BlackVolatilitySurface<?> surf, final DoublesPair limits) {
    //      return surf.accept(this, limits);
    //    }

    //********************************************
    // strike surfaces
    //********************************************

    @Override
    public Double visitStrike(final BlackVolatilitySurfaceStrike surface, final DoublesPair data) {

      Validate.isTrue(data.first >= 0, "Negative lower limit");
      Validate.isTrue(data.second > data.first, "upper limit not greater than lower");

      if (_t < 1e-4) {
        if (data.first < _f && data.second > _f) {
          final double atmVol = surface.getVolatility(_t, _f);
          return atmVol * atmVol;
        } else {
          return 0.0;
        }
      }

      final Function1D<Double, Double> integrand = getStrikeIntegrand(surface);
      //if external left tail residual is provided, only integrate from cut-off, otherwise we will double count
      final double lowerLimit = Math.max(_lowStrikeCutoff, data.first);
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
    public Double visitStrike(final BlackVolatilitySurfaceStrike surface) {

      final double atmVol = surface.getVolatility(_t, _f);
      if (_t < 1e-4) {
        return atmVol * atmVol;
      }
      final double rootT = Math.sqrt(_t);
      final double invNorTol = NORMAL.getInverseCDF(_tol);

      final Function1D<Double, Double> integrand = getStrikeIntegrand(surface);
      final Function1D<Double, Double> remainder = new Function1D<Double, Double>() {
        @SuppressWarnings("synthetic-access")
        @Override
        public Double evaluate(final Double strike) {
          if (strike == 0) {
            return 0.0;
          }
          final boolean isCall = strike >= _f;
          final double vol = surface.getVolatility(_t, strike);
          final double otmPrice = BlackFormulaRepository.price(_f, strike, _t, vol, isCall);
          final double res = (isCall ? otmPrice / strike : otmPrice / 2 / strike);
          return res;
        }
      };

      double putPart;
      if (_addResidual) {
        putPart = _integrator.integrate(integrand, _lowStrikeCutoff, _f);
        putPart += _residual;
      } else {
        double l = _f * Math.exp(invNorTol * atmVol * rootT); //initial estimate of lower limit
        putPart = _integrator.integrate(integrand, l, _f);
        double rem = remainder.evaluate(l);
        double error = rem / putPart;
        while (error > _tol) {
          l /= 2.0;
          putPart += _integrator.integrate(integrand, l, 2 * l);
          rem = remainder.evaluate(l);
          error = rem / putPart;
        }
        putPart += rem; //add on the (very small) remainder estimate otherwise we'll always underestimate variance
      }

      double u = _f * Math.exp(-invNorTol * atmVol * rootT); //initial estimate of upper limit
      double callPart = _integrator.integrate(integrand, _f, u);
      double rem = remainder.evaluate(u);
      double error = rem / callPart;
      while (error > _tol) {
        callPart += _integrator.integrate(integrand, u, 2 * u);
        u *= 2.0;
        rem = remainder.evaluate(u);
        error = rem / putPart;
      }
      // callPart += rem/2.0;
      //don't add on the remainder estimate as it is very conservative, and likely too large

      return 2 * (putPart + callPart) / _t;
    }

    private Function1D<Double, Double> getStrikeIntegrand(final BlackVolatilitySurfaceStrike surface) {
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
      return integrand;
    }

    //********************************************
    // delta surfaces
    //********************************************

    @Override
    public Double visitDelta(final BlackVolatilitySurfaceDelta surface, final DoublesPair data) {

      Validate.isTrue(data.first < data.second, "lower limit not less that upper");
      Validate.isTrue(data.first >= 0.0, "lower limit < 0.0");
      Validate.isTrue(data.second <= 1.0, "upper limit > 1.0");

      if (_t < 1e-4) {
        final double dnsVol = surface.getVolatilityForDelta(_t, 0.5);
        return dnsVol * dnsVol; //this will be identical to atm-vol for t-> 0
      }

      final Function1D<Double, Double> integrand = getDeltaIntegrand(surface);
      //find the delta corresponding to the at-the-money-forward (NOTE this is not the DNS of delta = 0.5)
      final double atmfVol = surface.getVolatility(_t, _f);
      final double atmfDelta = BlackFormulaRepository.delta(_f, _f, _t, atmfVol, true);

      final double lowerLimit = data.first;
      final double upperLimit = data.second;

      //Do the call/k^2 integral - split up into the the put integral and the call integral because the function is not smooth at strike = forward
      double res = _integrator.integrate(integrand, lowerLimit, atmfDelta); //Call part

      if (_addResidual) {
        //The lower strike cutoff is expressed as a absolute strike (i.e. k << f), while the upperLimit is a call delta (values close to one = small absolute strikes),
        //so we must covert the lower strike cutoff to a upper delta cutoff
        final double limitVol = surface.getVolatility(_t, _lowStrikeCutoff);
        final double limitDelta = BlackFormulaRepository.delta(_f, _lowStrikeCutoff, _t, limitVol, true);
        final double u = Math.min(upperLimit, limitDelta);
        res += _integrator.integrate(integrand, atmfDelta, u); //put part
        res += _residual;
      } else {
        res += _integrator.integrate(integrand, atmfDelta, upperLimit); //put part
      }
      return 2 * res / _t;
    }

    @Override
    public Double visitDelta(final BlackVolatilitySurfaceDelta surface) {

      if (_t < 1e-4) {
        final double atmVol = surface.getVolatility(_t, _f);
        return atmVol * atmVol;
      }

      final double lowerLimit = _tol;
      double upperLimit;
      if (_addResidual) {
        final double limitVol = surface.getVolatility(_t, _lowStrikeCutoff);
        upperLimit = BlackFormulaRepository.delta(_f, _lowStrikeCutoff, _t, limitVol, true);
      } else {
        upperLimit = 1 - _tol;
      }

      final DoublesPair limits = new DoublesPair(lowerLimit, upperLimit);
      return visitDelta(surface, limits);
    }

    private Function1D<Double, Double> getDeltaIntegrand(final BlackVolatilitySurfaceDelta surface) {
      final double eps = 1e-5;
      final double rootT = Math.sqrt(_t);

      final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
        @SuppressWarnings("synthetic-access")
        @Override
        public Double evaluate(final Double delta) {

          final double vol = surface.getVolatilityForDelta(_t, delta);
          final double sigmaRootT = vol * rootT;
          //TODO handle sigmaRootT -> 0

          final double strike = BlackImpliedStrikeFromDeltaFunction.impliedStrike(delta, true, _f, _t, vol);
          final boolean isCall = strike >= _f;
          final int sign = isCall ? 1 : -1;

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
          final double d2 = d1 - sigmaRootT;
          final double weight = (vol * rootT / NORMAL.getPDF(d1) + dSigmaDDelta * (d1 * rootT - vol * _t)) / strike;
          final double otmPrice = sign * (_f * (isCall ? delta : 1 - delta) - strike * NORMAL.getCDF(sign * d2));
          return weight * otmPrice;
        }
      };

      return integrand;
    }

    //********************************************
    // moneyness surfaces
    //********************************************

    @Override
    public Double visitMoneyness(final BlackVolatilitySurfaceMoneyness surface, final DoublesPair data) {
      final double l = Math.log(data.first);
      final double u = Math.log(data.second);
      Validate.isTrue(l > 0.0, "lower limit <= 0");
      Validate.isTrue(u > l, "lower limit >= upper limit");
      final BlackVolatilitySurfaceLogMoneyness logMS = BlackVolatilitySurfaceConverter.toLogMoneynessSurface(surface);
      return visitLogMoneyness(logMS, new DoublesPair(l, u));
    }

    @Override
    public Double visitMoneyness(final BlackVolatilitySurfaceMoneyness surface) {
      final BlackVolatilitySurfaceLogMoneyness logMS = BlackVolatilitySurfaceConverter.toLogMoneynessSurface(surface);
      return visitLogMoneyness(logMS);
    }

    //********************************************
    // log-moneyness surfaces
    //********************************************

    /**
     * Only use if the integral limits have been calculated elsewhere, or you need the contribution from a specific range
     */
    @Override
    public Double visitLogMoneyness(final BlackVolatilitySurfaceLogMoneyness surface, final DoublesPair data) {
      final double lower = data.first;
      final double upper = data.second;
      Validate.isTrue(upper > lower, "lower limit >= upper limit");
      final double atmVol = surface.getVolatilityForLogMoneyness(_t, 0.0);
      if (_t < 1e-4) { //if less than a hour from expiry, only the ATM-vol will count, so must check the integral range spans ATM
        if (lower * upper < 0.0) {
          return atmVol * atmVol;
        } else {
          return 0.0;
        }
      }

      final Function1D<Double, Double> integrand = getLogMoneynessIntegrand(surface);
      double putPart;
      if (_addResidual) {
        putPart = _integrator.integrate(integrand, Math.log(_lowStrikeCutoff / _f), 0.0);
        putPart += _residual;
      } else {
        putPart = _integrator.integrate(integrand, lower, 0.0);
      }
      final double callPart = _integrator.integrate(integrand, 0.0, upper);
      return 2 * (putPart + callPart) / _t;
    }

    /**
     * General method when you wish to compute the expected variance from a log-moneyness parametrised surface to within a certain tolerance
     * @param surface log-moneyness parametrised volatility surface
     * @return expected variance
     */
    @Override
    public Double visitLogMoneyness(final BlackVolatilitySurfaceLogMoneyness surface) {
      final double atmVol = surface.getVolatilityForLogMoneyness(_t, 0.0);
      if (_t < 1e-4) {
        return atmVol * atmVol;
      }
      final double rootT = Math.sqrt(_t);
      final double invNorTol = NORMAL.getInverseCDF(_tol);

      final Function1D<Double, Double> integrand = getLogMoneynessIntegrand(surface);

      double putPart;
      if (_addResidual) {
        putPart = _integrator.integrate(integrand, Math.log(_lowStrikeCutoff / _f), 0.0);
        putPart += _residual;
      } else {
        final double l = invNorTol * atmVol * rootT; //initial estimate of lower limit
        putPart = _integrator.integrate(integrand, l, 0.0);
        double rem = integrand.evaluate(l);
        double error = rem / putPart;
        int step = 1;
        while (error > _tol) {
          putPart += _integrator.integrate(integrand, (step + 1) * l, step * l);
          step++;
          rem = integrand.evaluate((step + 1) * l);
          error = rem / putPart;
        }
        putPart += rem; //add on the (very small) remainder estimate otherwise we'll always underestimate variance
      }

      final double u = _f * Math.exp(-invNorTol * atmVol * rootT); //initial estimate of upper limit
      double callPart = _integrator.integrate(integrand, 0.0, u);
      double rem = integrand.evaluate(u);
      double error = rem / callPart;
      int step = 1;
      while (error > _tol) {
        callPart += _integrator.integrate(integrand, step * u, (1 + step) * u);
        step++;
        rem = integrand.evaluate((1 + step) * u);
        error = rem / putPart;
      }
      //callPart += rem;
      //don't add on the remainder estimate as it is very conservative, and likely too large

      return 2 * (putPart + callPart) / _t;
    }

    private Function1D<Double, Double> getLogMoneynessIntegrand(final BlackVolatilitySurfaceLogMoneyness surface) {
      final double rootT = Math.sqrt(_t);

      final Function1D<Double, Double> integrand = new Function1D<Double, Double>() {
        @SuppressWarnings("synthetic-access")
        @Override
        public Double evaluate(final Double logMoneyness) {
          final boolean isCall = logMoneyness >= 0.0;
          final int sign = isCall ? 1 : -1;
          final double vol = surface.getVolatilityForLogMoneyness(_t, logMoneyness);
          final double sigmaRootT = vol * rootT;

          if (logMoneyness == 0.0) {
            return 2 * NORMAL.getCDF(0.5 * sigmaRootT) - 1.;
          }
          if (sigmaRootT < 1e-12) {
            return 0.0;
          }
          final double d1 = -logMoneyness / sigmaRootT + 0.5 * sigmaRootT;
          final double d2 = d1 - sigmaRootT;
          final double res = sign * (Math.exp(-logMoneyness) * NORMAL.getCDF(sign * d1) - NORMAL.getCDF(sign * d2));
          return res;
        }
      };
      return integrand;
    }
  }

}
