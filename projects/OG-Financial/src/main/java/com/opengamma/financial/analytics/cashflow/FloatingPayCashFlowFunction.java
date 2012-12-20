/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import com.opengamma.analytics.financial.instrument.FloatingPayCashFlowVisitor;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class FloatingPayCashFlowFunction extends FloatingCashFlowFunction {
  
  public FloatingPayCashFlowFunction() {
    super(ValueRequirementNames.FLOATING_PAY_CASH_FLOWS, FloatingPayCashFlowVisitor.getInstance());
  }
  
}
