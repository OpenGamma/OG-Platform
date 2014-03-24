/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondyield;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.engine.value.ValueRequirementNames.MARKET_YTM;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.YIELD_METHOD;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.BondAndBondFutureFunctionUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Base class for bond functions that produce analytics directly from the yield.
 * This function works only on {@link Trade}s and can handle all types of {@link BondSecurity}.
 * @param <T> The type of the result
 */
public abstract class BondFromYieldFunction<T> extends AbstractFunction.NonCompiledInvoker {
  /** The value requirement name */
  private final String _valueRequirementName;
  /** The calculator */
  private final InstrumentDerivativeVisitor<Double, T> _calculator;

  /**
   * @param valueRequirementName The value requirement name, not null
   * @param calculator The calculator, not null
   */
  public BondFromYieldFunction(final String valueRequirementName, final InstrumentDerivativeVisitor<Double, T> calculator) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    ArgumentChecker.notNull(calculator, "calculator");
    _valueRequirementName = valueRequirementName;
    _calculator = calculator;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final Double yield = (Double) inputs.getValue(MARKET_YTM);
    final InstrumentDerivative bond = BondAndBondFutureFunctionUtils.getBondOrBondFutureDerivative(executionContext, target, now, inputs);
    final T result = bond.accept(_calculator, yield);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), getResultProperties());
    return Collections.singleton(new ComputedValue(spec, result));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof BondSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Security security = target.getTrade().getSecurity();
    final FinancialSecurity financialSecurity = (FinancialSecurity) target.getTrade().getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<>();
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    requirements.addAll(BondAndBondFutureFunctionUtils.getConversionRequirements(financialSecurity, timeSeriesResolver));
    requirements.add(new ValueRequirement(MARKET_YTM, ComputationTargetSpecification.of(security), ValueProperties.builder().get()));
    return requirements;
  }

  /**
   * Gets the result properties.
   * @return The result properties
   */
  protected ValueProperties getResultProperties() {
    return createValueProperties()
        .with(CALCULATION_METHOD, YIELD_METHOD)
        .get();
  }

}
