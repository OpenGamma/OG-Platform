/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

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
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateInstrumentYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  // TODO: This will be hit for a curve definition on each calculation cycle, so it really needs to cache stuff rather than do any I/O
  private FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> _visitor;
  private FixedIncomeConverterDataProvider _definitionConverter;
  private final String _curveCalculationType;
  private String _forwardCurveName;
  private String _fundingCurveName;

  public InterestRateInstrumentYieldCurveNodeSensitivitiesFunction(final String forwardCurveName, final String fundingCurveName, final String curveCalculationType) {
    Validate.notNull(curveCalculationType, "curve calculation type");
    Validate.isTrue(curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING) ||
                    curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING),
                    "Did not recognise curve calculation type " + curveCalculationType);
    Validate.notNull(forwardCurveName, "forward curve name");
    Validate.notNull(fundingCurveName, "funding curve name");
    _forwardCurveName = forwardCurveName;
    _fundingCurveName = fundingCurveName;
    _curveCalculationType = curveCalculationType;
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
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, _forwardCurveName);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    final ValueRequirement forwardCurveSpecRequirement = getCurveSpecRequirement(target, _forwardCurveName);
    final Object forwardCurveSpecObject = inputs.getValue(forwardCurveSpecRequirement);
    if (forwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveSpecRequirement);
    }
    Object fundingCurveSpecObject = null;
    if (!_forwardCurveName.equals(_fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, _fundingCurveName);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
      final ValueRequirement fundingCurveSpecRequirement = getCurveSpecRequirement(target, _fundingCurveName);
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
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve
        : (YieldAndDiscountCurve) fundingCurveObject;
    final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) forwardCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpec = fundingCurveSpecObject == null ? forwardCurveSpec
        : (InterpolatedYieldCurveSpecificationWithSecurities) fundingCurveSpecObject;
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now,
        FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security,
            _fundingCurveName, _forwardCurveName), dataSource);
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    interpolatedCurves.put(_fundingCurveName, fundingCurve);
    interpolatedCurves.put(_forwardCurveName, forwardCurve);
    final YieldCurveBundle bundle = new YieldCurveBundle(interpolatedCurves);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivitiesForCurves;
    if (_curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING)) {
      sensitivitiesForCurves = CALCULATOR.calculateFromParRate(derivative, null, bundle, jacobian, PresentValueNodeSensitivityCalculator.getDefaultInstance());
    } else {
      final Object couponSensitivityObject = inputs.getValue(getCouponSensitivityRequirement(target));
      if (couponSensitivityObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
      sensitivitiesForCurves = CALCULATOR.calculateFromPresentValue(derivative, null, bundle, couponSensitivity, jacobian, PresentValueNodeSensitivityCalculator.getDefaultInstance());
    }
    if (_fundingCurveName.equals(_forwardCurveName)) {
      return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(_forwardCurveName, bundle, sensitivitiesForCurves, forwardCurveSpec, getForwardResultSpec(target));
    }
    final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs = new HashMap<String, InterpolatedYieldCurveSpecificationWithSecurities>();
    curveSpecs.put(_forwardCurveName, forwardCurveSpec);
    curveSpecs.put(_fundingCurveName, fundingCurveSpec);
    return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForMultipleCurves(_forwardCurveName, _fundingCurveName, getForwardResultSpec(target), getFundingResultSpec(target), 
        bundle, sensitivitiesForCurves, curveSpecs);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (_forwardCurveName.equals(_fundingCurveName)) {
      final Set<ValueRequirement> result = Sets.newHashSet(getCurveRequirement(target, _forwardCurveName),
                                                           getJacobianRequirement(target),
                                                           getCurveSpecRequirement(target, _forwardCurveName));
      if (_curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
        result.add(getCouponSensitivityRequirement(target));
      }
      return result; //TODO see if this is necessary
    } 
    final Set<ValueRequirement> result = Sets.newHashSet(getCurveRequirement(target, _forwardCurveName),
                                                         getCurveRequirement(target, _fundingCurveName),
                                                         getJacobianRequirement(target),
                                                         getCurveSpecRequirement(target, _forwardCurveName),
                                                         getCurveSpecRequirement(target, _fundingCurveName));
    if (_curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
      result.add(getCouponSensitivityRequirement(target));
    }
    return result;
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
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    if (security instanceof SwapSecurity) {
      final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
      return type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD || type == InterestRateInstrumentType.SWAP_IBOR_IBOR;
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType(security);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (_fundingCurveName.equals(_forwardCurveName)) {
      return Sets.newHashSet(getForwardResultSpec(target));
    }
    return Sets.newHashSet(getForwardResultSpec(target), getFundingResultSpec(target));
  }
  
  @Override
  public String getShortName() {
    return "InterestRateInstrumentYieldCurveNodeSensitivitiesFunction";
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName, _forwardCurveName, _fundingCurveName, _curveCalculationType);
  }

  protected ValueRequirement getJacobianRequirement(final ComputationTarget target) {
    return YieldCurveFunction.getJacobianRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), _forwardCurveName, _fundingCurveName, _curveCalculationType);
  }

  protected ValueRequirement getCouponSensitivityRequirement(final ComputationTarget target) {
    return YieldCurveFunction.getCouponSensitivityRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), _forwardCurveName, _fundingCurveName);
  }

  protected ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }
  
  private ValueSpecification getForwardResultSpec(final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties resultProperties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, ccy.getCode())
      .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
      .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationType)
      .with(ValuePropertyNames.CURVE, _forwardCurveName)
      .get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), resultProperties);
  }
  
  private ValueSpecification getFundingResultSpec(final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties resultProperties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, ccy.getCode())
      .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
      .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationType)
      .with(ValuePropertyNames.CURVE, _fundingCurveName)
      .get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), resultProperties);
  }
}
