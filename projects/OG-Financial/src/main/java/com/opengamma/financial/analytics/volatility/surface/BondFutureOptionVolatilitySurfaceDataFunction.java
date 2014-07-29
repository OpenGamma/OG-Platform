/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
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
import com.opengamma.financial.convention.expirycalc.BondFutureOptionExpiryCalculator;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class BondFutureOptionVolatilitySurfaceDataFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(BondFutureOptionVolatilitySurfaceDataFunction.class);

  private ConfigDBVolatilitySurfaceSpecificationSource _volatilitySurfaceSpecificationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilitySurfaceSpecificationSource = ConfigDBVolatilitySurfaceSpecificationSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Currency currency = target.getValue(PrimitiveComputationTargetType.CURRENCY);
    final Calendar calendar = new HolidaySourceCalendarAdapter(OpenGammaExecutionContext.getHolidaySource(executionContext), currency);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String fullSpecificationName = surfaceName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = _volatilitySurfaceSpecificationSource.getSpecification(fullSpecificationName, InstrumentTypeProperties.BOND_FUTURE_OPTION);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName);
    }
    String surfaceQuoteType = null;
    String surfaceQuoteUnits = null;
    String curveName = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      final ValueSpecification spec = input.getSpecification();
      final String valueName = spec.getValueName();
      if (valueName.equals(ValueRequirementNames.VOLATILITY_SURFACE_DATA)) {
        surfaceQuoteType = spec.getProperty(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE);
        surfaceQuoteUnits = spec.getProperty(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS);
      } else if (valueName.equals(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA)) {
        curveName = spec.getProperty(ValuePropertyNames.CURVE);
      }
    }
    if (surfaceQuoteType == null) {
      throw new OpenGammaRuntimeException("Could not get surface quote type");
    }
    if (surfaceQuoteUnits == null) {
      throw new OpenGammaRuntimeException("Could not get surface quote units");
    }

    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final ValueProperties surfaceProperties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.BOND_FUTURE_OPTION)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, surfaceQuoteType).with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, surfaceQuoteUnits).get();
    final Object volatilityDataObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties));
    if (volatilityDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Number, Double> surfaceData = (VolatilitySurfaceData<Number, Double>) volatilityDataObject;
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.BOND_FUTURE_OPTION).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), properties);
    if (surfaceQuoteUnits.equals(SurfaceAndCubePropertyNames.VOLATILITY_QUOTE)) {
      return Collections.singleton(new ComputedValue(spec, getSurfaceFromVolatilityQuote(surfaceData, now, calendar)));
    } else if (surfaceQuoteUnits.equals(SurfaceAndCubePropertyNames.PRICE_QUOTE)) {
      final NodalDoublesCurve futuresPrices = getFuturePricesCurve(target, curveName, inputs);
      final VolatilitySurfaceData<Double, Double> volSurface = getSurfaceFromPriceQuote(specification, surfaceData, futuresPrices, now, surfaceQuoteType, calendar);
      if (volSurface != null) {
        return Collections.singleton(new ComputedValue(spec, volSurface));
      }
    }
    throw new OpenGammaRuntimeException("Encountered an unexpected surfaceQuoteUnits. Valid values are found in SurfaceAndCubePropertyNames as VolatilityQuote or PriceQuote.");
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), createValueProperties()
        .withAny(ValuePropertyNames.SURFACE).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.BOND_FUTURE_OPTION).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      s_logger.error("Can only get a single surface; asked for {}", surfaceNames);
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final String fullSpecificationName = surfaceName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = _volatilitySurfaceSpecificationSource.getSpecification(fullSpecificationName, InstrumentTypeProperties.BOND_FUTURE_OPTION);
    if (specification == null) {
      s_logger.error("Could not get volatility surface specification named {}", fullSpecificationName);
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final ValueProperties surfaceProperties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.BOND_FUTURE_OPTION)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits()).get();
    requirements.add(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties));
    if (specification.getQuoteUnits().equals(SurfaceAndCubePropertyNames.PRICE_QUOTE)) { // Term structure of futures prices is also required
      final Set<String> curveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
      final String curveName;
      if (curveNames == null || curveNames.size() != 1) {
        curveName = surfaceName;
      } else {
        curveName = curveNames.iterator().next();
      }
      final ValueProperties curveProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.BOND_FUTURE_PRICE).get();
      final ValueRequirement curveRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), curveProperties);
      requirements.add(curveRequirement);
    }
    return requirements;
  }

  private static VolatilitySurfaceData<Double, Double> getSurfaceFromVolatilityQuote(final VolatilitySurfaceData<Number, Double> optionVolatilities, final ZonedDateTime now,
      final Calendar calendar) {
    final BondFutureOptionExpiryCalculator expiryCalculator = BondFutureOptionExpiryCalculator.getInstance();
    final Map<Pair<Double, Double>, Double> volatilityValues = new HashMap<Pair<Double, Double>, Double>();
    final DoubleArrayList tList = new DoubleArrayList();
    final DoubleArrayList kList = new DoubleArrayList();
    final LocalDate today = now.toLocalDate();
    final Object[] xs = optionVolatilities.getXs();
    for (final Object xObj : xs) {
      Number x = (Number) xObj;
      final Double t = TimeCalculator.getTimeBetween(today, expiryCalculator.getExpiryDate(x.intValue(), today, calendar));
      final Object[] ys = optionVolatilities.getYs();
      for (final Object yObj : ys) {
        Double y = (Double) yObj;
        final Double volatility = optionVolatilities.getVolatility(x, y);
        if (volatility != null) {
          tList.add(t);
          kList.add(y / 100.);
          volatilityValues.put(Pairs.of(t, y / 100.), volatility / 100); // TODO Normalisation, could this be done elsewhere?
        }
      }
    }
    return new VolatilitySurfaceData<Double, Double>(optionVolatilities.getDefinitionName(), optionVolatilities.getSpecificationName(), optionVolatilities.getTarget(),
        tList.toArray(new Double[0]), kList.toArray(new Double[0]), volatilityValues);
  }

  private static VolatilitySurfaceData<Double, Double> getSurfaceFromPriceQuote(final VolatilitySurfaceSpecification specification, final VolatilitySurfaceData<Number, Double> optionPrices,
      final NodalDoublesCurve futurePrices, final ZonedDateTime now, final String surfaceQuoteType, final Calendar calendar) {
    final BondFutureOptionExpiryCalculator expiryCalculator = BondFutureOptionExpiryCalculator.getInstance();
    double callAboveStrike = 0;
    if (specification.getSurfaceInstrumentProvider() instanceof CallPutSurfaceInstrumentProvider) {
      callAboveStrike = ((CallPutSurfaceInstrumentProvider<?, ?>) specification.getSurfaceInstrumentProvider()).useCallAboveStrike();
    }
    final Map<Pair<Double, Double>, Double> volatilityValues = new HashMap<Pair<Double, Double>, Double>();
    final DoubleArrayList txList = new DoubleArrayList();
    final DoubleArrayList kList = new DoubleArrayList();
    final LocalDate today = now.toLocalDate();
    final Double[] futureExpiries = futurePrices.getXData();
    final int nFutures = futureExpiries.length;
    if (nFutures == 0) {
      throw new OpenGammaRuntimeException("No future prices found for surface : " + specification.getName());
    }
    final Object[] xs = optionPrices.getXs();
    for (final Object xObj : xs) {
      final Number x = (Number) xObj;
      // Loop over option expiries
      final int nFutureOption = x.intValue();
      final LocalDate futureOptionExpiryDate = expiryCalculator.getExpiryDate(nFutureOption, today, calendar);
      final Double optionExpiry = TimeCalculator.getTimeBetween(today, futureOptionExpiryDate);
      int nFuture = 0;
      while (optionExpiry > futureExpiries[nFuture]) {
        nFuture++;
      }
      final Double forward = futurePrices.getYValue(futureExpiries[nFuture]);
      // Loop over strikes
      final Object[] ys = optionPrices.getYs();
      for (final Object yObj : ys) {
        final Double y = (Double) yObj;
        final Double price = optionPrices.getVolatility(x, y);
        if (price != null) {
          try {
            final boolean isCall = y > callAboveStrike ? true : false;
            double volatility;
            if (forward > 60) { //TODO quick hack to allow use of PX_SETTLE
              volatility = getVolatility(surfaceQuoteType, y / 100.0, price / 100, forward / 100, optionExpiry, isCall);
            } else {
              volatility = getVolatility(surfaceQuoteType, y / 100.0, price, forward, optionExpiry, isCall);
            }
            if (!CompareUtils.closeEquals(volatility, 0.0)) {
              txList.add(optionExpiry);
              kList.add(y / 100.0);
              volatilityValues.put(Pairs.of(optionExpiry, y / 100.), volatility);
            }
          } catch (final MathException e) {
            s_logger.info("Could not imply volatility for ({}, {}); error was {}", new Object[] {x, y, e.getMessage() });
          } catch (final IllegalArgumentException e) {
            s_logger.error("Could not imply volatility for future option number={}, strike={}; error was {}", new Object[] {x, y, e.getMessage() });
          }
        }
      }
    }
    return new VolatilitySurfaceData<Double, Double>(optionPrices.getDefinitionName(), optionPrices.getSpecificationName(), optionPrices.getTarget(), txList.toArray(new Double[0]),
        kList.toArray(new Double[0]), volatilityValues);
  }

  private static NodalDoublesCurve getFuturePricesCurve(final ComputationTarget target, final String curveName, final FunctionInputs inputs) {
    if (curveName == null) {
      throw new OpenGammaRuntimeException("Could not get curve name");
    }

    final ValueRequirement futuresRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.BOND_FUTURE_PRICE).get());
    final Object futurePricesObject = inputs.getValue(futuresRequirement);
    if (futurePricesObject == null) {
      throw new OpenGammaRuntimeException("Could not get futures price data");
    }
    final NodalDoublesCurve futurePrices = (NodalDoublesCurve) futurePricesObject;
    return futurePrices;
  }

  private static double getVolatility(final String surfaceQuoteType, final double strike, final double price, final double forward, final double t, final boolean isCall) {
    if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.CALL_STRIKE)) {
      return BlackFormulaRepository.impliedVolatility(price, forward, strike, t, true);
    }
    if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.PUT_STRIKE)) {
      return BlackFormulaRepository.impliedVolatility(price, forward, strike, t, false);
    }
    if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE)) {
      return BlackFormulaRepository.impliedVolatility(price, forward, strike, t, isCall);
    }
    throw new OpenGammaRuntimeException("Cannot handle surface quote type " + surfaceQuoteType);
  }

}
