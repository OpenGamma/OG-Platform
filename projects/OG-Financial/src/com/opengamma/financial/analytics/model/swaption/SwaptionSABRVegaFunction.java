/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SwaptionSABRVegaFunction extends SwaptionSABRFunction {

  public SwaptionSABRVegaFunction(final Currency currency, final String definitionName, final boolean useSABRExtrapolation, String forwardCurveName, String fundingCurveName) {
    super(currency, definitionName, useSABRExtrapolation, forwardCurveName, fundingCurveName);
  }

  public SwaptionSABRVegaFunction(final String currency, final String definitionName, final String useSABRExtrapolation, String forwardCurveName, String fundingCurveName) {
    super(currency, definitionName, useSABRExtrapolation, forwardCurveName, fundingCurveName);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return null;
  }

}
