/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.future.BondFutureSecurity;

/**
*
* @param <T> The type of data that the calculator needs
*/
public abstract class BondFutureFunction<T> extends AbstractFunction.NonCompiledInvoker {
  private BondFutureSecurityConverter _visitor;

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
    final ZonedDateTime date = ZonedDateTime.now(executionContext.getValuationClock());
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final BondFutureSecurity security = (BondFutureSecurity) target.getSecurity();
    final BondFutureDefinition definition = (BondFutureDefinition) security.accept(_visitor);
    final Double referencePrice = 0.0; // TODO Futures Refactor
    final String[] curveNames = getCurveNames(desiredValue);
    final BondFuture bondFuture = definition.toDerivative(date, referencePrice, curveNames);
    return calculate(security, bondFuture, getData(desiredValue, inputs, target), target);
  }

  protected abstract Set<ComputedValue> calculate(com.opengamma.financial.security.future.BondFutureSecurity security, BondFuture bondFuture, T data, ComputationTarget target);

  protected abstract T getData(ValueRequirement desiredValue, FunctionInputs inputs, ComputationTarget target);

  protected abstract String[] getCurveNames(ValueRequirement desiredValue);

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.BOND_FUTURE_SECURITY;
  }

}
