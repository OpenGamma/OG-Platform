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
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Produces the reciprocal of a currency conversion rate.
 */
public class CurrencyInversionFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Pattern s_validate = Pattern.compile("[A-Z]{3}_[A-Z]{3}");

  private String _rateLookupIdentifierScheme = CurrencyConversionFunction.DEFAULT_LOOKUP_IDENTIFIER_SCHEME;
  private String _rateLookupValueName = CurrencyConversionFunction.DEFAULT_LOOKUP_VALUE_NAME;

  public void setRateLookupValueName(final String rateLookupValueName) {
    ArgumentChecker.notNull(rateLookupValueName, "rateLookupValueName");
    _rateLookupValueName = rateLookupValueName;
  }

  public String getRateLookupValueName() {
    return _rateLookupValueName;
  }

  public void setRateLookupIdentifierScheme(final String rateLookupIdentifierScheme) {
    ArgumentChecker.notNull(rateLookupIdentifierScheme, "rateLookupIdentifierScheme");
    _rateLookupIdentifierScheme = rateLookupIdentifierScheme;
  }

  public String getRateLookupIdentifierScheme() {
    return _rateLookupIdentifierScheme;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Object rate = inputs.getValue(getRateLookupValueName());
    if (rate == null) {
      throw new IllegalArgumentException("input not provided");
    }
    if (!(rate instanceof Double)) {
      throw new IllegalArgumentException("input is not a double");
    }
    final double inverse = 1 / (double) (Double) rate;
    return Collections.singleton(new ComputedValue(createResultValueSpecification(target), inverse));
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    if (!getRateLookupIdentifierScheme().equals(target.getUniqueId().getScheme())) {
      return false;
    }
    return s_validate.matcher(target.getUniqueId().getValue()).matches();
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final int underscore = target.getUniqueId().getValue().indexOf('_');
    final String numerator = target.getUniqueId().getValue().substring(0, underscore);
    final String denominator = target.getUniqueId().getValue().substring(underscore + 1);
    return Collections.singleton(new ValueRequirement(getRateLookupValueName(), ComputationTargetType.PRIMITIVE, UniqueId.of(getRateLookupIdentifierScheme(), denominator + "_" + numerator)));
  }

  private ValueSpecification createResultValueSpecification(final ComputationTarget target) {
    return new ValueSpecification(getRateLookupValueName(), target.toSpecification(), createValueProperties().get());
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(createResultValueSpecification(target));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

}
