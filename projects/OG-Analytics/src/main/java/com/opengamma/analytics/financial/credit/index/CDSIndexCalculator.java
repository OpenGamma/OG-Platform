/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.index;

import java.util.Arrays;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.CreditCurveCalibrator;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CDSIndexCalculator {

  private final AnalyticCDSPricer _pricer;

  public CDSIndexCalculator() {
    _pricer = new AnalyticCDSPricer();
  }

  /**
   * The Points-Up-Front (PUF) of an index. This is the (clean) price of a unit notional index. The actual clean price is this multiplied by the (current) index notional 
   * (i.e. the initial notional times the index factor).
   * @param indexCDS analytic description of a CDS traded at a certain time
   * @param indexCoupon The coupon of the index (as a fraction)
   * @param yieldCurve The yield curve
   * @param intrinsicData credit curves, weights and recover
   * @return PUF of an index 
   */
  public double indexPUF(final CDSAnalytic indexCDS, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData) {
    ArgumentChecker.notNull(intrinsicData, "intrinsicData");
    if (intrinsicData.getNumOfDefaults() == intrinsicData.getIndexSize()) {
      throw new IllegalArgumentException("Index completely defaulted - not possible to rescale for PUF");
    }
    return indexPV(indexCDS, indexCoupon, yieldCurve, intrinsicData) / intrinsicData.getIndexFactor();
  }

  /**
  * Intrinsic (normalised) price an index from the credit curves of the individual single names.  To get the
   * actual index value, this multiplied by the <b>initial</b>  notional of the index
   * @param indexCDS analytic description of a CDS traded at a certain time
   * @param indexCoupon The coupon of the index (as a fraction)
   * @param yieldCurve The yield curve
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @return The index value for a unit  notional. 
   */
  public double indexPV(final CDSAnalytic indexCDS, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData) {
    final double prot = indexProtLeg(indexCDS, yieldCurve, intrinsicData);
    final double annuity = indexAnnuity(indexCDS, yieldCurve, intrinsicData);
    return prot - indexCoupon * annuity;
  }

  /**
   * Intrinsic (normalised) price an index from the credit curves of the individual single names.  To get the
   * actual index value, this multiplied by the <b>initial</b>  notional of the index
   * @param indexCDS analytic description of a CDS traded at a certain time
   * @param indexCoupon The coupon of the index (as a fraction)
   * @param yieldCurve The yield curve
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param priceType Clean or dirty price
   * @return The index value for a unit  notional. 
   */
  public double indexPV(final CDSAnalytic indexCDS, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData, final PriceType priceType) {
    final double prot = indexProtLeg(indexCDS, yieldCurve, intrinsicData);
    final double annuity = indexAnnuity(indexCDS, yieldCurve, intrinsicData, priceType);
    return prot - indexCoupon * annuity;
  }

  /**
   * Intrinsic (normalised) price an index from the credit curves of the individual single names.  To get the
   * actual index value, this multiplied by the <b>initial</b>  notional of the index
   * @param indexCDS analytic description of a CDS traded at a certain time
   * @param indexCoupon The coupon of the index (as a fraction)
   * @param yieldCurve The yield curve
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param priceType Clean or dirty price
   * @param valuationTime The leg value is calculated for today (t=0), then rolled forward (using the risk free yield curve) to the valuation time.
   * This is because cash payments occur on the cash-settlement-date, which is usually three working days after the trade date (today) 
   * @return The index value for a unit  notional. 
   */
  public double indexPV(final CDSAnalytic indexCDS, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData, final PriceType priceType,
      final double valuationTime) {
    final double prot = indexProtLeg(indexCDS, yieldCurve, intrinsicData, valuationTime);
    final double annuity = indexAnnuity(indexCDS, yieldCurve, intrinsicData, priceType, valuationTime);
    return prot - indexCoupon * annuity;
  }

  /**
   * The intrinsic index spread. this is defined as the ratio of the intrinsic protection leg to the intrinsic annuity. 
   *@see averageSpread
   * @param indexCDS analytic description of a CDS traded at a certain time
   * @param yieldCurve The yield curve
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @return intrinsic index spread (as a fraction)
   */
  public double intrinsicIndexSpread(final CDSAnalytic indexCDS, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData) {
    ArgumentChecker.notNull(intrinsicData, "intrinsicData");
    if (intrinsicData.getNumOfDefaults() == intrinsicData.getIndexSize()) {
      throw new IllegalArgumentException("Every name in the index is defaulted - cannot calculate a spread");
    }
    final double prot = indexProtLeg(indexCDS, yieldCurve, intrinsicData);
    final double annuity = indexAnnuity(indexCDS, yieldCurve, intrinsicData);
    return prot / annuity;
  }

  /**
   * The normalised intrinsic value of the protection leg of a CDS portfolio (index). The actual value of the leg is this multiplied by the <b>initial</b>  notional of the 
   * index
   * @param indexCDS representation of the index cashflows (seen from today). 
   * @param yieldCurve The current yield curves 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @return The normalised intrinsic value of the protection leg. 
   */
  public double indexProtLeg(final CDSAnalytic indexCDS, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData) {
    ArgumentChecker.notNull(indexCDS, "indexCDS");
    return indexProtLeg(indexCDS, yieldCurve, intrinsicData, indexCDS.getCashSettleTime());
  }

  /**
   * The normalised intrinsic value of the protection leg of a CDS portfolio (index). The actual value of the leg is this multiplied by the <b>initial</b>  notional of the 
   * index
   * @param indexCDS representation of the index cashflows (seen from today). 
   * @param yieldCurve The current yield curves 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param valuationTime Valuation time. The leg value is calculated for today (t=0), then rolled forward (using the risk free yield curve) to the valuation time.
   * This is because cash payments occur on the cash-settlement-date, which is usually three working days after the trade date (today) 
   * @return The normalised intrinsic value of the protection leg. 
   */
  public double indexProtLeg(final CDSAnalytic indexCDS, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData, final double valuationTime) {
    ArgumentChecker.notNull(indexCDS, "indexCDS");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(intrinsicData, "intrinsicData");

    final CDSAnalytic cds = indexCDS.withRecoveryRate(0.0);
    final int n = intrinsicData.getIndexSize();
    double protLeg = 0;
    for (int i = 0; i < n; i++) {
      if (!intrinsicData.isDefaulted(i)) {
        protLeg += intrinsicData.getWeight(i) * intrinsicData.getLGD(i) * _pricer.protectionLeg(cds, yieldCurve, intrinsicData.getCreditCurve(i), 0);
      }
    }
    protLeg /= yieldCurve.getDiscountFactor(valuationTime);
    return protLeg;
  }

  /**
   * The  intrinsic annuity of a CDS portfolio (index) for a unit (initial) notional. The value of the premium leg is this multiplied by the <b> initial</b> notional of the index 
   * and the index coupon (as a fraction).
   * @param indexCDS representation of the index cashflows (seen from today). 
   * @param yieldCurve The current yield curves 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param valuationTime Valuation time. The leg value is calculated for today (t=0), then rolled forward (using the risk free yield curve) to the valuation time.
   * This is because cash payments occur on the cash-settlement-date, which is usually three working days after the trade date (today) 
   * @return The  intrinsic annuity of a CDS portfolio (index)
   */
  public double indexAnnuity(final CDSAnalytic indexCDS, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData, final double valuationTime) {
    return indexAnnuity(indexCDS, yieldCurve, intrinsicData, PriceType.CLEAN, valuationTime);
  }

  /**
   * The  intrinsic annuity of a CDS portfolio (index) for a unit (initial) notional. The value of the premium leg is this multiplied by the <b> initial</b> notional of the index 
   * and the index coupon (as a fraction).
   * @param indexCDS representation of the index cashflows (seen from today). 
   * @param yieldCurve The current yield curves 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @return The normalised intrinsic annuity of a CDS portfolio (index)
   */
  public double indexAnnuity(final CDSAnalytic indexCDS, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData) {
    return indexAnnuity(indexCDS, yieldCurve, intrinsicData, PriceType.CLEAN, indexCDS.getCashSettleTime());
  }

  /**
   * The  intrinsic annuity of a CDS portfolio (index) for a unit (initial) notional. The value of the premium leg is this multiplied by the <b> initial</b> notional of the index 
   * and the index coupon (as a fraction).
   * @param indexCDS representation of the index cashflows (seen from today). 
   * @param yieldCurve The current yield curves 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param priceType Clean or dirty 
   * @return The normalised intrinsic annuity of a CDS portfolio (index)
   */
  public double indexAnnuity(final CDSAnalytic indexCDS, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData, final PriceType priceType) {
    ArgumentChecker.notNull(indexCDS, "indexCDS");
    return indexAnnuity(indexCDS, yieldCurve, intrinsicData, priceType, indexCDS.getCashSettleTime());
  }

  /**
   * The  intrinsic annuity of a CDS portfolio (index) for a unit (initial) notional. The value of the premium leg is this multiplied by the <b> initial</b> notional of the index 
   * and the index coupon (as a fraction).
   * @param indexCDS representation of the index cashflows (seen from today). 
   * @param yieldCurve The current yield curves 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @param priceType Clean or dirty 
   * @param valuationTime Valuation time. The leg value is calculated for today (t=0), then rolled forward (using the risk free yield curve) to the valuation time.
   * This is because cash payments occur on the cash-settlement-date, which is usually three working days after the trade date (today) 
   * @return The  intrinsic annuity of a CDS portfolio (index)
   */
  public double indexAnnuity(final CDSAnalytic indexCDS, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData, final PriceType priceType, final double valuationTime) {
    ArgumentChecker.notNull(indexCDS, "indexCDS");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(intrinsicData, "intrinsicData");

    final int n = intrinsicData.getIndexSize();
    double a = 0;
    for (int i = 0; i < n; i++) {
      if (!intrinsicData.isDefaulted(i)) {
        a += intrinsicData.getWeight(i) * _pricer.annuity(indexCDS, yieldCurve, intrinsicData.getCreditCurve(i), priceType, 0);
      }
    }
    a /= yieldCurve.getDiscountFactor(valuationTime);

    return a;
  }

  /**
   * The average spread of a CDS portfolio (index), defined as the weighted average of the (implied) par spreads of the constituent names 
    @see intrinsicIndexSpread
   * @param indexCDS representation of the index cashflows (seen from today). 
   * @param yieldCurve The current yield curves 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @return The average spread 
   */
  public double averageSpread(final CDSAnalytic indexCDS, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData) {
    ArgumentChecker.notNull(indexCDS, "indexCDS");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(intrinsicData, "intrinsicData");
    if (intrinsicData.getNumOfDefaults() == intrinsicData.getIndexSize()) {
      throw new IllegalArgumentException("Every name in the index is defaulted - cannot calculate a spread");
    }

    final CDSAnalytic cds = indexCDS.withRecoveryRate(0.0);
    final int n = intrinsicData.getIndexSize();
    double sum = 0;
    for (int i = 0; i < n; i++) {
      if (!intrinsicData.isDefaulted(i)) {
        final double protLeg = intrinsicData.getLGD(i) * _pricer.protectionLeg(cds, yieldCurve, intrinsicData.getCreditCurve(i));
        final double annuity = _pricer.annuity(cds, yieldCurve, intrinsicData.getCreditCurve(i));
        final double s = protLeg / annuity;
        sum += intrinsicData.getWeight(i) * s;
      }
    }
    sum /= intrinsicData.getIndexFactor();
    return sum;
  }

  /**
   * Imply a single (pseudo) credit curve for an index that will give the same index values at a set of terms (supplied via pillarCDS) as the intrinsic value.
   * @param pillarCDS Point to build the curve 
   * @param indexCoupon The index coupon 
   * @param yieldCurve The current yield curves 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @return A (pseudo) credit curve for an index
   */
  public ISDACompliantCreditCurve impliedIndexCurve(final CDSAnalytic[] pillarCDS, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData) {
    ArgumentChecker.noNulls(pillarCDS, "pillarCDS");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(intrinsicData, "intrinsicData");
    if (intrinsicData.getNumOfDefaults() == intrinsicData.getIndexSize()) {
      throw new IllegalArgumentException("Every name in the index is defaulted - cannot calculate implied index curve");
    }
    final int n = pillarCDS.length;
    final double[] puf = new double[n];
    final double indexFactor = intrinsicData.getIndexFactor();
    for (int i = 0; i < n; i++) {
      puf[i] = indexPV(pillarCDS[i], indexCoupon, yieldCurve, intrinsicData) / indexFactor; //PUF are always given for full index
    }
    final CreditCurveCalibrator calibrator = new CreditCurveCalibrator(pillarCDS, yieldCurve);
    final double[] coupons = new double[n];
    Arrays.fill(coupons, indexCoupon);
    return calibrator.calibrate(coupons, puf);
  }

  //*******************************************************************************************************************
  //* Forward values adjusted for defaults 
  //****************************************************************************************************************

  /**
   * For a future expiry date, the default adjusted forward index value is the expected (full) value of the index plus the cash settlement of any defaults before
   * the expiry date, valued on the (forward) cash settlement date (usually 3 working days after the expiry date - i.e. the expiry settlement date). 
   * @param fwdStartingCDS A forward starting CDS to represent cash flows in the index. The stepin date should be one day after the expiry and the cashSettlement 
   * date (usually) 3 working days after expiry
   * @param timeToExpiry the time in years between the trade date and expiry. This should use the same DCC as the curves (ACT365F unless manually changed). 
   * @param yieldCurve The yield curve 
   * @param indexCoupon The coupon of the index 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   *  initially 100 entries, and the realised recovery rates are 0.2 and 0.35, the this value is (0.8 + 0.65)/100 )  
   * @return the default adjusted forward index value
   */
  public double defaultAdjustedForwardIndexValue(final CDSAnalytic fwdStartingCDS, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final double indexCoupon,
      final IntrinsicIndexDataBundle intrinsicData) {
    ArgumentChecker.notNull(fwdStartingCDS, "fwdCDS");
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "timeToExpiry given as {}", timeToExpiry);
    ArgumentChecker.isTrue(fwdStartingCDS.getEffectiveProtectionStart() >= timeToExpiry, "effective protection start of {} is less than time to expiry of {}. Must provide a forward starting CDS",
        fwdStartingCDS.getEffectiveProtectionStart(), timeToExpiry);

    //the expected value of the index (not including default settlement) at the expiry settlement date 
    final double indexPV = indexPV(fwdStartingCDS, indexCoupon, yieldCurve, intrinsicData);
    final double d = expectedDefaultSettlementValue(timeToExpiry, intrinsicData);
    return indexPV + d;
  }

  /**
   * For a future expiry date, the default adjusted forward index value is the expected (full) value of the index plus the cash settlement of any defaults before
   * the expiry date, valued on the (forward) cash settlement date (usually 3 working days after the expiry date - i.e. the expiry settlement date). 
   * This calculation assumes an homogeneous pool that can be described by a single index curve. 
   * @param fwdStartingCDS A forward starting CDS to represent cash flows in the index. The stepin date should be one day after the expiry and the cashSettlement 
   * date (usually) 3 working days after expiry. This must contain the index recovery rate. 
   * @param timeToExpiry the time in years between the trade date and expiry. This should use the same DCC as the curves (ACT365F unless manually changed). 
   * @param yieldCurve The yield curve 
   * @param indexCoupon The coupon of the index 
   * @param indexCurve  Pseudo credit curve for the index.
   * @return the default adjusted forward index value
   */
  public double defaultAdjustedForwardIndexValue(final CDSAnalytic fwdStartingCDS, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final double indexCoupon,
      final ISDACompliantCreditCurve indexCurve) {
    ArgumentChecker.notNull(fwdStartingCDS, "fwdCDS");
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "timeToExpiry given as {}", timeToExpiry);
    ArgumentChecker.isTrue(fwdStartingCDS.getEffectiveProtectionStart() >= timeToExpiry, "effective protection start of {} is less than time to expiry of {}. Must provide a forward starting CDS",
        fwdStartingCDS.getEffectiveProtectionStart(), timeToExpiry);

    final double defSet = expectedDefaultSettlementValue(timeToExpiry, indexCurve, fwdStartingCDS.getLGD());
    return defSet + _pricer.pv(fwdStartingCDS, yieldCurve, indexCurve, indexCoupon);
  }

  /**
   * For a future expiry date, the default adjusted forward index value is the expected (full) value of the index plus the cash settlement of any defaults before
   * the expiry date, valued on the (forward) cash settlement date (usually 3 working days after the expiry date - i.e. the expiry settlement date). 
   * This calculation assumes an homogeneous pool that can be described by a single index curve. 
   * @param fwdStartingCDS A forward starting CDS to represent cash flows in the index. The stepin date should be one day after the expiry and the cashSettlement 
   * date (usually) 3 working days after expiry. This must contain the index recovery rate. 
   * @param timeToExpiry the time in years between the trade date and expiry. This should use the same DCC as the curves (ACT365F unless manually changed). 
   * @param initialIndexSize The initial number of names in the index 
   * @param yieldCurve The yield curve 
   * @param indexCoupon The coupon of the index 
   * @param indexCurve  Pseudo credit curve for the index.
   * @param initialDefaultSettlement The (normalised) value of any defaults that have already occurred (e.g. if two defaults have occurred from an index with
   *  initially 100 entries, and the realised recovery rates are 0.2 and 0.35, the this value is (0.8 + 0.65)/100 )  
   * @param numDefaults The number of defaults that have already occurred 
   * @return the default adjusted forward index value
   */
  public double defaultAdjustedForwardIndexValue(final CDSAnalytic fwdStartingCDS, final double timeToExpiry, final int initialIndexSize, final ISDACompliantYieldCurve yieldCurve,
      final double indexCoupon, final ISDACompliantCreditCurve indexCurve, final double initialDefaultSettlement, final int numDefaults) {
    ArgumentChecker.notNull(fwdStartingCDS, "fwdCDS");
    ArgumentChecker.isTrue(timeToExpiry >= 0.0, "timeToExpiry given as {}", timeToExpiry);
    ArgumentChecker.isTrue(fwdStartingCDS.getEffectiveProtectionStart() >= timeToExpiry, "effective protection start of {} is less than time to expiry of {}. Must provide a forward starting CDS",
        fwdStartingCDS.getEffectiveProtectionStart(), timeToExpiry);

    final double f = (initialIndexSize - numDefaults) / ((double) initialIndexSize);
    final double defSet = expectedDefaultSettlementValue(initialIndexSize, timeToExpiry, indexCurve, fwdStartingCDS.getLGD(), initialDefaultSettlement, numDefaults);
    return defSet + f * _pricer.pv(fwdStartingCDS, yieldCurve, indexCurve, indexCoupon);
  }

  /**
   * The (default adjusted) intrinsic forward spread of an index. This is defined as the ratio of expected value of the protection leg and default settlement to
   * the expected value of the annuity at expiry 
   * @param fwdStartingCDS  forward starting CDS to represent cash flows in the index. The stepin date should be one day after the expiry and the cashSettlement 
   * date (usually) 3 working days after expiry the time in years between the trade date and expiry. This should use the same DCC as the curves (ACT365F unless manually changed). 
   * @param timeToExpiry the time in years between the trade date and expiry. This should use the same DCC as the curves (ACT365F unless manually changed). 
   * @param yieldCurve The yield curve 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   *  initially 100 entries, and the realised recovery rates are 0.2 and 0.35, the this value is (0.8 + 0.65)/100 )  
   * @return The (default adjusted) forward spread (as a fraction)
   */
  public double defaultAdjustedForwardSpread(final CDSAnalytic fwdStartingCDS, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final IntrinsicIndexDataBundle intrinsicData) {
    ArgumentChecker.notNull(fwdStartingCDS, "fwdStartingCDS");
    ArgumentChecker.isTrue(fwdStartingCDS.getEffectiveProtectionStart() >= timeToExpiry, "effective protection start of {} is less than time to expiry of {}. Must provide a forward starting CDS",
        fwdStartingCDS.getEffectiveProtectionStart(), timeToExpiry);

    //Note: these values are all calculated for payment on the (forward) cash settlement date - there is no point discounting to today 
    final double protLeg = indexProtLeg(fwdStartingCDS, yieldCurve, intrinsicData);
    final double defSettle = expectedDefaultSettlementValue(timeToExpiry, intrinsicData);
    final double ann = indexAnnuity(fwdStartingCDS, yieldCurve, intrinsicData);
    return (protLeg + defSettle) / ann;
  }

  /**
   * The (default adjusted) intrinsic forward spread of an index <b>when no defaults have yet occurred</b>. This is defined as the ratio of expected value of the 
   * protection leg and default settlement to the expected value of the annuity at expiry.  This calculation assumes an homogeneous pool that can be described by a 
   * single index curve. 
  * @param fwdStartingCDS forward starting CDS to represent cash flows in the index. The stepin date should be one day after the expiry and the cashSettlement 
   * date (usually) 3 working days after expiry
   * @param timeToExpiry the time in years between the trade date and expiry. This should use the same DCC as the curves (ACT365F unless manually changed). 
   * @param yieldCurve The yield curve 
   * @param indexCurve Pseudo credit curve for the index.
   * @return The normalised expected default settlement value
   */
  public double defaultAdjustedForwardSpread(final CDSAnalytic fwdStartingCDS, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve indexCurve) {
    ArgumentChecker.notNull(fwdStartingCDS, "fwdCDS");
    ArgumentChecker.isTrue(fwdStartingCDS.getEffectiveProtectionStart() >= timeToExpiry, "effective protection start of {} is less than time to expiry of {}. Must provide a forward starting CDS",
        fwdStartingCDS.getEffectiveProtectionStart(), timeToExpiry);
    final double defSettle = expectedDefaultSettlementValue(timeToExpiry, indexCurve, fwdStartingCDS.getLGD());
    final double protLeg = _pricer.protectionLeg(fwdStartingCDS, yieldCurve, indexCurve);
    final double ann = _pricer.annuity(fwdStartingCDS, yieldCurve, indexCurve);
    return (protLeg + defSettle) / ann;
  }

  /**
   * The (default adjusted) intrinsic forward spread of an index. This is defined as the ratio of expected value of the protection leg and default settlement to
   * the expected value of the annuity at expiry.  This calculation assumes an homogeneous pool that can be described by a single index curve. 
   * @param fwdStartingCDS forward starting CDS to represent cash flows in the index. The stepin date should be one day after the expiry and the cashSettlement 
   * date (usually) 3 working days after expiry
   * @param timeToExpiry the time in years between the trade date and expiry. This should use the same DCC as the curves (ACT365F unless manually changed). 
   * @param initialIndexSize The initial number of names in the index 
   * @param yieldCurve The yield curve 
   * @param indexCurve Pseudo credit curve for the index.
   * @param initialDefaultSettlement The (normalised) value of any defaults that have already occurred (e.g. if two defaults have occurred from an index with
   *  initially 100 entries, and the realised recovery rates are 0.2 and 0.35, the this value is (0.8 + 0.65)/100 )  
   * @param numDefaults The number of defaults that have already occurred 
   * @return The normalised expected default settlement value
   */
  public double defaultAdjustedForwardSpread(final CDSAnalytic fwdStartingCDS, final double timeToExpiry, final int initialIndexSize, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve indexCurve, final double initialDefaultSettlement, final int numDefaults) {
    ArgumentChecker.notNull(fwdStartingCDS, "fwdCDS");
    ArgumentChecker.isTrue(fwdStartingCDS.getEffectiveProtectionStart() >= timeToExpiry, "effective protection start of {} is less than time to expiry of {}. Must provide a forward starting CDS",
        fwdStartingCDS.getEffectiveProtectionStart(), timeToExpiry);
    final double f = (initialIndexSize - numDefaults) / ((double) initialIndexSize);
    final double defSettle = expectedDefaultSettlementValue(initialIndexSize, timeToExpiry, indexCurve, fwdStartingCDS.getLGD(), initialDefaultSettlement, numDefaults);
    final double protLeg = f * _pricer.protectionLeg(fwdStartingCDS, yieldCurve, indexCurve);
    final double ann = f * _pricer.annuity(fwdStartingCDS, yieldCurve, indexCurve);
    return (protLeg + defSettle) / ann;
  }

  /**
   * The normalised expected default settlement value paid on the  exercise settlement date. The actual default settlement is this multiplied by the (initial) 
   * index notional.  
   * @param timeToExpiry Time to expiry 
   * @param intrinsicData credit curves, weights and recovery rates of the intrinsic names
   * @return The normalised expected default settlement value
   */
  public double expectedDefaultSettlementValue(final double timeToExpiry, final IntrinsicIndexDataBundle intrinsicData) {
    ArgumentChecker.notNull(intrinsicData, "intrinsicData");
    final int indexSize = intrinsicData.getIndexSize();
    double d = 0.0; //computed the expected default settlement amount (paid on the  expiry settlement date)
    for (int i = 0; i < indexSize; i++) {
      final double qBar = intrinsicData.isDefaulted(i) ? 1.0 : 1.0 - intrinsicData.getCreditCurve(i).getSurvivalProbability(timeToExpiry);
      d += intrinsicData.getWeight(i) * intrinsicData.getLGD(i) * qBar;
    }
    return d;
  }

  /**
    * The normalised expected default settlement value paid on the exercise settlement date <b>when no defaults have yet occurred</b>.
    *  The actual default settlement is this multiplied by the (initial) 
   * index notional. This calculation assumes an homogeneous pool that can be described by a single index curve.  
   * @param timeToExpiry Time to expiry 
   * @param indexCurve Pseudo credit curve for the index.
   * @param lgd The index Loss Given Default (LGD)
   * @return  The normalised expected default settlement value
   */
  public double expectedDefaultSettlementValue(final double timeToExpiry, final ISDACompliantCreditCurve indexCurve, final double lgd) {
    ArgumentChecker.notNull(indexCurve, "indexCurve");
    ArgumentChecker.isInRangeInclusive(0, 1, lgd);
    final double q = indexCurve.getSurvivalProbability(timeToExpiry);
    final double d = lgd * (1 - q);
    return d;
  }

  /**
   * The normalised expected default settlement value paid on the  exercise settlement date. The actual default settlement is this multiplied by the (initial) 
   * index notional.   This calculation assumes an homogeneous pool that can be described by a single index curve. 
   * @param initialIndexSize Initial index size 
   * @param timeToExpiry Time to expiry 
   * @param indexCurve Pseudo credit curve for the index.
   * @param lgd The index Loss Given Default (LGD)
   * @param initialDefaultSettlement  The (normalised) value of any defaults that have already occurred (e.g. if two defaults have occurred from an index with
   *  initially 100 entries, and the realised recovery rates are 0.2 and 0.35, the this value is (0.8 + 0.65)/100 )  
   * @param numDefaults The number of defaults that have already occurred 
   * @return The normalised expected default settlement value
   */
  public double expectedDefaultSettlementValue(final int initialIndexSize, final double timeToExpiry, final ISDACompliantCreditCurve indexCurve, final double lgd,
      final double initialDefaultSettlement, final int numDefaults) {
    ArgumentChecker.isTrue(initialIndexSize > 1, "initialIndexSize is {}", initialIndexSize);
    ArgumentChecker.notNull(indexCurve, "indexCurve");
    ArgumentChecker.isTrue(numDefaults >= 0, "negative numDefaults");
    ArgumentChecker.isTrue(numDefaults <= initialIndexSize, "More defaults ({}) than size of index ({})", numDefaults, initialIndexSize);
    final double defFrac = numDefaults / ((double) initialIndexSize);
    ArgumentChecker.isInRangeInclusive(0, defFrac, initialDefaultSettlement); //this upper range is if all current defaults have zero recovery 
    ArgumentChecker.isInRangeInclusive(0, 1, lgd);

    final double q = indexCurve.getSurvivalProbability(timeToExpiry);
    final double d = (1 - defFrac) * lgd * (1 - q) + initialDefaultSettlement;
    return d;
  }
}
