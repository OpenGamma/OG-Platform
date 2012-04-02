/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.time.calendar.DateProvider;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.DummyFunctionReinitializer;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.ConfigDBInterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.ConfigDBInterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveMarketDataFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveSpecificationFunction;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.functional.Function2;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Utility class for constructing parameters to random (but reasonable) securities.
 * 
 * @param <T> the security type, or a common super type if multiple types are being produced
 */
public abstract class SecurityGenerator<T extends ManageableSecurity> {

  private static final Logger s_logger = LoggerFactory.getLogger(SecurityGenerator.class);

  /**
   * Format dates.
   */
  public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();

  /**
   * Format rates.
   */
  public static final DecimalFormat RATE_FORMATTER = new DecimalFormat("0.###%");

  /**
   * Format notionals.
   */
  public static final DecimalFormat NOTIONAL_FORMATTER = new DecimalFormat("0,000");

  /**
   * Constant for the length of a year in days.
   */
  protected static final double YEAR_LENGTH = 365.25;

  private Random _random = new Random();
  private ConventionBundleSource _conventionSource;
  private ConfigSource _configSource;
  private HolidaySource _holidaySource;
  private HistoricalTimeSeriesSource _historicalSource;
  private RegionSource _regionSource;
  private ExchangeMaster _exchangeMaster;
  private SecurityMaster _securityMaster;
  private String _currencyCurveName;
  private ExternalScheme _preferredScheme;
  private Function2<Currency, Currency, ExternalId> _spotRateIdentifier;

  public Random getRandom() {
    return _random;
  }

  public void setRandom(final Random random) {
    _random = random;
  }

  protected int getRandom(final int n) {
    return getRandom().nextInt(n);
  }

  protected double getRandom(final double low, final double high) {
    return low + (high - low) * getRandom().nextDouble();
  }

  protected <X> X getRandom(final X[] xs) {
    return xs[getRandom(xs.length)];
  }

  protected <X> X getRandom(final List<X> xs) {
    return xs.get(getRandom(xs.size()));
  }

  protected int getRandom(final int[] xs) {
    return xs[getRandom(xs.length)];
  }

  protected double getRandom(final double[] xs) {
    return xs[getRandom(xs.length)];
  }

  public ConventionBundleSource getConventionSource() {
    return _conventionSource;
  }

  public void setConventionSource(final ConventionBundleSource conventionSource) {
    _conventionSource = conventionSource;
  }

  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public void setConfigSource(final ConfigSource configSource) {
    _configSource = configSource;
  }

  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  public void setHolidaySource(final HolidaySource holidaySource) {
    _holidaySource = holidaySource;
  }

  public HistoricalTimeSeriesSource getHistoricalSource() {
    return _historicalSource;
  }

  public void setHistoricalSource(final HistoricalTimeSeriesSource historicalSource) {
    _historicalSource = historicalSource;
  }

  public ExchangeMaster getExchangeMaster() {
    return _exchangeMaster;
  }

  public void setExchangeMaster(final ExchangeMaster exchangeMaster) {
    _exchangeMaster = exchangeMaster;
  }

  public RegionSource getRegionSource() {
    return _regionSource;
  }

  public void setRegionSource(final RegionSource regionSource) {
    _regionSource = regionSource;
  }

  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  public void setSecurityMaster(final SecurityMaster securityMaster) {
    _securityMaster = securityMaster;
  }

  public String getCurrencyCurveName() {
    return _currencyCurveName;
  }

  public void setCurrencyCurveName(final String currencyCurveName) {
    _currencyCurveName = currencyCurveName;
  }

  protected CurveSpecificationBuilderConfiguration getCurrencyCurveConfig(final Currency currency) {
    return getConfigSource().getByName(CurveSpecificationBuilderConfiguration.class, getCurrencyCurveName() + "_" + currency.getCode(), null);
  }

  public Function2<Currency, Currency, ExternalId> getSpotRateIdentifier() {
    return _spotRateIdentifier;
  }

  public void setSpotRateIdentifier(final Function2<Currency, Currency, ExternalId> spotRateIdentifier) {
    _spotRateIdentifier = spotRateIdentifier;
  }

