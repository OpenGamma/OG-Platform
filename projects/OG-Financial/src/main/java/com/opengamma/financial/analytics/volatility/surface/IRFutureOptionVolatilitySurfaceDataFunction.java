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
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.irfutureoption.FutureOptionUtils;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.IRFutureAndFutureOptionExpiryCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
//import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class IRFutureOptionVolatilitySurfaceDataFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(IRFutureOptionVolatilitySurfaceDataFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Currency currency = target.getValue(PrimitiveComputationTargetType.CURRENCY);
    final Calendar calendar = new HolidaySourceCalendarAdapter(OpenGammaExecutionContext.getHolidaySource(executionContext), currency);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBVolatilitySurfaceSpecificationSource source = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final String fullSpecificationName = surfaceName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = source.getSpecification(fullSpecificationName, InstrumentTypeProperties.IR_FUTURE_OPTION);
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

    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, surfaceQuoteType)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, surfaceQuoteUnits).get();
    final Object volatilityDataObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties));
    if (volatilityDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Object, Object> surfaceData = (VolatilitySurfaceData<Object, Object>) volatilityDataObject;
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), properties);
    if (surfaceQuoteUnits.equals(SurfaceAndCubePropertyNames.VOLATILITY_QUOTE)) {
      return Collections.singleton(new ComputedValue(spec, getSurfaceFromVolatilityQuote(surfaceData, now, calendar)));
    } else if (surfaceQuoteUnits.equals(SurfaceAndCubePropertyNames.PRICE_QUOTE)) {
      final NodalDoublesCurve futuresPrices = getFuturePricesCurve(target, curveName, inputs);
      final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
      final VolatilitySurfaceData<Double, Double> volSurface = getSurfaceFromPriceQuote(specification, surfaceData, futuresPrices, now, surfaceQuoteType, calendar, securitySource);
      if (volSurface != null) {
        return Collections.singleton(new ComputedValue(spec, volSurface));
      }
      return Collections.emptySet();
    } else {
      throw new OpenGammaRuntimeException("Encountered an unexpected surfaceQuoteUnits. Valid values are found in SurfaceAndCubePropertyNames as VolatilityQuote or PriceQuote.");
    }
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(),
        createValueProperties()
        .withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      s_logger.error("Can only get a single surface; asked for {}", surfaceNames);
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceSpecificationSource source = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final String fullSpecificationName = surfaceName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = source.getSpecification(fullSpecificationName, InstrumentTypeProperties.IR_FUTURE_OPTION);
    if (specification == null) {
      s_logger.error("Could not get volatility surface specification named {}", fullSpecificationName);
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION)
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
      final ValueProperties curveProperties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, curveName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_PRICE).get();
      final ValueRequirement curveRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), curveProperties);
      requirements.add(curveRequirement);
    }
    return requirements;
  }

  /** Build a volatility surface based on Expiry, T, and Strike, K. T is in measured in our standard OG-Analytic years */
  private static VolatilitySurfaceData<Double, Double> getSurfaceFromVolatilityQuote(final VolatilitySurfaceData<Object, Object> optionVolatilities, final ZonedDateTime now,
      final Calendar calendar) {
    final Map<Pair<Double, Double>, Double> volatilityValues = new HashMap<>();
    final DoubleArrayList tList = new DoubleArrayList();
    final DoubleArrayList kList = new DoubleArrayList();
    final LocalDate today = now.toLocalDate();
    for (final Object xObj : optionVolatilities.getXs()) {
      final Number x = (Number) xObj;
      final Double t = FutureOptionUtils.getIRFutureOptionTtm(x.intValue(), today, calendar);
      for (final Object yObj : optionVolatilities.getYs()) {
        Double y = (Double) yObj;
        final Double volatility = optionVolatilities.getVolatility(x, y);
        if (volatility != null) {
          tList.add(t);
          kList.add(y / 100.);
          volatilityValues.put(Pair.of(t, y / 100.), volatility / 100); // TODO Normalisation, could this be done elsewhere?
        }
      }
    }
    return new VolatilitySurfaceData<>(optionVolatilities.getDefinitionName(), optionVolatilities.getSpecificationName(),
        optionVolatilities.getTarget(), tList.toArray(new Double[0]), kList.toArray(new Double[0]), volatilityValues);
  }

  /** Build a volatility surface based on Expiry, T, and Strike, K. T is in measured in our standard OG-Analytic years */
  private static VolatilitySurfaceData<Double, Double> getSurfaceFromPriceQuote(final VolatilitySurfaceSpecification specification,
      final VolatilitySurfaceData<Object, Object> optionPrices, final NodalDoublesCurve futurePrices, final ZonedDateTime now, final String surfaceQuoteType,
      final Calendar calendar, final SecuritySource securitySource) {
    double callAboveStrike = 0;
    final SurfaceInstrumentProvider<Number, Double> instrumentProvider = (SurfaceInstrumentProvider<Number, Double>) specification.getSurfaceInstrumentProvider();
    ExchangeTradedInstrumentExpiryCalculator expiryRule;
    if (instrumentProvider instanceof CallPutSurfaceInstrumentProvider) {
      expiryRule = ((CallPutSurfaceInstrumentProvider<?, ?>) instrumentProvider).getExpiryRuleCalculator();
    } else {
      expiryRule = IRFutureAndFutureOptionExpiryCalculator.getInstance();
    }
    if (specification.getSurfaceInstrumentProvider() instanceof CallPutSurfaceInstrumentProvider) {
      callAboveStrike = ((CallPutSurfaceInstrumentProvider<?, ?>) specification.getSurfaceInstrumentProvider()).useCallAboveStrike();
    }
    final Map<Pair<Double, Double>, Double> volatilityValues = new HashMap<>();
    final DoubleArrayList txList = new DoubleArrayList();
    final DoubleArrayList kList = new DoubleArrayList();
    final LocalDate today = now.toLocalDate();
    for (final Object xObj : optionPrices.getXs()) { // Loop over option expiries
      final Number x = (Number) xObj;
      final LocalDate expiry = expiryRule.getExpiryDate(x.intValue(), today, calendar);
      final Double optionTtm = TimeCalculator.getTimeBetween(today, expiry);
      // Get the corresponding future, which may not share the same expiries as the option itself
      final Double[] futureExpiries = futurePrices.getXData();
      final int nFutures = futureExpiries.length;
      if (nFutures == 0) {
        s_logger.info("No future prices found for surface : " + specification.getName());
        return null;
      }
      // Loop over strikes
      for (final Object yObj : optionPrices.getYs()) {
        final Double y = (Double) yObj;
        try {
          final Double forward = specification.isUseUnderlyingSecurityForExpiry() ? getForwardFromSecurity(securitySource, today, futurePrices, instrumentProvider, x, y) :
            getForwardFromFuturesCurve(futurePrices, futureExpiries, optionTtm);
          final Double price = optionPrices.getVolatility(x, y);
          if (price != null) {
            try {

              // Compute the Black volatility implied from the option price
              final double volatility = getVolatility(surfaceQuoteType, y / 100.0, price, forward, optionTtm, callAboveStrike / 100.);
              if (!CompareUtils.closeEquals(volatility, 0.0)) {
                txList.add(optionTtm);
                kList.add(y / 100.0);
                volatilityValues.put(Pair.of(optionTtm, y / 100.), volatility);
              }
            } catch (final MathException e) {
              s_logger.info("Could not imply volatility for ({}, {}); error was {}", new Object[] {x, y, e.getMessage() });
            } catch (final IllegalArgumentException e) {
              s_logger.info("Could not imply volatility for ({}, {}); error was {}", new Object[] {x, y, e.getMessage() });
            }
          }
        } catch (final IllegalArgumentException e) {
          s_logger.info("Could not imply volatility for ({}, {}); error was {}", new Object[] {x, y, e.getMessage() });
        } catch (final OpenGammaRuntimeException e) {
          s_logger.info("Could not imply volatility for ({}, {}); error was {}", new Object[] {x, y, e.getMessage() });
        }
      }
    }
    return new VolatilitySurfaceData<>(optionPrices.getDefinitionName(), optionPrices.getSpecificationName(),
        optionPrices.getTarget(), txList.toArray(new Double[0]), kList.toArray(new Double[0]), volatilityValues);
  }

  private static double getForwardFromSecurity(final SecuritySource securitySource, final LocalDate today, final NodalDoublesCurve futurePrices,
      final SurfaceInstrumentProvider<Number, Double> instrumentProvider, final Number x, final Double y) {
    ExternalId id = instrumentProvider.getInstrument(x, y, today);
    if (id.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
      id = ExternalSchemes.bloombergTickerSecurityId(id.getValue());
    }
    final Security security = securitySource.getSingle(id.toBundle());
    if (security == null) {
      throw new OpenGammaRuntimeException("Could not get security with id " + id + " from the security source");
    }
    if (!(security instanceof IRFutureOptionSecurity)) {
      throw new OpenGammaRuntimeException("Security in surface that was not an interest rate future option: " + security);
    }
    final IRFutureOptionSecurity irFutureOption = (IRFutureOptionSecurity) security;
    final InterestRateFutureSecurity underlyingFuture = (InterestRateFutureSecurity) securitySource.getSingle(irFutureOption.getUnderlyingId().toBundle());
    if (underlyingFuture == null) {
      throw new OpenGammaRuntimeException("Could not get security with id " + irFutureOption.getUnderlyingId() + " from the security source");
    }
    final LocalDate futureMaturity = underlyingFuture.getExpiry().getExpiry().toLocalDate();
    final double underlyingExpiry1 = futureMaturity.isBefore(today) ? TimeCalculator.getTimeBetween(today, futureMaturity.plusYears(10)) : TimeCalculator.getTimeBetween(today, futureMaturity);
    return futurePrices.getYValue(underlyingExpiry1);
  }

  private static double getForwardFromFuturesCurve(final NodalDoublesCurve futurePrices, final Double[] futureExpiries, final double optionTtm) {
    Double underlyingExpiry;
    int i = 0;
    final int nFutures = futureExpiries.length;
    do {
      underlyingExpiry = futureExpiries[i++];
    } while (underlyingExpiry < optionTtm && i < nFutures);

    if (underlyingExpiry < optionTtm) {
      s_logger.info("Requesting an option price where the underlying future price isn't available. "
          + "Either there are too many expiries in VolatilitySurfaceDefinition or too few in the corresponding FuturePriceCurveDefinition");

    }
    return futurePrices.getYValue(underlyingExpiry);
  }

  /** Futures prices are required to form implied volatilities when the units of the input surface is quoted in prices. */
  private static NodalDoublesCurve getFuturePricesCurve(final ComputationTarget target, final String curveName, final FunctionInputs inputs) {
    if (curveName == null) {
      throw new OpenGammaRuntimeException("Could not get curve name");
    }

    final ValueRequirement futuresRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(),
        ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_PRICE).get());
    final Object futurePricesObject = inputs.getValue(futuresRequirement);
    if (futurePricesObject == null) {
      throw new OpenGammaRuntimeException("Could not get futures price data");
    }
    final NodalDoublesCurve futurePrices = (NodalDoublesCurve) futurePricesObject;
    return futurePrices;
  }

  /** FIXME This function relies on callAboveStrike. Needs a rework */
  private static double getVolatility(final String surfaceQuoteType, final double strike, final double price, final double forward, final double t, final Double callAboveStrike) {
    if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.CALL_STRIKE)) {
      return BlackFormulaRepository.impliedVolatility(price, forward, strike, t, true);
    }
    if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.PUT_STRIKE)) {
      return BlackFormulaRepository.impliedVolatility(price, forward, strike, t, false);
    }
    if (surfaceQuoteType.equals(SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE)) {
      if (callAboveStrike == null) {
        throw new OpenGammaRuntimeException("No value specified for useCallAboveStrikeValue in VolatilitySurfaceSpecification. See Configuration.");
      }
      // Futures Options are priced as options on rates, not prices.
      // A Call ON Futures PRICE is a PUT ON Futures RATE
      final boolean callOnRates = strike < callAboveStrike;
      return BlackFormulaRepository.impliedVolatility(price, 1 - forward, 1 - strike, t, callOnRates);
    }
    throw new OpenGammaRuntimeException("Cannot handle surface quote type " + surfaceQuoteType);
  }

}
