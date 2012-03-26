/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Source of random, but reasonable, swap security instances.
 */
public abstract class AbstractSwapSecurityGenerator extends SecurityGenerator<SwapSecurity> {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractSwapSecurityGenerator.class);
  private static final Tenor[] TENORS = new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS, Tenor.ofYears(7), Tenor.ofYears(10), Tenor.ofYears(12), Tenor.ofYears(15),
      Tenor.ofYears(20) };

  private int _daysTrading = 60;
  private String _historicalConfigDoc = "DEFAULT_TSS_CONFIG";

  public void setDaysTrading(final int daysTrading) {
    _daysTrading = daysTrading;
  }

  public int getDaysTrading() {
    return _daysTrading;
  }

  public void setHistoricalConfigDoc(final String historicalConfigDoc) {
    _historicalConfigDoc = historicalConfigDoc;
  }

  public String getHistoricalConfigDoc() {
    return _historicalConfigDoc;
  }

  /**
   * Return the curve config name to use.
   * 
   * @return the curve config name
   */
  protected abstract String getCurveConfigName();

  /**
   * Return the time series identifier and data field.
   * 
   * @param liborConvention the convention bundle, not null
   * @return the time series identifier and data field name.
   */
  protected abstract Pair<ExternalId, String> getTimeSeriesConvention(final ConventionBundle liborConvention);

  private ExternalId getSwapRateFor(ConfigSource configSource, Currency ccy, LocalDate tradeDate, Tenor tenor) {
    CurveSpecificationBuilderConfiguration curveSpecConfig = configSource.getByName(CurveSpecificationBuilderConfiguration.class, getCurveConfigName() + "_" + ccy.getCode(), null);
    if (curveSpecConfig == null) {
      return null;
    }
    final ExternalId swapSecurity;
    if (ccy.equals(Currency.USD)) {
      // Standard (i.e. matches convention) floating leg tenor for USD is 3M
      swapSecurity = curveSpecConfig.getSwap3MSecurity(tradeDate, tenor);
    } else {
      // Standard (i.e. matches convention) floating leg tenor for CHF, JPY, GBP, EUR is 6M
      swapSecurity = curveSpecConfig.getSwap6MSecurity(tradeDate, tenor);
    }
    return swapSecurity;
  }

  @Override
  public SwapSecurity createSecurity() {
    final Currency ccy = getRandomCurrency();
    LocalDate tradeDate;
    do {
      tradeDate = LocalDate.now().minusDays(getRandom(getDaysTrading()));
    } while (getHolidaySource().isHoliday(tradeDate, ccy));
    ConventionBundle swapConvention = getConventionSource().getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, ccy.getCode() + "_SWAP"));
    if (swapConvention == null) {
      s_logger.error("Couldn't get swap convention for {}", ccy.getCode());
      return null;
    }
    final Tenor maturity = getRandom(TENORS);
    // get the convention of the identifier of the initial rate
    ConventionBundle liborConvention = getConventionSource().getConventionBundle(swapConvention.getSwapFloatingLegInitialRate());
    if (liborConvention == null) {
      s_logger.error("Couldn't get libor convention for {}", swapConvention.getSwapFloatingLegInitialRate());
      return null;
    }
    // look up the rate timeseries identifier out of the bundle
    final Pair<ExternalId, String> tsConvention = getTimeSeriesConvention(liborConvention);
    final ExternalId tsIdentifier = tsConvention.getFirst();
    final String dataField = tsConvention.getSecond();
    // look up the value on our chosen trade date
    final HistoricalTimeSeries initialRateSeries = getHistoricalSource().getHistoricalTimeSeries(dataField, tsIdentifier.toBundle(), getHistoricalConfigDoc(), tradeDate, true, tradeDate, true);
    if (initialRateSeries == null || initialRateSeries.getTimeSeries().isEmpty()) {
      s_logger.error("couldn't get series for {} on {}", tsIdentifier, tradeDate);
      return null;
    }
    Double initialRate = initialRateSeries.getTimeSeries().getEarliestValue();
    // get the identifier for the swap rate for the maturity we're interested in (assuming the fixed rate will be =~ swap rate)
    final ExternalId swapRateForMaturityIdentifier = getSwapRateFor(getConfigSource(), ccy, tradeDate, maturity);
    if (swapRateForMaturityIdentifier == null) {
      s_logger.error("Couldn't get swap rate identifier for {} [{}] from {}", new Object[] {ccy, maturity, tradeDate });
      return null;
    }
    final HistoricalTimeSeries fixedRateSeries = getHistoricalSource().getHistoricalTimeSeries(dataField, swapRateForMaturityIdentifier.toBundle(), getHistoricalConfigDoc(), tradeDate, true,
        tradeDate, true);
    if (fixedRateSeries == null) {
      s_logger.error("can't find time series for {} on {}", swapRateForMaturityIdentifier, tradeDate);
      return null;
    }
    Double fixedRate = (fixedRateSeries.getTimeSeries().getEarliestValue() + getRandom().nextDouble()) / 100d;
    Double notional = (double) getRandom(100000) * 1000;
    ZonedDateTime tradeDateTime = ZonedDateTime.of(tradeDate, LocalTime.MIDNIGHT, TimeZone.UTC);
    ZonedDateTime maturityDateTime = ZonedDateTime.of(tradeDate.plus(maturity.getPeriod()), LocalTime.MIDNIGHT, TimeZone.UTC);
    String counterparty = "CParty";
    SwapLeg fixedLeg = new FixedInterestRateLeg(swapConvention.getSwapFixedLegDayCount(),
        swapConvention.getSwapFixedLegFrequency(),
        swapConvention.getSwapFixedLegRegion(),
        swapConvention.getSwapFixedLegBusinessDayConvention(),
        new InterestRateNotional(ccy, notional),
        false, fixedRate);
    FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(swapConvention.getSwapFloatingLegDayCount(),
        swapConvention.getSwapFloatingLegFrequency(),
        swapConvention.getSwapFloatingLegRegion(),
        swapConvention.getSwapFloatingLegBusinessDayConvention(),
        new InterestRateNotional(ccy, notional),
        false, tsIdentifier,
        FloatingRateType.IBOR);
    floatingLeg.setInitialFloatingRate(initialRate);
    String fixedLegDescription = RATE_FORMATTER.format(fixedRate);
    String floatingLegDescription = swapConvention.getSwapFloatingLegInitialRate().getValue();
    boolean isPayFixed = getRandom().nextBoolean();
    SwapLeg payLeg;
    String payLegDescription;
    SwapLeg receiveLeg;
    String receiveLegDescription;
    if (isPayFixed) {
      payLeg = fixedLeg;
      payLegDescription = fixedLegDescription;
      receiveLeg = floatingLeg;
      receiveLegDescription = floatingLegDescription;
    } else {
      payLeg = floatingLeg;
      payLegDescription = floatingLegDescription;
      receiveLeg = fixedLeg;
      receiveLegDescription = fixedLegDescription;
    }
    final SwapSecurity swap = new SwapSecurity(tradeDateTime, tradeDateTime, maturityDateTime, counterparty, payLeg, receiveLeg);
    swap.setName("IR Swap " + ccy + " " + NOTIONAL_FORMATTER.format(notional) + " " + maturityDateTime.toString(DATE_FORMATTER) + " - " + payLegDescription + " / " + receiveLegDescription);
    return swap;
  }

}
