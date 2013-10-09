/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;

/**
 * Construct a credit curve that is consistent with the ISDA c code - i.e. the credit curve is piecewise constant in the (forward)
 * hazard rates,  and agrees with ISDA (up to numerical round-off) for the same inputs 
 */
public interface ISDACompliantCreditCurveBuilder {

  /**
   * How should any arbitrage in the input data be handled 
   */
  public enum ArbitrageHandling {
    /**
     * If the market data has arbitrage, the curve will still build, but the survival probability will not be monotonically
     * decreasing (equivalently, some forward hazard rates will be negative)
     */
    Ignore,
    /**
     * An exception is throw if an arbitrage is found
     */
    Fail,
    /**
     * If a particular spread implies a negative forward hazard rate, the hazard rate is set to zero, and the calibration 
     * continues. The resultant curve will of course not exactly reprice the input CDSs, but will find new spreads that
     * just avoid arbitrage.   
     */
    ZeroHazardRate
  }

  /**
   * Bootstrapper the credit curve from a single market CDS quote. Obviously the resulting credit (hazard)
   *  curve will be flat.
   * @param calibrationCDS The single market CDS - this is the reference instruments used to build the credit curve 
   * @param marketQuote The market quote of the CDS 
   * @param yieldCurve The yield (or discount) curve  
   * @return The credit curve  
   */
  ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic calibrationCDS, final CDSQuoteConvention marketQuote, final ISDACompliantYieldCurve yieldCurve);

  /**
   * Bootstrapper the credit curve from a set of reference/calibration CDSs with market quotes 
   * @param calibrationCDSs The market CDSs - these are the reference instruments used to build the credit curve 
   * @param marketQuotes The market quotes of the CDSs 
   * @param yieldCurve The yield (or discount) curve 
   * @return The credit curve 
   */
  ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] calibrationCDSs, final CDSQuoteConvention[] marketQuotes, final ISDACompliantYieldCurve yieldCurve);

  /**
   * Bootstrapper the credit curve from a single market CDS quote given as a par spread. Obviously the resulting credit (hazard)
   *  curve will be flat.
   * @param cds  The single market CDS - this is the reference instruments used to build the credit curve 
   * @param parSpread The <b>fractional</b> par spread of the market CDS   
   * @param yieldCurve The yield (or discount) curve  
   * @return The credit curve  
   */
  ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic cds, final double parSpread, final ISDACompliantYieldCurve yieldCurve);

  /**
   * Bootstrapper the credit curve from a single market CDS quote given as points up-front (PUF) and a standard premium.
   * @param cds The single market CDS - this is the reference instruments used to build the credit curve 
   * @param premium The standard premium (coupon) as a fraction (these are 0.01 or 0.05 in North America)
   * @param yieldCurve The yield (or discount) curve
   * @param pointsUpfront points up-front as a fraction of notional 
   * @return The credit curve 
   */
  ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double pointsUpfront);

  /**
   * Bootstrapper the credit curve from a set of reference/calibration CDSs quoted with par spreads. 
   * @param calibrationCDSs  The market CDSs - these are the reference instruments used to build the credit curve 
   * @param parSpreads The <b>fractional</b> par spreads of the market CDSs    
   * @param yieldCurve The yield (or discount) curve  
   * @return The credit curve 
   */
  ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] calibrationCDSs, final double[] parSpreads, final ISDACompliantYieldCurve yieldCurve);

  /**
   * Bootstrapper the credit curve from a set of reference/calibration CDSs quoted with points up-front and standard premiums 
   * @param calibrationCDSs The market CDSs - these are the reference instruments used to build the credit curve 
   * @param premiums The standard premiums (coupons) as fractions (these are 0.01 or 0.05 in North America) 
   * @param yieldCurve  The yield (or discount) curve  
   * @param pointsUpfront points up-front as fractions of notional 
   * @return The credit curve
   */
  ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] calibrationCDSs, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront);

  /**
   * Bootstrapper the credit curve from a single CDS, by making it have zero clean price. Obviously the resulting credit (hazard) curve will be flat.
  * @param today The 'current' date
   * @param stepinDate Date when party assumes ownership. This is normally today + 1 (T+1). Aka assignment date or effective date.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param startDate The protection start date. If protectStart = true, then protections starts at the beginning of the day, otherwise it
   * is at the end.
   * @param endDate The maturity (or end of protection) of  the CDS 
   * @param fractionalParSpread - the (fractional) coupon that makes the CDS worth par (i.e. zero clean price)
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param tenor The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param protectStart Does protection start at the beginning of the day
   * @param yieldCurve Curve from which payments are discounted
   * @param recoveryRate the recovery rate 
   * @return The credit curve
   */
  ISDACompliantCreditCurve calibrateCreditCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate,
      final double fractionalParSpread, final boolean payAccOnDefault, final Period tenor, StubType stubType, final boolean protectStart, final ISDACompliantYieldCurve yieldCurve,
      final double recoveryRate);

  /**
   * Bootstrapper the credit curve, by making each market CDS in turn have zero clean price 
  * @param today The 'current' date
   * @param stepinDate Date when party assumes ownership. This is normally today + 1 (T+1). Aka assignment date or effective date.
   * @param valueDate The valuation date. The date that values are PVed to. Is is normally today + 3 business days.  Aka cash-settle date.
   * @param startDate The protection start date. If protectStart = true, then protections starts at the beginning of the day, otherwise it
   * is at the end.
   * @param endDates The maturities (or end of protection) of each of the CDSs - must be ascending 
   * @param fractionalParSpreads - the (fractional) coupon that makes each CDS worth par (i.e. zero clean price)
   * @param payAccOnDefault Is the accrued premium paid in the event of a default
   * @param tenor The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE
   *  - <b>Note</b> in this code NONE is not allowed
   * @param protectStart Does protection start at the beginning of the day
   * @param yieldCurve Curve from which payments are discounted
   * @param recoveryRate the recovery rate 
   * @return The credit curve
   */
  ISDACompliantCreditCurve calibrateCreditCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate[] endDates,
      final double[] fractionalParSpreads, final boolean payAccOnDefault, final Period tenor, StubType stubType, final boolean protectStart, final ISDACompliantYieldCurve yieldCurve,
      final double recoveryRate);

}
