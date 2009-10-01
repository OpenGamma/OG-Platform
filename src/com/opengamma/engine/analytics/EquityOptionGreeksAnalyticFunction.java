/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.security.Security;

/**
 * 
 *
 * @author jim
 */
public class EquityOptionGreeksAnalyticFunction
extends AbstractAnalyticFunction
implements SecurityAnalyticFunctionDefinition, SecurityAnalyticFunctionInvoker {

  @Override
  public Collection<AnalyticValue<?>> execute(
      FunctionExecutionContext executionContext, AnalyticFunctionInputs inputs,
      Security security) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    return null;
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getPossibleResults(Security security) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getShortName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isApplicableTo(String securityType) {
    // TODO Auto-generated method stub
    return false;
  }

}
