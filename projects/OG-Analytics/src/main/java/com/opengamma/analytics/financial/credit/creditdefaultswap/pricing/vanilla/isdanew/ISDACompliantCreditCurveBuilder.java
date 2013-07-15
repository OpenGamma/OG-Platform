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
   * Bootstrapper the credit curve, by making each market CDS in turn have zero clean price 
   * @param cds  The market CDSs - these are the reference instruments used to build the credit curve 
   * @param marketFracSpreads The <b>fractional</b> spreads of the market CDSs    
   * @param yieldCurve The yield (or discount) curve  
   * @return The credit curve 
   */
  ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] cds, final double[] fractionalSpreads, final ISDACompliantYieldCurve yieldCurve);

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
      final double[] fractionalParSpreads, final boolean payAccOnDefault, final Period tenor, StubType stubType, final boolean protectStart, final ISDACompliantDateYieldCurve yieldCurve,
      final double recoveryRate);

}
