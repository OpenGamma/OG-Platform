/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBFuturePriceCurveDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBFuturePriceCurveSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveDefinition;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveSpecification;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class FuturePriceCurveFunction extends AbstractFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(FuturePriceCurveFunction.class);

  private ConfigDBFuturePriceCurveDefinitionSource _futurePriceCurveDefinitionSource;
  private ConfigDBFuturePriceCurveSpecificationSource _futurePriceCurveSpecificationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _futurePriceCurveDefinitionSource = ConfigDBFuturePriceCurveDefinitionSource.init(context, this);
    _futurePriceCurveSpecificationSource = ConfigDBFuturePriceCurveSpecificationSource.init(context, this);
  }

  protected ConfigDBFuturePriceCurveDefinitionSource getFuturePriceCurveDefinitionSource() {
    return _futurePriceCurveDefinitionSource;
  }

  protected ConfigDBFuturePriceCurveSpecificationSource getFuturePriceCurveSpecificationSource() {
    return _futurePriceCurveSpecificationSource;
  }

  protected abstract String getInstrumentType();

  @SuppressWarnings("unchecked")
  private FuturePriceCurveDefinition<Object> getCurveDefinition(final ComputationTarget target, final String definitionName, final VersionCorrection versionCorrection) {
    final String fullDefinitionName = definitionName + "_" + target.getUniqueId().getValue();
    return (FuturePriceCurveDefinition<Object>) getFuturePriceCurveDefinitionSource().getDefinition(fullDefinitionName, getInstrumentType(), versionCorrection);
  }

  private FuturePriceCurveSpecification getCurveSpecification(final ComputationTarget target, final String specificationName, final VersionCorrection versionCorrection) {
    final String fullSpecificationName = specificationName + "_" + target.getUniqueId().getValue();
    return getFuturePriceCurveSpecificationSource().getSpecification(fullSpecificationName, getInstrumentType(), versionCorrection);
  }

  @SuppressWarnings("unchecked")
  public static Set<ValueRequirement> buildRequirements(final FuturePriceCurveSpecification futurePriceCurveSpecification,
      final FuturePriceCurveDefinition<Object> futurePriceCurveDefinition, final ZonedDateTime atInstant) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final FuturePriceCurveInstrumentProvider<Object> futurePriceCurveProvider = (FuturePriceCurveInstrumentProvider<Object>) futurePriceCurveSpecification.getCurveInstrumentProvider();
    for (final Object x : futurePriceCurveDefinition.getXs()) {
      final ExternalId identifier = futurePriceCurveProvider.getInstrument(x, atInstant.toLocalDate());
      result.add(new ValueRequirement(futurePriceCurveProvider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier));
    }
    return result;
  }

  protected abstract Double getTimeToMaturity(int n, LocalDate date, Calendar calendar);

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.CURRENCY;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext myContext, final ComputationTarget target) {
        final ValueProperties curveProperties = createValueProperties().withAny(ValuePropertyNames.CURVE)
            .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, getInstrumentType()).get();
        final ValueSpecification futurePriceCurveResult = new ValueSpecification(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), curveProperties);
        return Collections.singleton(futurePriceCurveResult);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext myContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final String curveName = constraints.getStrictValue(ValuePropertyNames.CURVE);
        if (curveName == null) {
          return null;
        }
        //TODO use separate definition and specification names?
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
        final Set<ValueRequirement> requirements = Collections.unmodifiableSet(buildRequirements(priceCurveSpecification, priceCurveDefinition, atZDT));
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
        final Currency currency = target.getValue(PrimitiveComputationTargetType.CURRENCY);
        final Calendar calendar = new HolidaySourceCalendarAdapter(OpenGammaExecutionContext.getHolidaySource(executionContext), currency);
        //TODO use separate definition and specification names?
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
        final LocalDate valDate = now.toLocalDate();
        if (inputs.getAllValues().isEmpty()) {
          throw new OpenGammaRuntimeException("Could not get any data for future price curve called " + curveSpecificationName);
        }
        for (final Object x : priceCurveDefinition.getXs()) {
          final Number xNum = (Number) x;
          ExternalId identifier = futurePriceCurveProvider.getInstrument(xNum, valDate);
          final ValueRequirement requirement = new ValueRequirement(futurePriceCurveProvider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier);
          Double futurePrice = null;
          if (inputs.getValue(requirement) != null) {
            futurePrice = (Double) inputs.getValue(requirement);
            if (futurePrice != null) {
              if (priceCurveSpecification.isUseUnderlyingSecurityForExpiry()) {
                // directly getting the expiry of the underliers
                if (identifier.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
                  identifier = ExternalSchemes.bloombergTickerSecurityId(identifier.getValue());
                }
                final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
                final Security security = securitySource.getSingle(identifier.toBundle());
                if (security != null) {
                  // check if the security is IRFutures here
                  final InterestRateFutureSecurity irFuture = (InterestRateFutureSecurity) security;
                  final LocalDate expiry = irFuture.getExpiry().getExpiry().toLocalDate();
                  final Double ttm = TimeCalculator.getTimeBetween(valDate, expiry);
                  xList.add(ttm);
                  prices.add(futurePrice);
                }
              } else {
                final ExchangeTradedInstrumentExpiryCalculator expiryCalc = futurePriceCurveProvider.getExpiryRuleCalculator();
                final LocalDate expiry = expiryCalc.getExpiryDate(xNum.intValue(), valDate, calendar);
                final Double ttm = TimeCalculator.getTimeBetween(valDate, expiry);
                xList.add(ttm);
                prices.add(futurePrice);
              }
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
