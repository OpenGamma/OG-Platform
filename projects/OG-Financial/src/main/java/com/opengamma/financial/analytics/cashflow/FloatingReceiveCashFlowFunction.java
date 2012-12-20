/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import com.opengamma.analytics.financial.instrument.FloatingReceiveCashFlowVisitor;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class FloatingReceiveCashFlowFunction extends FloatingCashFlowFunction {
  
  public FloatingReceiveCashFlowFunction() {
    super(ValueRequirementNames.FLOATING_RECEIVE_CASH_FLOWS, FloatingReceiveCashFlowVisitor.getInstance());
  }
  
}
