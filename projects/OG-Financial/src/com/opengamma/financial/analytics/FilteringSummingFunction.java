/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Summing function that only considers applicable child values
 */
public abstract class FilteringSummingFunction extends PropertyPreservingFunction {

  private final String _valueName;
  private final Set<String> _aggregationPropertyNames;
  
  protected FilteringSummingFunction(String valueName, Set<String> aggregationPropertyNames) {
    ArgumentChecker.notNull(valueName, "valueName");
    ArgumentChecker.notNull(aggregationPropertyNames, "aggregationPropertyNames");
    _valueName = valueName;
    _aggregationPropertyNames = aggregationPropertyNames;
  }
  
  protected abstract boolean isIncluded(FinancialSecurity security, ValueProperties filterProperties);

  //-------------------------------------------------------------------------
  @Override
  protected String[] getPreservedProperties() {
    Set<String> preserved = new HashSet<String>(Arrays.asList(new String[] {
      ValuePropertyNames.CUBE,
      ValuePropertyNames.CURRENCY,
      ValuePropertyNames.CURVE,
      ValuePropertyNames.CURVE_CURRENCY,
      YieldCurveFunction.PROPERTY_FORWARD_CURVE,
      YieldCurveFunction.PROPERTY_FUNDING_CURVE}));
    preserved.addAll(getAggregationPropertyNames());
    return preserved.toArray(new String[preserved.size()]);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Object currentSum = null;
    for (ComputedValue input : inputs.getAllValues()) {
      Object nextValue = input.getValue();
      currentSum = addValue(currentSum, nextValue);
    }
    ComputedValue computedValue = new ComputedValue(new ValueSpecification(getValueName(), target.toSpecification(), getCompositeValueProperties(inputs.getAllValues())), currentSum);
    return Collections.singleton(computedValue);
  }
  
  protected Object addValue(Object currentTotal, Object value) {
    return SumUtils.addValue(currentTotal, value, getValueName());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    // If any constraints are missing then all child values must be equal in that property. Otherwise, the constraint acts as a filter.
    Set<String> unspecifiedPropertyValues = new HashSet<String>();
    ValueProperties.Builder filterBuilder = ValueProperties.builder();
    for (String propertyName : getAggregationPropertyNames()) {
      Set<String> propertyValues = desiredValue.getConstraints().getValues(propertyName);
      if (propertyValues == null) {
        // All children must match on this property
        unspecifiedPropertyValues.add(propertyName);
      } else {
        // Filter on this property
        filterBuilder.with(propertyName, propertyValues);
      }
    }
    ValueProperties filterConstraints = filterBuilder.get();
    PortfolioNode portfolioNode = target.getPortfolioNode();
    ValueProperties constraints = getInputConstraint(desiredValue);    
    Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (Position position : portfolioNode.getPositions()) {
      FinancialSecurity security = (FinancialSecurity) position.getSecurity();
      if (!isIncluded(security, filterConstraints)) {
        continue;
      }      
      ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getUniqueId());
      ValueRequirement positionRequirement = new ValueRequirement(getValueName(), targetSpec, constraints);
      requirements.add(positionRequirement);
    }
    for (PortfolioNode childNode : portfolioNode.getChildNodes()) {
      // Require value on child node, constrained to match this node
      ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, childNode.getUniqueId());
      ValueRequirement childNodeRequirement = new ValueRequirement(getValueName(), targetSpec, constraints);
      requirements.add(childNodeRequirement);
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueProperties.Builder resultPropertiesBuilder = getResultProperties().copy();
    for (String aggregationProperty : getAggregationPropertyNames()) {
      resultPropertiesBuilder.withoutAny(aggregationProperty).withAny(aggregationProperty);
    }
    return Collections.singleton(getResultSpec(target, resultPropertiesBuilder.get()));
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    // Just ensure mutual compatibility across aggregation properties
    ValueSpecification resultSpec = new ValueSpecification(getValueName(), target.toSpecification(), getCompositeSpecificationProperties(inputs.keySet()));
    return Collections.singleton(resultSpec);
  }
  
  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getType() == ComputationTargetType.PORTFOLIO_NODE;
  }
  
  //-------------------------------------------------------------------------
  private String getValueName() {
    return _valueName;
  }
  
  private Set<String> getAggregationPropertyNames() {
    return _aggregationPropertyNames;
  }
    
  private ValueSpecification getResultSpec(ComputationTarget target, ValueProperties properties) {
    return new ValueSpecification(getValueName(), target.toSpecification(), properties);
  }

}