  private FunctionExecutionContext createFunctionExecutionContext(final LocalDate valuationTime) {
    final FunctionExecutionContext context = new FunctionExecutionContext();
    context.setValuationTime(ZonedDateTime.of(valuationTime, LocalTime.MIDDAY, TimeZone.UTC).toInstant());
    context.setValuationClock(DateUtils.fixedClockUTC(context.getValuationTime()));
    OpenGammaExecutionContext.setHolidaySource(context, getHolidaySource());
    OpenGammaExecutionContext.setRegionSource(context, getRegionSource());
    OpenGammaExecutionContext.setConventionBundleSource(context, getConventionSource());
    OpenGammaExecutionContext.setSecuritySource(context, new MasterSecuritySource(getSecurityMaster()));
    OpenGammaExecutionContext.setHistoricalTimeSeriesSource(context, getHistoricalSource());
    return context;
  }

  private FunctionCompilationContext createFunctionCompilationContext() {
    final FunctionCompilationContext context = new FunctionCompilationContext();
    OpenGammaCompilationContext.setInterpolatedYieldCurveDefinitionSource(context, new ConfigDBInterpolatedYieldCurveDefinitionSource(getConfigSource()));
    OpenGammaCompilationContext.setInterpolatedYieldCurveSpecificationBuilder(context, new ConfigDBInterpolatedYieldCurveSpecificationBuilder(getConfigSource()));
    context.setFunctionReinitializer(new DummyFunctionReinitializer());
    OpenGammaCompilationContext.setHolidaySource(context, getHolidaySource());
    OpenGammaCompilationContext.setRegionSource(context, getRegionSource());
    OpenGammaCompilationContext.setConventionBundleSource(context, getConventionSource());
    OpenGammaCompilationContext.setSecuritySource(context, new MasterSecuritySource(getSecurityMaster()));
    return context;
  }

  private CompiledFunctionDefinition createFunction(final FunctionCompilationContext compContext, final FunctionExecutionContext execContext, final AbstractFunction function) {
    function.setUniqueId(function.getClass().getSimpleName());
    function.init(compContext);
    return function.compile(compContext, execContext.getValuationTime());
  }
  
  private ComputedValue execute(final FunctionExecutionContext context, final CompiledFunctionDefinition function, final ComputationTarget target, final ValueRequirement output,
      final ComputedValue... inputs) {
    final FunctionInputsImpl functionInputs = new FunctionInputsImpl();
    for (ComputedValue input : inputs) {
      functionInputs.addValue(input);
    }
    return function.getFunctionInvoker().execute(context, functionInputs, target, Collections.singleton(output)).iterator().next();
  }

  private ComputedValue findMarketData(final ValueRequirement requirement) {
    final Pair<LocalDate, Double> value = getHistoricalSource().getLatestDataPoint(MarketDataRequirementNames.MARKET_VALUE, requirement.getTargetSpecification().getIdentifier().toBundle(), null);
    if (value == null) {
      return null;
    }
    return new ComputedValue(new ValueSpecification(requirement, "MARKET DATA"), value.getSecond());
  }

  private ComputedValue[] findMarketData(final Collection<ValueRequirement> requirements) {
    s_logger.debug("Resolving {}", requirements);
    final ComputedValue[] values = new ComputedValue[requirements.size()];
    int i = 0;
    for (ValueRequirement requirement : requirements) {
      final ComputedValue value = findMarketData(requirement);
      if (value == null) {
        s_logger.debug("Couldn't resolve {}", requirement);
        return null;
      }
      values[i++] = value;
    }
    return values;
  }

