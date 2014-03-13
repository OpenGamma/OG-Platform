/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadApproxFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadISDAFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.GaussHermiteQuadratureIntegrator1D;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.ArgumentChecker;

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

  private static final PortfolioSwapAdjustment PORTFOLIO_ADJ = new PortfolioSwapAdjustment();

  private final AnnuityForSpreadFunction _annuityFunc;
  private final AnnuityForSpreadFunction _annuityFuncQuick;
  private final CDSAnalytic _fwdCDS;
  private final double _expiry;
  private final ISDACompliantYieldCurve _yieldCurve;
  private final ISDACompliantYieldCurve _fwdYieldCurve;
  private final double _coupon;
  private final double _df;
  private final double _minExercisePrice;
  private final double _maxExercisePrice;

  public IndexOptionPricer(final CDSAnalytic fwdCDS, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final double coupon) {
    this(fwdCDS, timeToExpiry, yieldCurve, coupon, false);
  }

  /**
   * 
   * @param fwdCDS Forward CDS - this represents the index (a CDSAnayltic which holds the cash flow details) at <b>the option expiry</b> - i.e. the 'trade date' of
   *  the CDS should be the option expiry and <b>not</b> today (where we are valuing the option)  
   * @param timeToExpiry time to expiry of the option 
   * @param yieldCurve The current yield curve
   * @param coupon The index coupon 
   * @param useExactAnnuityCal if true the ISDA model (up-front) is use to compute the annuity, otherwise a credit triangle approximation is used. 
   */
  public IndexOptionPricer(final CDSAnalytic fwdCDS, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final double coupon, final boolean useExactAnnuityCal) {
    ArgumentChecker.notNull(fwdCDS, "fwdCDS");
    ArgumentChecker.isTrue(fwdCDS.getEffectiveProtectionStart() == 0.0, "fwdCDS should be a Forward CDS - set Java docs");

    _fwdYieldCurve = yieldCurve.withOffset(timeToExpiry);

    _annuityFunc = new AnnuityForSpreadISDAFunction(fwdCDS, _fwdYieldCurve);
    if (useExactAnnuityCal) {
      _annuityFuncQuick = _annuityFunc;
    } else {
      _annuityFuncQuick = new AnnuityForSpreadApproxFunction(fwdCDS, _fwdYieldCurve);
    }

    _fwdCDS = fwdCDS;
    _yieldCurve = yieldCurve;
    _expiry = timeToExpiry;
    _coupon = coupon;
    _df = yieldCurve.getDiscountFactor(timeToExpiry + fwdCDS.getCashSettleTime());

    _minExercisePrice = -coupon * _annuityFunc.evaluate(0.);
    _maxExercisePrice = fwdCDS.getLGD();
  }

  /**
   * Calculate the option premium (price per unit of notional) 
   * @see CDSIndexCalculator
   * @param defaultAdjIndexValue The default adjusted forward index value (or ATM Forward value). Use CDSIndexCalculator to compute this. 
   * @param vol The log-normal volatility of the pseudo spread 
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param isPayer true for payer and false for receiver option 
   * @return The option premium 
   */
  public double getOptionPremium(final double defaultAdjIndexValue, final double vol, final IndexOptionStrike strike, final boolean isPayer) {
    ArgumentChecker.notNull(strike, "strike");
    if (strike instanceof SpreadBasedStrike) {
      return getOptionPriceForSpreadQuotedIndex(defaultAdjIndexValue, vol, strike.amount(), isPayer);
    } else if (strike instanceof ExerciseAmount) {
      return getOptionPriceForPriceQuotedIndex(defaultAdjIndexValue, vol, strike.amount(), isPayer);
    } else {
      throw new IllegalArgumentException("unknow  strike type " + strike.getClass());
    }
  }

  public double getOptionPriceForSpreadQuotedIndex(final double defaultAdjIndexValue, final double vol, final double strike, final boolean isPayer) {
    ArgumentChecker.isTrue(strike >= 0.0, "strike cannot be negative");
    final double x0 = calibrateX0(defaultAdjIndexValue, vol); //mean of pseudo-spread 
    final double gK = (strike - _coupon) * _annuityFunc.evaluate(strike); //the excise price 
    return optionPrice(defaultAdjIndexValue, x0, vol, gK, isPayer);
  }

  /**
   * Price an option of a CDS index that is priced based. The ATM forward (default adjusted index value) and exercise price are given for a unit notional 
   * @param defaultAdjIndexValue The expected value of the index plus default settlement at option expiry (rolled to expiry settlement)
   * @param vol The volatility of the pseudo-spread, X
   * @param gK Exercise price 
   * @param isPayer true for payer, false for receiver 
   * @return The option price 
   */
  public double getOptionPriceForPriceQuotedIndex(final double defaultAdjIndexValue, final double vol, final double gK, final boolean isPayer) {
    ArgumentChecker.isTrue(defaultAdjIndexValue >= _minExercisePrice && defaultAdjIndexValue < _maxExercisePrice,
        "The defaulted adjusted forward index price must be in the range {} to {} - value of {} is outside this", _minExercisePrice, _maxExercisePrice, defaultAdjIndexValue);
    ArgumentChecker.isTrue(gK >= _minExercisePrice && gK < _maxExercisePrice, "The exercise price must be in the range {} to {} - value of {} is outside this", _minExercisePrice, _maxExercisePrice,
        gK);

    final double x0 = calibrateX0(defaultAdjIndexValue, vol); //mean of pseudo-spread 
    return optionPrice(defaultAdjIndexValue, x0, vol, gK, isPayer);
  }

  private double optionPrice(final double defaultAdjIndexValue, final double x0, final double vol, final double gK, final boolean isPayer) {
    final double otmPrice = otmOptionPrice(defaultAdjIndexValue, x0, vol, gK);
    final boolean priceAsCall = (gK >= defaultAdjIndexValue);
    if (isPayer == priceAsCall) {
      return otmPrice; //asked for an OTM price 
    }
    final double f = _df * (defaultAdjIndexValue - gK);
    if (isPayer) {
      return f + otmPrice; //want a payer but priced a receiver 
    } else {
      return otmPrice - f; //want a receiver but priced a payer 
    }
  }

  private double otmOptionPrice(final double defaultAdjIndexValue, final double x0, final double vol, final double gK) {
    final double zStar = getZStar(x0, vol, gK); //critical value of z 
    final boolean isCall = (gK >= defaultAdjIndexValue);
    final Function1D<Double, Double> integrand = getPriceIntegrand(x0, vol, gK, isCall);
    if (isCall) {
      return _df * RK.integrate(integrand, zStar, Math.max(8.0, zStar + 2));
    } else {
      return _df * RK.integrate(integrand, Math.min(-8.0, zStar - 2), zStar);
    }
  }

  public double impliedVol(final double atmFwd, final double gK, final double price, final boolean isPayer) {

    final boolean priceAsCall = (gK >= atmFwd);
    if (priceAsCall == isPayer) {
      return impliedVol(atmFwd, gK, price);
    }

    final double f = _df * (atmFwd - gK);
    if (isPayer) {
      return impliedVol(atmFwd, gK, price - f);
    } else {
      return impliedVol(atmFwd, gK, price + f);
    }
  }

  private double impliedVol(final double atmFwd, final double gK, final double otmPrice) {
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double vol) {
        final double x0 = calibrateX0(atmFwd, vol);
        final double ratio = otmOptionPrice(atmFwd, x0, vol, gK) / otmPrice;
        return ratio - 1.0;
      }
    };
    return ROOTFINDER.getRoot(func, 0.3);
  }

  /**
   * This calibrates X0 (the mean value of the pseudo-spread, X) for a given volatility (vol) such that the expected
   * value of $F_I(X)$ equals the calculated value of the default-adjusted forward portfolio swap
   * @param defaultAdjIndexValue The calculated value of the default-adjusted forward portfolio swap
   * @param vol The volatility of the pseudo-spread, X
   * @return The calibrated value of X0
   */
  public double calibrateX0(final double defaultAdjIndexValue, final double vol) {

    final DoubleFunction1D funcLowAccuracy = new DoubleFunction1D() {
      @Override
      public Double evaluate(final Double x0) {
        final Function1D<Double, Double> intergrand = getGaussHermiteIntegrand(x0, vol);
        return GHQ2.integrateFromPolyFunc(intergrand) - defaultAdjIndexValue;
      }
    };

    final DoubleFunction1D funcHiAccuracy = new DoubleFunction1D() {
      @Override
      public final Double evaluate(final Double x0) {
        final Function1D<Double, Double> intergrand = getGaussHermiteIntegrand(x0, vol);
        return GHQ7.integrateFromPolyFunc(intergrand) - defaultAdjIndexValue;
      }
    };

    final MarketQuoteConverter converter = new MarketQuoteConverter();
    final double guess = converter.pufToQuotedSpread(_fwdCDS, _coupon, _fwdYieldCurve, defaultAdjIndexValue);
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
        return (x - _coupon) * _annuityFuncQuick.evaluate(x);
      }
    };
  }

  private Function1D<Double, Double> getPriceIntegrand(final double x0, final double vol, final double exciseAmt, final boolean isPayer) {

    final int sign = isPayer ? 1 : -1;
    final Function1D<Double, Double> fi = getFiForZ(x0, vol);
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double z) {
        return Math.max(0.0, sign * (fi.evaluate(z) - exciseAmt)) * NORMAL.getPDF(z);
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

}
