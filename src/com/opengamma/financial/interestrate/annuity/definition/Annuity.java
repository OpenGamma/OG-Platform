/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

/**
 * 
 */
public interface Annuity {

  double[] getPaymentTimes();

  double[] getYearFractions();

  int getNumberOfPayments();

  double getNotional();

}
