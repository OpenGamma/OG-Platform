/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import com.opengamma.analytics.financial.instrument.FixedReceiveCashFlowVisitor;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class FixedReceiveCashFlowFunction extends FixedCashFlowFunction {
  
  public FixedReceiveCashFlowFunction() {
    super(ValueRequirementNames.FIXED_RECEIVE_CASH_FLOWS, FixedReceiveCashFlowVisitor.getInstance());
  }

}
