/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collection;
import java.util.Collections;

import com.opengamma.engine.analytics.AbstractPrimitiveAnalyticFunction;
import com.opengamma.engine.analytics.AnalyticFunctionInputs;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueDefinitionImpl;
import com.opengamma.engine.analytics.DoubleAnalyticValue;
import com.opengamma.engine.analytics.FunctionExecutionContext;
import com.opengamma.engine.analytics.PrimitiveAnalyticFunctionDefinition;
import com.opengamma.engine.analytics.PrimitiveAnalyticFunctionInvoker;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */


public class HardCodedCostOfCarryAnalyticFunction<T extends OptionDefinition<?>>
extends AbstractPrimitiveAnalyticFunction
implements PrimitiveAnalyticFunctionDefinition, PrimitiveAnalyticFunctionInvoker {
  
  private final double _costOfCarry;
  
  @SuppressWarnings("unchecked")
  private static final AnalyticValueDefinition<Double> s_resultDefinition = 
    new AnalyticValueDefinitionImpl<Double>(new KeyValuePair<String, Object>("TYPE", "COST_OF_CARRY"));

  public HardCodedCostOfCarryAnalyticFunction(double costOfCarry) {
    _costOfCarry = costOfCarry;
  }
  
  public HardCodedCostOfCarryAnalyticFunction() {
    _costOfCarry = 0.01;
  }
  
  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs() {
    return Collections.<AnalyticValueDefinition<?>>emptySet();
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getPossibleResults() {
    return Collections.<AnalyticValueDefinition<?>>singleton(s_resultDefinition);
  }

  @Override
  public Collection<AnalyticValue<?>> execute(
      FunctionExecutionContext executionContext, AnalyticFunctionInputs inputs) {
    return Collections.<AnalyticValue<?>>singleton(
        new DoubleAnalyticValue(
            s_resultDefinition, _costOfCarry)
    );
  }

  @Override
  public String getShortName() {
    return "HardCodedCostOfCarry";
  }
}
