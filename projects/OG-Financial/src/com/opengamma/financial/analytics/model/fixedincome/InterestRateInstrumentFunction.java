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

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.BondFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class InterestRateInstrumentFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * 
   */
  protected static final String RESULT_PROPERTY_TYPE = "Type";

  /**
   * 
   */
  protected static final String TYPE_FORWARD = "Forward";

  /**
   * 
   */
  protected static final String TYPE_FUNDING = "Funding";

  private FixedIncomeConverterDataProvider _definitionConverter;
  private final String _valueRequirementName;
  private FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> _visitor;

  public InterestRateInstrumentFunction(final String valueRequirementName) {
    Validate.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  protected String getValueRequirementName() {
    return _valueRequirementName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext
        .getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, conventionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource,
        regionSource);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    final FutureSecurityConverter futureConverter = new FutureSecurityConverter(bondFutureConverter, irFutureConverter);
    _visitor =
        FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
            .cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter).swapSecurityVisitor(swapConverter)
            .futureSecurityVisitor(futureConverter)
            .bondSecurityVisitor(bondConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType((FinancialSecurity) target.getSecurity());
  }

  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties();
    FixedIncomeInstrumentCurveExposureHelper.valuePropertiesForSecurity((FinancialSecurity) target.getSecurity(), properties);
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties(target);
    properties.withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  // REVIEW 2011-11-25 andrew -- The property for the curve name should really be NAME when applied to the curve object;
  // it should only become CURVE when applied to something using it (like this).

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForwardCurveName, final String advisoryFundingCurveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    if (advisoryForwardCurveName != null) {
      properties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, advisoryForwardCurveName);
    }
    if (advisoryFundingCurveName != null) {
      properties.with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, advisoryFundingCurveName);
    }
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> forwardCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if ((forwardCurves == null) || (fundingCurves == null) || (forwardCurves.size() != 1) || (fundingCurves.size() != 1)) {
      // Can't support an unbound request; an injection function must be used (or declare all as optional and use [PLAT-1771])
      return null;
    }
    final String forwardCurve = forwardCurves.iterator().next();
    final String fundingCurve = fundingCurves.iterator().next();
    if (forwardCurve.equals(fundingCurve)) {
      return Collections.singleton(getCurveRequirement(target, forwardCurve, null, null));
    } else {
      return Sets.newHashSet(getCurveRequirement(target, forwardCurve, forwardCurve, fundingCurve), getCurveRequirement(target, fundingCurve, forwardCurve, fundingCurve));
    }
  }

  protected Set<ValueSpecification> getResults(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    final ValueProperties.Builder properties = createValueProperties(target).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE,
        fundingCurveName);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() == 1) {
      final String curveName = inputs.keySet().iterator().next().getProperty(ValuePropertyNames.CURVE);
      return getResults(target, curveName, curveName);
    } else {
      assert inputs.size() == 2;
      // Only need to check one; the advisory forward/funding will be correct on either
      final ValueSpecification spec = inputs.keySet().iterator().next();
      final String forwardCurveName = spec.getProperty(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      final String fundingCurveName = spec.getProperty(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      return getResults(target, forwardCurveName, fundingCurveName);
    }
  }

  @Override
  public String getShortName() {
    return "InterestRateInstrumentFunction";
  }

  public abstract Set<ComputedValue> getComputedValues(InstrumentDerivative derivative, YieldCurveBundle bundle, FinancialSecurity security, ComputationTarget target, String forwardCurveName,
      String fundingCurveName);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, fundingCurveName, null, null);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve
        : (YieldAndDiscountCurve) fundingCurveObject;
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {fundingCurveName, forwardCurveName }, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve });
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now, FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security,
        fundingCurveName, forwardCurveName), dataSource);
    return getComputedValues(derivative, bundle, security, target, forwardCurveName, fundingCurveName);
  }

  protected ValueSpecification getResultSpec(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), createValueProperties(target).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get());
  }

}
