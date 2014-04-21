/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import org.apache.log4j.Logger;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.index.IntrinsicIndexDataBundle;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadISDAFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BlackIndexOptionPricer {
  private static final Logger LOGGER = Logger.getLogger(BlackIndexOptionPricer.class.getName());

  private final AnnuityForSpreadFunction _annuityFunc;
  private final double _expiry;

  private final double _coupon;
  private final double _df;
  private final double _minExercisePrice;
  private final double _maxExercisePrice;
  private final double _daFwdSpread;
  private final double _fAnnuity;

  /**
   * Price options on CDS indices using an approximation where the forward annuity is `frozen' to today's value - this allows the use for the Black formula.
   * @param fwdCDS Forward CDS - this represents the index (a CDSAnayltic which holds the cash flow details) at <b>the option expiry</b> - i.e. the 'trade date' of
   *  the CDS should be the option expiry and <b>not</b> today (where we are valuing the option)  
   * @param timeToExpiry time to expiry of the option 
   * @param yieldCurve The current yield curve
   * @param indexCoupon The index coupon 
   * @param defaultAdjustedFwdSpread The default-adjusted forward spread (if not given extraneously, use {@link CDSIndexCalculator#defaultAdjustedForwardSpread} to calculate)
   * @param pvFwdAnnuity The present value of the forward annuity of the underlying index (if not given extraneously, use {@link CDSIndexCalculator#indexAnnuity} to calculate this)
   */
  public BlackIndexOptionPricer(final CDSAnalytic fwdCDS, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final double indexCoupon, final double defaultAdjustedFwdSpread,
      final double pvFwdAnnuity) {
    this(fwdCDS, timeToExpiry, yieldCurve, indexCoupon, new double[] {defaultAdjustedFwdSpread, pvFwdAnnuity });
  }

  /**
   * Price options on CDS indices using an approximation where the forward annuity is `frozen' to today's value - this allows the use for the Black formula.
   * @param fwdCDS Forward CDS - this represents the index (a CDSAnayltic which holds the cash flow details) at <b>the option expiry</b> - i.e. the 'trade date' of
   *  the CDS should be the option expiry and <b>not</b> today (where we are valuing the option)  
   * @param timeToExpiry time to expiry of the option 
   * @param yieldCurve The current yield curve
   * @param indexCoupon The index coupon  
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names. Usually these would have first been adjusted to match index prices (using {@link PortfolioSwapAdjustment#adjustCurves})
   */
  public BlackIndexOptionPricer(final CDSAnalytic fwdCDS, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final double indexCoupon, final IntrinsicIndexDataBundle intrinsicData) {
    this(fwdCDS, timeToExpiry, yieldCurve, indexCoupon, getFwdSpreadAndAnnuity(fwdCDS, timeToExpiry, yieldCurve, indexCoupon, intrinsicData));
  }

  private BlackIndexOptionPricer(final CDSAnalytic fwdCDS, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final double indexCoupon, final double[] fwdSpreadAndAnnity) {
    ArgumentChecker.notNull(fwdCDS, "fwdCDS");
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "timeToExpiry must be positive. Value given {}", timeToExpiry);
    ArgumentChecker.isTrue(fwdCDS.getEffectiveProtectionStart() == 0.0, "fwdCDS should be a Forward CDS - set Java docs");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.isTrue(indexCoupon > 0.0, "indexCoupon must be positive");
    ArgumentChecker.isTrue(fwdSpreadAndAnnity.length == 2, "too many parameters passed in");
    final double defaultAdjustedFwdSpread = fwdSpreadAndAnnity[0];
    final double pvFwdAnnuity = fwdSpreadAndAnnity[1];
    ArgumentChecker.isTrue(defaultAdjustedFwdSpread > 0.0, "defaultAdjustedFwdSpread must be positive");
    if (indexCoupon > 1) {
      LOGGER.warn("Index Coupon should be given as a fraction; a value of " + indexCoupon + " is " + indexCoupon * 1e4 + "basis points.");
    }
    if (defaultAdjustedFwdSpread > 10) {
      LOGGER.warn("defaultAdjustedFwdSpread should be given as a fraction; a value of " + defaultAdjustedFwdSpread + " is " + defaultAdjustedFwdSpread * 1e4 + "basis points.");
    }
    ArgumentChecker.isTrue(pvFwdAnnuity > 0, "pvFwdAnnuity must be positive");
    ArgumentChecker.isTrue(pvFwdAnnuity < fwdCDS.getProtectionEnd() * 1.1, "Value of annuity of {} is greater than length (in years) of forward CDS. Annuity should be given for unit notional.",
        pvFwdAnnuity);

    _annuityFunc = new AnnuityForSpreadISDAFunction(fwdCDS, yieldCurve.withOffset(timeToExpiry));

    _expiry = timeToExpiry;
    _coupon = indexCoupon;
    _df = yieldCurve.getDiscountFactor(timeToExpiry + fwdCDS.getCashSettleTime());
    _daFwdSpread = defaultAdjustedFwdSpread;
    _fAnnuity = pvFwdAnnuity;

    _minExercisePrice = -indexCoupon * _annuityFunc.evaluate(0.);
    _maxExercisePrice = fwdCDS.getLGD();
  }

  private static double[] getFwdSpreadAndAnnuity(final CDSAnalytic fwdCDS, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final double indexCoupon,
      final IntrinsicIndexDataBundle intrinsicData) {
    final CDSIndexCalculator indexCal = new CDSIndexCalculator();
    final double[] res = new double[2];

    final CDSAnalytic fwdStartCDS = fwdCDS.withOffset(timeToExpiry);
    res[0] = indexCal.defaultAdjustedForwardSpread(fwdStartCDS, timeToExpiry, yieldCurve, intrinsicData);
    res[1] = indexCal.indexAnnuity(fwdCDS, yieldCurve, intrinsicData, 0.0);
    return res;
  }

  /**
   * Calculate the option premium (price per unit of notional) 
   * @see CDSIndexCalculator
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The volatility of the default-adjusted forward spread 
   * @param isPayer true for payer and false for receiver option 
   * @return The option premium 
   */
  public double getOptionPremium(final IndexOptionStrike strike, final double vol, final boolean isPayer) {
    ArgumentChecker.notNull(strike, "strike");
    if (strike instanceof SpreadBasedStrike) {
      return getOptionPriceForSpreadQuotedIndex(strike.amount(), vol, isPayer);
    } else if (strike instanceof ExerciseAmount) {
      return getOptionPriceForPriceQuotedIndex(strike.amount(), vol, isPayer);
    } else {
      throw new IllegalArgumentException("unknow  strike type " + strike.getClass());
    }
  }

  /**
   * Price an option of a CDS index that is spread based (i.e. the strike is given as a spread). 
   * @param strike The strike as a spread
   * @param vol The volatility of the default-adjusted forward spread 
   * @param isPayer true for payer, false for receiver 
   * @return The option price 
   */
  public double getOptionPriceForSpreadQuotedIndex(final double strike, final double vol, final boolean isPayer) {
    ArgumentChecker.isTrue(strike >= 0.0, "strike cannot be negative");

    final double gK = (strike - _coupon) * _annuityFunc.evaluate(strike); //the excise price 
    return getOptionPriceForPriceQuotedIndex(gK, vol, isPayer);
  }

  /**
   * Price an option of a CDS index that is priced based (i.e. the exercise price is given directly). 
   * @param gK Exercise price 
   * @param vol The volatility of the default-adjusted forward spread 
   * @param isPayer true for payer, false for receiver 
   * @return The option price 
   */
  public double getOptionPriceForPriceQuotedIndex(final double gK, final double vol, final boolean isPayer) {
    ArgumentChecker.isTrue(gK >= _minExercisePrice && gK < _maxExercisePrice, "The exercise price must be in the range {} to {} - value of {} is outside this", _minExercisePrice, _maxExercisePrice,
        gK);
    final double kMod = _coupon + gK * _df / _fAnnuity;
    final double modBlackPrice = _fAnnuity * BlackFormulaRepository.price(_daFwdSpread, kMod, _expiry, vol, isPayer);
    return modBlackPrice;
  }

  /**
   * Get the implied volatility given a known option premium (price for unit notional)
   * @param strike strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param optionPremium The option premium 
   * @param isPayer true for payer, false for receiver 
   * @return The implied volatility
   */
  public double getImpliedVolatility(final IndexOptionStrike strike, final double optionPremium, final boolean isPayer) {
    ArgumentChecker.notNull(strike, "strike");
    if (strike instanceof SpreadBasedStrike) {
      return getImpliedVolForSpreadStrike(strike.amount(), optionPremium, isPayer);
    } else if (strike instanceof ExerciseAmount) {
      return getImpliedVolForExercisePrice(strike.amount(), optionPremium, isPayer);
    } else {
      throw new IllegalArgumentException("unknow  strike type " + strike.getClass());
    }
  }

  /**
   * Get the implied volatility given a known option premium (price for unit notional)
   * @param strike The strike as a spread
   * @param optionPremium The option premium 
   * @param isPayer true for payer, false for receiver 
   * @return The implied volatility
   */
  public double getImpliedVolForSpreadStrike(final double strike, final double optionPremium, final boolean isPayer) {
    final double gK = (strike - _coupon) * _annuityFunc.evaluate(strike); //the excise price 
    return getImpliedVolForExercisePrice(gK, optionPremium, isPayer);
  }

  /**
   * Get the implied volatility given a known option premium (price for unit notional)
   * @param gK Exercise price 
   * @param optionPremium The option premium 
   * @param isPayer true for payer, false for receiver 
   * @return The implied volatility
   */
  public double getImpliedVolForExercisePrice(final double gK, final double optionPremium, final boolean isPayer) {
    final double kMod = _coupon + gK * _df / _fAnnuity;
    final double norPrice = optionPremium / _fAnnuity;
    final double iv = BlackFormulaRepository.impliedVolatility(norPrice, _daFwdSpread, kMod, _expiry, isPayer);
    return iv;
  }
}
