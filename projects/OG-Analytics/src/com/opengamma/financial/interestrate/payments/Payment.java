/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import com.opengamma.financial.interestrate.InterestRateDerivative;

/**
 * 
 */
public interface Payment extends InterestRateDerivative {

  double getPaymentTime();

  String getFundingCurveName();

}
