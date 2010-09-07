/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.interestratefuture;

import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;

/**
 * 
 */
public class InterestRateFutureSecurityToInterestRateFutureConverter {

  public static InterestRateFuture getInterestRateFuture(InterestRateFutureSecurity security, String indexCurveName) {
    return null;
    /*
     * Settlement date = from convention (2 days before fixing date)
     */
    /*
     * Fixing date = 3rd Wednesday of the expiry month (-> no need to adjust date [what happens if it is a holiday?])
     */
    /*
     * Maturity date = 3m from fixing date 
     */
    /*
     * Adjust date 
     */
    /*
     * Year count fraction (index year fraction)
     */
    /*
     * Value year fraction = 0.25 ($) = 0.125 (£) etc
     */
    /*
     * price = 100 - r * 100
     */
    /*
     * curve name
     */
  }
}
