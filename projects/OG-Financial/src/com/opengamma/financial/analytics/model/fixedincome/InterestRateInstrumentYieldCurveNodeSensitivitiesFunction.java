/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.BondSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.CashSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FRASecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.fixedincome.SwapSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;
import com.opengamma.financial.analytics.interestratefuture.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterestRateInstrumentYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  // TODO: This will be hit for a curve definition on each calculation cycle, so it really needs to cache stuff rather than do any I/O
  private FinancialSecurityVisitorAdapter<FixedIncomeInstrumentConverter<?>> _visitor;
  private FixedIncomeConverterDataProvider _definitionConverter;
  private final String _curveCalculationType;

  public InterestRateInstrumentYieldCurveNodeSensitivitiesFunction(final String curveCalculationType) {
    Validate.notNull(curveCalculationType, "curve calculation type");
    Validate.isTrue(curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING) ||
                    curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING),
                    "Did not recognise curve calculation type " + curveCalculationType);
    _curveCalculationType = curveCalculationType;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext
        .getConventionBundleSource(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, conventionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource,
        regionSource);
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    _visitor =
        FinancialSecurityVisitorAdapter.<FixedIncomeInstrumentConverter<?>> builder()
            .cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter).swapSecurityVisitor(swapConverter)
            .futureSecurityVisitor(irFutureConverter)
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
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    final String forwardCurveName = curveNames.getFirst();
    final String fundingCurveName = curveNames.getSecond();
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    final ValueRequirement forwardCurveSpecRequirement = getCurveSpecRequirement(target, forwardCurveName, null, null);
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
      final ValueRequirement fundingCurveSpecRequirement = getCurveSpecRequirement(target, fundingCurveName, null, null);
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
    final FixedIncomeInstrumentConverter<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final InterestRateDerivative derivative = _definitionConverter.convert(security, definition, now,
        FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security,
            fundingCurveName, forwardCurveName), dataSource);
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {forwardCurveName, fundingCurveName},
        new YieldAndDiscountCurve[] {forwardCurve, fundingCurve});
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    interpolatedCurves.put(forwardCurveName, bundle.getCurve(forwardCurveName));
    interpolatedCurves.put(fundingCurveName, bundle.getCurve(fundingCurveName));
    DoubleMatrix1D sensitivitiesForCurves;
    if (_curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING)) {
      sensitivitiesForCurves = CALCULATOR.calculateFromParRate(derivative, null, interpolatedCurves, jacobian, PresentValueNodeSensitivityCalculator.getDefaultInstance());
    } else {
      final Object couponSensitivityObject = inputs.getValue(getCouponSensitivityRequirement(target, forwardCurveName, fundingCurveName));
      if (couponSensitivityObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
      sensitivitiesForCurves = CALCULATOR.calculateFromPresentValue(derivative, null, interpolatedCurves, couponSensitivity, jacobian, PresentValueNodeSensitivityCalculator.getDefaultInstance());
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    if (fundingCurveName.equals(forwardCurveName)) {
      return getSensitivitiesForSingleCurve(target, security, curveNames.getFirst(), bundle, sensitivitiesForCurves, currency, forwardCurveSpec);
    }
    final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs = new HashMap<String, InterpolatedYieldCurveSpecificationWithSecurities>();
    curveSpecs.put(forwardCurveName, forwardCurveSpec);
    curveSpecs.put(fundingCurveName, fundingCurveSpec);
    return getSensitivitiesForMultipleCurves(target, security, forwardCurveName, fundingCurveName, bundle, sensitivitiesForCurves, currency, curveSpecs);
  }

  private Set<ComputedValue> getSensitivitiesForSingleCurve(final ComputationTarget target, final FinancialSecurity security, final String curveName,
      final YieldCurveBundle bundle, final DoubleMatrix1D sensitivitiesForCurve, final Currency currency, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec) {
    final int n = sensitivitiesForCurve.getNumberOfElements();
    final YieldAndDiscountCurve curve = bundle.getCurve(curveName);
    final Double[] keys = curve.getCurve().getXData();
    final double[] values = new double[n];
    final Object[] labels = YieldCurveLabelGenerator.getLabels(curveSpec, currency, curveName);
    DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    for (int i = 0; i < n; i++) {
      labelledMatrix = (DoubleLabelledMatrix1D) labelledMatrix.add(keys[i], labels[i], sensitivitiesForCurve.getEntry(i));
    }
    final YieldCurveNodeSensitivityDataBundle data = new YieldCurveNodeSensitivityDataBundle(currency, labelledMatrix, curveName);
    ValueProperties resultProperties = FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(
        (FinancialSecurity) target.getSecurity(), curveName, curveName, createValueProperties());
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    resultProperties = resultProperties.copy()
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationType)
        .get();
    final ValueSpecification specification = new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), resultProperties);
    return Collections.singleton(new ComputedValue(specification, data.getLabelledMatrix()));
  }

  //TODO at some point this needs to deal with more than two curves
  private Set<ComputedValue> getSensitivitiesForMultipleCurves(final ComputationTarget target, final FinancialSecurity security, final String forwardCurveName, final String fundingCurveName,
      final YieldCurveBundle bundle, final DoubleMatrix1D sensitivitiesForCurves, final Currency currency, final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs) {
    final int nForward = bundle.getCurve(forwardCurveName).getCurve().size();
    final int nFunding = bundle.getCurve(fundingCurveName).getCurve().size();
    final Map<String, DoubleMatrix1D> sensitivities = new HashMap<String, DoubleMatrix1D>();
    sensitivities.put(forwardCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), 0, nForward)));
    sensitivities.put(fundingCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), nForward, nFunding + 1)));
    final String[] relevantCurvesForDerivative = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security,
        fundingCurveName, forwardCurveName);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (final String curveName : relevantCurvesForDerivative) {
      results.addAll(getSensitivitiesForSingleCurve(target, security, curveName, bundle, sensitivities.get(curveName), currency, curveSpecs.get(curveName)));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(context, desiredValue);
    if (curveNames.getFirst().equals(curveNames.getSecond())) {
      final Set<ValueRequirement> result = Sets.newHashSet(getCurveRequirement(target, curveNames.getFirst(), null, null),
                                                           getJacobianRequirement(target, curveNames.getFirst(), curveNames.getSecond()),
                                                           getCurveSpecRequirement(target, curveNames.getFirst(), curveNames.getFirst(), curveNames.getSecond()));
      if (_curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
        result.add(getCouponSensitivityRequirement(target, curveNames.getFirst(), curveNames.getSecond()));
      }
      return result;
    } else {
      final Set<ValueRequirement> result = Sets.newHashSet(getCurveRequirement(target, curveNames.getFirst(), curveNames.getFirst(), curveNames.getSecond()),
                                                           getCurveRequirement(target, curveNames.getSecond(), curveNames.getFirst(), curveNames.getSecond()),
                                                           getJacobianRequirement(target, curveNames.getFirst(), curveNames.getSecond()));
      if (_curveCalculationType.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
        result.add(getCouponSensitivityRequirement(target, curveNames.getFirst(), curveNames.getSecond()));
      }
      return result;
    }
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
    return InterestRateInstrumentType.isFixedIncomeInstrumentType((FinancialSecurity) target.getSecurity());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    ValueProperties resultProperties = FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(
        (FinancialSecurity) target.getSecurity(), createValueProperties());
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    resultProperties = resultProperties.copy()
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationType)
        .get();
    return Collections.singleton(new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), resultProperties));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    ValueProperties resultProperties = FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(
        (FinancialSecurity) target.getSecurity(), curveNames.getSecond(), curveNames.getFirst(), createValueProperties());
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    resultProperties = resultProperties.copy()
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationType)
        .get();
    return Collections
        .singleton(new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), resultProperties));
  }

  @Override
  public String getShortName() {
    return "InterestRateInstrumentYieldCurveNodeSensitivitiesFunction";
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName, advisoryForward, advisoryFunding, _curveCalculationType);
  }

  protected ValueRequirement getJacobianRequirement(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    return YieldCurveFunction.getJacobianRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), forwardCurveName, fundingCurveName, _curveCalculationType);
  }

  protected ValueRequirement getCouponSensitivityRequirement(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    return YieldCurveFunction.getCouponSensitivityRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), forwardCurveName, fundingCurveName);
  }

  protected ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName, final String forwardCurveName, final String fundingCurveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    if (forwardCurveName != null) {
      properties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).withOptional(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    }
    if (fundingCurveName != null) {
      properties.with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).withOptional(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    }
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }
}
