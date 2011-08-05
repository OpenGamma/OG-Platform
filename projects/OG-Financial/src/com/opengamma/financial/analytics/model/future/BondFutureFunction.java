/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.future.BondFutureSecurityDefinition;
import com.opengamma.financial.security.future.BondFutureSecurity;

/**
 * 
 * @param <T> The type of data that the calculator needs
 */
public abstract class BondFutureFunction<T> extends AbstractFunction.NonCompiledInvoker {
  private String _creditCurveName;
  private String _riskFreeCurveName;
  private BondFutureSecurityConverter _visitor;

  public BondFutureFunction(final String creditCurveName, final String riskFreeCurveName) {
    Validate.notNull(creditCurveName, "credit curve name");
    Validate.notNull(creditCurveName, "risk-free curve name");
    _creditCurveName = creditCurveName;
    _riskFreeCurveName = riskFreeCurveName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    _visitor = new BondFutureSecurityConverter(securitySource, bondConverter);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime date = executionContext.getValuationClock().zonedDateTime();
    final BondFutureSecurity security = (BondFutureSecurity) target.getSecurity();
    final BondFutureSecurityDefinition definition = (BondFutureSecurityDefinition) security.accept(_visitor);
    final com.opengamma.financial.interestrate.future.definition.BondFutureSecurity bondFuture = definition.toDerivative(date, _creditCurveName, _riskFreeCurveName);
    return calculate(security, bondFuture, getData(inputs, target), target);
  }

  protected abstract Set<ComputedValue> calculate(com.opengamma.financial.security.future.BondFutureSecurity security,
      com.opengamma.financial.interestrate.future.definition.BondFutureSecurity bondFuture, T data, ComputationTarget target);

  protected abstract T getData(FunctionInputs inputs, ComputationTarget target);

  protected String getCreditCurveName() {
    return _creditCurveName;
  }

  protected String getRiskFreeCurveName() {
    return _riskFreeCurveName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof com.opengamma.financial.security.future.BondFutureSecurity;
  }

}
