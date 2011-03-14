/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.bond.BondSecurityToBondDefinitionConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 */
public abstract class BondFunction extends NonCompiledInvoker {
  private final String _bondCurveName = "BondCurve";
  private final String _requirementName;

  public BondFunction(final String requirementName) {
    Validate.notNull(requirementName, "requirementName");
    _requirementName = requirementName;
  }

  public String getRequirementName() {
    return _requirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final BondSecurity security = (BondSecurity) target.getSecurity();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueId());
    final Object value = inputs.getValue(requirement);
    if (value == null) {
      throw new NullPointerException("Could not get " + requirement);
    }
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final BondDefinition bond = new BondSecurityToBondDefinitionConverter(holidaySource, conventionSource).getBond(security, true);
    return getComputedValues(executionContext, security.getCurrency(), security, bond, value, now.toLocalDate(), _bondCurveName);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, target.getSecurity().getUniqueId()));
    }
    return null;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY) {
      final Security security = target.getSecurity();
      return security instanceof BondSecurity;
    }
    return false;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  protected CurrencyUnit getCurrencyForTarget(final ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getSecurity();
    return bond.getCurrency();
  }

  protected abstract Set<ComputedValue> getComputedValues(FunctionExecutionContext context, CurrencyUnit currency, Security security, BondDefinition bond, Object value, LocalDate now,
      String yieldCurveName);

}
