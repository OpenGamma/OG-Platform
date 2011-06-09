/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.core.historicaldata.HistoricalDataSource;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.fixedincome.CashSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.DefinitionConverterDataProvider;
import com.opengamma.financial.analytics.fixedincome.FRASecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.fixedincome.SwapSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;

/**
 * 
 */
public abstract class InterestRateInstrumentFunction extends AbstractFunction.NonCompiledInvoker {
  private static final DefinitionConverterDataProvider DEFINITION_CONVERTER = new DefinitionConverterDataProvider(
      "BLOOMBERG", "PX_LAST"); //TODO this should not be hard-coded
  private final String _valueRequirementName;
  private final String _fundingCurveName;
  private final String _forwardCurveName;
  private FinancialSecurityVisitorAdapter<FixedIncomeInstrumentConverter<?>> _visitor;

  public InterestRateInstrumentFunction(String valueRequirementName, String curveName) { //TODO need to be able to take in any number of curves
    _valueRequirementName = valueRequirementName;
    _fundingCurveName = curveName;
    _forwardCurveName = curveName;
  }

  public InterestRateInstrumentFunction(String valueRequirementName, String fundingCurveName, String forwardCurveName) { //TODO need to be able to take in any number of curves
    _valueRequirementName = valueRequirementName;
    _fundingCurveName = fundingCurveName;
    _forwardCurveName = forwardCurveName;
  }

  @Override
  public void init(FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext
        .getConventionBundleSource(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, conventionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource,
        regionSource);
    _visitor =
        FinancialSecurityVisitorAdapter.<FixedIncomeInstrumentConverter<?>> builder()
            .cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter).swapSecurityVisitor(swapConverter)
            .create();
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target, Set<ValueRequirement> desiredValues) {
    FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalDataSource dataSource = OpenGammaExecutionContext
        .getHistoricalDataSource(executionContext);
    ValueRequirement forwardCurveRequirement = getCurveRequirement(target, _forwardCurveName, null, null);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new NullPointerException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!_forwardCurveName.equals(_fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, _fundingCurveName, null, null);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new NullPointerException("Could not get " + fundingCurveRequirement);
      }
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve
        : (YieldAndDiscountCurve) fundingCurveObject;
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {_forwardCurveName, _fundingCurveName},
        new YieldAndDiscountCurve[] {forwardCurve, fundingCurve});
    FixedIncomeInstrumentConverter<?> definition = security.accept(_visitor);
    InterestRateDerivative derivative = DEFINITION_CONVERTER.convert(security, definition, now, FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security,
        _fundingCurveName, _forwardCurveName), dataSource);
    return getComputedValues(derivative, bundle, security, _forwardCurveName, _fundingCurveName);
  }

  public abstract Set<ComputedValue> getComputedValues(InterestRateDerivative derivative, YieldCurveBundle bundle,
      FinancialSecurity security, String forwardCurveName, String fundingCurveName);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType((FinancialSecurity) target.getSecurity());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target,
      ValueRequirement desiredValue) {
    if (_forwardCurveName.equals(_fundingCurveName)) {
      return Collections.singleton(getCurveRequirement(target, _forwardCurveName, null, null));
    }
    return Sets.newHashSet(getCurveRequirement(target, _forwardCurveName, _forwardCurveName, _fundingCurveName),
        getCurveRequirement(target, _fundingCurveName, _forwardCurveName, _fundingCurveName));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(
        new ValueSpecification(_valueRequirementName, target.toSpecification(),
            FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(
                (FinancialSecurity) target.getSecurity(),
                _fundingCurveName, _forwardCurveName, createValueProperties()))); //TODO how do I get the curve names?
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    return Collections
        .singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(),
            FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(
                (FinancialSecurity) target.getSecurity(),
                _fundingCurveName, _forwardCurveName, createValueProperties())));
  }

  @Override
  public String getShortName() {
    return "InterestRateInstrumentFunction";
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName,
      final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName,
        advisoryForward, advisoryFunding);
  }

}
