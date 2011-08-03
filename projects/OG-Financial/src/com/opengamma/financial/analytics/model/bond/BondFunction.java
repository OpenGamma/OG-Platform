/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 * @param <T> The type of data that the calculator needs
 */
public abstract class BondFunction<T> extends AbstractFunction.NonCompiledInvoker {
  /** String indicating that the calculator used curves */
  public static final String FROM_CURVES_METHOD = "FromCurves";
  /** String indicating that the calculator used the clean price */
  public static final String FROM_CLEAN_PRICE_METHOD = "FromCleanPrice";
  /** String indicating that the calculator used the dirty price */
  public static final String FROM_DIRTY_PRICE_METHOD = "FromDirtyPrice";
  /** String indicating that the calculator used the yield */
  public static final String FROM_YIELD_METHOD = "FromYield";
  private final String _creditCurveName;
  private final String _riskFreeCurveName;
  private BondSecurityConverter _visitor;

  public BondFunction(final String creditCurveName, final String riskFreeCurveName) {
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
    _visitor = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime date = executionContext.getValuationClock().zonedDateTime();
    final BondSecurity security = (BondSecurity) target.getSecurity();
    final BondFixedSecurityDefinition definition = (BondFixedSecurityDefinition) security.accept(_visitor);
    final BondFixedSecurity bond = definition.toDerivative(date, _creditCurveName, _riskFreeCurveName);
    return calculate(bond, getData(inputs, target), target);
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
    return target.getSecurity() instanceof BondSecurity;
  }

  protected abstract Set<ComputedValue> calculate(BondFixedSecurity bond, T data, ComputationTarget target);

  protected abstract T getData(FunctionInputs inputs, ComputationTarget target);

  protected String getCreditCurveName() {
    return _creditCurveName;
  }

  protected String getRiskFreeCurveName() {
    return _riskFreeCurveName;
  }
}
