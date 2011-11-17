/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;

/**
 * Function to shift a yield curve's market data, implemented using properties and constraints.
 * <p>
 * The shift expression is applied to each of the market data value lines.
 */
public class YieldCurveMarketDataShiftFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveMarketDataShiftFunction.class);

  /**
   * Property to shift a yield curve's market data.
   */
  protected static final String SHIFT = "SHIFT";

  @Override
  public String getShortName() {
    return "YieldCurveMarketDataShift";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> shift = constraints.getValues(SHIFT);
    if ((shift == null) || shift.isEmpty() || constraints.isOptional(SHIFT)) {
      return null;
    }
    final ValueProperties properties = desiredValue.getConstraints().copy().withoutAny(SHIFT).with(SHIFT, "0").withOptional(SHIFT).get();
    return Collections.singleton(new ValueRequirement(desiredValue.getValueName(), desiredValue.getTargetSpecification(), properties));
  }

  private ValueProperties.Builder createValueProperties(final ValueSpecification input) {
    return input.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    final ValueProperties properties = createValueProperties(input).withAny(SHIFT).get();
    return Collections.singleton(new ValueSpecification(input.getValueName(), input.getTargetSpecification(), properties));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue input = inputs.getAllValues().iterator().next();
    final ValueSpecification inputSpec = input.getSpecification();
    final SnapshotDataBundle marketData = (SnapshotDataBundle) input.getValue();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String shift = desiredValue.getConstraint(SHIFT);
    final ValueProperties.Builder properties = createValueProperties(inputSpec).with(SHIFT, shift);
    final OverrideOperationCompiler compiler = OpenGammaExecutionContext.getOverrideOperationCompiler(executionContext);
    if (compiler == null) {
      throw new IllegalStateException("No override operation compiler for " + shift + " in execution context");
    }
    s_logger.debug("Applying {} to {}", shift, marketData);
    final OverrideOperation operation = compiler.compile(shift);
    for (Map.Entry<UniqueId, Double> dataPoint : marketData.getDataPoints().entrySet()) {
      s_logger.debug("Applying to {}", dataPoint);
      final Object result = operation.apply(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, dataPoint.getKey()), dataPoint.getValue());
      s_logger.debug("Got result {}", result);
      if (result instanceof Number) {
        dataPoint.setValue(((Number) result).doubleValue());
      } else {
        s_logger.warn("Result of override operation was not numeric");
      }
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(inputSpec.getValueName(), inputSpec.getTargetSpecification(), properties.get()), marketData));
  }
}
