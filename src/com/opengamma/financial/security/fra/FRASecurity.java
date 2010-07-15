/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fra;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;

/**
 * 
 */
public class FRASecurity extends FinancialSecurity {
  private static final String SECURITY_TYPE = "FRA";
  private ZonedDateTime _startDate;
  private ZonedDateTime _endDate;

  public FRASecurity(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    super(SECURITY_TYPE);
    Validate.notNull(startDate);
    Validate.notNull(endDate);
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }
    _startDate = startDate;
    _endDate = endDate;
  }

  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  public void setStartDate(final ZonedDateTime startDate) {
    _startDate = startDate;
  }

  public ZonedDateTime getEndDate() {
    return _endDate;
  }

  public void setEndDate(final ZonedDateTime endDate) {
    _endDate = endDate;
  }

  @Override
  public final <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    return visitor.visitFRASecurity(this);
  }

  public <T> T accept(final FRASecurityVisitor<T> visitor) {
    return visitor.visitFRASecurity(this);
  }

}
