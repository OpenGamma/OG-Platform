/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.core.common.Currency;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.bond.BondSecurityToBondConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 */
public abstract class BondFunction extends NonCompiledInvoker {
  private final String _bondCurveName = "BondCurve";
  private final String _requirementName;
  private final String _fieldName;

  public BondFunction(final String requirementName, final String fieldName) {
    Validate.notNull(requirementName, "requirementName");
    _requirementName = requirementName;
    _fieldName = fieldName;
  }

  public String getRequirementName() {
    return _requirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final BondSecurity security = (BondSecurity) position.getSecurity();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueId());
    final Object value = inputs.getValue(requirement);
    if (value == null) {
      throw new NullPointerException("Could not get " + requirement);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //    final LocalDate closeOfDay = now.minusDays(1).toLocalDate();
    //    final LocalDate startDate = now.minusDays(7).toLocalDate();
    //    final HistoricalDataSource historicalDataSource = OpenGammaExecutionContext.getHistoricalDataSource(executionContext);
    //    final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = historicalDataSource.getHistoricalData(security.getIdentifiers(), "BLOOMBERG", "CMPL", _fieldName,
    //            startDate, true, closeOfDay, false);
    //    if (tsPair == null) {
    //      throw new NullPointerException("Could not get identifier / price series pair for security " + security);
    //    }
    //    final DoubleTimeSeries<?> ts = tsPair.getSecond();
    //    if (ts == null) {
    //      throw new NullPointerException("Could not get ts for security " + security);
    //    }
    //    final double value = ts.getLatestValue();
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final Bond bond = new BondSecurityToBondConverter(holidaySource, conventionSource).getBond(security, _bondCurveName, now);
    return getComputedValues(position, bond, value);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, target.getPosition().getSecurity().getUniqueId()));
      //return Collections.emptySet();
    }
    return null;
  }

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

  protected Currency getCurrencyForTarget(final ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getPosition().getSecurity();
    return bond.getCurrency();
  }

  protected abstract Set<ComputedValue> getComputedValues(Position position, Bond bond, Object value);

}
