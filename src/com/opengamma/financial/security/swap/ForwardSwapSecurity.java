/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

/**
 * 
 *
 * @author jim
 */
public class ForwardSwapSecurity extends SwapSecurity {

  private ZonedDateTime _forwardStartDate;

  /**
   * @param effectiveDate the 'effective' or 'value' date
   * @param maturityDate the 'maturity' or 'termination' date
   * @param counterparty the counterparty
   * @param payLeg the pay leg
   * @param receiveLeg the receive leg
   * @param forwardStartDate the start date of the forward swap
   */
  public ForwardSwapSecurity(ZonedDateTime effectiveDate, ZonedDateTime maturityDate, String counterparty,
      SwapLeg payLeg, SwapLeg receiveLeg, ZonedDateTime forwardStartDate) {
    super(effectiveDate, maturityDate, counterparty, payLeg, receiveLeg);
    
    Validate.notNull(forwardStartDate);
    
    if (forwardStartDate.isBefore(effectiveDate)) {
      throw new IllegalArgumentException("Forward start date cannot be before effective date");
    }
    if (forwardStartDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Forward start date cannot be after the maturity date");
    }
    _forwardStartDate = forwardStartDate;
  }

  public ZonedDateTime getForwardStartDate() {
    return _forwardStartDate;
  }
}
