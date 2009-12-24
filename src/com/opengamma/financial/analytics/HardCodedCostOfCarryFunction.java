/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collection;
import java.util.Collections;

import com.opengamma.engine.function.AbstractPrimitiveFunction;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.PrimitiveFunctionDefinition;
import com.opengamma.engine.function.PrimitiveFunctionInvoker;
import com.opengamma.engine.value.AnalyticValueDefinition;
import com.opengamma.engine.value.AnalyticValueDefinitionImpl;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.DoubleComputedValue;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */


public class HardCodedCostOfCarryFunction<T extends OptionDefinition>
extends AbstractPrimitiveFunction
implements PrimitiveFunctionDefinition, PrimitiveFunctionInvoker {
  
  private final double _costOfCarry;
  
  @SuppressWarnings("unchecked")
  private static final AnalyticValueDefinition<Double> s_resultDefinition = 
    new AnalyticValueDefinitionImpl<Double>(new KeyValuePair<String, Object>("TYPE", "COST_OF_CARRY"));

  public HardCodedCostOfCarryFunction(double costOfCarry) {
    _costOfCarry = costOfCarry;
  }
  
  public HardCodedCostOfCarryFunction() {
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
  public Collection<ComputedValue<?>> execute(
      FunctionExecutionContext executionContext, FunctionInputs inputs) {
    return Collections.<ComputedValue<?>>singleton(
        new DoubleComputedValue(
            s_resultDefinition, _costOfCarry)
    );
  }

  @Override
  public String getShortName() {
    return "HardCodedCostOfCarry";
  }
}
