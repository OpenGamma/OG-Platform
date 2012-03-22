/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.deposit;

import javax.time.calendar.ZonedDateTime;

import org.joda.beans.PropertyDefinition;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SimpleZeroDepositSecurity extends FinancialSecurity {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /** The security type */
  public static final String SECURITY_TYPE = "SIMPLE_ZERO_DEPOSIT";

  /** The currency. */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;

  /** The start date. */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _startDate;

  /** The end date. */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _endDate;

  /** The rate. */
  @PropertyDefinition
  private double _rate;

  /** The region. */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _region;

  SimpleZeroDepositSecurity() {
    super();
  }

  SimpleZeroDepositSecurity(final Currency currency, final ZonedDateTime startDate, final ZonedDateTime endDate, final double rate, final ExternalId region) {
    super(SECURITY_TYPE);
  }
}
