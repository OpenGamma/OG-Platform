/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cmsspread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
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
import com.opengamma.financial.analytics.conversion.CapFloorCMSSpreadSecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeFunctionHelper;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CapFloorCMSSpreadSABRYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  @SuppressWarnings("unchecked")
  private static final VolatilityFunctionProvider<SABRFormulaData> SABR_FUNCTION = (VolatilityFunctionProvider<SABRFormulaData>) VolatilityFunctionFactory
      .getCalculator(VolatilityFunctionFactory.HAGAN);
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private final PresentValueNodeSensitivityCalculator _nodeSensitivityCalculator;
  private CapFloorCMSSpreadSecurityConverter _capFloorVisitor;
  private final String _forwardCurveName;
  private final String _fundingCurveName;
  private final VolatilityCubeFunctionHelper _helper;
  private FixedIncomeConverterDataProvider _definitionConverter;

  public CapFloorCMSSpreadSABRYieldCurveNodeSensitivitiesFunction(final String currency, final String definitionName, final String forwardCurveName,
      final String fundingCurveName) {
    this(Currency.of(currency), definitionName, forwardCurveName, fundingCurveName);
  }

  public CapFloorCMSSpreadSABRYieldCurveNodeSensitivitiesFunction(final Currency currency, final String definitionName, final String forwardCurveName,
      final String fundingCurveName) {
    _nodeSensitivityCalculator = PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivitySABRCalculator.getInstance());
    _helper = new VolatilityCubeFunctionHelper(currency, definitionName);
    _fundingCurveName = fundingCurveName;
    _forwardCurveName = forwardCurveName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    _capFloorVisitor = new CapFloorCMSSpreadSecurityConverter(holidaySource, conventionSource);
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final CapFloorCMSSpreadSecurity security = (CapFloorCMSSpreadSecurity) target.getSecurity();
    final InstrumentDefinition<?> capFloorDefinition = security.accept(_capFloorVisitor);
    final InstrumentDerivative capFloor =  _definitionConverter.convert(security, capFloorDefinition, now,
        new String[] {_fundingCurveName, _forwardCurveName}, dataSource);
    final Currency currency = security.getCurrency();
    final Object forwardCurveObject = inputs.getValue(getForwardCurveRequirement(currency, _forwardCurveName, _fundingCurveName));
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final Object couponSensitivitiesObject = inputs.getValue(getCouponSensitivitiesRequirement(currency, _forwardCurveName, _fundingCurveName));
    if (couponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve instrument coupon sensitivities");
    }
    final Object jacobianObject = inputs.getValue(getJacobianRequirement(currency, _forwardCurveName, _fundingCurveName));
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get jacobian");
    }
    final Object forwardCurveSpecObject = inputs.getValue(getForwardCurveSpecRequirement(currency, _forwardCurveName));
    if (forwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve spec");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) forwardCurveSpecObject;
    if (_forwardCurveName.equals(_fundingCurveName)) {
      final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
      final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
      interpolatedCurves.put(_forwardCurveName, forwardCurve);
      final YieldCurveBundle bundle = new YieldCurveBundle(interpolatedCurves);
      final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivitiesObject;
      final SABRInterestRateDataBundle data = getModelData(target, inputs, bundle);
      final DoubleMatrix2D jacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(jacobianObject));
      final DoubleMatrix1D result = CALCULATOR.calculateFromPresentValue(capFloor, null, data, couponSensitivity, jacobian, _nodeSensitivityCalculator);
      return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(_forwardCurveName, bundle, result, forwardCurveSpec, getForwardResultSpec(target, currency));
    }
    final Object fundingCurveObject = inputs.getValue(getFundingCurveRequirement(currency, _forwardCurveName, _fundingCurveName));
    if (fundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get funding curve");
    }
    final Object fundingCurveSpecObject = inputs.getValue(getFundingCurveSpecRequirement(currency, _fundingCurveName));
    if (fundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get funding curve spec");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) fundingCurveSpecObject;
    final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingCurveObject;
    interpolatedCurves.put(_fundingCurveName, fundingCurve);
    interpolatedCurves.put(_forwardCurveName, forwardCurve);
    final YieldCurveBundle bundle = new YieldCurveBundle(interpolatedCurves);
    final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivitiesObject;
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(jacobianObject));
    final SABRInterestRateDataBundle data = getModelData(target, inputs, bundle);
    final DoubleMatrix1D result = CALCULATOR.calculateFromPresentValue(capFloor, null, data, couponSensitivity, jacobian, _nodeSensitivityCalculator);
    final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs = new HashMap<String, InterpolatedYieldCurveSpecificationWithSecurities>();
    curveSpecs.put(_fundingCurveName, fundingCurveSpec);
    curveSpecs.put(_forwardCurveName, forwardCurveSpec);
    return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForMultipleCurves(_forwardCurveName, _fundingCurveName, getForwardResultSpec(target, currency), 
        getFundingResultSpec(target, currency), bundle, result, curveSpecs);
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
    return target.getSecurity() instanceof CapFloorCMSSpreadSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final CapFloorCMSSpreadSecurity capFloor = (CapFloorCMSSpreadSecurity) target.getSecurity();
    final Currency currency = capFloor.getCurrency();
    if (_forwardCurveName.equals(_fundingCurveName)) {
      result.add(getForwardCurveRequirement(currency, _forwardCurveName, _fundingCurveName));
      result.add(getForwardCurveSpecRequirement(currency, _forwardCurveName));
    } else {
      result.add(getForwardCurveRequirement(currency, _forwardCurveName, _fundingCurveName));
      result.add(getFundingCurveRequirement(currency, _forwardCurveName, _fundingCurveName));
      result.add(getForwardCurveSpecRequirement(currency, _forwardCurveName));
      result.add(getFundingCurveSpecRequirement(currency, _fundingCurveName));
    }
    result.add(getCouponSensitivitiesRequirement(currency, _forwardCurveName, _fundingCurveName));
    result.add(getJacobianRequirement(currency, _forwardCurveName, _fundingCurveName));
    result.add(getCubeRequirement(target));
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final CapFloorCMSSpreadSecurity capFloor = (CapFloorCMSSpreadSecurity) target.getSecurity();
    final Currency currency = capFloor.getCurrency();
    return Sets.newHashSet(getForwardResultSpec(target, currency), getFundingResultSpec(target, currency));
  }

  private ValueRequirement getForwardCurveRequirement(final Currency currency, final String forwardCurveDefinitionName, final String fundingCurveDefinitionName) {
    final ValueRequirement forwardCurveRequirement = YieldCurveFunction.getCurveRequirement(currency, forwardCurveDefinitionName, forwardCurveDefinitionName, fundingCurveDefinitionName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
    return forwardCurveRequirement;
  }

  private ValueRequirement getForwardCurveSpecRequirement(final Currency currency, final String forwardCurveDefinitionName) {
    final ValueRequirement forwardCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, forwardCurveDefinitionName).get());
    return forwardCurveRequirement;
  }

  private ValueRequirement getFundingCurveRequirement(final Currency currency, final String forwardCurveDefinitionName, final String fundingCurveDefinitionName) {
    final ValueRequirement fundingCurveRequirement = YieldCurveFunction.getCurveRequirement(currency, fundingCurveDefinitionName, forwardCurveDefinitionName, fundingCurveDefinitionName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
    return fundingCurveRequirement;
  }

  private ValueRequirement getFundingCurveSpecRequirement(final Currency currency, final String fundingCurveDefinitionName) {
    final ValueRequirement fundingCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, fundingCurveDefinitionName).get());
    return fundingCurveRequirement;
  }

  private ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String forwardCurveDefinitionName, final String fundingCurveDefinitionName) {
    return YieldCurveFunction.getCouponSensitivityRequirement(currency, forwardCurveDefinitionName, fundingCurveDefinitionName);
  }

  private ValueRequirement getJacobianRequirement(final Currency currency, final String forwardCurveDefinitionName, final String fundingCurveDefinitionName) {
    return YieldCurveFunction.getJacobianRequirement(currency, forwardCurveDefinitionName, fundingCurveDefinitionName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
  }

  private ValueRequirement getCubeRequirement(final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.CUBE, _helper.getDefinitionName()).get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, FinancialSecurityUtils.getCurrency(target.getSecurity()), properties);
  }

  private ValueSpecification getForwardResultSpec(final ComputationTarget target, final Currency ccy) {
    ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE, _forwardCurveName).get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), result);
  }
  
  private ValueSpecification getFundingResultSpec(final ComputationTarget target, final Currency ccy) {
    ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE, _fundingCurveName).get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), result);
  }
  
  private SABRInterestRateDataBundle getModelData(final ComputationTarget target, final FunctionInputs inputs, final YieldCurveBundle bundle) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueRequirement surfacesRequirement = getCubeRequirement(target);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    if (!surfaces.getCurrency().equals(currency)) {
      throw new OpenGammaRuntimeException("Don't know how this happened");
    }
    final VolatilitySurface alphaSurface = surfaces.getAlphaSurface();
    final VolatilitySurface betaSurface = surfaces.getBetaSurface();
    final VolatilitySurface nuSurface = surfaces.getNuSurface();
    final VolatilitySurface rhoSurface = surfaces.getRhoSurface();
    final DayCount dayCount = surfaces.getDayCount();
    return new SABRInterestRateDataBundle(new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, SABR_FUNCTION), bundle);
  }

}
