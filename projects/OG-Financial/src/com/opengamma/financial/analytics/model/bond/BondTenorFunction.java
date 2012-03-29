/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class BondTenorFunction extends NonCompiledInvoker {

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

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final BondSecurity security = (BondSecurity) target.getSecurity();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext
        .getConventionBundleSource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final BondSecurityConverter visitor = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFixedSecurityDefinition bond = (BondFixedSecurityDefinition) security.accept(visitor);
    final ZonedDateTime firstCouponDate = bond.getCoupon().getNthPayment(0).getAccrualStartDate();
    final ZonedDateTime lastCouponDate = bond.getCoupon().getNthPayment(bond.getCoupon().getNumberOfPayments() - 1).getPaymentDate();
    final double t = DateUtils.getDaysBetween(firstCouponDate, lastCouponDate) / 365;
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.BOND_TENOR, security), getUniqueId());
    return Sets.newHashSet(new ComputedValue(specification, t));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Sets.newHashSet();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.BOND_TENOR, target.getSecurity()), getUniqueId()));
  }

}
