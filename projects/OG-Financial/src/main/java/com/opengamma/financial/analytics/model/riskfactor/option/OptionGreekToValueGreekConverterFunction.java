/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.greeks.Underlying;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.analytics.financial.riskfactor.GreekDataBundle;
import com.opengamma.analytics.financial.riskfactor.GreekToValueGreekConverter;
import com.opengamma.analytics.financial.sensitivity.ValueGreek;
import com.opengamma.analytics.financial.trade.OptionTradeData;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.core.position.Position;
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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.analytics.greeks.AvailableValueGreeks;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class OptionGreekToValueGreekConverterFunction extends AbstractFunction.NonCompiledInvoker {

  private final Function1D<GreekDataBundle, Map<ValueGreek, Double>> _converter = new GreekToValueGreekConverter();
  private final String _requirementName;

  public OptionGreekToValueGreekConverterFunction(final String requirementName) {
    ArgumentChecker.notNull(requirementName, "requirement name");
    _requirementName = requirementName;
  }

  public String getRequirementName() {
    return _requirementName;
  }

  protected ValueProperties getInputConstraint(final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    Set<String> currencies = constraints.getValues(ValuePropertyNames.CURRENCY);
    if ((currencies == null) || currencies.isEmpty()) {
      return ValueProperties.withAny(ValuePropertyNames.CURRENCY).get();
    } else {
      return ValueProperties.with(ValuePropertyNames.CURRENCY, currencies).get();
    }
  }

  // CompiledFunctionDefinition

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    Security sec = target.getPosition().getSecurity();
    if (sec instanceof EquityOptionSecurity || sec instanceof EquitySecurity) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getRequirementName(), target.toSpecification(), createValueProperties().withAny(ValuePropertyNames.CURRENCY).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Security security = position.getSecurity();
    List<UnderlyingType> underlyings;
    Underlying order;
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final String underlyingGreekRequirementName = AvailableValueGreeks.getGreekRequirementNameForValueGreekName(getRequirementName());
    requirements.add(new ValueRequirement(underlyingGreekRequirementName, ComputationTargetType.SECURITY, security.getUniqueId(), getInputConstraint(desiredValue)));
    order = AvailableGreeks.getGreekForValueRequirementName(underlyingGreekRequirementName).getUnderlying();
    if (order == null) {
      throw new UnsupportedOperationException("No available order for configured value greek " + getRequirementName());
    }
    underlyings = order.getUnderlyings();
    if (underlyings.isEmpty()) {
      ;
      // TODO what to do here? will only happen for the price
    } else {
      for (final UnderlyingType underlying : underlyings) {
        try {
          requirements.add(UnderlyingTypeToValueRequirementMapper.getValueRequirement(underlying, security));
        } catch (final Exception e) {
        }
      }
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    final ValueProperties.Builder properties = createValueProperties();
    final String underlyingGreekRequirementName = AvailableValueGreeks.getGreekRequirementNameForValueGreekName(getRequirementName());
    for (final ValueSpecification input : inputs.keySet()) {
      if (underlyingGreekRequirementName.equals(input.getValueName())) {
        properties.with(ValuePropertyNames.CURRENCY, input.getProperty(ValuePropertyNames.CURRENCY));
        break;
      }
    }
    results.add(new ValueSpecification(getRequirementName(), target.toSpecification(), properties.get()));
    return results;
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final Security security = position.getSecurity();
    final GreekResultCollection greekResultCollection = new GreekResultCollection();
    final Map<UnderlyingType, Double> underlyingData = new HashMap<UnderlyingType, Double>();
    Greek greek;
    Underlying order;
    List<UnderlyingType> underlyings;
    final String underlyingGreekRequirementName = AvailableValueGreeks.getGreekRequirementNameForValueGreekName(getRequirementName());
    final Double greekResult = (Double) inputs.getValue(underlyingGreekRequirementName);
    greek = AvailableGreeks.getGreekForValueRequirementName(underlyingGreekRequirementName);
    greekResultCollection.put(greek, greekResult);
    double pointValue = 1.0;
    if (security instanceof EquityOptionSecurity) {
      pointValue = ((EquityOptionSecurity) security).getPointValue();
    }
    final OptionTradeData tradeData = new OptionTradeData(position.getQuantity().doubleValue(), pointValue);
    order = greek.getUnderlying();
    underlyings = order.getUnderlyings();
    for (final UnderlyingType underlying : underlyings) {
      final Double underlyingValue = (Double) inputs.getValue(UnderlyingTypeToValueRequirementMapper.getValueRequirement(underlying, security));
      if (underlyingValue == null) {
        throw new NullPointerException("Could not get value for " + underlying + " for security " + security);
      } else {
        underlyingData.put(underlying, underlyingValue);
      }
    }
    final GreekDataBundle dataBundle = new GreekDataBundle(greekResultCollection, underlyingData, tradeData);
    final Map<ValueGreek, Double> sensitivities = _converter.evaluate(dataBundle);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    ValueGreek valueGreek;
    Double valueGreekResult;
    ValueSpecification resultSpecification;
    ComputedValue resultValue;
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final ValueRequirement dV : desiredValues) {
      valueGreek = AvailableValueGreeks.getValueGreekForValueRequirementName(dV.getValueName());
      valueGreekResult = sensitivities.get(valueGreek);
      resultSpecification = new ValueSpecification(dV.getValueName(), targetSpec, dV.getConstraints());
      resultValue = new ComputedValue(resultSpecification, valueGreekResult);
      results.add(resultValue);
    }
    return results;
  }

}
