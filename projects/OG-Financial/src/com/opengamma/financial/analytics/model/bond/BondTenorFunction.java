/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.bond.BondSecurityToBondDefinitionConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class BondTenorFunction extends NonCompiledInvoker {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.POSITION) {
      final Security security = target.getPosition().getSecurity();
      return security instanceof BondSecurity;
    }
    return false;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final BondDefinition bond = new BondSecurityToBondDefinitionConverter(holidaySource, conventionSource).getBond((BondSecurity) position.getSecurity(), true);
    final LocalDate[] nominalDates = bond.getNominalDates();
    final double t = DateUtil.getDaysBetween(nominalDates[0], nominalDates[nominalDates.length - 1]) / 365;
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.BOND_TENOR, position), getUniqueId());
    return Sets.newHashSet(new ComputedValue(specification, t));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Sets.newHashSet();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.BOND_TENOR, target.getPosition()), getUniqueId()));
    }
    return null;
  }

}
