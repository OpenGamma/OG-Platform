/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.index.IntrinsicIndexDataBundle;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FiniteDifferenceSpreadSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.SuperFastCreditCurveBuilder;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class FiniteDifferenceGreekCalculator {

  private static final double ONE_BPS = 1e-4;
  private static final double TEN_BPS = 1e-3;

  private static final double BUMP_MIN = 1e-9;
  private static final double BUMP_MAX = 1.0;
  private static final ISDACompliantCreditCurveBuilder CC_BUILDER = new SuperFastCreditCurveBuilder();
  private static final CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();
  private static final PortfolioSwapAdjustment PSA = new PortfolioSwapAdjustment();
  private static final CS01OptionCalculator CS01_OPT_CAL = new CS01OptionCalculator();
  private static final FiniteDifferenceSpreadSensitivityCalculator CS01_CAL = new FiniteDifferenceSpreadSensitivityCalculator();

  /**
   * The spot Delta of an index option. This defined as the sensitivity of the option price to the price of underlying index (any defaults from the index reduce its notional).
   * The calculation used the intrinsic credit curves to compute an index price, an ATM (default-adjusted) forward price and a option price. The index price is then bumped by a small amount,
   * and the curves (re)adjusted to match this price - with these new curves, a new ATM forward price and option value is calculated. The direction of the bumps and how they are used  to estimate the 
   * sensitivity depends on the FiniteDifferenceType
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCDX The spot CDS that represent the index
   * @param indexCoupon The index coupon
   * @param yieldCurve The current yield curve
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param bumpAmount The bump amount
   * @param type {@link FiniteDifferenceType#FORWARD}, {@link FiniteDifferenceType#BACKWARD}, or {@link FiniteDifferenceType#CENTRAL}. In most situations,
   * {@link FiniteDifferenceType#CENTRAL} is preferable for accuracy, but often the forward difference is used. Not null
   * @return finite difference estimate of Delta
   */
  public double delta(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic indexCDX, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final IntrinsicIndexDataBundle intrinsicData, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double bumpAmount, final FiniteDifferenceType type) {

    ArgumentChecker.isTrue(Math.abs(bumpAmount) > BUMP_MIN, "Bump given as {}, but must have abs value greater than {}", bumpAmount, BUMP_MIN);
    ArgumentChecker.isTrue(Math.abs(bumpAmount) < BUMP_MAX, "Bump given as {}, but must have abs value less than {}. Bump must be given as fraction", bumpAmount, BUMP_MAX);

    final double indexFactor = intrinsicData.getIndexFactor();
    final CDSAnalytic fwdStartingCDS = fwdCDS.withOffset(timeToExpiry);
    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, timeToExpiry, yieldCurve, indexCoupon);

    final Function1D<Double, Double> optPriceFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double indexPrice) {
        final double puf = indexPrice / indexFactor;
        final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(puf, indexCDX, indexCoupon, yieldCurve, intrinsicData);
        final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, yieldCurve, indexCoupon, adjCurves);
        return pricer.getOptionPremium(atmFwd, vol, strike, isPayer);
      }
    };

    final ScalarFirstOrderDifferentiator diff = new ScalarFirstOrderDifferentiator(type, bumpAmount);
    final Function1D<Double, Double> g = diff.differentiate(optPriceFunc);
    final double indexPrice = INDEX_CAL.indexPV(indexCDX, indexCoupon, yieldCurve, intrinsicData);
    return g.evaluate(indexPrice);
  }

  private double optPrice(final double timeToExpiry, final CDSAnalytic indexCDX, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData,
      final IndexOptionStrike strike, final double vol, final boolean isPayer, final double puf, final CDSAnalytic fwdStartingCDS, final IndexOptionPricer pricer) {
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(puf, indexCDX, indexCoupon, yieldCurve, intrinsicData);
    final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, yieldCurve, indexCoupon, adjCurves);
    return pricer.getOptionPremium(atmFwd, vol, strike, isPayer);
  }

  /**
   * Compute the delta as a ratio of the CS01 of an index option to the CS01 of the index. The CS01 of the index is computed by bumping up the index quoted spread and recomputing
   * the price from this bumped flat spread (using {@link FiniteDifferenceSpreadSensitivityCalculator#parallelCS01}); the CS01 of the option is computed using a homogeneous pool approximation 
   * (using {@link CS01OptionCalculator#indexCurveApprox}). Both bumps ar 1bp.
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCDX The spot CDS that represent the index
   * @param quote Market quote of index 
   * @param indexCoupon index coupon
   * @param yieldCurve The current yield curve
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @return the (CS01 ratio) delta
   */
  public double deltaByCS01(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic indexCDX, final CDSQuoteConvention quote, final double indexCoupon,
      final ISDACompliantYieldCurve yieldCurve, final IndexOptionStrike strike, final double vol, final boolean isPayer) {
    return deltaByCS01(fwdCDS, timeToExpiry, indexCDX, quote, indexCoupon, yieldCurve, strike, vol, isPayer, ONE_BPS);
  }

  /**
   * Compute the delta as a ratio of the CS01 of an index option to the CS01 of the index. The CS01 of the index is computed by bumping up the index quoted spread and recomputing
   * the price from this bumped flat spread (using {@link FiniteDifferenceSpreadSensitivityCalculator#parallelCS01}); the CS01 of the option is computed using a homogeneous pool approximation 
   * (using {@link CS01OptionCalculator#indexCurveApprox})
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCDX The spot CDS that represent the index
   * @param quote Market quote of index 
   * @param indexCoupon index coupon
   * @param yieldCurve The current yield curve
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param bumpAmount The bump amount (normally 1bp, 1e-4)
   * @return the (CS01 ratio) delta
   */
  public double deltaByCS01(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic indexCDX, final CDSQuoteConvention quote, final double indexCoupon,
      final ISDACompliantYieldCurve yieldCurve, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double bumpAmount) {
    //TODO method that takes an index factor 
    final double optCS01 = CS01_OPT_CAL.indexCurveApprox(fwdCDS, timeToExpiry, indexCDX, quote, indexCoupon, yieldCurve, strike, vol, isPayer, bumpAmount);
    final double indexCS01 = CS01_CAL.parallelCS01(indexCDX, quote, yieldCurve, bumpAmount);
    return optCS01 / indexCS01;
  }

  /**
   * The spot Gamma of an index option. This defined as the sensitivity of the option delta to the price of underlying index, which is the second-order sensitivity of the option price to
   * the price of the index. 
   * The calculation used the intrinsic credit curves to compute an index price, an ATM (default-adjusted) forward price and a option price. The index price is then bumped by a small amount,
   * and the curves (re)adjusted to match this price - with these new curves, a new ATM forward price and option value is calculated. Gamma is computed using central finite difference.
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCDX The spot CDS that represent the index
   * @param indexCoupon The index coupon
   * @param yieldCurve The current yield curve
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param bumpAmount The bump amount
   * @return finite difference estimate of Gamma
   */
  public double gamma(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic indexCDX, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final IntrinsicIndexDataBundle intrinsicData, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double bumpAmount) {

    ArgumentChecker.isTrue(Math.abs(bumpAmount) > BUMP_MIN, "Bump given as {}, but must have abs value greater than {}", bumpAmount, BUMP_MIN);
    ArgumentChecker.isTrue(Math.abs(bumpAmount) < BUMP_MAX, "Bump given as {}, but must have abs value less than {}. Bump must be given as fraction", bumpAmount, BUMP_MAX);

    final double f = intrinsicData.getIndexFactor();
    final double puf = INDEX_CAL.indexPV(indexCDX, indexCoupon, yieldCurve, intrinsicData) / f;
    final double dPUF = bumpAmount / f;
    //TODO don't have a test for this
    final CDSAnalytic fwdStartingCDS = fwdCDS.withOffset(timeToExpiry);
    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, timeToExpiry, yieldCurve, indexCoupon);

    final double downPrice = optPrice(timeToExpiry, indexCDX, indexCoupon, yieldCurve, intrinsicData, strike, vol, isPayer, puf - dPUF, fwdStartingCDS, pricer);
    final double upPrice = optPrice(timeToExpiry, indexCDX, indexCoupon, yieldCurve, intrinsicData, strike, vol, isPayer, puf + dPUF, fwdStartingCDS, pricer);
    final double basePrice = optPrice(timeToExpiry, indexCDX, indexCoupon, yieldCurve, intrinsicData, strike, vol, isPayer, puf, fwdStartingCDS, pricer);
    return (upPrice + downPrice - 2 * basePrice) / bumpAmount / bumpAmount;
  }

  /**
   * This is defined as the difference in Delta (computed as a CS01 ratio {@link #deltaByCS01} with bump of 1bp) computed with the quoted spread bumped up by 10bps from its normal value. 
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCDX The spot CDS that represent the index
   * @param quote Market quote of index 
   * @param indexCoupon index coupon
   * @param yieldCurve The current yield curve
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @return the Gamma as a Delta difference 
   */
  public double gammaByCS01(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic indexCDX, final CDSQuoteConvention quote, final double indexCoupon,
      final ISDACompliantYieldCurve yieldCurve, final IndexOptionStrike strike, final double vol, final boolean isPayer) {
    return gammaByCS01(fwdCDS, timeToExpiry, indexCDX, quote, indexCoupon, yieldCurve, strike, vol, isPayer, ONE_BPS, TEN_BPS);
  }

  /**
   * This is defined as the difference in Delta (computed as a CS01 ratio {@link #deltaByCS01}) computed with the quoted spread bumped up (usually by 10bps) from its normal value. 
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCDX The spot CDS that represent the index
   * @param quote Market quote of index 
   * @param indexCoupon index coupon
   * @param yieldCurve The current yield curve
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param bumpAmount The bump amount The bump amount to use in the Delta (CS01 ratio) calculation (normally 1bp, 1e-4)
   * @param largeBumpAmount The bump in quoted spread to calculate the new delta at (normally 10bp, 1e-3)
   * @return the Gamma as a Delta difference 
   */
  public double gammaByCS01(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic indexCDX, final CDSQuoteConvention quote, final double indexCoupon,
      final ISDACompliantYieldCurve yieldCurve, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double bumpAmount, final double largeBumpAmount) {

    final CDSQuoteConvention largeBumpedQuote = CS01_CAL.bumpQuote(indexCDX, quote, yieldCurve, largeBumpAmount);

    final double baseCS01 = deltaByCS01(fwdCDS, timeToExpiry, indexCDX, quote, indexCoupon, yieldCurve, strike, vol, isPayer, bumpAmount);
    final double bumpedCS01 = deltaByCS01(fwdCDS, timeToExpiry, indexCDX, largeBumpedQuote, indexCoupon, yieldCurve, strike, vol, isPayer, bumpAmount);
    return bumpedCS01 - baseCS01;
  }

  /**
   * This is the sensitivity of the option price to the log-normal volatility of the flat (pseudo) spread. This calculation does not depend on the details of the CDS pool, just the computed value
   * of the ATM forward price. 
   * @param atmFwdPrice The ATM Forward price. This can be a given, or calculated using {@link CDSIndexCalculator#defaultAdjustedForwardIndexValue} 
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCoupon The index coupon
   * @param yieldCurve The current yield curve
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param bumpAmount The bump amount
   * @param type {@link FiniteDifferenceType#FORWARD}, {@link FiniteDifferenceType#BACKWARD}, or {@link FiniteDifferenceType#CENTRAL}. In most situations,
   * {@link FiniteDifferenceType#CENTRAL} is preferable for accuracy, but often the forward difference is used. Not null
   * @return The option vega
   */
  public double vega(final double atmFwdPrice, final CDSAnalytic fwdCDS, final double timeToExpiry, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve, final IndexOptionStrike strike,
      final double vol, final boolean isPayer, final double bumpAmount, final FiniteDifferenceType type) {
    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, timeToExpiry, yieldCurve, indexCoupon);

    final Function1D<Double, Double> priceForSigmaFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double sigma) {
        return pricer.getOptionPremium(atmFwdPrice, sigma, strike, isPayer);
      }
    };

    final ScalarFirstOrderDifferentiator differ = new ScalarFirstOrderDifferentiator(type, bumpAmount);
    final Function1D<Double, Double> vega = differ.differentiate(priceForSigmaFunc);
    return vega.evaluate(vol);
  }

  /**
   * Theta is the sensitivity of the option price to calendar time; defined this way the Theta of a payer option is always negative. It is calculated by computing an expected option price
   * some time step ahead (normally one day), assuming the ATM forward price remains the same (i.e. just using its Martingale property) and the yield curve takes its expected value. <p>
   * Implicit in the Martingale property of the ATM forward is that defaults can occur over the time step (use {@link #thetaWithoutDefault} to compute a Theta conditional on no defaults).
   * @param atmFwdPrice The ATM Forward price. This can be a given, or calculated using {@link CDSIndexCalculator#defaultAdjustedForwardIndexValue} 
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCoupon The index coupon
   * @param yieldCurve The current yield curve
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param timeStep The time step (1./365 is normally used) 
   * @return The option theta 
   */
  public double thetaWithDefault(final double atmFwdPrice, final CDSAnalytic fwdCDS, final double timeToExpiry, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final IndexOptionStrike strike, final double vol, final boolean isPayer, final double timeStep) {

    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, timeToExpiry, yieldCurve, indexCoupon);
    final IndexOptionPricer fwdPricer = new IndexOptionPricer(fwdCDS, timeToExpiry - timeStep, yieldCurve.withOffset(timeStep), indexCoupon);
    final double basePrice = pricer.getOptionPremium(atmFwdPrice, vol, strike, isPayer);
    final double fwdPrice = fwdPricer.getOptionPremium(atmFwdPrice, vol, strike, isPayer);

    return (fwdPrice - basePrice) / timeStep;
  }

  /**
   * Theta is the sensitivity of the option price to calendar time; defined this way the Theta of a payer option is always negative. It is calculated by computing an expected option price
   * some time step ahead (normally one day), assuming the ATM forward price remains the same (i.e. just using its Martingale property) and the yield curve takes its expected value. <p>
   * Implicit in the Martingale property of the ATM forward is that defaults can occur over the time step (use {@link #thetaWithoutDefault} to compute a Theta conditional on no defaults).
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCoupon The index coupon
   * @param yieldCurve The current yield curve
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param timeStep The time step (1./365 is normally used) 
   * @return The option theta 
   */
  public double thetaWithDefault(final CDSAnalytic fwdCDS, final double timeToExpiry, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData,
      final IndexOptionStrike strike, final double vol, final boolean isPayer, final double timeStep) {
    final double atmFwd = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdCDS, timeToExpiry, yieldCurve, indexCoupon, intrinsicData);
    return thetaWithDefault(atmFwd, fwdCDS, timeToExpiry, indexCoupon, yieldCurve, strike, vol, isPayer, timeStep);
  }

  /**
   * Theta is the sensitivity of the option price to calendar time; defined this way the Theta of a payer option is always negative. It is calculated by computing an expected option price
   * some time step ahead (normally one day), assuming <b>no defaults occur</b>, and the credit &  yield curves take their expected values. <p>
   * A more natural definition of Theta includes defaults (use {@link #thetaWithDefault} to compute this).
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCoupon The index coupon
   * @param yieldCurve The current yield curve
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param timeStep The time step (1./365 is normally used) 
   * @return The option Theta conditional on no defaults over the time step 
   */
  public double thetaWithoutDefault(final CDSAnalytic fwdCDS, final double timeToExpiry, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final IntrinsicIndexDataBundle intrinsicData, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double timeStep) {

    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, timeToExpiry, yieldCurve, indexCoupon);
    final IndexOptionPricer fwdPricer = new IndexOptionPricer(fwdCDS, timeToExpiry - timeStep, yieldCurve.withOffset(timeStep), indexCoupon);
    final double atmFwd = atmFwdBump(timeToExpiry, indexCoupon, yieldCurve, intrinsicData, fwdCDS, 0.0);
    final double fwdATMFwd = atmFwdBump(timeToExpiry, indexCoupon, yieldCurve, intrinsicData, fwdCDS, timeStep);
    final double basePrice = pricer.getOptionPremium(atmFwd, vol, strike, isPayer);
    final double fwdPrice = fwdPricer.getOptionPremium(fwdATMFwd, vol, strike, isPayer);
    //   System.out.println("ATM Fwd\t" + atmFwd + "\t" + fwdATMFwd);
    return (fwdPrice - basePrice) / timeStep;
  }

  /**
   * The irDV01 (interest rate DV01) is the change in the price of an option when the market rates of the instruments used to build the yield curve are increased (usually by 1bps). 
   * The quoted spread of the index is held constant, then a `bumped' PUF calculated using the bumped yield curve; the intrinsic credit curves are adjusted to match this bumped PUF 
   * (using the bumped yield curve). The two sets of credit curves, together with the two yield curves are used to obtain two option prices - the difference (divided by the bumpAmount) is the
   * irDV01. 
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCDX The spot CDS that represent the index
   * @param indexCoupon the index coupon 
   * @param yieldCurveBuilder A yield curve builder - takes rates (of market instruments) and produces a yield curve   
   * @param irRates The market rates of the instruments used to build the yield curve
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param bumpAmount The bump amount (normally 1bp)
   * @return The irDV01 
   */
  public double irDV01(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic indexCDX, final double indexCoupon, final ISDACompliantYieldCurveBuild yieldCurveBuilder,
      final double[] irRates, final IntrinsicIndexDataBundle intrinsicData, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double bumpAmount) {

    final CDSAnalytic fwdStartingCDS = fwdCDS.withOffset(timeToExpiry);

    final MarketQuoteConverter converter = new MarketQuoteConverter();
    final ISDACompliantYieldCurve yc = yieldCurveBuilder.build(irRates);
    final ISDACompliantYieldCurve ycBumped = bumpYieldCurve(yieldCurveBuilder, irRates, bumpAmount);
    final double puf = INDEX_CAL.indexPUF(indexCDX, indexCoupon, yc, intrinsicData);
    final double qs = converter.pufToQuotedSpread(indexCDX, indexCoupon, yc, puf);
    final double pufBumped = converter.quotedSpreadToPUF(indexCDX, indexCoupon, ycBumped, qs);
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(pufBumped, indexCDX, indexCoupon, ycBumped, intrinsicData);

    final double fwd = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, yc, indexCoupon, intrinsicData);
    final double fwdUp = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, ycBumped, indexCoupon, adjCurves);
    final IndexOptionPricer pricer = new IndexOptionPricer(fwdCDS, timeToExpiry, yc, indexCoupon);
    final IndexOptionPricer pricerUp = new IndexOptionPricer(fwdCDS, timeToExpiry, ycBumped, indexCoupon);

    final double p = pricer.getOptionPremium(fwd, vol, strike, isPayer);
    final double pUp = pricerUp.getOptionPremium(fwdUp, vol, strike, isPayer);

    return (pUp - p) / bumpAmount;
  }

  /**
   * The irDV01 (interest rate DV01) is the change in the price of an option when the market rates of the instruments used to build the yield curve are increased (usually by 1bps). 
   * The quoted spread of the index is held constant, then a `bumped' PUF calculated using the bumped yield curve; these in turn are used to compute flat credit curves and two option prices
   * computed using a homogeneous pool approximation (two ATM forward values are computed using {@link CDSIndexCalculator#defaultAdjustedForwardIndexValue} 
   * @param fwdCDS Forward CDS - represents the CDS at the expiry date (i.e. made with the tradeDate equal to the option expiry date).
   * @param timeToExpiry time to expiry of the option 
   * @param indexCDX The spot CDS that represent the index
   * @param indexCoupon the index coupon 
   * @param indexPUF PUF of the index
   * @param yieldCurveBuilder A yield curve builder - takes rates (of market instruments) and produces a yield curve   
   * @param irRates The market rates of the instruments used to build the yield curve
   * @param strike The option strike. This can be either given as the exercise price directly (ExerciseAmount) or as a spread (SpreadBasedStrike)
   * @param vol The log-normal volatility of the flat (pseudo) spread 
   * @param isPayer true for payer and false for receiver option 
   * @param bumpAmount The bump amount (normally 1bp)
   * @return The irDV01 
   */
  public double irDV01(final CDSAnalytic fwdCDS, final double timeToExpiry, final CDSAnalytic indexCDX, final double indexCoupon, final double indexPUF,
      final ISDACompliantYieldCurveBuild yieldCurveBuilder, final double[] irRates, final IndexOptionStrike strike, final double vol, final boolean isPayer, final double bumpAmount) {
    final AnalyticCDSPricer pricer = new AnalyticCDSPricer();
    final CDSAnalytic fwdStartingCDS = fwdCDS.withOffset(timeToExpiry);
    final ISDACompliantYieldCurve yc = yieldCurveBuilder.build(irRates);
    final ISDACompliantCreditCurve indexCurve = CC_BUILDER.calibrateCreditCurve(indexCDX, indexCoupon, yc, indexPUF);
    final double indexSpread = pricer.parSpread(indexCDX, yc, indexCurve);
    final double fwd = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, yc, indexCoupon, indexCurve);
    final IndexOptionPricer optPricer = new IndexOptionPricer(fwdCDS, timeToExpiry, yc, indexCoupon);
    final double p = optPricer.getOptionPremium(fwd, vol, strike, isPayer);

    //recal everything with bumped yc
    final ISDACompliantYieldCurve ycUp = bumpYieldCurve(yieldCurveBuilder, irRates, bumpAmount);
    final ISDACompliantCreditCurve indexCurveUp = CC_BUILDER.calibrateCreditCurve(indexCDX, indexSpread, ycUp);
    final double fwdUp = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry, ycUp, indexCoupon, indexCurveUp);
    final IndexOptionPricer optPricerUp = new IndexOptionPricer(fwdCDS, timeToExpiry, ycUp, indexCoupon);
    final double pUp = optPricerUp.getOptionPremium(fwdUp, vol, strike, isPayer);
    return (pUp - p) / bumpAmount;
  }

  private double atmFwdBump(final double timeToExpiry, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinicData, final CDSAnalytic fwdCDS,
      final double timeStep) {

    final CDSAnalytic fwdStartingCDS = fwdCDS.withOffset(timeToExpiry - timeStep);
    if (timeStep == 0.0) {
      return INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry - timeStep, yieldCurve, indexCoupon, intrinicData);
    }

    final ISDACompliantYieldCurve fwdYieldCurve = yieldCurve.withOffset(timeStep);
    final int n = intrinicData.getIndexSize();
    final ISDACompliantCreditCurve[] fwdCC = new ISDACompliantCreditCurve[n];

    for (int i = 0; i < n; i++) {
      if (!intrinicData.isDefaulted(i)) {
        fwdCC[i] = new ISDACompliantCreditCurve(intrinicData.getCreditCurve(i).withOffset(timeStep));
      }
    }

    return INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartingCDS, timeToExpiry - timeStep, fwdYieldCurve, indexCoupon, intrinicData.withCreditCurves(fwdCC));
  }

  private ISDACompliantYieldCurve bumpYieldCurve(final ISDACompliantYieldCurveBuild builder, final double[] rates, final double bumpAmount) {
    final int n = rates.length;
    final double[] bumped = new double[n];
    System.arraycopy(rates, 0, bumped, 0, n);
    for (int i = 0; i < n; i++) {
      bumped[i] += bumpAmount;
    }
    return builder.build(bumped);
  }

}
