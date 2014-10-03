/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import org.threeten.bp.LocalDate;

import com.opengamma.util.money.CurrencyAmount;

/**
 * Representation of a cash flow.
 */
public interface CashFlowDetails {
  
  /**
   * Returns the currency and notional of the cash flow.
   * @return the currency and notional of the cash flow
   */
  CurrencyAmount getNotional();

  /**
   * Returns the payment date of the cash flow.
   * @return the payment date of the cash flow.
   */
  LocalDate getPaymentDate();
  
  /**
   * Returns the date at which the cash flow starts accruing.
   * @return the date at which the cash flow starts accruing.
   */
  LocalDate getAccrualStartDate();

  /**
   * Returns the date at which the cash flow stops accruing.
   * @return the date at which the cash flow stops accruing.
   */
  LocalDate getAccrualEndDate();
}
