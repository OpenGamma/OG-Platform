/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;

/**
 * Produces the reciprocal of a currency conversion rate.
 * <p>
 * This should only be put in the repository when there are functions that will map the conversion requirements directly to market data tickers. Do not use with {@link CurrencyMatrixSourcingFunction}.
 */
public class CurrencyInversionFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Pattern s_validate = Pattern.compile("[A-Z]{3}_[A-Z]{3}");

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object rate = inputs.getValue(CurrencyConversionFunction.RATE_LOOKUP_VALUE_NAME);
    if (rate == null) {
      throw new IllegalArgumentException("input not provided");
    }
    if (!(rate instanceof Double)) {
      throw new IllegalArgumentException("input is not a double");
    }
    final double inverse = 1 / (Double) rate;
    return Collections.singleton(new ComputedValue(createResultValueSpecification(target), inverse));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!CurrencyConversionFunction.RATE_LOOKUP_SCHEME.equals(target.getUniqueId().getScheme())) {
      return false;
    }
    return s_validate.matcher(target.getUniqueId().getValue()).matches();
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final int underscore = target.getUniqueId().getValue().indexOf('_');
    final String numerator = target.getUniqueId().getValue().substring(0, underscore);
    final String denominator = target.getUniqueId().getValue().substring(underscore + 1);
    return Collections.singleton(new ValueRequirement(CurrencyConversionFunction.RATE_LOOKUP_VALUE_NAME, ComputationTargetType.PRIMITIVE, UniqueId.of(CurrencyConversionFunction.RATE_LOOKUP_SCHEME,
        denominator + "_" + numerator)));
  }

  private ValueSpecification createResultValueSpecification(final ComputationTarget target) {
    return new ValueSpecification(CurrencyConversionFunction.RATE_LOOKUP_VALUE_NAME, target.toSpecification(), createValueProperties().get());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(createResultValueSpecification(target));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

}
