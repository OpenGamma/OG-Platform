/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import com.opengamma.financial.interestrate.InterestRateDerivative;

/**
 * 
 */
public interface Annuity extends InterestRateDerivative {

  double[] getPaymentTimes();

  double[] getYearFractions();

  double getNotional();

  int getNumberOfPayments();

  String getFundingCurveName();

  Annuity withZeroSpread();

  Annuity withUnitCoupons();

}
