/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/**
 *
 */
public class EquityOptionVolatilitySurfaceDataFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionVolatilitySurfaceDataFunction.class);
  /** The supported schemes */
  private static final Set<ExternalScheme> s_validSchemes = ImmutableSet.of(ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.BLOOMBERG_TICKER_WEAK, ExternalSchemes.ACTIVFEED_TICKER);

  @Override
  /**
   * {@inheritDoc} <p>
   * INPUT: We are taking a VolatilitySurfaceData object, which contains all number of missing data, plus strikes and vols are in percentages <p>
   * OUTPUT: and converting this into a StandardVolatilitySurfaceData object, which has no empty values, expiry is in years, and the strike and vol scale is without unit (35% -> 0.35)
   */
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    final ZonedDateTime valTime = ZonedDateTime.now(executionContext.getValuationClock());
    final LocalDate valDate = valTime.toLocalDate();

    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);

    final Object specificationObject = inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE_SPEC);
    if (specificationObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface specification");
    }
    final VolatilitySurfaceSpecification specification = (VolatilitySurfaceSpecification) specificationObject;
    final String surfaceQuoteUnits = specification.getQuoteUnits();
    // Get the volatility surface data object
    final Object rawSurfaceObject = inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE_DATA);
    if (rawSurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Object, Object> rawSurface = (VolatilitySurfaceData<Object, Object>) rawSurfaceObject;
    final VolatilitySurfaceData<Double, Double> stdVolSurface;
    if (surfaceQuoteUnits.equals(SurfaceAndCubePropertyNames.VOLATILITY_QUOTE)) {
      stdVolSurface = getSurfaceFromVolatilityQuote(valDate, rawSurface);
    } else if (surfaceQuoteUnits.equals(SurfaceAndCubePropertyNames.PRICE_QUOTE)) {
      // Get the discount curve
      final Object discountCurveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
      if (discountCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get discount curve");
      }
      final YieldCurve discountCurve = (YieldCurve) discountCurveObject;
      // Get the forward curve
      final Object forwardCurveObject = inputs.getValue(ValueRequirementNames.FORWARD_CURVE);
      if (forwardCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get forward curve");
      }
      final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
      stdVolSurface = getSurfaceFromPriceQuote(valDate, rawSurface, forwardCurve, discountCurve, specification);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle quote units " + surfaceQuoteUnits);
    }
    // Return
    final ValueProperties constraints = desiredValue.getConstraints().copy()
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    final ValueSpecification stdVolSpec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA,
        target.toSpecification(), constraints);
    return Collections.singleton(new ComputedValue(stdVolSpec, stdVolSurface));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE; // Bloomberg ticker, weak ticker or Activ ticker
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getValue() instanceof ExternalIdentifiable) {
      final ExternalId identifier = ((ExternalIdentifiable) target.getValue()).getExternalId();
      return s_validSchemes.contains(identifier.getScheme());
    }
    return false;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(),
        properties);
    return Collections.singleton(spec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // Function requires a VolatilitySurfaceData
    // Build the surface name, in two parts: the given name and the target
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> instrumentTypes = constraints.getValues(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
    if (instrumentTypes != null && instrumentTypes.size() == 1) {
      if (!Iterables.getOnlyElement(instrumentTypes).equals(InstrumentTypeProperties.EQUITY_OPTION)) {
        return null;
      }
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      s_logger.error("Function takes only get a single surface. Asked for {}", surfaceNames);
      return null;
    }
    final String givenName = Iterables.getOnlyElement(surfaceNames);
    //FIXME: Modify to take ExternalId to avoid incorrect cast to UniqueId
    final String fullName = givenName + "_" + EquitySecurityUtils.getTrimmedTarget(UniqueId.parse(target.getValue().toString()));

    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceSpecificationSource volSpecSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final VolatilitySurfaceSpecification specification = volSpecSource.getSpecification(fullName, InstrumentTypeProperties.EQUITY_OPTION);
    if (specification == null) {
      s_logger.error("Could not get volatility surface specification with name " + fullName);
      return null;
    }
    // Build the ValueRequirements' constraints
    final String quoteUnits = specification.getQuoteUnits();
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, givenName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, quoteUnits)
        .get();
    final ValueRequirement surfaceReq = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), properties);
    final ValueRequirement specificationReq = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_SPEC, target.toSpecification(), properties);
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(surfaceReq);
    requirements.add(specificationReq);
    if (quoteUnits.equals(SurfaceAndCubePropertyNames.PRICE_QUOTE)) {
      // We require forward and discount curves to imply the volatility
      final Set<String> curveNameValues = constraints.getValues(ValuePropertyNames.CURVE);
      if (curveNameValues == null || curveNameValues.size() != 1) {
        return null;
      }
      final Set<String> curveCalculationValues = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      if (curveCalculationValues == null || curveCalculationValues.size() != 1) {
        return null;
      }
      final Set<String> curveCurrencyValues = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY);
      if (curveCurrencyValues == null || curveCurrencyValues.size() != 1) {
        return null;
      }
      final String curveName = Iterables.getOnlyElement(curveNameValues);
      final String curveCalculationConfig = Iterables.getOnlyElement(curveCalculationValues);
      final Currency ccy = Currency.of(Iterables.getOnlyElement(curveCurrencyValues));
      // DiscountCurve
      final ValueProperties fundingProperties = ValueProperties.builder()  // Note that createValueProperties is _not_ used - otherwise engine can't find the requirement
          .with(ValuePropertyNames.CURVE, curveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
          .get();
      final ValueRequirement discountCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(ccy), fundingProperties);
      requirements.add(discountCurveRequirement);
      // ForwardCurve
      final String curveCalculationMethod = ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD; // TODO Remove hard-coding of forward curve calculation method by adding as property
      final ValueProperties curveProperties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, curveName)
          .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, curveCalculationMethod)
          .get();
      final ValueRequirement forwardCurveRequirement = new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), curveProperties);
      requirements.add(forwardCurveRequirement);
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION);
    boolean surfaceNameSet = false;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification key = entry.getKey();
      if (key.getValueName().equals(ValueRequirementNames.VOLATILITY_SURFACE_DATA)) {
        properties.with(ValuePropertyNames.SURFACE, key.getProperty(ValuePropertyNames.SURFACE));
        surfaceNameSet = true;
      } else if (key.getValueName().equals(ValueRequirementNames.FORWARD_CURVE)) {
        final ValueProperties curveProperties = key.getProperties().copy()
            .withoutAny(ValuePropertyNames.FUNCTION)
            .get();
        for (final String property : curveProperties.getProperties()) {
          properties.with(property, curveProperties.getValues(property));
        }
        //don't check if forward curve is set, because it isn't needed if the quotes are volatility
      }
    }
    assert surfaceNameSet;
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), properties.get()));
  }

  private static VolatilitySurfaceData<Double, Double> getSurfaceFromVolatilityQuote(final LocalDate valDate, final VolatilitySurfaceData<Object, Object> rawSurface) {
    // Remove empties, convert expiries from number to years, and scale vols
    final Map<Pair<Double, Double>, Double> volValues = new HashMap<>();
    final DoubleArrayList tList = new DoubleArrayList();
    final DoubleArrayList kList = new DoubleArrayList();
    final Object[] xs = rawSurface.getXs();
    for (final Object x : xs) {
      Double t;
      if (x instanceof Number) {
        t = FutureOptionExpiries.EQUITY.getFutureOptionTtm(((Number) x).intValue(), valDate);
      } else if (x instanceof LocalDate) {
        t = TimeCalculator.getTimeBetween((LocalDate) x, valDate);
      } else {
        throw new OpenGammaRuntimeException("Cannot not handle surfaces with x-axis type " + x.getClass());
      }
      if (t > 5. / 365.) { // Bootstrapping vol surface to this data causes far more trouble than any gain. The data simply isn't reliable.
        final Double[] ysAsDoubles = getYs(rawSurface.getYs());
        for (final Double strike : ysAsDoubles) {
          final Double vol = rawSurface.getVolatility(x, strike);
          if (vol != null) {
            tList.add(t);
            kList.add(strike);
            volValues.put(Pair.of(t, strike), vol / 100.);
          }
        }
      }
    }
    final VolatilitySurfaceData<Double, Double> stdVolSurface = new VolatilitySurfaceData<>(rawSurface.getDefinitionName(), rawSurface.getSpecificationName(), rawSurface.getTarget(),
        tList.toArray(new Double[0]), kList.toArray(new Double[0]), volValues);
    return stdVolSurface;
  }

  @SuppressWarnings("deprecation")
  private static VolatilitySurfaceData<Double, Double> getSurfaceFromPriceQuote(final LocalDate valDate, final VolatilitySurfaceData<Object, Object> rawSurface,
      final ForwardCurve forwardCurve, final YieldCurve discountCurve, final VolatilitySurfaceSpecification specification) {
    // quote type
    final String surfaceQuoteType = specification.getSurfaceQuoteType();
    double callAboveStrike = 0;
    boolean optionIsCall = true;
    boolean quoteTypeIsCallPutStrike = false;
    if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.CALL_STRIKE)) {
      optionIsCall = true;
    } else if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.PUT_STRIKE)) {
      optionIsCall = false;
    } else if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE)) {
      callAboveStrike = ((CallPutSurfaceInstrumentProvider<?, ?>) specification.getSurfaceInstrumentProvider()).useCallAboveStrike();
      quoteTypeIsCallPutStrike = true;
    } else {
      throw new OpenGammaRuntimeException("Cannot handle surface quote type " + surfaceQuoteType);
    }
    // exercise type
    //TODO REVIEW emcleod 9-7-2013 why is this not using specification.getExerciseType() instanceof AmericanExerciseType?
    final boolean isAmerican = (specification.getExerciseType()).getName().startsWith("A");
    BjerksundStenslandModel americanModel = null;
    final double spot = forwardCurve.getSpot();
    if (isAmerican) {
      americanModel = new BjerksundStenslandModel();
    }
    // Main loop: Remove empties, convert expiries from number to years, and imply vols
    final Map<Pair<Double, Double>, Double> volValues = new HashMap<>();
    final DoubleArrayList tList = new DoubleArrayList();
    final DoubleArrayList kList = new DoubleArrayList();
    final Object[] xs = rawSurface.getXs();
    for (final Object x : xs) {
      Double t;
      if (x instanceof Number) {
        t = FutureOptionExpiries.EQUITY.getFutureOptionTtm(((Number) x).intValue(), valDate);
      } else if (x instanceof LocalDate) {
        t = TimeCalculator.getTimeBetween((LocalDate) x, valDate);
      } else {
        throw new OpenGammaRuntimeException("Cannot not handle surfaces with x-axis type " + x.getClass());
      }
      final double forward = forwardCurve.getForward(t);
      final double zerobond = discountCurve.getDiscountFactor(t);
      final Double[] ysAsDoubles = getYs(rawSurface.getYs());
      for (final Double strike : ysAsDoubles) {
        final Double price = rawSurface.getVolatility(x, strike);
        if (price != null) {
          try {
            if (quoteTypeIsCallPutStrike) {
              optionIsCall = strike > callAboveStrike ? true : false;
            }
            final double vol;
            if (isAmerican) {
              vol = americanModel.impliedVolatility(price, spot, strike, -Math.log(zerobond) / t, Math.log(forward / spot) / t, t, optionIsCall);
            } else {
              final double fwdPrice = price / zerobond;
              vol = BlackFormulaRepository.impliedVolatility(fwdPrice, forward, strike, t, optionIsCall);
            }
            tList.add(t);
            kList.add(strike);
            volValues.put(Pair.of(t, strike), vol);
          } catch (final Exception e) {
            LocalDate expiry = null;
            if (x instanceof Number) {
              expiry = FutureOptionExpiries.EQUITY.getFutureOptionExpiry(((Number) x).intValue(), valDate);
            } else if (x instanceof LocalDate) {
              expiry = (LocalDate) x;
            }
            s_logger.info("Liquidity problem: input price, forward and zero bond imply negative volatility at strike, {}, and expiry, {}",
                strike, expiry);
          }
        }
      }
    }
    final VolatilitySurfaceData<Double, Double> stdVolSurface = new VolatilitySurfaceData<>(rawSurface.getDefinitionName(), rawSurface.getSpecificationName(), rawSurface.getTarget(),
        tList.toArray(new Double[0]), kList.toArray(new Double[0]), volValues);
    return stdVolSurface;
  }

  private static Double[] getYs(final Object ys) {
    if (ys instanceof Double[]) {
      return (Double[]) ys;
    }
    final Object[] tempArray = (Object[]) ys;
    final Double[] result = new Double[tempArray.length];
    for (int i = 0; i < tempArray.length; i++) {
      result[i] = (Double) tempArray[i];
    }
    return result;
  }

}
