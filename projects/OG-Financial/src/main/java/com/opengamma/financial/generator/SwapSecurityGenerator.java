/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Source of random, but reasonable, swap security instances.
 */
public class SwapSecurityGenerator extends SecurityGenerator<SwapSecurity> {

  private static final Logger s_logger = LoggerFactory.getLogger(SwapSecurityGenerator.class);
  private static final Tenor[] TENORS = new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS, Tenor.ofYears(7), Tenor.ofYears(10), Tenor.ofYears(12), Tenor.ofYears(15),
      Tenor.ofYears(20) };

  private int _daysTrading = 60;
  private LocalDate _swaptionExpiry;

  public void setDaysTrading(final int daysTrading) {
    _daysTrading = daysTrading;
  }

  public int getDaysTrading() {
    return _daysTrading;
  }
  
  public void setSwationExpiry(final LocalDate swaptionExpiry) {
    _swaptionExpiry = swaptionExpiry;
  }
  
  public LocalDate getSwaptionExpiry() {
    return _swaptionExpiry;
  }

  /**
   * Return the time series identifier.
   * 
   * @param liborConvention the convention bundle, not null
   * @return the time series identifier
   */
  protected ExternalId getTimeSeriesIdentifier(final ConventionBundle liborConvention) {
    return liborConvention.getIdentifiers().getExternalId(getPreferredScheme());
  }

  private ExternalId getSwapRateFor(Currency ccy, LocalDate tradeDate, Tenor tenor) {
    final CurveSpecificationBuilderConfiguration curveSpecConfig = getCurrencyCurveConfig(ccy);
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
    int i = 0;
    do {
      if (getSwaptionExpiry() == null) { // just a normal swap
        tradeDate = LocalDate.now().minusDays(getRandom(getDaysTrading()));
      } else {
        tradeDate = getSwaptionExpiry().plusDays(2 + i++); // effective date should be at least two days after expiry of swaption.
      }
    } while (getHolidaySource().isHoliday(tradeDate, ccy));
    ConventionBundle swapConvention = getConventionBundleSource().getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, ccy.getCode() + "_SWAP"));
    if (swapConvention == null) {
      s_logger.error("Couldn't get swap convention for {}", ccy.getCode());
      return null;
    }
    final Tenor maturity = getRandom(TENORS);
    // get the convention of the identifier of the initial rate
    ConventionBundle liborConvention = getConventionBundleSource().getConventionBundle(swapConvention.getSwapFloatingLegInitialRate());
    if (liborConvention == null) {
      s_logger.error("Couldn't get libor convention for {}", swapConvention.getSwapFloatingLegInitialRate());
      return null;
    }
    // look up the rate timeseries identifier out of the bundle
    final ExternalId tsIdentifier = getTimeSeriesIdentifier(liborConvention);
    // look up the value on our chosen trade date
    final HistoricalTimeSeries initialRateSeries = getHistoricalSource().getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, tsIdentifier.toBundle(), null, tradeDate,
        true, tradeDate, true);
    if (initialRateSeries == null || initialRateSeries.getTimeSeries().isEmpty()) {
      s_logger.error("couldn't get series for {} on {}", tsIdentifier, tradeDate);
      return null;
    }
    Double initialRate = initialRateSeries.getTimeSeries().getEarliestValue();
    // get the identifier for the swap rate for the maturity we're interested in (assuming the fixed rate will be =~ swap rate)
    final ExternalId swapRateForMaturityIdentifier = getSwapRateFor(ccy, tradeDate, maturity);
    if (swapRateForMaturityIdentifier == null) {
      s_logger.error("Couldn't get swap rate identifier for {} [{}] from {}", new Object[] {ccy, maturity, tradeDate });
      return null;
    }
    final HistoricalTimeSeries fixedRateSeries = getHistoricalSource().getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, swapRateForMaturityIdentifier.toBundle(),
        null, tradeDate, true,
        tradeDate, true);
    if (fixedRateSeries == null) {
      s_logger.error("can't find time series for {} on {}", swapRateForMaturityIdentifier, tradeDate);
      return null;
    }
    Double fixedRate = (fixedRateSeries.getTimeSeries().getEarliestValue() + getRandom().nextDouble()) / 100d;
    Double notional = (double) (getRandom(99999) + 1) * 1000;
    ZonedDateTime tradeDateTime = tradeDate.atStartOfDay(ZoneOffset.UTC);
    ZonedDateTime maturityDateTime = tradeDate.plus(maturity.getPeriod()).atStartOfDay(ZoneOffset.UTC);
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

  @Override
  public ManageableTrade createSecurityTrade(final QuantityGenerator quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    ManageableTrade trade = null;
    final SwapSecurity swap = createSecurity();
    if (swap != null) {
      trade = new ManageableTrade(quantity.createQuantity(), persister.storeSecurity(swap), swap.getTradeDate().toLocalDate(), swap.getTradeDate().toOffsetDateTime().toOffsetTime(), 
          ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
    }
    return trade;
  }

}
