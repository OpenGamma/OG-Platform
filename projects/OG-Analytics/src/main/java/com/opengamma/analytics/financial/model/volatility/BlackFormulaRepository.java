/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.lang.annotation.ExternalFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * This <b>SHOULD</b> be the repository for Black formulas - i.e. the price, common greeks (delta, gamma, vega) and
 * implied volatility. Other
 * classes that have higher level abstractions (e.g. option data bundles) should call these functions.
 * As the numeraire (e.g. the zero bond p(0,T) in the T-forward measure) in the Black formula is just a multiplication
 * factor, all prices,
 * input/output, are <b>forward</b> prices, i.e. (spot price)/numeraire
 * NOTE THAT a "reference value" is returned if computation comes across an ambiguous expression
 */
public abstract class BlackFormulaRepository {

  private static final Logger s_logger = LoggerFactory.getLogger(BlackFormulaRepository.class);

  private static final double LARGE = 1.e13;
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final double SMALL = 1.0E-13;
  private static final double EPS = 1e-15;
  private static final int MAX_ITERATIONS = 20; // something's wrong if Newton-Raphson taking longer than this
  private static final double VOL_TOL = 1e-9; // 1 part in 100,000 basis points will do for implied vol

  /**
   * The <b>forward</b> price of an option using the Black formula
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @param isCall True for calls, false for puts
   * @return The <b>forward</b> price
   */
  @ExternalFunction
  public static double price(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol, final boolean isCall) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    final int sign = isCall ? 1 : -1;
    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);
    double d1 = 0.;
    double d2 = 0.;

    if (bFwd && bStr) {
      s_logger.info("(large value)/(large value) ambiguous");
      return isCall ? (forward >= strike ? forward : 0.) : (strike >= forward ? strike : 0.);
    }
    if (sigmaRootT < SMALL) {
      return Math.max(sign * (forward - strike), 0.0);
    }
    if (Math.abs(forward - strike) < SMALL || bSigRt) {
      d1 = 0.5 * sigmaRootT;
      d2 = -0.5 * sigmaRootT;
    } else {
      d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
      d2 = d1 - sigmaRootT;
    }

    final double nF = NORMAL.getCDF(sign * d1);
    final double nS = NORMAL.getCDF(sign * d2);
    final double first = nF == 0. ? 0. : forward * nF;
    final double second = nS == 0. ? 0. : strike * nS;

    final double res = sign * (first - second);
    return Math.max(0., res);

  }

  /**
   * The PV of a single option
   * @param data required data on the option
   * @param lognormalVol The Black volatility
   * @return option PV
   */
  public static double price(final SimpleOptionData data, final double lognormalVol) {
    ArgumentChecker.notNull(data, "null data");
    return data.getDiscountFactor() *
        price(data.getForward(), data.getStrike(), data.getTimeToExpiry(), lognormalVol, data.isCall());
  }

  /**
   * The PV of a strip of options all with the same Black volatility
   * @param data array of required data on the option
   * @param lognormalVol The Black volatility
   * @return PV of option strip
   */
  public static double price(final SimpleOptionData[] data, final double lognormalVol) {
    ArgumentChecker.noNulls(data, "null data");
    final int n = data.length;
    double sum = 0;
    for (int i = 0; i < n; i++) {
      final SimpleOptionData temp = data[i];
      sum += temp.getDiscountFactor() *
          price(temp.getForward(), temp.getStrike(), temp.getTimeToExpiry(), lognormalVol, temp.isCall());
    }
    return sum;
  }

  /**
   * The forward (i.e. driftless) delta
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @param isCall true for call
   * @return The forward delta
   */
  @ExternalFunction
  public static double delta(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol, final boolean isCall) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    final int sign = isCall ? 1 : -1;

    double d1 = 0.;
    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);

    if (bSigRt) {
      return isCall ? 1. : 0.;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return (isCall ? (forward > strike ? 1.0 : 0.0) : (forward > strike ? 0.0 : -1.0));
      }
      s_logger.info("(log 1.)/0., ambiguous value");
      return isCall ? 0.5 : -0.5;
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d1 = 0.5 * sigmaRootT;
    } else {
      d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
    }

    return sign * NORMAL.getCDF(sign * d1);
  }

  public static double strikeForDelta(final double forward, final double forwardDelta, final double timeToExpiry,
      final double lognormalVol, final boolean isCall) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue((isCall && forwardDelta > 0 && forwardDelta < 1) ||
        (!isCall && forwardDelta > -1 && forwardDelta < 0), "delta out of range", forwardDelta);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    final int sign = isCall ? 1 : -1;
    final double d1 = sign * NORMAL.getInverseCDF(sign * forwardDelta);

    double sigmaSqT = lognormalVol * lognormalVol * timeToExpiry;
    if (Double.isNaN(sigmaSqT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaSqT = 1.;
    }

    return forward * Math.exp(-d1 * Math.sqrt(sigmaSqT) + 0.5 * sigmaSqT);
  }

  /**
   * The driftless dual delta (first derivative of option price with respect to strike)
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @param isCall true for call
   * @return The dual delta
   */
  @ExternalFunction
  public static double dualDelta(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol, final boolean isCall) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    final int sign = isCall ? 1 : -1;

    double d2 = 0.;
    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);

    if (bSigRt) {
      return isCall ? 0. : 1.;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return (isCall ? (forward > strike ? -1.0 : 0.0) : (forward > strike ? 0.0 : 1.0));
      }
      s_logger.info("(log 1.)/0., ambiguous value");
      return isCall ? -0.5 : 0.5;
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d2 = -0.5 * sigmaRootT;
    } else {
      d2 = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    }

    return -sign * NORMAL.getCDF(sign * d2);
  }

  /**
   * The simple delta.
   * Note that this is not the standard delta one is accustomed to.
   * The argument of the cumulative normal is simply d = Math.log(forward / strike) / sigmaRootT
   * 
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @param isCall true for call
   * @return The forward delta
   */
  @ExternalFunction
  public static double simpleDelta(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol, final boolean isCall) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    final int sign = isCall ? 1 : -1;

    double d = 0.;
    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);

    if (bSigRt) {
      return isCall ? 0.5 : -0.5;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return (isCall ? (forward > strike ? 1.0 : 0.0) : (forward > strike ? 0.0 : -1.0));
      }
      s_logger.info("(log 1.)/0., ambiguous");
      return isCall ? 0.5 : -0.5;
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d = 0.;
    } else {
      d = Math.log(forward / strike) / sigmaRootT;
    }

    return sign * NORMAL.getCDF(sign * d);
  }

  /**
   * The forward (i.e. driftless) gamma, 2nd order sensitivity of the forward option value to the forward.
   * <p>
   * $\frac{\partial^2 FV}{\partial^2 f}$
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @return The forward gamma
   */
  @ExternalFunction
  public static double gamma(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    double d1 = 0.;
    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);

    if (bSigRt) {
      return 0.;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return 0.0;
      }
      s_logger.info("(log 1.)/0. ambiguous");
      return bFwd ? NORMAL.getPDF(0.) : NORMAL.getPDF(0.) / forward / sigmaRootT;
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d1 = 0.5 * sigmaRootT;
    } else {
      d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
    }

    final double nVal = NORMAL.getPDF(d1);
    return nVal == 0. ? 0. : nVal / forward / sigmaRootT;
  }

  /**
   * The driftless dual gamma
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @return The dual gamma
   */
  @ExternalFunction
  public static double dualGamma(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    double d2 = 0.;
    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);

    if (bSigRt) {
      return 0.;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return 0.0;
      }
      s_logger.info("(log 1.)/0. ambiguous");
      return bStr ? NORMAL.getPDF(0.) : NORMAL.getPDF(0.) / strike / sigmaRootT;
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d2 = -0.5 * sigmaRootT;
    } else {
      d2 = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    }

    final double nVal = NORMAL.getPDF(d2);
    return nVal == 0. ? 0. : nVal / strike / sigmaRootT;
  }

  /**
   * The driftless cross gamma - the sensitity of the delta to the strike $\frac{\partial^2 V}{\partial f \partial K}$
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @return The dual gamma
   */
  @ExternalFunction
  public static double crossGamma(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    double d2 = 0.;
    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);

    if (bSigRt) {
      return 0.;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return 0.0;
      }
      s_logger.info("(log 1.)/0. ambiguous");
      return bFwd ? -NORMAL.getPDF(0.) : -NORMAL.getPDF(0.) / forward / sigmaRootT;
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d2 = -0.5 * sigmaRootT;
    } else {
      d2 = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    }

    final double nVal = NORMAL.getPDF(d2);
    return nVal == 0. ? 0. : -nVal / forward / sigmaRootT;
  }

  /**
   * The theta (non-forward), the sensitivity of the present value to a change in time to maturity, $\-frac{\partial
   * V}{\partial T}$
   * 
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @param isCall true for call, false for put
   * @param interestRate the interest rate
   * @return theta
   */
  @ExternalFunction
  public static double theta(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol, final boolean isCall, final double interestRate) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");

    if (-interestRate > LARGE) {
      return 0.;
    }
    final double driftLess = driftlessTheta(forward, strike, timeToExpiry, lognormalVol);
    if (Math.abs(interestRate) < SMALL) {
      return driftLess;
    }

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    final int sign = isCall ? 1 : -1;
    // final double b = 0; // for now set cost of carry to 0

    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);
    double d1 = 0.;
    double d2 = 0.;

    double priceLike = Double.NaN;
    final double rt = (timeToExpiry < SMALL && Math.abs(interestRate) > LARGE) ? (interestRate > 0. ? 1. : -1.)
        : interestRate * timeToExpiry;
    if (bFwd && bStr) {
      s_logger.info("(large value)/(large value) ambiguous");
      priceLike = isCall ? (forward >= strike ? forward : 0.) : (strike >= forward ? strike : 0.);
    } else {
      if (sigmaRootT < SMALL) {
        if (rt > LARGE) {
          priceLike = isCall ? (forward > strike ? forward : 0.0) : (forward > strike ? 0.0 : -forward);
        } else {
          priceLike = isCall ? (forward > strike ? forward - strike * Math.exp(-rt) : 0.0) : (forward > strike ? 0.0
              : -forward + strike * Math.exp(-rt));
        }
      } else {
        if (Math.abs(forward - strike) < SMALL | bSigRt) {
          d1 = 0.5 * sigmaRootT;
          d2 = -0.5 * sigmaRootT;
        } else {
          d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
          d2 = d1 - sigmaRootT;
        }
        final double nF = NORMAL.getCDF(sign * d1);
        final double nS = NORMAL.getCDF(sign * d2);
        final double first = nF == 0. ? 0. : forward * nF;
        final double second = ((nS == 0.) | (Math.exp(-interestRate * timeToExpiry) == 0.)) ? 0. : strike *
            Math.exp(-interestRate * timeToExpiry) * nS;
        priceLike = sign * (first - second);
      }
    }

    final double res = (interestRate > LARGE && Math.abs(priceLike) < SMALL) ? 0. : interestRate * priceLike;
    return Math.abs(res) > LARGE ? res : driftLess + res;
  }

  /**
   * The theta (non-forward), the sensitivity of the present value to a change in time to maturity, $\-frac{\partial
   * V}{\partial T}$
   * This is consistent with {@link BlackScholesFormulaRepository}
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @param isCall true for call, false for put
   * @param interestRate the interest rate
   * @return theta
   */
  @ExternalFunction
  public static double thetaMod(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol, final boolean isCall, final double interestRate) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");

    if (-interestRate > LARGE) {
      return 0.;
    }
    final double driftLess = driftlessTheta(forward, strike, timeToExpiry, lognormalVol);
    if (Math.abs(interestRate) < SMALL) {
      return driftLess;
    }

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    final int sign = isCall ? 1 : -1;

    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);
    double d2 = 0.;

    double priceLike = Double.NaN;
    final double rt = (timeToExpiry < SMALL && Math.abs(interestRate) > LARGE) ? (interestRate > 0. ? 1. : -1.)
        : interestRate * timeToExpiry;
    if (bFwd && bStr) {
      s_logger.info("(large value)/(large value) ambiguous");
      priceLike = isCall ? 0. : (strike >= forward ? strike : 0.);
    } else {
      if (sigmaRootT < SMALL) {
        if (rt > LARGE) {
          priceLike = 0.;
        } else {
          priceLike = isCall ? (forward > strike ? -strike : 0.0) : (forward > strike ? 0.0 : +strike);
        }
      } else {
        if (Math.abs(forward - strike) < SMALL | bSigRt) {
          d2 = -0.5 * sigmaRootT;
        } else {
          d2 = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
        }
        final double nS = NORMAL.getCDF(sign * d2);
        priceLike = (nS == 0.) ? 0. : -sign * strike * nS;
      }
    }

    final double res = (interestRate > LARGE && Math.abs(priceLike) < SMALL) ? 0. : interestRate * priceLike;
    return Math.abs(res) > LARGE ? res : driftLess + res;
  }

  /**
   * The forward (i.e. driftless) theta
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @return The driftless theta
   */
  @ExternalFunction
  public static double driftlessTheta(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);
    double d1 = 0.;

    if (bSigRt) {
      return 0.;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return 0.0;
      }
      s_logger.info("log(1)/0 ambiguous");
      if (rootT < SMALL) {
        return forward < SMALL ? -NORMAL.getPDF(0.) * lognormalVol / 2. : (lognormalVol < SMALL ? -forward *
            NORMAL.getPDF(0.) / 2. : -forward * NORMAL.getPDF(0.) * lognormalVol / 2. / rootT);
      }
      if (lognormalVol < SMALL) {
        return bFwd ? -NORMAL.getPDF(0.) / 2. / rootT : -forward * NORMAL.getPDF(0.) * lognormalVol / 2. / rootT;
      }
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d1 = 0.5 * sigmaRootT;
    } else {
      d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
    }

    final double nVal = NORMAL.getPDF(d1);
    return nVal == 0. ? 0. : -forward * nVal * lognormalVol / 2. / rootT;
  }

  /**
   * The forward vega of an option, i.e. the sensitivity of the option's forward price wrt the implied volatility (which
   * is just the the spot vega
   * divided by the the numeraire)
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @return The forward vega
   */
  @ExternalFunction
  public static double vega(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);
    double d1 = 0.;

    if (bSigRt) {
      return 0.;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return 0.;
      }
      s_logger.info("log(1)/0 ambiguous");
      return (rootT < SMALL && forward > LARGE) ? NORMAL.getPDF(0.) : forward * rootT * NORMAL.getPDF(0.);
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d1 = 0.5 * sigmaRootT;
    } else {
      d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
    }

    final double nVal = NORMAL.getPDF(d1);
    return nVal == 0. ? 0. : forward * rootT * nVal;
  }

  @ExternalFunction
  public static double vega(final SimpleOptionData data, final double lognormalVol) {
    ArgumentChecker.notNull(data, "null data");
    return data.getDiscountFactor() * vega(data.getForward(), data.getStrike(), data.getTimeToExpiry(), lognormalVol);
  }

  /**
   * The driftless vanna of an option, i.e. second order derivative of the option value, once to the underlying forward
   * and once to volatility.
   * <p>
   * $\frac{\partial^2 FV}{\partial f \partial \sigma}$
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @return The forward vanna
   */
  @ExternalFunction
  public static double vanna(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);
    double d1 = 0.;
    double d2 = 0.;

    if (bSigRt) {
      return 0.;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return 0.0;
      }
      s_logger.info("log(1)/0 ambiguous");
      return lognormalVol < SMALL ? -NORMAL.getPDF(0.) / lognormalVol : NORMAL.getPDF(0.) * rootT;
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d1 = 0.5 * sigmaRootT;
      d2 = -0.5 * sigmaRootT;
    } else {
      d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
      d2 = d1 - sigmaRootT;
    }

    final double nVal = NORMAL.getPDF(d1);
    return nVal == 0. ? 0. : -nVal * d2 / lognormalVol;
  }

  /**
   * The driftless dual vanna of an option, i.e. second order derivative of the option value, once to the strike and
   * once to volatility.
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @return The forward dual vanna
   */
  @ExternalFunction
  public static double dualVanna(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);
    double d1 = 0.;
    double d2 = 0.;

    if (bSigRt) {
      return 0.;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return 0.0;
      }
      s_logger.info("log(1)/0 ambiguous");
      return lognormalVol < SMALL ? -NORMAL.getPDF(0.) / lognormalVol : -NORMAL.getPDF(0.) * rootT;
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d1 = 0.5 * sigmaRootT;
      d2 = -0.5 * sigmaRootT;
    } else {
      d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
      d2 = d1 - sigmaRootT;
    }

    final double nVal = NORMAL.getPDF(d2);
    return nVal == 0. ? 0. : nVal * d1 / lognormalVol;
  }

  /**
   * The driftless vomma (aka volga) of an option, i.e. second order derivative of the option forward price with respect
   * to the implied volatility.
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @return The forward vomma
   */
  @ExternalFunction
  public static double vomma(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol) {
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    final boolean bFwd = (forward > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);
    double d1 = 0.;
    double d2 = 0.;

    if (bSigRt) {
      return 0.;
    }
    if (sigmaRootT < SMALL) {
      if (Math.abs(forward - strike) >= SMALL && !(bFwd && bStr)) {
        return 0.0;
      }
      s_logger.info("log(1)/0 ambiguous");
      if (bFwd) {
        return rootT < SMALL ? NORMAL.getPDF(0.) / lognormalVol : forward * NORMAL.getPDF(0.) * rootT / lognormalVol;
      }
      return lognormalVol < SMALL ? forward * NORMAL.getPDF(0.) * rootT / lognormalVol : -forward * NORMAL.getPDF(0.) *
          timeToExpiry * lognormalVol / 4.;
    }
    if (Math.abs(forward - strike) < SMALL | (bFwd && bStr)) {
      d1 = 0.5 * sigmaRootT;
      d2 = -0.5 * sigmaRootT;
    } else {
      d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
      d2 = d1 - sigmaRootT;
    }

    final double nVal = NORMAL.getPDF(d1);
    final double res = nVal == 0. ? 0. : forward * nVal * rootT * d1 * d2 / lognormalVol;
    return res;
  }

  /**
   * The driftless volga (aka vomma) of an option, i.e. second order derivative of the option forward price with respect
   * to the implied volatility.
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @return The forward vomma
   */
  @ExternalFunction
  public static double volga(final double forward, final double strike, final double timeToExpiry,
      final double lognormalVol) {
    return vomma(forward, strike, timeToExpiry, lognormalVol);
  }

  /**
   * Get the log-normal (Black) implied volatility of an European option
   * @param price The <b>forward</b> price - i.e. the market price divided by the numeraire (i.e. the zero bond p(0,T)
   * for the T-forward measure)
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param isCall true for call
   * @return log-normal (Black) implied volatility
   */
  public static double impliedVolatility(final double price, final double forward, final double strike,
      final double timeToExpiry, final boolean isCall) {
    ArgumentChecker.isTrue(price > 0.0, "negative/NaN price; have {}", price);
    ArgumentChecker.isTrue(forward > 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike > 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);

    ArgumentChecker.isFalse(Double.isInfinite(forward), "forward is Infinity");
    ArgumentChecker.isFalse(Double.isInfinite(strike), "strike is Infinity");
    ArgumentChecker.isFalse(Double.isInfinite(timeToExpiry), "timeToExpiry is Infinity");

    final double intrinsicPrice = Math.max(0., (isCall ? 1 : -1) * (forward - strike));

    final double targetPrice = price - intrinsicPrice; // Math.max(0., price - intrinsicPrice) should not used for least
                                                       // chi square
    final double sigmaGuess = 0.3;
    return impliedVolatility(targetPrice, forward, strike, timeToExpiry, sigmaGuess);
  }

  /**
   * Get the log-normal (Black) implied volatility of an out-the-money European option starting from an initial guess
   * @param otmPrice The <b>forward</b> price - i.e. the market price divided by the numeraire (i.e. the zero bond
   * p(0,T) for the T-forward measure)
   * <b>Note</b> This MUST be an OTM price - i.e. a call price for strike >= forward and a put price otherwise
   * @param forward The forward value of the underlying
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param volGuess a guess of the implied volatility
   * @return log-normal (Black) implied volatility
   */
  @ExternalFunction
  public static double impliedVolatility(final double otmPrice, final double forward, final double strike,
      final double timeToExpiry, final double volGuess) {
    ArgumentChecker.isTrue(otmPrice >= 0.0, "negative/NaN otmPrice; have {}", otmPrice);
    ArgumentChecker.isTrue(forward >= 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(volGuess >= 0.0, "negative/NaN volGuess; have {}", volGuess);

    ArgumentChecker.isFalse(Double.isInfinite(otmPrice), "otmPrice is Infinity");
    ArgumentChecker.isFalse(Double.isInfinite(forward), "forward is Infinity");
    ArgumentChecker.isFalse(Double.isInfinite(strike), "strike is Infinity");
    ArgumentChecker.isFalse(Double.isInfinite(timeToExpiry), "timeToExpiry is Infinity");
    ArgumentChecker.isFalse(Double.isInfinite(volGuess), "volGuess is Infinity");

    if (otmPrice == 0) {
      return 0;
    }
    ArgumentChecker.isTrue(otmPrice < Math.min(forward, strike), "otmPrice of {} exceeded upper bound of {}", otmPrice,
        Math.min(forward, strike));

    if (forward == strike) {
      return NORMAL.getInverseCDF(0.5 * (otmPrice / forward + 1)) * 2 / Math.sqrt(timeToExpiry);
    }

    final boolean isCall = strike >= forward;

    Function1D<Double, Double> priceFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return price(forward, strike, timeToExpiry, x, isCall);
      }
    };

    Function1D<Double, Double> vegaFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return vega(forward, strike, timeToExpiry, x);
      }
    };

    return impliedVolatility(otmPrice, priceFunc, vegaFunc, volGuess);
  }

  /**
   * This is a generic implied volatility solver (which is an implementation of Newton-Raphson). It just required you to
   * supply two functions which give a price and a vega for a volatility; these could be the Black price and vega or
   * otherwise.
   * @param price The target price
   * @param priceFunc Price function - gives the price for a volatility
   * @param vegaFunc Vega function - gives the vega for a volatility
   * @param volGuess a guess of the implied volatility
   * @return The volatility
   */
  public static double impliedVolatility(double price, Function1D<Double, Double> priceFunc,
      Function1D<Double, Double> vegaFunc, final double volGuess) {
    ArgumentChecker.notNull(priceFunc, "priceFunc");
    ArgumentChecker.notNull(vegaFunc, "vegaFunc");
    ArgumentChecker.isTrue(volGuess >= 0.0, "negative/NaN volGuess; have {}", volGuess);
    ArgumentChecker.isFalse(Double.isInfinite(volGuess), "volGuess is Infinity");

    double lowerSigma;
    double upperSigma;

    try {
      final double[] temp = bracketRoot(price, priceFunc, volGuess, Math.min(volGuess, 0.1));
      lowerSigma = temp[0];
      upperSigma = temp[1];
    } catch (final MathException e) {
      throw new IllegalArgumentException(e.toString() + " No implied Volatility for this price. price: " + price);
    }
    double sigma = (lowerSigma + upperSigma) / 2.0;
    final double maxChange = 0.5;

    double p = priceFunc.evaluate(sigma);
    double v = vegaFunc.evaluate(sigma);
    // TODO check if this is ever called
    if (v == 0 || Double.isNaN(v)) {
      return solveByBisection(price, priceFunc, lowerSigma, upperSigma);
    }
    double diff = p - price;
    boolean above = diff > 0;
    if (above) {
      upperSigma = sigma;
    } else {
      lowerSigma = sigma;
    }

    double trialChange = -diff / v;
    double actChange;
    if (trialChange > 0.0) {
      actChange = Math.min(maxChange, Math.min(trialChange, upperSigma - sigma));
    } else {
      actChange = Math.max(-maxChange, Math.max(trialChange, lowerSigma - sigma));
    }

    int count = 0;
    while (Math.abs(actChange) > VOL_TOL) {
      sigma += actChange;
      p = priceFunc.evaluate(sigma);
      v = vegaFunc.evaluate(sigma);

      if (v == 0.0 || Double.isNaN(v)) {
        return solveByBisection(price, priceFunc, lowerSigma, upperSigma);
      }

      diff = p - price;
      above = diff > 0;
      if (above) {
        upperSigma = sigma;
      } else {
        lowerSigma = sigma;
      }

      trialChange = -diff / v;
      if (trialChange > 0.0) {
        actChange = Math.min(maxChange, Math.min(trialChange, upperSigma - sigma));
      } else {
        actChange = Math.max(-maxChange, Math.max(trialChange, lowerSigma - sigma));
      }

      if (count++ > MAX_ITERATIONS) {
        return solveByBisection(price, priceFunc, lowerSigma, upperSigma);
      }
    }

    return sigma + actChange; // apply the final change
  }

  /**
   * The implied volatility of an option
   * @param data basic description of the option
   * @param price he (market) price of the option
   * @return The implied volatility
   */
  public static double impliedVolatility(final SimpleOptionData data, final double price) {
    ArgumentChecker.notNull(data, "null data");
    return impliedVolatility(price / data.getDiscountFactor(), data.getForward(), data.getStrike(),
        data.getTimeToExpiry(), data.isCall());
  }

  /**
   * Find the single volatility for a portfolio of European options such that the sum of Black prices of the options
   * (with that volatility)
   * equals the (market) price of the portfolio - this is the implied volatility of the portfolio. A concrete example is
   * a cap (floor) which
   * can be viewed as a portfolio of caplets (floorlets)
   * @param data basic description of each option
   * @param price The (market) price of the portfolio
   * @return The implied volatility of the portfolio
   */
  public static double impliedVolatility(final SimpleOptionData[] data, final double price) {
    Validate.notEmpty(data, "no option data given");
    double intrinsicPrice = 0.0;
    for (final SimpleOptionData option : data) {
      intrinsicPrice += Math.max(0, (option.isCall() ? 1 : -1) * option.getDiscountFactor() *
          (option.getForward() - option.getStrike()));
    }
    Validate.isTrue(price >= intrinsicPrice, "option price (" + price + ") less than intrinsic value (" +
        intrinsicPrice + ")");

    if (Double.doubleToLongBits(price) == Double.doubleToLongBits(intrinsicPrice)) {
      return 0.0;
    }

    double sigma = 0.3;

    Function1D<Double, Double> priceFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        double modelPrice = 0.0;
        for (final SimpleOptionData option : data) {
          modelPrice += price(option, x);
        }
        return modelPrice;
      }
    };

    Function1D<Double, Double> vegaFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        double vega = 0.0;
        for (final SimpleOptionData option : data) {
          vega += vega(option, x);
        }
        return vega;
      }
    };

    return impliedVolatility(price, priceFunc, vegaFunc, sigma);
  }

  /**
   * Computes the implied strike from delta and volatility in the Black formula.
   * @param delta The option delta
   * @param isCall The call (true) / put (false) flag.
   * @param forward The forward.
   * @param time The time to expiration.
   * @param volatility The volatility.
   * @return The strike.
   */
  @ExternalFunction
  public static double impliedStrike(final double delta, final boolean isCall, final double forward, final double time,
      final double volatility) {
    Validate.isTrue(delta > -1 && delta < 1, "Delta out of range");
    Validate.isTrue(isCall ^ (delta < 0), "Delta incompatible with call/put: " + isCall + ", " + delta);
    Validate.isTrue(forward > 0, "Forward negative");
    final double omega = (isCall ? 1.0 : -1.0);
    final double strike = forward *
        Math.exp(-volatility * Math.sqrt(time) * omega * NORMAL.getInverseCDF(omega * delta) + volatility * volatility *
            time / 2);
    return strike;
  }

  /**
   * Computes the implied strike and its derivatives from delta and volatility in the Black formula.
   * @param delta The option delta
   * @param isCall The call (true) / put (false) flag.
   * @param forward The forward.
   * @param time The time to expiration.
   * @param volatility The volatility.
   * @param derivatives The derivatives of the implied strike with respect to the input. The array is changed by the
   * method.
   * Derivatives with respect to: [0] delta, [1] forward, [2] time, [3] volatility.
   * @return The strike.
   */
  public static double impliedStrike(final double delta, final boolean isCall, final double forward, final double time,
      final double volatility, final double[] derivatives) {
    Validate.isTrue(delta > -1 && delta < 1, "Delta out of range");
    Validate.isTrue(isCall ^ (delta < 0), "Delta incompatible with call/put: " + isCall + ", " + delta);
    Validate.isTrue(forward > 0, "Forward negative");
    final double omega = (isCall ? 1.0 : -1.0);
    final double sqrtt = Math.sqrt(time);
    final double n = NORMAL.getInverseCDF(omega * delta);
    final double part1 = Math.exp(-volatility * sqrtt * omega * n + volatility * volatility * time / 2);
    final double strike = forward * part1;
    // Backward sweep
    final double strikeBar = 1.0;
    final double part1Bar = forward * strikeBar;
    final double nBar = part1 * -volatility * Math.sqrt(time) * omega * part1Bar;
    derivatives[0] = omega / NORMAL.getPDF(n) * nBar;
    derivatives[1] = part1 * strikeBar;
    derivatives[2] = part1 * (-volatility * omega * n * 0.5 / sqrtt + volatility * volatility / 2) * part1Bar;
    derivatives[3] = part1 * (-sqrtt * omega * n + volatility * time) * part1Bar;
    return strike;
  }

  private static double[] bracketRoot(final double forwardPrice, final Function1D<Double, Double> priceFunc,
      double sigma, double change) {
    final BracketRoot bracketer = new BracketRoot();
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double volatility) {
        return priceFunc.evaluate(volatility) / forwardPrice - 1.0;
      }
    };
    return bracketer.getBracketedPoints(func, sigma - Math.abs(change), sigma + Math.abs(change), 0,
        Double.POSITIVE_INFINITY);
  }

  private static double solveByBisection(final double forwardPrice, final Function1D<Double, Double> priceFunc,
      double lowerSigma, double upperSigma) {

    final BisectionSingleRootFinder rootFinder = new BisectionSingleRootFinder(VOL_TOL);
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double volatility) {
        final double trialPrice = priceFunc.evaluate(volatility);
        return trialPrice / forwardPrice - 1.0;
      }
    };
    return rootFinder.getRoot(func, lowerSigma, upperSigma);
  }

}
