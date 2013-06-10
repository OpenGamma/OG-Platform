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

    final double rootT = Math.sqrt(timeToExpiry);
    double sigmaRootT = lognormalVol * rootT;
    if (Double.isNaN(sigmaRootT)) {
      s_logger.info("lognormalVol * Math.sqrt(timeToExpiry) ambiguous");
      sigmaRootT = 1.;
    }
    final double factor = Math.exp(costOfCarry * timeToExpiry);
    double rescaledSpot = Double.isNaN(factor) ? spot : spot * factor;
    double discount = Math.exp(-interestRate * timeToExpiry);
    if (Double.isNaN(discount)) {
      s_logger.info("interestRate * timeToExpiry ambiguous");
      discount = 1.;
    }
    final int sign = isCall ? 1 : -1;
    final boolean bFwd = (rescaledSpot > LARGE);
    final boolean bStr = (strike > LARGE);
    final boolean bSigRt = (sigmaRootT > LARGE);
    double d1 = 0.;
    double d2 = 0.;

    d1 = Math.log(rescaledSpot / strike) / sigmaRootT + 0.5 * sigmaRootT;
    d2 = d1 - sigmaRootT;
    return discount < SMALL ? 0. : sign * (rescaledSpot * NORMAL.getCDF(sign * d1) - strike * NORMAL.getCDF(sign * d2)) * discount;
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

    if (strike < SMALL) {
      return isCall ? 1.0 : 0.0;
    }
    final int sign = isCall ? 1 : -1;
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;

    if (sigmaRootT < SMALL) {
      return (isCall ? (spot > strike ? 1.0 : 0.0) : (spot > strike ? 0.0 : -1.0));
    }

    final double d1 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol + 0.5 * sigmaRootT;

    return sign * Math.exp((costOfCarry - interestRate) * timeToExpiry) * NORMAL.getCDF(sign * d1);
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
    Validate.isTrue((isCall && spotDelta > 0) || (!isCall && spotDelta < 0), "delta out of range");

    final int sign = isCall ? 1 : -1;
    final double d1 = sign * NORMAL.getInverseCDF(sign * Math.exp(-(costOfCarry - interestRate) * timeToExpiry) * spotDelta);
    return spot * Math.exp(-d1 * lognormalVol * Math.sqrt(timeToExpiry) + (costOfCarry + 0.5 * lognormalVol * lognormalVol) * timeToExpiry);
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

    final int sign = isCall ? 1 : -1;
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;

    final double d2 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol - 0.5 * sigmaRootT;

    return -sign * Math.exp(-interestRate * timeToExpiry) * NORMAL.getCDF(sign * d2);
  }

  /**
  * The simple delta.
  * Note that this is not the standard delta one is accustomed to.
  * The argument of the cumulative normal is simply d = Math.log(spot / strike) / sigmaRootT
  *
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param isCall true for call
  * @return The spot delta
  */
  @ExternalFunction
  public static double simpleDelta(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final boolean isCall) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    if (strike < SMALL) {
      return isCall ? 1.0 : 0.0;
    }
    final int sign = isCall ? 1 : -1;
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;

    if (sigmaRootT < SMALL) {
      return (isCall ? (spot > strike ? 1.0 : 0.0) : (spot > strike ? 0.0 : -1.0));
    }

    final double d = Math.log(spot / strike) / sigmaRootT;

    return sign * NORMAL.getCDF(sign * d);
  }

  /**
  * The spot   gamma, 2nd order sensitivity of the spot option value to the spot. <p>
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
    if (spot == 0 || strike == 0.0) {
      return 0.0;
    }
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;
    if (sigmaRootT == 0.0) {
      if (spot != strike) {
        return 0.0;
      }
      // The gamma is infinite in this case
      return Double.POSITIVE_INFINITY;
    }
    final double d1 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol + 0.5 * sigmaRootT;

    return NORMAL.getPDF(d1) * Math.exp((costOfCarry - interestRate) * timeToExpiry) / spot / sigmaRootT;
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
    if (strike == 0) {
      return 0.0;
    }
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;
    final double d2 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol - 0.5 * sigmaRootT;

    return NORMAL.getPDF(d2) * Math.exp(-interestRate * timeToExpiry) / strike / sigmaRootT;
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
    if (strike == 0) {
      return 0.0;
    }
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;
    final double d2 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol - 0.5 * sigmaRootT;

    return -NORMAL.getPDF(d2) * Math.exp(-interestRate * timeToExpiry) / spot / sigmaRootT;
  }

  /**
  * The theta (non-spot), the sensitivity of the present value to a change in time to maturity, $\-frac{\partial V}{\partial T}$
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

    final int sign = isCall ? 1 : -1;
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;
    final double d1 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol + 0.5 * sigmaRootT;
    final double d2 = d1 - sigmaRootT;

    final double value = -spot * NORMAL.getPDF(d1) * Math.exp((costOfCarry - interestRate) * timeToExpiry) * lognormalVol / 2 / rootT - sign * (costOfCarry - interestRate) * spot *
        Math.exp((costOfCarry - interestRate) * timeToExpiry) * NORMAL.getCDF(sign * d1) - sign * interestRate * strike
        * Math.exp(-interestRate * timeToExpiry) * NORMAL.getCDF(sign * d2);

    return value;
  }

  /**
  * The spot driftless theta
  * @param spot The spot value of the underlying
  * @param strike The Strike
  * @param timeToExpiry The time-to-expiry
  * @param lognormalVol The log-normal volatility
  * @param interestRate The interest rate 
  * @param costOfCarry The cost-of-carry  rate
  * @return The driftless theta
  */
  @ExternalFunction
  public static double driftlessTheta(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry) {
    ArgumentChecker.isTrue(spot >= 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike >= 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol >= 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    if (spot == 0 || strike == 0.0) {
      return 0.0;
    }
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;
    if (sigmaRootT == 0.0) {
      if (spot != strike) {
        return 0.0;
      }
      // The gamma is infinite in this case
      return Double.POSITIVE_INFINITY;
    }
    final double d1 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol + 0.5 * sigmaRootT;

    return -spot * Math.exp((costOfCarry - interestRate) * timeToExpiry) * NORMAL.getPDF(d1) * lognormalVol / 2 / rootT;
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
    final double sigmaRootT = lognormalVol * rootT;

    //    if (Math.abs(spot - strike) < SMALL) {
    //      return spot * rootT * NORMAL.getPDF(sigmaRootT / 2);
    //    }
    //
    //    if (sigmaRootT < SMALL || strike < SMALL) {
    //      return 0.0;
    //    }

    final double d1 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol + 0.5 * sigmaRootT;
    return spot * Math.exp((costOfCarry - interestRate) * timeToExpiry) * rootT * NORMAL.getPDF(d1);
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

    if (spot == 0.0 || strike == 0.0) {
      return 0.0;
    }
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;

    if (sigmaRootT < SMALL || strike < SMALL) {
      return 0.0;
    }

    final double d1 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol + 0.5 * sigmaRootT;
    final double d2 = d1 - sigmaRootT;
    return -NORMAL.getPDF(d1) * d2 * Math.exp((costOfCarry - interestRate) * timeToExpiry) / lognormalVol;
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

    if (spot == 0.0 || strike == 0.0) {
      return 0.0;
    }
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;

    if (sigmaRootT < SMALL || strike < SMALL) {
      return 0.0;
    }

    final double d1 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol + 0.5 * sigmaRootT;
    final double d2 = d1 - sigmaRootT;
    return NORMAL.getPDF(d2) * d1 * Math.exp(-interestRate * timeToExpiry) / lognormalVol;
  }

  /**
  * The driftless vomma (aka volga) of an option, i.e. second order derivative of the option spot price with respect to the implied volatility.
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

    if (spot == 0.0 || strike == 0.0) {
      return 0.0;
    }
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;

    if (sigmaRootT < SMALL || strike < SMALL) {
      return 0.0;
    }

    final double d1 = Math.log(spot / strike) / sigmaRootT + costOfCarry * rootT / lognormalVol + 0.5 * sigmaRootT;
    final double d2 = d1 - sigmaRootT;
    return spot * Math.exp((costOfCarry - interestRate) * timeToExpiry) * NORMAL.getPDF(d1) * rootT * d1 * d2 / lognormalVol;
  }

  /**
  * The driftless volga (aka vomma) of an option, i.e. second order derivative of the option spot price with respect to the implied volatility.
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
}
