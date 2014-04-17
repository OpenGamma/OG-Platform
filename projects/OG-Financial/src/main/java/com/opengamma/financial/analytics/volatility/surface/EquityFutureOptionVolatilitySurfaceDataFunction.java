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
import org.testng.internal.collections.Pair;

import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;

/**
 *
 */
public class EquityFutureOptionVolatilitySurfaceDataFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityFutureOptionVolatilitySurfaceDataFunction.class);
  /** The supported schemes */
  private static final Set<ExternalScheme> s_validSchemes = ImmutableSet.of(ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.BLOOMBERG_TICKER_WEAK, ExternalSchemes.ACTIVFEED_TICKER);

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
    final VolatilitySurfaceData<Pair<Integer, Tenor>, Double> rawSurface = (VolatilitySurfaceData<Pair<Integer, Tenor>, Double>) rawSurfaceObject;

    final VolatilitySurfaceData<Double, Double> stdVolSurface;
    if (surfaceQuoteUnits.equals(SurfaceAndCubePropertyNames.VOLATILITY_QUOTE)) {
      stdVolSurface = getSurfaceFromVolatilityQuote(valDate, rawSurface);
    } else if (surfaceQuoteUnits.equals(SurfaceAndCubePropertyNames.PRICE_QUOTE)) {
      // Get the forward curve
      final Object forwardCurveObject = inputs.getValue(ValueRequirementNames.FORWARD_CURVE);
      if (forwardCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get forward curve");
      }
      final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
      stdVolSurface = getSurfaceFromPriceQuote(valDate, rawSurface, forwardCurve, specification);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle quote units " + surfaceQuoteUnits);
    }
    // Return
    final ValueProperties constraints = desiredValue.getConstraints().copy().with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    final ValueSpecification stdVolSpec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), constraints);
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
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), properties);
    return Collections.singleton(spec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // Function requires a VolatilitySurfaceData
    // Build the surface name, in two parts: the given name and the target
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> instrumentTypes = constraints.getValues(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
    if (instrumentTypes != null && instrumentTypes.size() == 1) {
      if (!Iterables.getOnlyElement(instrumentTypes).equals(InstrumentTypeProperties.EQUITY_FUTURE_OPTION)) {
        return null;
      }
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      s_logger.error("Function takes only get a single surface. Asked for {}", surfaceNames);
      return null;
    }
    final String givenName = Iterables.getOnlyElement(surfaceNames);
    final String fullName = givenName + "_" + EquitySecurityUtils.getTrimmedTarget(((ExternalIdentifiable) target.getValue()).getExternalId());

    final VolatilitySurfaceSpecification specification = _volatilitySurfaceSpecificationSource.getSpecification(fullName, InstrumentTypeProperties.EQUITY_FUTURE_OPTION);
    if (specification == null) {
      s_logger.error("Could not get volatility surface specification with name " + fullName);
      return null;
    }

    final String quoteUnits = specification.getQuoteUnits();
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, givenName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_FUTURE_OPTION)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, quoteUnits).get();
    final ValueProperties fullNameProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, givenName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_FUTURE_OPTION)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, quoteUnits).get();
    final ValueRequirement surfaceReq = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), properties);
    final ValueRequirement specificationReq = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_SPEC, target.toSpecification(), properties);
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(surfaceReq);
    requirements.add(specificationReq);
    if (quoteUnits.equals(SurfaceAndCubePropertyNames.PRICE_QUOTE)) {
      final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
      if (curveNames == null || curveNames.size() != 1) {
        return null;
      }
      final String curveName = Iterables.getOnlyElement(curveNames);
      //TODO get rid of hard-coding and add to properties
      final String curveCalculationMethod = ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD;
      final ValueProperties curveProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName)
          .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
      final ValueRequirement forwardCurveRequirement = new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), curveProperties);
      requirements.add(forwardCurveRequirement);
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder properties = createValueProperties().with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_FUTURE_OPTION);
    boolean surfaceNameSet = false;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification key = entry.getKey();
      if (key.getValueName().equals(ValueRequirementNames.VOLATILITY_SURFACE_DATA)) {
        properties.with(ValuePropertyNames.SURFACE, key.getProperty(ValuePropertyNames.SURFACE));
        surfaceNameSet = true;
      } else if (key.getValueName().equals(ValueRequirementNames.FORWARD_CURVE)) {
        final ValueProperties curveProperties = key.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).get();
        for (final String property : curveProperties.getProperties()) {
          properties.with(property, curveProperties.getValues(property));
        }
        //don't check if forward curve is set, because it isn't needed if the quotes are volatility
      }
    }
    assert surfaceNameSet;
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), properties.get()));
  }

  private static VolatilitySurfaceData<Double, Double> getSurfaceFromVolatilityQuote(final LocalDate valDate, final VolatilitySurfaceData<Pair<Integer, Tenor>, Double> rawSurface) {
    // Remove empties, convert expiries from number to years, and scale vols
    final Map<Pair<Double, Double>, Double> volValues = new HashMap<>();
    final DoubleArrayList tList = new DoubleArrayList();
    final DoubleArrayList kList = new DoubleArrayList();
    for (final Pair<Integer, Tenor> nthExpiry : rawSurface.getXs()) {
      final double t = FutureOptionExpiries.EQUITY_FUTURE.getFutureOptionTtm(nthExpiry.getFirst(), valDate, nthExpiry.getSecond()); //TODO need information about expiry calculator
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
    final VolatilitySurfaceData<Double, Double> stdVolSurface = new VolatilitySurfaceData<>(rawSurface.getDefinitionName(), rawSurface.getSpecificationName(), rawSurface.getTarget(),
        tList.toArray(new Double[0]), kList.toArray(new Double[0]), volValues);
    return stdVolSurface;
  }

  private static VolatilitySurfaceData<Double, Double> getSurfaceFromPriceQuote(final LocalDate valDate, final VolatilitySurfaceData<Pair<Integer, Tenor>, Double> rawSurface,
      final ForwardCurve forwardCurve, final VolatilitySurfaceSpecification specification) {
    final String surfaceQuoteType = specification.getSurfaceQuoteType();
    double callAboveStrike = 0;
    if (specification.getSurfaceInstrumentProvider() instanceof CallPutSurfaceInstrumentProvider) {
      callAboveStrike = ((CallPutSurfaceInstrumentProvider<?, ?>) specification.getSurfaceInstrumentProvider()).useCallAboveStrike();
    }
    // Remove empties, convert expiries from number to years, and imply vols
    final Map<Pair<Double, Double>, Double> volValues = new HashMap<>();
    final DoubleArrayList tList = new DoubleArrayList();
    final DoubleArrayList kList = new DoubleArrayList();
    for (final Pair<Integer, Tenor> nthExpiry : rawSurface.getXs()) {
      final double t = FutureOptionExpiries.EQUITY_FUTURE.getFutureOptionTtm(nthExpiry.getFirst(), valDate, nthExpiry.getSecond()); //TODO need information about expiry calculator
      final double forward = forwardCurve.getForward(t);
      if (t > 5. / 365.) { // Bootstrapping vol surface to this data causes far more trouble than any gain. The data simply isn't reliable.
        for (final Double strike : rawSurface.getYs()) {
          final Double price = rawSurface.getVolatility(nthExpiry, strike);
          if (price != null) {
            try {
              final double vol;
              if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.CALL_STRIKE)) {
                vol = BlackFormulaRepository.impliedVolatility(price, forward, strike, t, true);
              } else if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.PUT_STRIKE)) {
                vol = BlackFormulaRepository.impliedVolatility(price, forward, strike, t, false);
              } else if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE)) {
                final boolean isCall = strike > callAboveStrike ? true : false;
                vol = BlackFormulaRepository.impliedVolatility(price, forward, strike, t, isCall);
              } else {
                throw new OpenGammaRuntimeException("Cannot handle surface quote type " + surfaceQuoteType);
              }
              tList.add(t);
              kList.add(strike);
              volValues.put(Pairs.of(t, strike), vol);
            } catch (final Exception e) {
            }
          }
        }
      }
    }
    final VolatilitySurfaceData<Double, Double> stdVolSurface = new VolatilitySurfaceData<>(rawSurface.getDefinitionName(), rawSurface.getSpecificationName(), rawSurface.getTarget(),
        tList.toArray(new Double[0]), kList.toArray(new Double[0]), volValues);
    return stdVolSurface;
  }
}
