/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
