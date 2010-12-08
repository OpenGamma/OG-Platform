/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import java.util.List;

import com.opengamma.financial.interestrate.future.definition.BondFuture;

/**
 * 
 */
public abstract class BondFutureCalculator {

  //TODO do we need a list for the delivery dates?
  //TODO bundle for the deliverables data
  public abstract double[] calculate(BondFuture bondFuture, List<Double> deliveryDates, List<Double> cleanPrices, List<Double> accruedInterest, List<Double> repoRates, double futurePrice);
}