  protected Double getApproxFXRate(final LocalDate date, final Pair<Currency, Currency> currencies) {
    final Currency payCurrency;
    final Currency receiveCurrency;
    if (FXUtils.isInBaseQuoteOrder(currencies.getFirst(), currencies.getSecond())) {
      payCurrency = currencies.getFirst();
      receiveCurrency = currencies.getSecond();
    } else {
      payCurrency = currencies.getSecond();
      receiveCurrency = currencies.getFirst();
    }
    final ExternalId spotRateIdentifier = getSpotRateIdentifier().execute(payCurrency, receiveCurrency);
    final Pair<LocalDate, Double> spotRate = getHistoricalSource().getLatestDataPoint(MarketDataRequirementNames.MARKET_VALUE,
        spotRateIdentifier.toBundle(), null);
    if (spotRate == null) {
      s_logger.debug("No spot rate for {}", spotRateIdentifier);
      return null;
    }
    s_logger.debug("Got spot rate {} for {}", spotRate, spotRateIdentifier);
    final FunctionExecutionContext execContext = createFunctionExecutionContext(spotRate.getFirst());
    final FunctionCompilationContext compContext = createFunctionCompilationContext();
    final CompiledFunctionDefinition payYieldCurveSpecificationFunction = createFunction(compContext, execContext, new YieldCurveSpecificationFunction(payCurrency, getCurrencyCurveName()));
    final CompiledFunctionDefinition payYieldCurveMarketDataFunction = createFunction(compContext, execContext, new YieldCurveMarketDataFunction(payCurrency, getCurrencyCurveName()));
    final CompiledFunctionDefinition receiveYieldCurveSpecificationFunction = createFunction(compContext, execContext, new YieldCurveSpecificationFunction(receiveCurrency, getCurrencyCurveName()));
    final CompiledFunctionDefinition receiveYieldCurveMarketDataFunction = createFunction(compContext, execContext, new YieldCurveMarketDataFunction(receiveCurrency, getCurrencyCurveName()));
    final CompiledFunctionDefinition marketInstrumentImpliedYieldCurveFunction = createFunction(compContext, execContext, new MarketInstrumentImpliedYieldCurveFunction(
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    final CompiledFunctionDefinition fxForwardCurveFromYieldCurveFunction = createFunction(compContext, execContext, new FXForwardCurveFromYieldCurveFunction());
    ComputationTarget target;
    // PAY
    target = new ComputationTarget(payCurrency);
    // PAY - YieldCurveMarketDataFunction
    final ComputedValue[] payCurveDataRequirements = findMarketData(payYieldCurveMarketDataFunction.getRequirements(compContext, target, null));
    if (payCurveDataRequirements == null) {
      s_logger.debug("Missing market data for curve on {}", payCurrency);
      return null;
    }
    final ComputedValue payCurveMarketData = execute(execContext, payYieldCurveMarketDataFunction, target, null, payCurveDataRequirements);
    // PAY - YieldCurveSpecificationFunction
    final ComputedValue payCurveSpec = execute(execContext, payYieldCurveSpecificationFunction, target, new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, target.toSpecification(),
        ValueProperties.none()), payCurveMarketData);
    // PAY - MarketInstrumentImpliedYieldCurveFunction
    final ComputedValue payCurve = execute(execContext, marketInstrumentImpliedYieldCurveFunction, target, new ValueRequirement(ValueRequirementNames.YIELD_CURVE, target.toSpecification(),
            ValueProperties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getCurrencyCurveName()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getCurrencyCurveName())
                .with(ValuePropertyNames.CURVE, getCurrencyCurveName()).get()), payCurveSpec, payCurveMarketData);
    // RECEIVE
    target = new ComputationTarget(receiveCurrency);
    // RECEIVE - YieldCurveMarketDataFunction
    final ComputedValue[] receiveCurveDataRequirements = findMarketData(receiveYieldCurveMarketDataFunction.getRequirements(compContext, target, null));
    if (receiveCurveDataRequirements == null) {
      s_logger.debug("Missing market data for curve on {}", receiveCurrency);
      return null;
    }
    final ComputedValue receiveCurveMarketData = execute(execContext, receiveYieldCurveMarketDataFunction, target, null, receiveCurveDataRequirements);
    // RECEIVE - YieldCurveSpecificationFunction
    final ComputedValue receiveCurveSpec = execute(execContext, receiveYieldCurveSpecificationFunction, target, new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, target.toSpecification(),
        ValueProperties.none()), receiveCurveMarketData);
    // RECEIVE - MarketInstrumentImpliedYieldCurveFunction
    final ComputedValue receiveCurve = execute(execContext, marketInstrumentImpliedYieldCurveFunction, target, new ValueRequirement(ValueRequirementNames.YIELD_CURVE, target.toSpecification(),
        ValueProperties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getCurrencyCurveName()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getCurrencyCurveName())
            .with(ValuePropertyNames.CURVE, getCurrencyCurveName()).get()), receiveCurveSpec, receiveCurveMarketData);
    // FXForwardCurveFromYieldCurveFunction
    target = new ComputationTarget(UnorderedCurrencyPair.of(payCurrency, receiveCurrency));
    final ForwardCurve fxForwardCurve = (ForwardCurve) execute(
        execContext,
        fxForwardCurveFromYieldCurveFunction,
        target,
        new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), ValueProperties.with(ValuePropertyNames.CURVE, getCurrencyCurveName()).get()),
        payCurve,
        receiveCurve,
        new ComputedValue(ValueSpecification.of(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE,
            UniqueId.of(spotRateIdentifier.getScheme().getName(), spotRateIdentifier.getValue()),
            ValueProperties.with(ValuePropertyNames.FUNCTION, "SPOT").get()), spotRate.getSecond())).getValue();
    double rate = fxForwardCurve.getForward((double) Period.between(spotRate.getFirst(), date).getDays() / YEAR_LENGTH);
    if (!FXUtils.isInBaseQuoteOrder(currencies.getFirst(), currencies.getSecond())) {
      rate = 1 / rate;
    }
    s_logger.debug("Calculated rate {} for {} on {}", new Object[] {rate, currencies, date });
    return rate;
  }

  public ExternalScheme getPreferredScheme() {
    return _preferredScheme;
  }

  public void setPreferredScheme(final ExternalScheme preferredScheme) {
    _preferredScheme = preferredScheme;
  }

  public static Currency[] getDefaultCurrencies() {
    return new Currency[] {Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF };
  }

  public Currency[] getCurrencies() {
    return getDefaultCurrencies();
  }

  protected Currency getRandomCurrency() {
    return getRandom(getCurrencies());
  }

  private boolean isWorkday(final DayOfWeek dow, final Currency currency) {
    // TODO: use a proper convention/holiday source
    return dow.getValue() < 6;
  }

  private boolean isHoliday(final DateProvider ldp, final Currency currency) {
    return getHolidaySource().isHoliday(ldp.toLocalDate(), currency);
  }

  /**
   * Returns the date unchanged if this is a working day, otherwise advances the date.
   * 
   * @param zdt the date to consider
   * @param currency the currency identifying the holiday zone
   * @return the original or adjusted date
   */
  // TODO: replace this with a date adjuster
  protected ZonedDateTime nextWorkingDay(ZonedDateTime zdt, final Currency currency) {
    while (!isWorkday(zdt.getDayOfWeek(), currency) || isHoliday(zdt, currency)) {
      zdt = zdt.plusDays(1);
    }
    return zdt;
  }

  protected ZonedDateTime nextWorkingDay(ZonedDateTime zdt, final Currency... currencies) {
    ArgumentChecker.isTrue(currencies.length > 0, "currencies");
    do {
      for (Currency currency : currencies) {
        if (!isWorkday(zdt.getDayOfWeek(), currency) || isHoliday(zdt, currency)) {
          zdt = zdt.plusDays(1);
          continue;
        }
      }
      return zdt;
    } while (true);
  }

  /**
   * Returns the date unchanged if this is a working day, otherwise retreats the date.
   * 
   * @param zdt the date to consider
   * @param currency the currency identifying the holiday zone
   * @return the original or adjusted date
   */
  // TODO: replace this with a date adjuster
  protected ZonedDateTime previousWorkingDay(ZonedDateTime zdt, final Currency currency) {
    while (!isWorkday(zdt.getDayOfWeek(), currency) || isHoliday(zdt, currency)) {
      zdt = zdt.minusDays(1);
    }
    return zdt;
  }

  protected ZonedDateTime previousWorkingDay(ZonedDateTime zdt, final Currency... currencies) {
    ArgumentChecker.isTrue(currencies.length > 0, "currencies");
    do {
      for (Currency currency : currencies) {
        if (!isWorkday(zdt.getDayOfWeek(), currency) || isHoliday(zdt, currency)) {
          zdt = zdt.minusDays(1);
          continue;
        }
      }
      return zdt;
    } while (true);
  }

  /**
   * Creates a new random, but reasonable, security.
   * 
   * @return the new security, or null if no security can be generated
   */
  public abstract T createSecurity();

  /**
   * Creates a new random, but reasonable, trade.
   * 
   * @param quantityGenerator the supplied quantity generator
   * @param securityPersister the supplied security persister
   * @return the new trade, or null if no trade can be generated
   */
  public ManageableTrade createSecurityTrade(final QuantityGenerator quantityGenerator, final SecurityPersister securityPersister) {
    return null;
  }

}
