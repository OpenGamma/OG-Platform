/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.index.IntrinsicIndexDataBundle;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FiniteDifferenceSpreadSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.SuperFastCreditCurveBuilder;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CS01OptionCalculator {

  private static final double BUMP_MIN = 1e-9;
  private static final double BUMP_MAX = 1.0;
  private static final ISDACompliantCreditCurveBuilder CC_BUILDER = new SuperFastCreditCurveBuilder();
  private static final CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();
  private static final PortfolioSwapAdjustment PSA = new PortfolioSwapAdjustment();
  private static final FiniteDifferenceSpreadSensitivityCalculator CS01_CAL = new FiniteDifferenceSpreadSensitivityCalculator();

  public double indexCurveApprox(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic pillarCDS, final CDSQuoteConvention quote, final double indexCoupon,
      final ISDACompliantYieldCurve yieldCurve, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double bumpAmount) {
    return indexCurveApprox(fwdCDS, timeToExpiry, new CDSAnalytic[] {pillarCDS }, new CDSQuoteConvention[] {quote }, indexCoupon, yieldCurve, strike, vol, isPayer, bumpAmount);
  }

  public double indexCurveApprox(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic[] pillarCDS, final CDSQuoteConvention[] quotes, final double indexCoupon,
      final ISDACompliantYieldCurve yieldCurve, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double bumpAmount) {

    ArgumentChecker.notNull(strike, "strike");
    ArgumentChecker.isTrue(Math.abs(bumpAmount) > BUMP_MIN, "Bump given as {}, but must have abs value greater than {}", bumpAmount, BUMP_MIN);
    ArgumentChecker.isTrue(Math.abs(bumpAmount) < BUMP_MAX, "Bump given as {}, but must have abs value less than {}. Bump must be given as fraction", bumpAmount, BUMP_MAX);

    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, timeToExpiry, yieldCurve, indexCoupon);
    //build index credit curve from quotes  
    final ISDACompliantCreditCurve cc = CC_BUILDER.calibrateCreditCurve(pillarCDS, quotes, yieldCurve);
    //bump the quotes and rebuild a credit curve 
    final CDSQuoteConvention[] bumpedQuotes = CS01_CAL.bumpQuotes(pillarCDS, quotes, yieldCurve, bumpAmount);
    final ISDACompliantCreditCurve bumpedCC = CC_BUILDER.calibrateCreditCurve(pillarCDS, bumpedQuotes, yieldCurve);

    //TODO don't have a test for this
    final CDSAnalytic fwdStartingCDS = fwdCDS.withOffset(timeToExpiry);

    final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, yieldCurve, indexCoupon, cc);
    final double bumpedAtmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, yieldCurve, indexCoupon, bumpedCC);

    final double basePrice = pricer.getOptionPremium(atmFwd, vol, strike, isPayer);
    final double bumpedPrice = pricer.getOptionPremium(bumpedAtmFwd, vol, strike, isPayer);

    return (bumpedPrice - basePrice) / bumpAmount;
  }

  /**
   * Calculation of the (parallel) CS01 of an index option, using the intrinsic value of the index: intrinsic credit curves are passed in (via intriniscData - usually these would have
   * been adjusted to reprice the index), and these are used to compute the PUF of the index (given by indexCDX); the (implied) quoted spread of the index is then bumped up (by bumpAmount - 
   * normally 1bps, 1e-4) and the intrinsic curves (re)adjusted to match the bumped PUF. With these two sets of curves, we calculate two values of ATM forward price, and these two values are
   * use to compute two option prices (using Pedersen model). The difference (divided by the bumpAmount) is returned.<p>
   * For standard CS01 use a bumpAmount of 1e-4 and multiple the result by the notional times 1e-4.   
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCDX The spot CDS that represent the index
   * @param indexCoupon The index coupon 
   * @param yieldCurve The current yield curve
   * @param intriniscData credit curves, weights and recovery rates of the intrinsic names
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param bumpAmount The bump amount. Use 1e-4 for standard CS01
   * @return The difference in option price divided by the bumpAmount. 
   */
  public double fullCal(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic indexCDX, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final IntrinsicIndexDataBundle intriniscData, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double bumpAmount) {
    return fullCal(fwdCDS, timeToExpiry, new CDSAnalytic[] {indexCDX }, indexCoupon, yieldCurve, intriniscData, strike, vol, isPayer, bumpAmount);
  }

  /**
   * Calculation of the (parallel) CS01 of an index option, using the intrinsic value of the index: intrinsic credit curves are passed in (via intriniscData - usually these would have
   * been adjusted to reprice the index), and these are used to compute the PUF for pillar points (given by indexCDX); the (implied) quoted spreads of these pillars is then bumped up (by bumpAmount - 
   * normally 1bps, 1e-4) and the intrinsic curves (re)adjusted to match the bumped PUF. With these two sets of curves, we calculate two values of ATM forward price, and these two values are
   * use to compute two option prices (using Pedersen model). The difference (divided by the bumpAmount) is returned.<p>
   * For standard CS01 use a bumpAmount of 1e-4 and multiple the result by the notional times 1e-4.   
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCDX The spot CDS that represent the `pillars' 
   * @param indexCoupon The index coupon 
   * @param yieldCurve The current yield curve
   * @param intriniscData credit curves, weights and recovery rates of the intrinsic names
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat () spread 
   * @param isPayer true for payer and false for receiver option 
   * @param bumpAmount The bump amount. Use 1e-4 for standard CS01
   * @return The difference in option price divided by the bumpAmount. 
   */
  public double fullCal(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic[] indexCDX, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final IntrinsicIndexDataBundle intriniscData, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double bumpAmount) {

    //TODO don't have a test for this
    final CDSAnalytic fwdStartingCDS = fwdCDS.withOffset(timeToExpiry);

    final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, yieldCurve, indexCoupon, intriniscData);
    final double indexfactor = intriniscData.getIndexFactor();
    final int n = indexCDX.length;
    final double[] basePUF = new double[n];
    final double[] bumpedPUF = new double[n];
    for (int i = 0; i < n; i++) {
      basePUF[i] = INDEX_CAL.indexPV(indexCDX[i], indexCoupon, yieldCurve, intriniscData) / indexfactor;
      final PointsUpFront temp = (PointsUpFront) CS01_CAL.bumpQuote(indexCDX[i], new PointsUpFront(indexCoupon, basePUF[i]), yieldCurve, bumpAmount);
      bumpedPUF[i] = temp.getPointsUpFront();
    }

    final IntrinsicIndexDataBundle bumpedCurves = PSA.adjustCurves(bumpedPUF, indexCDX, indexCoupon, yieldCurve, intriniscData);
    final double bumpedAtmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, yieldCurve, indexCoupon, bumpedCurves);

    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, timeToExpiry, yieldCurve, indexCoupon);

    final double basePrice = pricer.getOptionPremium(atmFwd, vol, strike, isPayer);
    final double bumpedPrice = pricer.getOptionPremium(bumpedAtmFwd, vol, strike, isPayer);

    return (bumpedPrice - basePrice) / bumpAmount;
  }

}
