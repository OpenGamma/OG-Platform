/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.lang.annotation.ExternalFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class BlackScholesFormulaRepository {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final double SMALL = 1.0E-13;
  private static final double LARGE = 1.0E13;

  private static final Logger s_logger = LoggerFactory.getLogger(BlackScholesFormulaRepository.class);

  /**
  * The <b>spot</b> price
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @param isCall True for calls, false for puts
  * @return The <b>spot</b> price
  */
  @ExternalFunction
  public static double price(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry, final boolean isCall) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    if (interestRate > LARGE) {
      return 0.;
    }
    if (-interestRate > LARGE) {
      return Double.POSITIVE_INFINITY;
    }
    double discount = Math.abs(interestRate) < SMALL ? 1. : Math.exp(-interestRate * timeToExpiry);

    if (costOfCarry > LARGE) {
      return isCall ? Double.POSITIVE_INFINITY : 0.;
    }
    if (-costOfCarry > LARGE) {
      final double res = isCall ? 0. : (discount > SMALL ? strike * discount : 0.);
      return Double.isNaN(res) ? discount : res;
    }
    double factor = Math.exp(costOfCarry * timeToExpiry);

    if (spot > LARGE * strike) {
      final double tmp = Math.exp((costOfCarry - interestRate) * timeToExpiry);
      return isCall ? (tmp > SMALL ? spot * tmp : 0.) : 0.;
    }
    if (LARGE * spot < strike) {
      return (isCall || discount < SMALL) ? 0. : strike * discount;
    }
    if (spot > LARGE && strike > LARGE) {
      final double tmp = Math.exp((costOfCarry - interestRate) * timeToExpiry);
      return isCall ? (tmp > SMALL ? spot * tmp : 0.) : (discount > SMALL ? strike * discount : 0.);
    }

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      sigmaRootT = 1.; //ref value is returned
    }

    final int sign = isCall ? 1 : -1;
    final double rescaledSpot = factor * spot;
    if (sigmaRootT < SMALL) {
      final double res = isCall ? (rescaledSpot > strike ? discount * (rescaledSpot - strike) : 0.) : (rescaledSpot < strike ? discount * (strike - rescaledSpot) : 0.);
      return Double.isNaN(res) ? sign * (spot - discount * strike) : res;
    }

    double d1 = 0.;
    double d2 = 0.;
    if (Math.abs(spot - strike) < SMALL || sigmaRootT > LARGE) {
      final double coefD1 = (costOfCarry / lognormalVol + 0.5 * lognormalVol);
      final double coefD2 = (costOfCarry / lognormalVol - 0.5 * lognormalVol);
      final double tmpD1 = coefD1 * rootT;
      final double tmpD2 = coefD2 * rootT;
      d1 = Double.isNaN(tmpD1) ? 0. : tmpD1;
      d2 = Double.isNaN(tmpD2) ? 0. : tmpD2;
    } else {
      final double tmp = costOfCarry * rootT / lognormalVol;
      final double sig = (costOfCarry >= 0.) ? 1. : -1.;
      final double scnd = Double.isNaN(tmp) ? ((lognormalVol < LARGE && lognormalVol > SMALL) ? sig / lognormalVol : sig * rootT) : tmp;
      d1 = Math.log(spot / strike) / sigmaRootT + scnd + 0.5 * sigmaRootT;
      d2 = d1 - sigmaRootT;
    }
    //    if (Double.isNaN(d1) || Double.isNaN(d2)) {
    //      throw new IllegalArgumentException("NaN found");
    //    }
    final double res = sign * discount * (rescaledSpot * NORMAL.getCDF(sign * d1) - strike * NORMAL.getCDF(sign * d2));
    return Double.isNaN(res) ? 0. : Math.max(res, 0.);
  }

  /**
  * The spot delta
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry rate
  * @param isCall true for call
  * @return The spot delta
  */
  @ExternalFunction
  public static double delta(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry, final boolean isCall) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    double coef = 0.;
    if ((interestRate > LARGE && costOfCarry > LARGE) || (-interestRate > LARGE && -costOfCarry > LARGE) || Math.abs(costOfCarry - interestRate) < SMALL) {
      coef = 1.; //ref value is returned
    } else {
      final double rate = costOfCarry - interestRate;
      if (rate > LARGE) {
        return isCall ? Double.POSITIVE_INFINITY : (costOfCarry > LARGE ? 0. : Double.NEGATIVE_INFINITY);
      }
      if (-rate > LARGE) {
        return 0.;
      }
      coef = (Math.abs(rate) < SMALL && timeToExpiry > LARGE) ? 1. : Math.exp(rate * timeToExpiry);
    }

    if (spot > LARGE * strike) {
      return isCall ? coef : 0.;
    }
    if (spot < SMALL * strike) {
      return isCall ? 0. : -coef;
    }

    final int sign = isCall ? 1 : -1;
    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      sigmaRootT = 1.; //ref value is returned
    }

    double factor = Math.exp(costOfCarry * timeToExpiry);
    if (Double.isNaN(factor)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factor = 1.;
    }
    double rescaledSpot = spot * factor;

    double d1 = 0.;
    if (Math.abs(spot - strike) < SMALL || sigmaRootT > LARGE || (spot > LARGE && strike > LARGE)) {
      final double coefD1 = (costOfCarry / lognormalVol + 0.5 * lognormalVol);
      final double tmp = coefD1 * rootT;
      d1 = Double.isNaN(tmp) ? 0. : tmp;
    } else {
      if (sigmaRootT < SMALL) {
        return isCall ? (rescaledSpot > strike ? coef : 0.) : (rescaledSpot < strike ? -coef : 0.);
      }
      final double tmp = costOfCarry * rootT / lognormalVol;
      final double sig = (costOfCarry >= 0.) ? 1. : -1.;
      final double scnd = Double.isNaN(tmp) ? ((lognormalVol < LARGE && lognormalVol > SMALL) ? sig / lognormalVol : sig * rootT) : tmp;
      d1 = Math.log(spot / strike) / sigmaRootT + scnd + 0.5 * sigmaRootT;
    }
    //    if (Double.isNaN(d1)) {
    //      throw new IllegalArgumentException("NaN found");
    //    }
    final double norm = NORMAL.getCDF(sign * d1);

    return norm < SMALL ? 0. : sign * coef * norm;
  }

  /**
   * 
   * @param spot The spot value of the underlying
   * @param spotDelta The spot delta
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility
   * @param interestRate The interest rate 
   * @param costOfCarry The cost-of-carry rate
   * @param isCall true for call
   * @return The strike
   */
  public static double strikeForDelta(final double spot, final double spotDelta, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry,
      final boolean isCall) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    double coeff = Math.exp((costOfCarry - interestRate) * timeToExpiry);
    if (Double.isNaN(coeff)) {
      s_logger.info("interestRate * timeToExpiry ambiguous");
      coeff = 1.;
    }
    final double rescaledDelta = spotDelta / coeff;
    Validate.isTrue((isCall && rescaledDelta > 0 && rescaledDelta < 1.) || (!isCall && spotDelta < 0 && rescaledDelta > -1.),
        "delta/Math.exp((costOfCarry - interestRate) * timeToExpiry) out of range, ", rescaledDelta);

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    double factor = Math.exp(costOfCarry * timeToExpiry);
    if (Double.isNaN(factor)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factor = 1.;
    }
    double rescaledSpot = spot * factor;

    final int sign = isCall ? 1 : -1;
    final double d1 = sign * NORMAL.getInverseCDF(sign * rescaledDelta);
    return rescaledSpot * Math.exp(-d1 * sigmaRootT + 0.5 * sigmaRootT * sigmaRootT);
  }

  /**
  * The dual delta (first derivative of option price with respect to strike)
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @param isCall true for call
  * @return The dual delta
  */
  @ExternalFunction
  public static double dualDelta(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry,
      final boolean isCall) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    double discount = 0.;
    if (-interestRate > LARGE) {
      return isCall ? Double.NEGATIVE_INFINITY : (costOfCarry > LARGE ? 0. : Double.POSITIVE_INFINITY);
    }
    if (interestRate > LARGE) {
      return 0.;
    }
    discount = (Math.abs(interestRate) < SMALL && timeToExpiry > LARGE) ? 1. : Math.exp(-interestRate * timeToExpiry);

    if (spot > LARGE * strike) {
      return isCall ? -discount : 0.;
    }
    if (spot < SMALL * strike) {
      return isCall ? 0. : discount;
    }

    final int sign = isCall ? 1 : -1;
    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      sigmaRootT = 1.; //ref value is returned
    }

    double factor = Math.exp(costOfCarry * timeToExpiry);
    if (Double.isNaN(factor)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factor = 1.;
    }
    double rescaledSpot = spot * factor;

    double d2 = 0.;
    if (Math.abs(spot - strike) < SMALL || sigmaRootT > LARGE || (spot > LARGE && strike > LARGE)) {
      final double coefD2 = (costOfCarry / lognormalVol - 0.5 * lognormalVol);
      final double tmp = coefD2 * rootT;
      d2 = Double.isNaN(tmp) ? 0. : tmp;
    } else {
      if (sigmaRootT < SMALL) {
        return isCall ? (rescaledSpot > strike ? -discount : 0.) : (rescaledSpot < strike ? discount : 0.);
      }
      final double tmp = costOfCarry * rootT / lognormalVol;
      final double sig = (costOfCarry >= 0.) ? 1. : -1.;
      final double scnd = Double.isNaN(tmp) ? ((lognormalVol < LARGE && lognormalVol > SMALL) ? sig / lognormalVol : sig * rootT) : tmp;
      d2 = Math.log(spot / strike) / sigmaRootT + scnd - 0.5 * sigmaRootT;
    }
    //    if (Double.isNaN(d2)) {
    //      throw new IllegalArgumentException("NaN found");
    //    }
    final double norm = NORMAL.getCDF(sign * d2);

    return norm < SMALL ? 0. : -sign * discount * norm;
  }

  /**
  * The spot gamma, 2nd order sensitivity of the spot option value to the spot. <p>
  * $\frac{\partial^2 FV}{\partial^2 f}$
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @return The spot gamma
  */
  @ExternalFunction
  public static double gamma(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    double coef = 0.;
    if ((interestRate > LARGE && costOfCarry > LARGE) || (-interestRate > LARGE && -costOfCarry > LARGE) || Math.abs(costOfCarry - interestRate) < SMALL) {
      coef = 1.; //ref value is returned
    } else {
      final double rate = costOfCarry - interestRate;
      if (rate > LARGE) {
        return costOfCarry > LARGE ? 0. : Double.POSITIVE_INFINITY;
      }
      if (-rate > LARGE) {
        return 0.;
      }
      coef = (Math.abs(rate) < SMALL && timeToExpiry > LARGE) ? 1. : Math.exp(rate * timeToExpiry);
    }

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      sigmaRootT = 1.; //ref value is returned
    }
    if (spot > LARGE * strike || spot < SMALL * strike || sigmaRootT > LARGE) {
      return 0.;
    }

    double factor = Math.exp(costOfCarry * timeToExpiry);
    if (Double.isNaN(factor)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factor = 1.;
    }

    double d1 = 0.;
    if (Math.abs(spot - strike) < SMALL || (spot > LARGE && strike > LARGE)) {
      final double coefD1 = (Math.abs(costOfCarry) < SMALL && lognormalVol < SMALL) ? Math.signum(costOfCarry) + 0.5 * lognormalVol : (costOfCarry / lognormalVol + 0.5 * lognormalVol);
      final double tmp = coefD1 * rootT;
      d1 = Double.isNaN(tmp) ? 0. : tmp;
    } else {
      if (sigmaRootT < SMALL) {
        final double scnd = (Math.abs(costOfCarry) > LARGE && rootT < SMALL) ? Math.signum(costOfCarry) : costOfCarry * rootT;
        final double tmp = (Math.log(spot / strike) / rootT + scnd) / lognormalVol;
        d1 = Double.isNaN(tmp) ? 0. : tmp;
      } else {
        final double tmp = costOfCarry * rootT / lognormalVol;
        final double sig = (costOfCarry >= 0.) ? 1. : -1.;
        final double scnd = Double.isNaN(tmp) ? ((lognormalVol < LARGE && lognormalVol > SMALL) ? sig / lognormalVol : sig * rootT) : tmp;
        d1 = Math.log(spot / strike) / sigmaRootT + scnd + 0.5 * sigmaRootT;
      }
    }
    //    if (Double.isNaN(d1)) {
    //      throw new IllegalArgumentException("NaN found");
    //    }
    final double norm = NORMAL.getPDF(d1);

    final double res = norm < SMALL ? 0. : coef * norm / spot / sigmaRootT;
    return Double.isNaN(res) ? Double.POSITIVE_INFINITY : res;
  }

  /**
  * The dual gamma
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @return The dual gamma
  */
  @ExternalFunction
  public static double dualGamma(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    if (-interestRate > LARGE) {
      return costOfCarry > LARGE ? 0. : Double.POSITIVE_INFINITY;
    }
    if (interestRate > LARGE) {
      return 0.;
    }
    double discount = (Math.abs(interestRate) < SMALL && timeToExpiry > LARGE) ? 1. : Math.exp(-interestRate * timeToExpiry);

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      sigmaRootT = 1.; //ref value is returned
    }
    if (spot > LARGE * strike || spot < SMALL * strike || sigmaRootT > LARGE) {
      return 0.;
    }

    double factor = Math.exp(costOfCarry * timeToExpiry);
    if (Double.isNaN(factor)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factor = 1.;
    }

    double d2 = 0.;
    if (Math.abs(spot - strike) < SMALL || (spot > LARGE && strike > LARGE)) {
      final double coefD1 = (Math.abs(costOfCarry) < SMALL && lognormalVol < SMALL) ? Math.signum(costOfCarry) - 0.5 * lognormalVol : (costOfCarry / lognormalVol - 0.5 * lognormalVol);
      final double tmp = coefD1 * rootT;
      d2 = Double.isNaN(tmp) ? 0. : tmp;
    } else {
      if (sigmaRootT < SMALL) {
        final double scnd = (Math.abs(costOfCarry) > LARGE && rootT < SMALL) ? Math.signum(costOfCarry) : costOfCarry * rootT;
        final double tmp = (Math.log(spot / strike) / rootT + scnd) / lognormalVol;
        d2 = Double.isNaN(tmp) ? 0. : tmp;
      } else {
        final double tmp = costOfCarry * rootT / lognormalVol;
        final double sig = (costOfCarry >= 0.) ? 1. : -1.;
        final double scnd = Double.isNaN(tmp) ? ((lognormalVol < LARGE && lognormalVol > SMALL) ? sig / lognormalVol : sig * rootT) : tmp;
        d2 = Math.log(spot / strike) / sigmaRootT + scnd - 0.5 * sigmaRootT;
      }
    }
    //    if (Double.isNaN(d2)) {
    //      throw new IllegalArgumentException("NaN found");
    //    }
    final double norm = NORMAL.getPDF(d2);

    final double res = norm < SMALL ? 0. : discount * norm / strike / sigmaRootT;
    return Double.isNaN(res) ? Double.POSITIVE_INFINITY : res;
  }

  /**
  * The cross gamma - the sensitity of the delta to the strike $\frac{\partial^2 V}{\partial f \partial K}$
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @return The dual gamma
  */
  @ExternalFunction
  public static double crossGamma(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    if (-interestRate > LARGE) {
      return costOfCarry > LARGE ? 0. : Double.NEGATIVE_INFINITY;
    }
    if (interestRate > LARGE) {
      return 0.;
    }
    double discount = (Math.abs(interestRate) < SMALL && timeToExpiry > LARGE) ? 1. : Math.exp(-interestRate * timeToExpiry);

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      sigmaRootT = 1.; //ref value is returned
    }
    if (spot > LARGE * strike || spot < SMALL * strike || sigmaRootT > LARGE) {
      return 0.;
    }

    double factor = Math.exp(costOfCarry * timeToExpiry);
    if (Double.isNaN(factor)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factor = 1.;
    }

    double d2 = 0.;
    if (Math.abs(spot - strike) < SMALL || (spot > LARGE && strike > LARGE)) {
      final double coefD1 = (Math.abs(costOfCarry) < SMALL && lognormalVol < SMALL) ? Math.signum(costOfCarry) - 0.5 * lognormalVol : (costOfCarry / lognormalVol - 0.5 * lognormalVol);
      final double tmp = coefD1 * rootT;
      d2 = Double.isNaN(tmp) ? 0. : tmp;
    } else {
      if (sigmaRootT < SMALL) {
        final double scnd = (Math.abs(costOfCarry) > LARGE && rootT < SMALL) ? Math.signum(costOfCarry) : costOfCarry * rootT;
        final double tmp = (Math.log(spot / strike) / rootT + scnd) / lognormalVol;
        d2 = Double.isNaN(tmp) ? 0. : tmp;
      } else {
        final double tmp = costOfCarry * rootT / lognormalVol;
        final double sig = (costOfCarry >= 0.) ? 1. : -1.;
        final double scnd = Double.isNaN(tmp) ? ((lognormalVol < LARGE && lognormalVol > SMALL) ? sig / lognormalVol : sig * rootT) : tmp;
        d2 = Math.log(spot / strike) / sigmaRootT + scnd - 0.5 * sigmaRootT;
      }
    }
    //    if (Double.isNaN(d2)) {
    //      throw new IllegalArgumentException("NaN found");
    //    }
    final double norm = NORMAL.getPDF(d2);

    final double res = norm < SMALL ? 0. : -discount * norm / spot / sigmaRootT;
    return Double.isNaN(res) ? Double.NEGATIVE_INFINITY : res;
  }

  /**
  * The theta, the sensitivity of the present value to a change in time to maturity, $\-frac{\partial V}{\partial T}$
  *
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @param isCall true for call, false for put
  * @return theta
  */
  @ExternalFunction
  public static double theta(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry, final boolean isCall) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    if (Math.abs(interestRate) > LARGE) {
      return 0.;
    }
    double discount = (Math.abs(interestRate) < SMALL && timeToExpiry > LARGE) ? 1. : Math.exp(-interestRate * timeToExpiry);

    if (costOfCarry > LARGE) {
      return isCall ? Double.NEGATIVE_INFINITY : 0.;
    }
    if (-costOfCarry > LARGE) {
      final double res = isCall ? 0. : (discount > SMALL ? strike * discount * interestRate : 0.);
      return Double.isNaN(res) ? discount : res;
    }

    if (spot > LARGE * strike) {
      final double tmp = Math.exp((costOfCarry - interestRate) * timeToExpiry);
      final double res = isCall ? (tmp > SMALL ? -(costOfCarry - interestRate) * spot * tmp : 0.) : 0.;
      return Double.isNaN(res) ? tmp : res;
    }
    if (LARGE * spot < strike) {
      final double res = isCall ? 0. : (discount > SMALL ? strike * discount * interestRate : 0.);
      return Double.isNaN(res) ? discount : res;
    }
    if (spot > LARGE && strike > LARGE) {
      return Double.POSITIVE_INFINITY;
    }

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      sigmaRootT = 1.; //ref value is returned
    }

    final int sign = isCall ? 1 : -1;
    double d1 = 0.;
    double d2 = 0.;
    if (Math.abs(spot - strike) < SMALL) {
      final double coefD1 = (Math.abs(costOfCarry) < SMALL && lognormalVol < SMALL) ? Math.signum(costOfCarry) + 0.5 * lognormalVol : (costOfCarry / lognormalVol + 0.5 * lognormalVol);
      final double tmpD1 = Math.abs(coefD1) < SMALL ? 0. : coefD1 * rootT;
      d1 = Double.isNaN(tmpD1) ? Math.signum(coefD1) : tmpD1;
      final double coefD2 = (Math.abs(costOfCarry) < SMALL && lognormalVol < SMALL) ? Math.signum(costOfCarry) - 0.5 * lognormalVol : (costOfCarry / lognormalVol - 0.5 * lognormalVol);
      final double tmpD2 = Math.abs(coefD2) < SMALL ? 0. : coefD2 * rootT;
      d2 = Double.isNaN(tmpD2) ? Math.signum(coefD2) : tmpD2;
    } else {
      if (sigmaRootT < SMALL) {
        d1 = (Math.log(spot / strike) / rootT + costOfCarry * rootT) / lognormalVol;
        d2 = d1;
      } else {
        if (sigmaRootT > LARGE) {
          final double coefD1 = (costOfCarry / lognormalVol + 0.5 * lognormalVol);
          final double coefD2 = (costOfCarry / lognormalVol - 0.5 * lognormalVol);
          d1 = Math.abs(coefD1) > SMALL ? coefD1 * rootT : 0.;
          d2 = Math.abs(coefD2) > SMALL ? coefD2 * rootT : 0.;
        } else {
          final double tmp = (Math.abs(costOfCarry) < SMALL && lognormalVol < SMALL) ? rootT : ((Math.abs(costOfCarry) < SMALL && rootT > LARGE) ? 1. / lognormalVol : costOfCarry / lognormalVol *
              rootT);
          d1 = Math.log(spot / strike) / sigmaRootT + tmp + 0.5 * sigmaRootT;
          d2 = d1 - sigmaRootT;
        }
      }
    }
    //    if (Double.isNaN(d1) || Double.isNaN(d2)) {
    //      throw new IllegalArgumentException("NaN found");
    //    }
    final double norm = NORMAL.getPDF(d1);
    final double rescaledSpot = Math.exp((costOfCarry - interestRate) * timeToExpiry) * spot;
    final double rescaledStrike = discount * strike;
    final double normForSpot = NORMAL.getCDF(sign * d1);
    final double normForStrike = NORMAL.getCDF(sign * d2);
    final double spotTerm = normForSpot < SMALL ? 0. : (Double.isNaN(rescaledSpot) ? -sign * Math.signum((costOfCarry - interestRate)) * rescaledSpot : -sign *
        ((costOfCarry - interestRate) * rescaledSpot * normForSpot));
    final double strikeTerm = normForStrike < SMALL ? 0. : (Double.isNaN(rescaledSpot) ? sign * (-Math.signum(interestRate) * discount) : sign * (-interestRate * rescaledStrike * normForStrike));

    double coef = rescaledSpot * lognormalVol / rootT;
    if (Double.isNaN(coef)) {
      s_logger.info("spot * lognormalVol / rootT, ambiguous");
      coef = 1.;
    }
    final double dlTerm = norm < SMALL ? 0. : -0.5 * norm * coef;

    final double res = dlTerm + spotTerm + strikeTerm;
    return Double.isNaN(res) ? 0. : res;
  }

  //  /**
  //  * The spot driftless theta
  //  * @param spot The spot value of the underlying
  //  * @param strike The Strike
  //  * @param timeToExpiry The time-to-expiry
  //  * @param lognormalVol The log-normal volatility
  //  * @return The driftless theta
  //  */
  //  @ExternalFunction
  //  public static double driftlessTheta(final double spot, final double strike, final double timeToExpiry, final double lognormalVol) {
  //    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
  //    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
  //    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
  //    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
  //
  //    final double rootT = Math.sqrt(timeToExpiry);
  //    double sigmaRootT = lognormalVol * rootT;
  //    if (Double.isNaN(sigmaRootT)) {
  //      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
  //      sigmaRootT = 1.;
  //    }
  //
  //    double d = 0.;
  //
  //    double logRatio = Math.log(spot / strike);
  //    if (Double.isNaN(logRatio)) {
  //      s_logger.info("spot *  Math.exp(costOfCarry * timeToExpiry) / strike, ambiguous");
  //      logRatio = 1.;
  //    }
  //
  //    if (Math.abs(logRatio) < SMALL * sigmaRootT || (Math.abs(logRatio) > LARGE && sigmaRootT > LARGE)) {
  //      d = 0.5 * sigmaRootT;
  //    } else {
  //      if (Math.abs(logRatio) > LARGE * sigmaRootT || (Math.abs(logRatio) < SMALL && sigmaRootT < SMALL)) {
  //        d = logRatio / sigmaRootT;
  //      } else {
  //        d = logRatio / sigmaRootT + 0.5 * sigmaRootT;
  //      }
  //    }
  //    final double norm = NORMAL.getPDF(d);
  //
  //    double tmp = spot * lognormalVol / rootT;
  //    if (Double.isNaN(tmp)) {
  //      s_logger.info("spot * lognormalVol / rootT, ambiguous");
  //      if (Double.isNaN(spot * lognormalVol)) {
  //        tmp = 1 / rootT;
  //      } else {
  //        if (Double.isNaN(spot / rootT)) {
  //          tmp = lognormalVol;
  //        } else {
  //          tmp = spot;
  //        }
  //      }
  //    }
  //
  //    return norm < SMALL ? 0. : -0.5 * norm * tmp;
  //  }

  /**
   * Charm, minus of second order derivative of option value, once spot and once time to maturity
   * @param spot  The spot value of the underlying
   * @param strike  The strike 
   * @param timeToExpiry The time to expiry
   * @param lognormalVol The log-normal volatility
   * @param interestRate  The interest rate
   * @param costOfCarry  The cost of carry
   * @param isCall  True for call
   * @return  The charm
   */
  public static double charm(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry, final boolean isCall) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    double coeff = Math.exp((costOfCarry - interestRate) * timeToExpiry);
    if (coeff < SMALL) {
      return 0.;
    }
    if (Double.isNaN(coeff)) {
      s_logger.info("(costOfCarry - interestRate) * timeToExpiry ambiguous");
      coeff = 1.;
    }

    final int sign = isCall ? 1 : -1;
    double d1 = 0.;
    double d2 = 0.;
    if (Math.abs(spot - strike) < SMALL || (spot > LARGE && strike > LARGE)) {
      final double coefD1 = Double.isNaN(Math.abs(costOfCarry) / lognormalVol) ? Math.signum(costOfCarry) + 0.5 * lognormalVol : (costOfCarry / lognormalVol + 0.5 * lognormalVol);
      final double tmpD1 = Math.abs(coefD1) < SMALL ? 0. : coefD1 * rootT;
      d1 = Double.isNaN(tmpD1) ? Math.signum(coefD1) : tmpD1;
      final double coefD2 = Double.isNaN(Math.abs(costOfCarry) / lognormalVol) ? Math.signum(costOfCarry) - 0.5 * lognormalVol : (costOfCarry / lognormalVol - 0.5 * lognormalVol);
      final double tmpD2 = Math.abs(coefD2) < SMALL ? 0. : coefD2 * rootT;
      d2 = Double.isNaN(tmpD2) ? Math.signum(coefD2) : tmpD2;
    } else {
      if (sigmaRootT < SMALL) {
        final double scnd = (Math.abs(costOfCarry) > LARGE && rootT < SMALL) ? Math.signum(costOfCarry) : costOfCarry * rootT;
        final double tmp = (Math.log(spot / strike) / rootT + scnd) / lognormalVol;
        d1 = Double.isNaN(tmp) ? 0. : tmp;
        d2 = d1;
      } else {
        if (sigmaRootT > LARGE) {
          final double coefD1 = (costOfCarry / lognormalVol + 0.5 * lognormalVol);
          final double coefD2 = (costOfCarry / lognormalVol - 0.5 * lognormalVol);
          d1 = Math.abs(coefD1) > SMALL ? coefD1 * rootT : 0.;
          d2 = Math.abs(coefD2) > SMALL ? coefD2 * rootT : 0.;
        } else {
          final double tmp = costOfCarry * rootT / lognormalVol;
          final double sig = (costOfCarry >= 0.) ? 1. : -1.;
          final double scnd = Double.isNaN(tmp) ? ((lognormalVol < LARGE && lognormalVol > SMALL) ? sig / lognormalVol : sig * rootT) : tmp;
          final double d1Tmp = Math.log(spot / strike) / sigmaRootT + scnd + 0.5 * sigmaRootT;
          final double d2Tmp = Math.log(spot / strike) / sigmaRootT + scnd - 0.5 * sigmaRootT;
          d1 = Double.isNaN(d1Tmp) ? 0. : d1Tmp;
          d2 = Double.isNaN(d2Tmp) ? 0. : d2Tmp;
        }
      }
    }
    //    if (Double.isNaN(d1) || Double.isNaN(d2)) {
    //      throw new IllegalArgumentException("NaN found");
    //    }
    double cocMod = costOfCarry / sigmaRootT;
    if (Double.isNaN(cocMod)) {
      s_logger.info("costOfCarry / sigmaRootT, ambiguous");
      cocMod = 1.;
    }

    double tmp = d2 / timeToExpiry;
    tmp = Double.isNaN(tmp) ? (d2 >= 0. ? 1. : -1.) : tmp;
    double coefPdf = cocMod - 0.5 * tmp;

    final double normPdf = NORMAL.getPDF(d1);
    final double normCdf = NORMAL.getCDF(sign * d1);
    final double first = normPdf < SMALL ? 0. : (Double.isNaN(coefPdf) ? 0. : normPdf * coefPdf);
    final double second = normCdf < SMALL ? 0. : (costOfCarry - interestRate) * normCdf;
    final double res = -coeff * (first + sign * second);

    return Double.isNaN(res) ? 0. : res;
  }

  /**
   * Dual charm, minus of second order derivative of option value, once strike and once time to maturity
   * @param spot  The spot value of the underlying
   * @param strike  The strike 
   * @param timeToExpiry The time to expiry
   * @param lognormalVol The log-normal volatility
   * @param interestRate  The interest rate
   * @param costOfCarry  The cost of carry
   * @param isCall  True for call
   * @return  The dual charm
   */
  public static double dualCharm(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry,
      final boolean isCall) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    double discount = Math.exp(-interestRate * timeToExpiry);
    if (discount < SMALL) {
      return 0.;
    }
    if (Double.isNaN(discount)) {
      s_logger.info("- interestRate * timeToExpiry ambiguous");
      discount = 1.;
    }

    final int sign = isCall ? 1 : -1;
    double d1 = 0.;
    double d2 = 0.;
    if (Math.abs(spot - strike) < SMALL || (spot > LARGE && strike > LARGE)) {
      final double coefD1 = Double.isNaN(Math.abs(costOfCarry) / lognormalVol) ? Math.signum(costOfCarry) + 0.5 * lognormalVol : (costOfCarry / lognormalVol + 0.5 * lognormalVol);
      final double tmpD1 = Math.abs(coefD1) < SMALL ? 0. : coefD1 * rootT;
      d1 = Double.isNaN(tmpD1) ? Math.signum(coefD1) : tmpD1;
      final double coefD2 = Double.isNaN(Math.abs(costOfCarry) / lognormalVol) ? Math.signum(costOfCarry) - 0.5 * lognormalVol : (costOfCarry / lognormalVol - 0.5 * lognormalVol);
      final double tmpD2 = Math.abs(coefD2) < SMALL ? 0. : coefD2 * rootT;
      d2 = Double.isNaN(tmpD2) ? Math.signum(coefD2) : tmpD2;
    } else {
      if (sigmaRootT < SMALL) {
        final double scnd = (Math.abs(costOfCarry) > LARGE && rootT < SMALL) ? Math.signum(costOfCarry) : costOfCarry * rootT;
        final double tmp = (Math.log(spot / strike) / rootT + scnd) / lognormalVol;
        d1 = Double.isNaN(tmp) ? 0. : tmp;
        d2 = d1;
      } else {
        if (sigmaRootT > LARGE) {
          final double coefD1 = (costOfCarry / lognormalVol + 0.5 * lognormalVol);
          final double coefD2 = (costOfCarry / lognormalVol - 0.5 * lognormalVol);
          d1 = Math.abs(coefD1) > SMALL ? coefD1 * rootT : 0.;
          d2 = Math.abs(coefD2) > SMALL ? coefD2 * rootT : 0.;
        } else {
          final double tmp = costOfCarry * rootT / lognormalVol;
          final double sig = (costOfCarry >= 0.) ? 1. : -1.;
          final double scnd = Double.isNaN(tmp) ? ((lognormalVol < LARGE && lognormalVol > SMALL) ? sig / lognormalVol : sig * rootT) : tmp;
          final double d1Tmp = Math.log(spot / strike) / sigmaRootT + scnd + 0.5 * sigmaRootT;
          final double d2Tmp = Math.log(spot / strike) / sigmaRootT + scnd - 0.5 * sigmaRootT;
          d1 = Double.isNaN(d1Tmp) ? 0. : d1Tmp;
          d2 = Double.isNaN(d2Tmp) ? 0. : d2Tmp;
        }
      }
    }
    //    if (Double.isNaN(d1) || Double.isNaN(d2)) {
    //      throw new IllegalArgumentException("NaN found");
    //    }
    double coefPdf = 0.;
    if (timeToExpiry < SMALL) {
      coefPdf = (Math.abs(spot - strike) < SMALL || (spot > LARGE && strike > LARGE)) ? 1. / sigmaRootT : Math.log(spot / strike) / sigmaRootT / timeToExpiry;
    } else {
      double cocMod = costOfCarry / sigmaRootT;
      if (Double.isNaN(cocMod)) {
        s_logger.info("costOfCarry / sigmaRootT, ambiguous");
        cocMod = 1.;
      }
      double tmp = d1 / timeToExpiry;
      tmp = Double.isNaN(tmp) ? (d1 >= 0. ? 1. : -1.) : tmp;
      coefPdf = cocMod - 0.5 * tmp;
    }

    final double normPdf = NORMAL.getPDF(d2);
    final double normCdf = NORMAL.getCDF(sign * d2);
    final double first = normPdf < SMALL ? 0. : (Double.isNaN(coefPdf) ? 0. : normPdf * coefPdf);
    final double second = normCdf < SMALL ? 0. : interestRate * normCdf;
    final double res = discount * (first - sign * second);

    return Double.isNaN(res) ? 0. : res;
  }

  /**
  * The spot vega of an option, i.e. the sensitivity of the option's spot price wrt the implied volatility (which is just the the spot vega
  * divided by the the numeraire)
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @return The spot vega
  */
  @ExternalFunction
  public static double vega(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    double factorInd = costOfCarry * timeToExpiry;
    if (Double.isNaN(factorInd)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factorInd = 0.;
    }

    double coeff = Math.exp((costOfCarry - interestRate) * timeToExpiry);
    if (coeff < SMALL) {
      return 0.;
    }
    if (Double.isNaN(coeff)) {
      s_logger.info("(costOfCarry - interestRate) * timeToExpiry ambiguous");
      coeff = 1.;
    }

    double d1 = 0.;

    double logRatio = Math.log(spot / strike) + factorInd;
    if (Double.isNaN(logRatio)) {
      s_logger.info("spot *  Math.exp(costOfCarry * timeToExpiry) / strike, ambiguous");
      logRatio = 1.;
    }

    if (Math.abs(logRatio) < SMALL * sigmaRootT || (Math.abs(logRatio) > LARGE && sigmaRootT > LARGE)) {
      d1 = 0.5 * sigmaRootT;
    } else {
      if (Math.abs(logRatio) > LARGE * sigmaRootT || (Math.abs(logRatio) < SMALL && sigmaRootT < SMALL)) {
        d1 = logRatio / sigmaRootT;
      } else {
        d1 = logRatio / sigmaRootT + 0.5 * sigmaRootT;
      }
    }
    final double norm = NORMAL.getPDF(d1);
    double tmp = spot * coeff * rootT;
    if (Double.isNaN(tmp)) {
      s_logger.info("spot * Math.exp(costOfCarry - interestRate) * timeToExpiry) * rootT, ambiguous");
      tmp = coeff;
    }

    return norm < SMALL ? 0. : tmp * norm;
  }

  /**
  * The vanna of an option, i.e. second order derivative of the option value, once to the underlying spot and once to volatility.<p>
  * $\frac{\partial^2 FV}{\partial f \partial \sigma}$
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @return The spot vanna
  */
  @ExternalFunction
  public static double vanna(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    double factorInd = costOfCarry * timeToExpiry;
    if (Double.isNaN(factorInd)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factorInd = 0.;
    }

    double coeff = Math.exp((costOfCarry - interestRate) * timeToExpiry);
    if (coeff < SMALL) {
      return 0.;
    }
    if (Double.isNaN(coeff)) {
      s_logger.info("(costOfCarry - interestRate) * timeToExpiry ambiguous");
      coeff = 1.;
    }

    double d1 = 0.;
    double d2 = 0.;

    double logRatio = Math.log(spot / strike) + factorInd;
    if (Double.isNaN(logRatio)) {
      s_logger.info("spot *  Math.exp(costOfCarry * timeToExpiry) / strike, ambiguous");
      logRatio = 1.;
    }

    if (Math.abs(logRatio) < SMALL * sigmaRootT || (Math.abs(logRatio) > LARGE && sigmaRootT > LARGE)) {
      d1 = 0.5 * sigmaRootT;
      d2 = -0.5 * sigmaRootT;
    } else {
      if (Math.abs(logRatio) > LARGE * sigmaRootT || (Math.abs(logRatio) < SMALL && sigmaRootT < SMALL)) {
        d1 = logRatio / sigmaRootT;
        d2 = logRatio / sigmaRootT;
      } else {
        d1 = logRatio / sigmaRootT + 0.5 * sigmaRootT;
        d2 = d1 - sigmaRootT;
      }
    }

    final double norm = NORMAL.getPDF(d1);
    double tmp = d2 * coeff / lognormalVol;
    if (Double.isNaN(tmp)) {
      s_logger.info("d2 * Math.exp((costOfCarry - interestRate) * timeToExpiry) / lognormalVol, ambiguous");
      tmp = coeff;
    }
    return norm < SMALL ? 0. : -norm * tmp;
  }

  /**
  * The dual vanna of an option, i.e. second order derivative of the option value, once to the strike and once to volatility.
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @return The spot dual vanna
  */
  @ExternalFunction
  public static double dualVanna(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    double factorInd = costOfCarry * timeToExpiry;
    if (Double.isNaN(factorInd)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factorInd = 0.;
    }

    double coeff = Math.exp(-interestRate * timeToExpiry);
    if (coeff < SMALL) {
      return 0.;
    }
    if (Double.isNaN(coeff)) {
      s_logger.info("interestRate * timeToExpiry ambiguous");
      coeff = 1.;
    }

    double d1 = 0.;
    double d2 = 0.;

    double logRatio = Math.log(spot / strike) + factorInd;
    if (Double.isNaN(logRatio)) {
      s_logger.info("spot *  Math.exp(costOfCarry * timeToExpiry) / strike, ambiguous");
      logRatio = 1.;
    }

    if (Math.abs(logRatio) < SMALL * sigmaRootT || (Math.abs(logRatio) > LARGE && sigmaRootT > LARGE)) {
      d1 = 0.5 * sigmaRootT;
      d2 = -0.5 * sigmaRootT;
    } else {
      if (Math.abs(logRatio) > LARGE * sigmaRootT || (Math.abs(logRatio) < SMALL && sigmaRootT < SMALL)) {
        d1 = logRatio / sigmaRootT;
        d2 = logRatio / sigmaRootT;
      } else {
        d1 = logRatio / sigmaRootT + 0.5 * sigmaRootT;
        d2 = d1 - sigmaRootT;
      }
    }

    final double norm = NORMAL.getPDF(d2);
    double tmp = d1 * coeff / lognormalVol;
    if (Double.isNaN(tmp)) {
      s_logger.info("d1 * Math.exp(- interestRate * timeToExpiry) / lognormalVol, ambiguous");
      tmp = coeff;
    }

    return norm < SMALL ? 0. : norm * tmp;
  }

  /**
  * The vomma (aka volga) of an option, i.e. second order derivative of the option spot price with respect to the implied volatility.
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @return The spot vomma
  */
  @ExternalFunction
  public static double vomma(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    double factorInd = costOfCarry * timeToExpiry;
    if (Double.isNaN(factorInd)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factorInd = 0.;
    }

    double coeff = Math.exp((costOfCarry - interestRate) * timeToExpiry);
    if (coeff < SMALL) {
      return 0.;
    }
    if (Double.isNaN(coeff)) {
      s_logger.info("(costOfCarry - interestRate) * timeToExpiry ambiguous");
      coeff = 1.;
    }

    double d1 = 0.;
    double d1d2Mod = 0.;

    double logRatio = Math.log(spot / strike) + factorInd;
    if (Double.isNaN(logRatio)) {
      s_logger.info("spot *  Math.exp(costOfCarry * timeToExpiry) / strike, ambiguous");
      logRatio = 1.;
    }

    if (Math.abs(logRatio) < SMALL * sigmaRootT | (Math.abs(logRatio) > LARGE && sigmaRootT > LARGE)) {
      d1 = 0.5 * sigmaRootT;
      d1d2Mod = -0.25 * sigmaRootT * timeToExpiry;
    } else {
      if (Math.abs(logRatio) > LARGE * sigmaRootT | (Math.abs(logRatio) < SMALL && sigmaRootT < SMALL)) {
        d1 = logRatio / sigmaRootT;
        d1d2Mod = logRatio * logRatio / sigmaRootT / lognormalVol / lognormalVol;
      } else {
        d1 = logRatio / sigmaRootT + 0.5 * sigmaRootT;
        d1d2Mod = logRatio * logRatio / sigmaRootT / lognormalVol / lognormalVol - 0.25 * sigmaRootT * timeToExpiry;
      }
    }

    final double norm = NORMAL.getPDF(d1);
    double tmp = d1d2Mod * spot * coeff;
    if (Double.isNaN(tmp)) {
      s_logger.info("d1 * d2 * spot * Math.exp((costOfCarry - interestRate) * timeToExpiry) * rootT / lognormalVol, ambiguous");
      tmp = coeff;
    }

    return norm < SMALL ? 0. : norm * tmp;
  }

  /**
  * The volga (aka vomma) of an option, i.e. second order derivative of the option spot price with respect to the implied volatility.
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @return The spot vomma
  */
  @ExternalFunction
  public static double volga(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry) {
    return vomma(spot, strike, timeToExpiry, lognormalVol, interestRate, costOfCarry);
  }

  /**
  * The vega bleed of an option, i.e. second order derivative of the option spot price, once to the volatility and once to the time.
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @return The spot vomma
  */
  @ExternalFunction
  public static double vegaBleed(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    double factorInd = costOfCarry * timeToExpiry;
    if (Double.isNaN(factorInd)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factorInd = 0.;
    }

    double coeff = Math.exp((costOfCarry - interestRate) * timeToExpiry);
    if (coeff < SMALL) {
      return 0.;
    }
    if (Double.isNaN(coeff)) {
      s_logger.info("(costOfCarry - interestRate) * timeToExpiry ambiguous");
      coeff = 1.;
    }

    double d1 = 0.;
    double d1Mod = 0.;
    double d1d2Mod = 0.;

    double logRatio = Math.log(spot / strike) + factorInd;
    if (Double.isNaN(logRatio)) {
      s_logger.info("spot *  Math.exp(costOfCarry * timeToExpiry) / strike, ambiguous");
      logRatio = 1.;
    }

    if (Math.abs(logRatio) < SMALL * sigmaRootT | (Math.abs(logRatio) > LARGE && sigmaRootT > LARGE)) {
      d1 = 0.5 * sigmaRootT;
      d1Mod = 0.5 * costOfCarry * rootT;
      d1d2Mod = lognormalVol * sigmaRootT / 8.;
    } else {
      if (Math.abs(logRatio) > LARGE * sigmaRootT | (Math.abs(logRatio) < SMALL && sigmaRootT < SMALL)) {
        d1 = logRatio / sigmaRootT;
        d1Mod = costOfCarry * logRatio / lognormalVol / sigmaRootT;
        d1d2Mod = -0.5 * logRatio * logRatio / sigmaRootT / sigmaRootT / rootT;
      } else {
        d1 = logRatio / sigmaRootT + 0.5 * sigmaRootT;
        d1Mod = d1 * costOfCarry / lognormalVol;
        d1d2Mod = -0.5 * logRatio * logRatio / sigmaRootT / sigmaRootT / rootT + lognormalVol * sigmaRootT / 8.;
      }
    }

    if (Double.isNaN(d1Mod)) {
      s_logger.info("d1 * costOfCarry / lognormalVol, ambiguous");
      d1Mod = 1.;
    }
    double extra = (interestRate - costOfCarry) * rootT + d1Mod - 0.5 / rootT + d1d2Mod;
    if (Double.isNaN(extra)) {
      s_logger.info("(interestRate - costOfCarry) * rootT - d1 * costOfCarry / lognormalVol - (1 + d1 * d2)/(2 * rootT), ambiguous");
      return 0.;
    }

    final double norm = NORMAL.getPDF(d1);
    double tmp = spot * coeff * extra;
    if (Double.isNaN(tmp)) {
      s_logger
          .info("spot * Math.exp(costOfCarry - interestRate) * timeToExpiry) * rootT * (interestRate - costOfCarry - d1 * costOfCarry / lognormalVol - (1 + d1 * d2)/(2 * timeToExpiry), ambiguous");
      tmp = coeff;
    }

    return norm < SMALL ? 0. : tmp * norm;
  }

  /**
   * The rho, the derivative of the option value with respect to the risk free interest rate 
   * Note that costOfCarry = interestRate - dividend, which the derivative also acts on
   * @param spot The spot value of the underlying
   * @param strike The strike
   * @param timeToExpiry The time to expiry
   * @param lognormalVol The log-normal volatility
   * @param interestRate  The interest rate
   * @param costOfCarry  The cost of carry
   * @param isCall  True for call
   * @return The rho
   */
  @ExternalFunction
  public static double rho(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry,
      final boolean isCall) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    double factor = Math.exp(costOfCarry * timeToExpiry);
    if (Double.isNaN(factor)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factor = 1.;
    }
    double rescaledSpot = spot * factor;

    double discount = Math.exp(-interestRate * timeToExpiry);
    if (discount < SMALL) {
      return 0.;
    }
    if (Double.isNaN(discount)) {
      s_logger.info("interestRate * timeToExpiry ambiguous");
      discount = 1.;
    }

    final int sign = isCall ? 1 : -1;
    double d2 = 0.;

    double logRatio = Math.log(rescaledSpot / strike);
    if (Double.isNaN(logRatio)) {
      s_logger.info("spot *  Math.exp(costOfCarry * timeToExpiry) / strike, ambiguous");
      logRatio = 1.;
    }

    if (Math.abs(logRatio) < SMALL * sigmaRootT | (Math.abs(logRatio) > LARGE && sigmaRootT > LARGE)) {
      d2 = -0.5 * sigmaRootT;
    } else {
      if (Math.abs(logRatio) > LARGE * sigmaRootT | (Math.abs(logRatio) < SMALL && sigmaRootT < SMALL)) {
        d2 = logRatio / sigmaRootT;
      } else {
        d2 = logRatio / sigmaRootT - 0.5 * sigmaRootT;
      }
    }
    final double norm = NORMAL.getCDF(sign * d2);
    double coeff = timeToExpiry * strike * discount;
    if (Double.isNaN(coeff)) {
      s_logger.info("timeToExpiry * strike * Math.exp(-interestRate * timeToExpiry), ambiguous");
      coeff = discount;
    }

    return norm < SMALL ? 0. : sign * coeff * norm;
  }

  /**
   * The carry rho, the derivative of the option value with respect to the cost of carry 
   * Note that costOfCarry = interestRate - dividend, which the derivative also acts on
   * @param spot The spot value of the underlying
   * @param strike The strike
   * @param timeToExpiry The time to expiry
   * @param lognormalVol The log-normal volatility
   * @param interestRate  The interest rate
   * @param costOfCarry  The cost of carry
   * @param isCall  True for call
   * @return The carry rho
   */
  @ExternalFunction
  public static double carryRho(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry,
      final boolean isCall) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }

    double factor = Math.exp(costOfCarry * timeToExpiry);
    if (Double.isNaN(factor)) {
      s_logger.info("costOfCarry * timeToExpiry ambiguous");
      factor = 1.;
    }
    double rescaledSpot = spot * factor;

    double coeff = Math.exp((costOfCarry - interestRate) * timeToExpiry);
    if (coeff < SMALL) {
      return 0.;
    }
    if (Double.isNaN(coeff)) {
      s_logger.info("(costOfCarry - interestRate) * timeToExpiry ambiguous");
      coeff = 1.;
    }

    final int sign = isCall ? 1 : -1;
    double d1 = 0.;

    double logRatio = Math.log(rescaledSpot / strike);
    if (Double.isNaN(logRatio)) {
      s_logger.info("spot *  Math.exp(costOfCarry * timeToExpiry) / strike, ambiguous");
      logRatio = 1.;
    }

    if (Math.abs(logRatio) < SMALL * sigmaRootT | (Math.abs(logRatio) > LARGE && sigmaRootT > LARGE)) {
      d1 = 0.5 * sigmaRootT;
    } else {
      if (Math.abs(logRatio) > LARGE * sigmaRootT | (Math.abs(logRatio) < SMALL && sigmaRootT < SMALL)) {
        d1 = logRatio / sigmaRootT;
      } else {
        d1 = logRatio / sigmaRootT + 0.5 * sigmaRootT;
      }
    }
    final double norm = NORMAL.getCDF(sign * d1);
    double tmp = timeToExpiry * spot * coeff;
    if (Double.isNaN(coeff)) {
      s_logger.info("timeToExpiry * spot * Math.exp((costOfCarry - interestRate) * timeToExpiry), ambiguous");
      tmp = coeff;
    }

    return norm < SMALL ? 0. : sign * tmp * norm;
  }
}
