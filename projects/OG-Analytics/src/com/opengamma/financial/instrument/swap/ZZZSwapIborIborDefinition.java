/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.CouponIborSpreadDefinition;

/**
 * 
 */
public class ZZZSwapIborIborDefinition extends ZZZSwapDefinition<CouponIborDefinition, CouponIborSpreadDefinition> {
  /**
   * Constructor of the ibor-ibor swap from its two legs. The first leg has no spread, the second leg has a spread.
   * @param firstLeg The first Ibor leg.
   * @param secondLeg The second Ibor leg.
   */
  public ZZZSwapIborIborDefinition(AnnuityCouponIborDefinition firstLeg, AnnuityCouponIborSpreadDefinition secondLeg) {
    super(firstLeg, secondLeg);
    Validate.isTrue(firstLeg.getCurrency() == secondLeg.getCurrency(), "legs should have the same currency");
  }

  /**
   * The leg with no spread.
   * @return The annuity.
   */
  public AnnuityCouponIborDefinition getLegWithoutSpread() {
    return (AnnuityCouponIborDefinition) getFirstLeg();
  }

  /**
   * The leg with the spread.
   * @return The annuity.
   */
  public AnnuityCouponIborSpreadDefinition getLegWithSpread() {
    return (AnnuityCouponIborSpreadDefinition) getSecondLeg();
  }

}
