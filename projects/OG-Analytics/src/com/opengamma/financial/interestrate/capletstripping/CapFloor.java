/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.capletstripping;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;

/**
 * 
 */
public class CapFloor extends GenericAnnuity<CapFloorIbor> {

  /**
   * @param payments
   */
  public CapFloor(CapFloorIbor[] payments) {
  //TODO check inputs 
    super(payments);
  }
  
  public double getStrike() {
    return getNthPayment(0).getStrike();
  }

}
