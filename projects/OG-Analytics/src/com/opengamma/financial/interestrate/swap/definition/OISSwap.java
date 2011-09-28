/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;


/**
 * 
 */
public class OISSwap extends Swap<CouponFixed, CouponOIS> {

  /**
   * @param firstLeg
   * @param secondLeg
   */
  public OISSwap(GenericAnnuity<CouponFixed> firstLeg, GenericAnnuity<CouponOIS> secondLeg) {
    super(firstLeg, secondLeg);
  }

}
