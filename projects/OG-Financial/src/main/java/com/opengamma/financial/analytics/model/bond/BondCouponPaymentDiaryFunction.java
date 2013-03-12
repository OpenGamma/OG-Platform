/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 */
public class BondCouponPaymentDiaryFunction extends NonCompiledInvoker {
  private BondSecurityConverter _visitor;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    _visitor = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.BOND_SECURITY;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final BondSecurity security = (BondSecurity) target.getSecurity();
    final BondFixedSecurityDefinition bond = (BondFixedSecurityDefinition) security.accept(_visitor);
    final AnnuityCouponFixedDefinition coupons = bond.getCoupons();
    final int n = coupons.getNumberOfPayments();
    final LocalDate[] dates = new LocalDate[n];
    final double[] payments = new double[n];
    for (int i = 0; i < n; i++) {
      final CouponFixedDefinition coupon = coupons.getNthPayment(i);
      dates[i] = coupon.getPaymentDate().toLocalDate();
      payments[i] = coupon.getAmount() * coupon.getNotional();
    }
    payments[n - 1] += coupons.getNthPayment(n - 1).getNotional();
    final LocalDateLabelledMatrix1D matrix = new LocalDateLabelledMatrix1D(dates, payments);
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES, target.toSpecification(), createValueProperties().get()), matrix));
  }

}
