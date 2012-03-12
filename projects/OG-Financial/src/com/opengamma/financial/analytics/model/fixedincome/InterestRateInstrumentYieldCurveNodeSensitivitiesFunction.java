/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.InterpolatedYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateInstrumentYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * The value name calculated by this function.
   */
  public static final String VALUE_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;

  private static final String RESULT_PROPERTY_TYPE = "Type";
  private static final String TYPE_FORWARD = "Forward";
  private static final String TYPE_FUNDING = "Funding";

  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  // TODO: This will be hit for a curve definition on each calculation cycle, so it really needs to cache stuff rather than do any I/O
  private FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> _visitor;
  private FixedIncomeConverterDataProvider _definitionConverter;
  private final String _curveCalculationType;

  public InterestRateInstrumentYieldCurveNodeSensitivitiesFunction(final String curveCalculationType) {
    Validate.notNull(curveCalculationType, "curve calculation type");
    Validate.isTrue(
        curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING) || curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)
        || curveCalculationType.equals(InterpolatedYieldCurveFunction.CALCULATION_METHOD_NAME), "Did not recognise curve calculation type " + curveCalculationType);
    _curveCalculationType = curveCalculationType;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter();
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    final FutureSecurityConverter futureConverter = new FutureSecurityConverter(bondFutureConverter, irFutureConverter);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter).swapSecurityVisitor(swapConverter)
        .futureSecurityVisitor(futureConverter).bondSecurityVisitor(bondConverter).create();
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
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    //TODO remove this when we've checked that removing IR futures from the fixed income instrument types
    // doesn't break curves
    if (target.getSecurity() instanceof InterestRateFutureSecurity) {
      return false;
    }
    if (security instanceof SwapSecurity) {
      final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
      return type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD
          || type == InterestRateInstrumentType.SWAP_IBOR_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_OIS;
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType(security);
  }

  private ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final Security security = target.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationType)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    return properties;
  }

  private ValueProperties.Builder createValueProperties(final ComputationTarget target, final String curveName, final String forwardCurveName,
      final String fundingCurveName) {
    final Security security = target.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationType)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName);
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties(target);
    return Collections.singleton(new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> forwardCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if ((forwardCurves == null) || (fundingCurves == null) || (forwardCurves.size() != 1) || (fundingCurves.size() != 1)) {
      // Can't support an unbound request; an injection function must be used (or declare all as optional and use [PLAT-1771])
      return null;
    }
    // TODO: if "CURVE" is specified, check that it is one of the forward/funding curve names
    final String forwardCurve = forwardCurves.iterator().next();
    final String fundingCurve = fundingCurves.iterator().next();
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(6);
    if (_curveCalculationType.equals(InterpolatedYieldCurveFunction.CALCULATION_METHOD_NAME)) {
      requirements.add(getCurveRequirement(target, forwardCurve, forwardCurve, forwardCurve));
    } else {
      if (forwardCurve.equals(fundingCurve)) {
        requirements.add(getCurveRequirement(target, forwardCurve, forwardCurve, forwardCurve));
        requirements.add(getJacobianRequirement(target, forwardCurve, forwardCurve));
        requirements.add(getCurveSpecRequirement(target, forwardCurve));
      } else {
        requirements.add(getCurveRequirement(target, forwardCurve, forwardCurve, fundingCurve));
        requirements.add(getCurveRequirement(target, fundingCurve, forwardCurve, fundingCurve));
        requirements.add(getJacobianRequirement(target, forwardCurve, fundingCurve));
        requirements.add(getCurveSpecRequirement(target, forwardCurve));
        requirements.add(getCurveSpecRequirement(target, fundingCurve));
      }
      if (_curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
        requirements.add(getCouponSensitivityRequirement(target, forwardCurve, fundingCurve));
      }
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String forwardCurveName = null;
    String fundingCurveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.YIELD_CURVE_JACOBIAN.equals(input.getKey().getValueName())) {
        if (forwardCurveName != null || fundingCurveName != null) {
          throw new IllegalStateException("ValueRequirementNames.YIELD_CURVE_JACOBIAN forwardCurveName/fundingCurveName must not be defined twice: " + inputs);
        }
        forwardCurveName = input.getKey().getProperty(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
        fundingCurveName = input.getKey().getProperty(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      }
    }
    if (forwardCurveName == null) {
      throw new IllegalStateException("ValueRequirementNames.YIELD_CURVE_JACOBIAN forwardCurveName must not be null: " + inputs);
    }
    if (fundingCurveName == null) {
      throw new IllegalStateException("ValueRequirementNames.YIELD_CURVE_JACOBIAN fundingCurveName must not be null: " + inputs);
    }
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    if (_curveCalculationType.equals(InterpolatedYieldCurveFunction.CALCULATION_METHOD_NAME)) {
      return Collections.singleton(new ValueSpecification(VALUE_REQUIREMENT, targetSpec, createValueProperties(target, forwardCurveName, forwardCurveName,
          fundingCurveName).get()));
    } else {
      if (forwardCurveName.equals(fundingCurveName)) {
        return Collections.singleton(new ValueSpecification(VALUE_REQUIREMENT, targetSpec, createValueProperties(target, forwardCurveName, forwardCurveName,
            fundingCurveName).get()));
      } else {
        return Sets.newHashSet(new ValueSpecification(VALUE_REQUIREMENT, targetSpec, createValueProperties(target, forwardCurveName, forwardCurveName, fundingCurveName).get()),
            new ValueSpecification(VALUE_REQUIREMENT, targetSpec, createValueProperties(target, fundingCurveName, forwardCurveName, fundingCurveName).get()));
      }
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final String forwardCurveName = constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE).iterator().next();
    final String fundingCurveName = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE).iterator().next();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    final ValueRequirement forwardCurveSpecRequirement = getCurveSpecRequirement(target, forwardCurveName);
    final Object forwardCurveSpecObject = inputs.getValue(forwardCurveSpecRequirement);
    if (forwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveSpecRequirement);
    }
    Object fundingCurveSpecObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, fundingCurveName, null, null);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
      final ValueRequirement fundingCurveSpecRequirement = getCurveSpecRequirement(target, fundingCurveName);
      fundingCurveSpecObject = inputs.getValue(fundingCurveSpecRequirement);
      if (fundingCurveSpecObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveSpecRequirement);
      }
    }
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve : (YieldAndDiscountCurve) fundingCurveObject;
    final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) forwardCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpec = fundingCurveSpecObject == null ? forwardCurveSpec
        : (InterpolatedYieldCurveSpecificationWithSecurities) fundingCurveSpecObject;
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now,
        FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, fundingCurveName, forwardCurveName), dataSource);
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    interpolatedCurves.put(fundingCurveName, fundingCurve);
    interpolatedCurves.put(forwardCurveName, forwardCurve);
    final YieldCurveBundle bundle = new YieldCurveBundle(interpolatedCurves);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivitiesForCurves;
    if (_curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING)) {
      sensitivitiesForCurves = CALCULATOR.calculateFromParRate(derivative, null, bundle, jacobian, PresentValueNodeSensitivityCalculator.getDefaultInstance());
    } else if (_curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
      final Object couponSensitivityObject = inputs.getValue(getCouponSensitivityRequirement(target, forwardCurveName, fundingCurveName));
      if (couponSensitivityObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
      sensitivitiesForCurves = CALCULATOR.calculateFromPresentValue(derivative, null, bundle, couponSensitivity, jacobian, PresentValueNodeSensitivityCalculator.getDefaultInstance());
    } else {
      sensitivitiesForCurves = CALCULATOR.calculateFromSimpleInterpolatedCurve(derivative, bundle, PresentValueNodeSensitivityCalculator.getDefaultInstance());
    }
    final ValueProperties.Builder forwardCurveProperties = createValueProperties(target, forwardCurveName, forwardCurveName, fundingCurveName);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueSpecification forwardResultSpec = new ValueSpecification(VALUE_REQUIREMENT, targetSpec, forwardCurveProperties.get());
    if (fundingCurveName.equals(forwardCurveName)) {
      return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(forwardCurveName, bundle, sensitivitiesForCurves, forwardCurveSpec, forwardResultSpec);
    }
    final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs = new HashMap<String, InterpolatedYieldCurveSpecificationWithSecurities>();
    final ValueProperties.Builder fundingCurveProperties = createValueProperties(target, fundingCurveName, forwardCurveName, fundingCurveName);
    curveSpecs.put(forwardCurveName, forwardCurveSpec);
    curveSpecs.put(fundingCurveName, fundingCurveSpec);
    final ValueSpecification fundingResultSpec = new ValueSpecification(VALUE_REQUIREMENT, targetSpec, fundingCurveProperties.get());
    return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForMultipleCurves(forwardCurveName, fundingCurveName, forwardResultSpec, fundingResultSpec, bundle, sensitivitiesForCurves, curveSpecs);
  }

  @Override
  public String getShortName() {
    return "InterestRateInstrumentYieldCurveNodeSensitivitiesFunction";
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForwardCurve, final String advisoryFundingCurve) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    if (advisoryForwardCurve != null) {
      properties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, advisoryForwardCurve);
    }
    if (advisoryFundingCurve != null) {
      properties.with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, advisoryFundingCurve);
    }
    properties.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationType);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  protected ValueRequirement getJacobianRequirement(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    return YieldCurveFunction.getJacobianRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), forwardCurveName, fundingCurveName, _curveCalculationType);
  }

  protected ValueRequirement getCouponSensitivityRequirement(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    return YieldCurveFunction.getCouponSensitivityRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), forwardCurveName, fundingCurveName);
  }

  protected ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

}
