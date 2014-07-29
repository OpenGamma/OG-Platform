/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class CommodityOptionVolatilitySurfaceDataFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(CommodityOptionVolatilitySurfaceDataFunction.class);

  private ConfigDBVolatilitySurfaceSpecificationSource _volatilitySurfaceSpecificationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilitySurfaceSpecificationSource = ConfigDBVolatilitySurfaceSpecificationSource.init(context, this);
  }

  @Override
  /**
   * {@inheritDoc} <p>
   * INPUT: We are taking a VolatilitySurfaceData object, which contains all number of missing data, plus strikes and vols are in percentages <p>
   * OUTPUT: and converting this into a StandardVolatilitySurfaceData object, which has no empty values, expiry is in years, and the strike and vol scale is without unit (35% -> 0.35)
   */
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    final ZonedDateTime valTime = ZonedDateTime.now(executionContext.getValuationClock());
    final LocalDate valDate = valTime.toLocalDate();

    final Currency currency = (Currency) target.getValue();
    final Calendar calendar = new HolidaySourceCalendarAdapter(OpenGammaExecutionContext.getHolidaySource(executionContext), currency);

    // 1. Build the surface name, in two parts: the given name and the target
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);

    // 2. Get the RawEquityVolatilitySurfaceData object
    final Object rawSurfaceObject = inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE_DATA);
    if (rawSurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Number, Double> rawSurface = (VolatilitySurfaceData<Number, Double>) rawSurfaceObject;

    //2a Get forward curve
    final Object forwardCurveObject = inputs.getValue(ValueRequirementNames.FORWARD_CURVE);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;

    // 3. Remove empties, convert expiries from number to years, and scale vols
    final Map<Pair<Double, Double>, Double> volValues = new HashMap<Pair<Double, Double>, Double>();
    final DoubleArrayList tList = new DoubleArrayList();
    final DoubleArrayList kList = new DoubleArrayList();
    // SurfaceInstrumentProvider just used to get expiry calculator - find a better way as this is quite ugly.
    final String surfacePrefix = surfaceName.split("\\_")[1];
    final ExchangeTradedInstrumentExpiryCalculator expiryCalculator = new BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider(surfacePrefix, "Comdty", "", 0., "")
        .getExpiryRuleCalculator();
    for (final Number nthExpiry : rawSurface.getXs()) {
      final double t = TimeCalculator.getTimeBetween(valDate, expiryCalculator.getExpiryDate(nthExpiry.intValue(), valDate, calendar));

      if (!isValidStrike(forwardCurve, rawSurface, t, nthExpiry)) {
        continue;
      }

      if (t > 5. / 365.) { // Bootstrapping vol surface to this data causes far more trouble than any gain. The data simply isn't reliable.
        for (final Double strike : rawSurface.getYs()) {
          final Double vol = rawSurface.getVolatility(nthExpiry, strike);
          if (vol != null) {
            tList.add(t);
            kList.add(strike);
            volValues.put(Pairs.of(t, strike), vol / 100.);
          }
        }
      }
    }
    final VolatilitySurfaceData<Double, Double> stdVolSurface = new VolatilitySurfaceData<Double, Double>(rawSurface.getDefinitionName(), rawSurface.getSpecificationName(),
        rawSurface.getTarget(), tList.toArray(new Double[0]), kList.toArray(new Double[0]), volValues);

    // 4. Return
    final ValueProperties stdVolProperties = createValueProperties().with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.COMMODITY_FUTURE_OPTION).get();
    final ValueSpecification stdVolSpec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), stdVolProperties);
    return Collections.singleton(new ComputedValue(stdVolSpec, stdVolSurface));
  }

  /**
   * Some strikes blow up the black function - strip them out
   * 
   * @return true if strike works with black function
   */
  private boolean isValidStrike(final ForwardCurve forwardCurve, final VolatilitySurfaceData<Number, Double> rawSurface, final double t, final Number nExpiry) {
    final double forward = forwardCurve.getForward(t);
    // FIXME: Skip points that the Black surface will choke on. Remove this later
    Double low = null;
    Double high = null;
    for (final Double strike : rawSurface.getYs()) {
      final Double vol = rawSurface.getVolatility(nExpiry, strike);
      if (vol != null) {
        low = strike;
        break;
      }
    }
    for (int i = rawSurface.getYs().length - 1; i != 0; i--) {
      final Double strike = rawSurface.getYs()[i];
      final Double vol = rawSurface.getVolatility(nExpiry, strike);
      if (vol != null) {
        high = strike;
        break;
      }
    }
    if ((low == null) || (high == null) || (low > forward) || (high < forward)) {
      return false;
    }
    return true;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), createValueProperties()
        .withAny(ValuePropertyNames.SURFACE).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.COMMODITY_FUTURE_OPTION).get());
    return Collections.singleton(spec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // Function requires a VolatilitySurfaceData, typically supplied by RawOptionVolatilitySurfaceDataFunction
    // 1. Build the surface name, in two parts: the given name and the target
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      throw new OpenGammaRuntimeException("Function takes only get a single surface. One has asked for " + surfaceNames);
    }
    final String givenName = surfaceNames.iterator().next();
    final String fullName = givenName + "_" + target.getUniqueId().getValue();

    // 2. Look up the specification
    final VolatilitySurfaceSpecification specification = _volatilitySurfaceSpecificationSource.getSpecification(fullName, InstrumentTypeProperties.COMMODITY_FUTURE_OPTION);
    if (specification == null) {
      s_logger.error("Could not get volatility surface specification with name " + fullName);
      return null;
    }

    // Add forward curve so we can discount strikes > forward
    final ValueProperties forwardProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE, givenName).get();
    final ValueRequirement forwardRequirement = new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), forwardProperties);

    // 3. Build the ValueRequirements' constraints
    final ValueProperties constraints = ValueProperties.builder().with(ValuePropertyNames.SURFACE, givenName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.COMMODITY_FUTURE_OPTION)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits()).get();
    // 4. Return
    final ValueRequirement surfaceReq = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), constraints);
    //return Collections.singleton(surfaceReq);
    return Sets.newHashSet(forwardRequirement, surfaceReq);
  }

}
