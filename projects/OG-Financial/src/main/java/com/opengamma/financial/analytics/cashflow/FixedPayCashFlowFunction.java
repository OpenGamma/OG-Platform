/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import com.opengamma.analytics.financial.instrument.FixedPayCashFlowVisitor;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class FixedPayCashFlowFunction extends FixedCashFlowFunction {
  
  public FixedPayCashFlowFunction() {
    super(ValueRequirementNames.FIXED_PAY_CASH_FLOWS, FixedPayCashFlowVisitor.getInstance());
  }
}
