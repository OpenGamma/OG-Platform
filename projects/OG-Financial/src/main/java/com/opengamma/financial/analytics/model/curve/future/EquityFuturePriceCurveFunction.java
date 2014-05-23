/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.future;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveDefinition;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveSpecification;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * Function providing an equity future curve.
 */
public class EquityFuturePriceCurveFunction extends FuturePriceCurveFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(FuturePriceCurveFunction.class);

  private static final Calendar WEEKDAYS = new MondayToFridayCalendar("MTWThF");

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.EQUITY_FUTURE_PRICE;
  }

  @SuppressWarnings("unchecked")
  private FuturePriceCurveDefinition<Object> getCurveDefinition(final ComputationTarget target, final String definitionName, final VersionCorrection versionCorrection) {
    if (!(target.getValue() instanceof ExternalIdentifiable)) {
      return null;
    }
    ExternalId id = ((ExternalIdentifiable) target.getValue()).getExternalId();
    final String ticker = EquitySecurityUtils.getIndexOrEquityName(id);
    final String fullDefinitionName = definitionName + "_" + ticker;
    return (FuturePriceCurveDefinition<Object>) getFuturePriceCurveDefinitionSource().getDefinition(fullDefinitionName, getInstrumentType(), versionCorrection);
  }

  private FuturePriceCurveSpecification getCurveSpecification(final ComputationTarget target, final String specificationName, final VersionCorrection versionCorrection) {
    if (!(target.getValue() instanceof ExternalIdentifiable)) {
      return null;
    }
    ExternalId id = ((ExternalIdentifiable) target.getValue()).getExternalId();
    final String ticker = EquitySecurityUtils.getIndexOrEquityName(id);
    final String fullSpecificationName = specificationName + "_" + ticker;
    return getFuturePriceCurveSpecificationSource().getSpecification(fullSpecificationName, getInstrumentType(), versionCorrection);
  }

  @SuppressWarnings("unchecked")
  public static Set<ValueRequirement> buildRequirements(final FuturePriceCurveSpecification futurePriceCurveSpecification,
      final FuturePriceCurveDefinition<Object> futurePriceCurveDefinition, final ValueRequirement desiredValue, final ZonedDateTime atInstant) {
    final Set<ValueRequirement> result = new HashSet<>();
    final FuturePriceCurveInstrumentProvider<Number> futurePriceCurveProvider = (FuturePriceCurveInstrumentProvider<Number>) futurePriceCurveSpecification.getCurveInstrumentProvider();
    final String dataFieldName = getDataFieldName(futurePriceCurveProvider, desiredValue);
    for (final Object x : futurePriceCurveDefinition.getXs()) {
      final ExternalId identifier = futurePriceCurveProvider.getInstrument((Number) x, atInstant.toLocalDate());
      result.add(new ValueRequirement(dataFieldName, ComputationTargetType.PRIMITIVE, identifier));
    }
    return result;
  }

  private static String getDataFieldName(FuturePriceCurveInstrumentProvider<Number> futurePriceCurveProvider, ValueRequirement desiredValue) {
    return futurePriceCurveProvider.getDataFieldName();
  }

  @Override
  protected Double getTimeToMaturity(final int n, final LocalDate date, final Calendar calendar) {
    throw new OpenGammaRuntimeException("Unexpected call");
  }

  /* Spot value of the equity index or name */
  private ValueRequirement getSpotRequirement(final ComputationTarget target, final String dataFieldName) {
    return new ValueRequirement(dataFieldName, ComputationTargetType.PRIMITIVE, target.getUniqueId());
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext myContext, final ComputationTarget target) {
        final ValueProperties curveProperties = createValueProperties().withAny(ValuePropertyNames.CURVE)
            .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, getInstrumentType()).get();
        final ValueSpecification futurePriceCurveResult = new ValueSpecification(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), curveProperties);
        return Collections.singleton(futurePriceCurveResult);
      }

      @SuppressWarnings({"synthetic-access", "unchecked" })
      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext myContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final String curveName = constraints.getStrictValue(ValuePropertyNames.CURVE);
        if (curveName == null) {
          return null;
        }
        final String curveDefinitionName = curveName;
        final String curveSpecificationName = curveName;
        final VersionCorrection versionCorrection = myContext.getComputationTargetResolver().getVersionCorrection();
        final FuturePriceCurveDefinition<Object> priceCurveDefinition = getCurveDefinition(target, curveDefinitionName, versionCorrection);
        if (priceCurveDefinition == null) {
          s_logger.error("Price curve definition for target {} with curve name {} and instrument type {} was null", new Object[] {target, curveDefinitionName, getInstrumentType() });
          return null;
        }
        final FuturePriceCurveSpecification priceCurveSpecification = getCurveSpecification(target, curveSpecificationName, versionCorrection);
        if (priceCurveSpecification == null) {
          s_logger.error("Price curve specification for target {} with curve name {} and instrument type {} was null", new Object[] {target, curveSpecificationName, getInstrumentType() });
          return null;
        }
        final FuturePriceCurveInstrumentProvider<Number> futurePriceCurveProvider = (FuturePriceCurveInstrumentProvider<Number>) priceCurveSpecification.getCurveInstrumentProvider();
        final String dataFieldName = getDataFieldName(futurePriceCurveProvider, desiredValue);

        Set<ValueRequirement> requirements = Sets.newHashSet();
        requirements.add(getSpotRequirement(target, dataFieldName));
        requirements.addAll(buildRequirements(priceCurveSpecification, priceCurveDefinition, desiredValue, atZDT));
        return requirements;
      }

      @Override
      public boolean canHandleMissingInputs() {
        return true;
      }

      @Override
      public boolean canHandleMissingRequirements() {
        return true;
      }

      @SuppressWarnings({"synthetic-access", "unchecked" })
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final String curveDefinitionName = curveName;
        final String curveSpecificationName = curveName;
        final VersionCorrection versionCorrection = executionContext.getComputationTargetResolver().getVersionCorrection();
        final FuturePriceCurveDefinition<Object> priceCurveDefinition = getCurveDefinition(target, curveDefinitionName, versionCorrection);
        final FuturePriceCurveSpecification priceCurveSpecification = getCurveSpecification(target, curveSpecificationName, versionCorrection);
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        final DoubleArrayList xList = new DoubleArrayList();
        final DoubleArrayList prices = new DoubleArrayList();
        final FuturePriceCurveInstrumentProvider<Number> futurePriceCurveProvider = (FuturePriceCurveInstrumentProvider<Number>) priceCurveSpecification.getCurveInstrumentProvider();
        final String dataFieldName = getDataFieldName(futurePriceCurveProvider, desiredValue);
        final ExchangeTradedInstrumentExpiryCalculator expiryCalc = futurePriceCurveProvider.getExpiryRuleCalculator();
        final LocalDate valDate = now.toLocalDate();
        if (inputs.getAllValues().isEmpty()) {
          throw new OpenGammaRuntimeException("Could not get any data for future price curve called " + curveSpecificationName);
        }
        // Add spot
        final Object spotUnderlying = inputs.getValue(getSpotRequirement(target, dataFieldName));
        if (spotUnderlying != null) {
          xList.add(0.0);
          prices.add((Double) spotUnderlying);
        }
        // Add futures
        for (final Object x : priceCurveDefinition.getXs()) {
          final Number xNum = (Number) x;
          final ExternalId identifier = futurePriceCurveProvider.getInstrument(xNum, valDate);
          final ValueRequirement requirement = new ValueRequirement(dataFieldName, ComputationTargetType.PRIMITIVE, identifier);
          Double futurePrice = null;
          if (inputs.getValue(requirement) != null) {
            futurePrice = (Double) inputs.getValue(requirement);
            if (futurePrice != null) {
              LocalDate expiry = expiryCalc.getExpiryDate(xNum.intValue(), valDate, WEEKDAYS); // TODO Add true holiday calendar
              final Double ttm = TimeCalculator.getTimeBetween(valDate, expiry);
              xList.add(ttm);
              prices.add(futurePrice);
            }
          }
        }
        final ValueSpecification futurePriceCurveResult = new ValueSpecification(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), createValueProperties()
            .with(ValuePropertyNames.CURVE, curveName).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, getInstrumentType()).get());
        final NodalDoublesCurve curve = NodalDoublesCurve.from(xList.toDoubleArray(), prices.toDoubleArray());
        final ComputedValue futurePriceCurveResultValue = new ComputedValue(futurePriceCurveResult, curve);
        return Sets.newHashSet(futurePriceCurveResultValue);
      }
    };
  }
}
