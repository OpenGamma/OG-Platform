/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class MarketQuotePositionFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, 
      Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final Collection<ComputedValue> marketQuotes = inputs.getAllValues();
    final Iterator<ComputedValue> iter = marketQuotes.iterator();    
    final Double marketQuote = (Double) iter.next().getValue();
    while (iter.hasNext()) {
      final Double test = (Double) iter.next().getValue();
      if (Double.compare(marketQuote, test) != 0) {
        throw new OpenGammaRuntimeException("Have different values for market quote in the same position");
      }
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.MARKET_QUOTE, target.toSpecification(), properties), marketQuote));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    final Position position = target.getPosition();
    final Collection<Trade> trades = position.getTrades();
    for (Trade trade : trades) {
      Security security = trade.getSecurity();
      if (security instanceof InterestRateFutureSecurity || security instanceof DeliverableSwapFutureSecurity) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();    
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.MARKET_QUOTE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final ValueProperties properties = desiredValue.getConstraints();
    final Position position = target.getPosition();
    final Collection<Trade> trades = position.getTrades();
    final Set<ValueRequirement> requirements = new HashSet<>();
    for (Trade trade : trades) {
      requirements.add(new ValueRequirement(ValueRequirementNames.MARKET_QUOTE, ComputationTargetSpecification.of(trade), properties));
    }
    return requirements;
  }

}
