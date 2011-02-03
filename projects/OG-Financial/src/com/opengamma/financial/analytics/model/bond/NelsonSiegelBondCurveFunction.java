/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.InstantProvider;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.core.common.Currency;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.NelsonSiegelBondCurveModel;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 */
public class NelsonSiegelBondCurveFunction extends AbstractFunction {
  /** Name of the property type*/
  public static final String PROPERTY_CURVE_CALCULATION_TYPE = "Nelson_Siegel_Bond_Curve";
  /** Name of the property*/
  public static final String PROPERTY_PREFIX = "Nelson-Siegel";
  private static final NelsonSiegelBondCurveModel MODEL = new NelsonSiegelBondCurveModel();
  private static final NonLinearLeastSquare MINIMISER = new NonLinearLeastSquare();
  private final Currency _currency;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;

  public NelsonSiegelBondCurveFunction(final String currencyName) {
    Validate.notNull(currencyName, "currency name");
    _currency = Currency.getInstance(currencyName);
  }

  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _result = new ValueSpecification(ValueRequirementNames.NS_BOND_CURVE, new ComputationTargetSpecification(_currency), createValueProperties().with(PROPERTY_CURVE_CALCULATION_TYPE,
        PROPERTY_PREFIX + "_" + _currency.getISOCode()).get());
    _results = Sets.newHashSet(_result);
  }

  @Override
  public String getShortName() {
    return "NelsonSiegelBondCurveFunction";
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
    return new AbstractInvokingCompiledFunction() {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        return null;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        return ObjectUtils.equals(target.getUniqueId(), _currency.getUniqueId());
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        // all bond YTM
        return null;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        if (canApplyTo(context, target)) {
          return _results;
        }
        return null;
      }

    };
  }

}
