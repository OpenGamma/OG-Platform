/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_TOLERANCE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_N_INTEGRATION_POINTS;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
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
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.CreditDefaultSwapSecurityConverter;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.credit.CreditCurveIdentifier;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class LegacyVanillaCDSFunction extends AbstractFunction.NonCompiledInvoker {
  private CreditDefaultSwapSecurityConverter _converter;
  private final String _valueRequirement;
  private final PriceType _priceType;

  public LegacyVanillaCDSFunction(final String valueRequirement) {
    ArgumentChecker.notNull(valueRequirement, "value requirement");
    if (valueRequirement.equals(ValueRequirementNames.DIRTY_PRICE)) {
      _priceType = PriceType.DIRTY;
    } else if (valueRequirement.equals(ValueRequirementNames.CLEAN_PRICE)) {
      _priceType = PriceType.CLEAN;
    } else {
      throw new IllegalArgumentException("Can only calculate clean and dirty prices");
    }
    _valueRequirement = valueRequirement;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    _converter = new CreditDefaultSwapSecurityConverter(holidaySource, regionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final Object yieldCurveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (yieldCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve");
    }
    final Object hazardRateCurveObject = inputs.getValue(ValueRequirementNames.HAZARD_RATE_CURVE);
    if (hazardRateCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get hazard rate curve");
    }
    final ISDACurve yieldCurve = (ISDACurve) yieldCurveObject;
    final HazardRateCurve hazardRateCurve = (HazardRateCurve) hazardRateCurveObject;
    final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String nPointsProperty = desiredValue.getConstraint(PROPERTY_N_INTEGRATION_POINTS);
    final int nIntegrationPoints = Integer.parseInt(nPointsProperty);
    final PresentValueLegacyCreditDefaultSwap calculator = new PresentValueLegacyCreditDefaultSwap(nIntegrationPoints);
    final LegacyCreditDefaultSwapDefinition cds = _converter.visitLegacyVanillaCDSSecurity(security, now, _priceType);
    final double price = calculator.getPresentValueCreditDefaultSwap(cds, yieldCurve, hazardRateCurve);
    final ValueProperties properties = getProperties(desiredValue);
    final ValueSpecification spec = new ValueSpecification(_valueRequirement, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, price));
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
    return target.getSecurity() instanceof LegacyVanillaCDSSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getProperties(target);
    return Collections.singleton(new ValueSpecification(_valueRequirement, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> yieldCurveNames = constraints.getValues(PROPERTY_YIELD_CURVE);
    if (yieldCurveNames == null || yieldCurveNames.size() != 1) {
      return null;
    }
    final Set<String> yieldCurveCalculationConfigs = constraints.getValues(PROPERTY_YIELD_CURVE_CALCULATION_METHOD);
    if (yieldCurveCalculationConfigs == null || yieldCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> hazardRateCurveNames = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE);
    if (hazardRateCurveNames == null || hazardRateCurveNames.size() != 1) {
      return null;
    }
    final Set<String> hazardRateCurveCalculationConfigs = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD);
    if (hazardRateCurveCalculationConfigs == null || hazardRateCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> nCurveIterationsName = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS);
    if (nCurveIterationsName == null || nCurveIterationsName.size() != 1) {
      return null;
    }
    final Set<String> tolerances = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_TOLERANCE);
    if (tolerances == null || tolerances.size() != 1) {
      return null;
    }
    final Set<String> rangeMultipliers = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER);
    if (rangeMultipliers == null || rangeMultipliers.size() != 1) {
      return null;
    }
    final Set<String> nIntegrationPoints = constraints.getValues(PROPERTY_N_INTEGRATION_POINTS);
    if (nIntegrationPoints == null || nIntegrationPoints.size() != 1) {
      return null;
    }
    final String yieldCurveName = Iterables.getOnlyElement(yieldCurveNames);
    final String yieldCurveCalculationConfig = Iterables.getOnlyElement(yieldCurveCalculationConfigs);
    final String hazardRateCurveName = Iterables.getOnlyElement(hazardRateCurveNames);
    final String nCurveIterations = Iterables.getOnlyElement(nCurveIterationsName);
    final String tolerance = Iterables.getOnlyElement(tolerances);
    final String rangeMultiplier = Iterables.getOnlyElement(rangeMultipliers);
    final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final ValueProperties ycProperties = ValueProperties.builder()
        .with(CURVE, yieldCurveName)
        .with(CURVE_CALCULATION_CONFIG, yieldCurveCalculationConfig)
        .with(CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME).get();
    final ValueProperties hazardRateCurveProperties = ValueProperties.builder()
        .with(CURVE, hazardRateCurveName)
        .with(PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS, nCurveIterations)
        .with(PROPERTY_HAZARD_RATE_CURVE_TOLERANCE, tolerance)
        .with(PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER, rangeMultiplier).get();
    final CreditCurveIdentifier creditId = CreditCurveIdentifier.of(security.getReferenceEntity(), security.getDebtSeniority(), security.getRestructuringClause());
    final ValueRequirement yieldCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), ycProperties);
    final ValueRequirement hazardCurveRequirement = new ValueRequirement(ValueRequirementNames.HAZARD_RATE_CURVE, ComputationTargetType.PRIMITIVE, creditId.getUniqueId(), hazardRateCurveProperties);
    return Sets.newHashSet(yieldCurveRequirement, hazardCurveRequirement);
  }

  private ValueProperties getProperties(final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    return createValueProperties()
        .withAny(PROPERTY_YIELD_CURVE)
        .withAny(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG)
        .with(PROPERTY_YIELD_CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .withAny(PROPERTY_HAZARD_RATE_CURVE)
        .withAny(PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS)
        .withAny(PROPERTY_HAZARD_RATE_CURVE_TOLERANCE)
        .withAny(PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER)
        .with(CURRENCY, currency)
        .with(CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .withAny(PROPERTY_N_INTEGRATION_POINTS)
        .get();
  }

  private ValueProperties getProperties(final ValueRequirement desiredValue) {
    return desiredValue.getConstraints().copy().get();
  }

}
