/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.collect.Sets;
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
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.BondSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 */
public class BondCouponPaymentDiaryFunction extends NonCompiledInvoker {

  public BondCouponPaymentDiaryFunction() {
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES, target.getSecurity()), getUniqueId()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet();
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

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final BondSecurity security = (BondSecurity) target.getSecurity();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext
        .getConventionBundleSource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final BondSecurityConverter visitor = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    BondDefinition bond = (BondDefinition) security.accept(visitor);
    final double[] coupons = bond.getCoupons();
    final double notional = security.getRedemptionValue();
    final int n = bond.getCoupons().length;
    final LocalDate[] couponPaymentDates = (LocalDate[]) ArrayUtils.subarray(bond.getSettlementDates(), 1, n + 1);
    final Object[] labels = new Object[n];
    final double[] payments = new double[n];
    for (int i = 0; i < n; i++) {
      payments[i] = coupons[i] * notional;
      labels[i] = couponPaymentDates[i].toString();
    }
    payments[n - 1] += notional;
    final LocalDateLabelledMatrix1D matrix = new LocalDateLabelledMatrix1D(couponPaymentDates, labels, payments);
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES, security), getUniqueId()), matrix));
  }

}
