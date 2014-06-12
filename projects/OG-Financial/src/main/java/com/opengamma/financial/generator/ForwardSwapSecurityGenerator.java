/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Source of random, but reasonable, forward swap security instances.
 */
public class ForwardSwapSecurityGenerator extends SecurityGenerator<ForwardSwapSecurity> {

  private static final Logger s_logger = LoggerFactory.getLogger(ForwardSwapSecurityGenerator.class);
  private static final Tenor[] TENORS = new Tenor[] {Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS };
  private static final Tenor[] FORWARDS = new Tenor[] {Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, Tenor.NINE_MONTHS };

  private int _daysTrading = 30;

  public void setDaysTrading(final int daysTrading) {
    _daysTrading = daysTrading;
  }

  public int getDaysTrading() {
    return _daysTrading;
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

  private ExternalId getSwapRateFor(Currency ccy, LocalDate tradeDate, Tenor maturityTenor, Tenor forwardTenor) {
    final CurveSpecificationBuilderConfiguration curveSpecConfig = getCurrencyCurveConfig(ccy);
    if (curveSpecConfig == null) {
      return null;
    }
    final ExternalId swapSecurity;
    final Tenor tenor;
    final int months = (int) maturityTenor.getPeriod().toTotalMonths() + (int) forwardTenor.getPeriod().toTotalMonths();
    if (months < 12) {
      tenor = Tenor.ofMonths(months);
    } else {
      // TODO: this isn't particularly great
      tenor = Tenor.ofYears(months / 12);
    }
    try {
      if (ccy.equals(Currency.USD)) {
        // Standard (i.e. matches convention) floating leg tenor for USD is 3M
        swapSecurity = curveSpecConfig.getSwap3MSecurity(tradeDate, tenor);
      } else {
        // Standard (i.e. matches convention) floating leg tenor for CHF, JPY, GBP, EUR is 6M
        swapSecurity = curveSpecConfig.getSwap6MSecurity(tradeDate, tenor);
      }
    } catch (OpenGammaRuntimeException e) {
      return null;
    }
    return swapSecurity;
  }

  @Override
  public ForwardSwapSecurity createSecurity() {
    final Currency ccy = getRandomCurrency();
    final ZonedDateTime now = ZonedDateTime.now();
    final ZonedDateTime tradeDate = previousWorkingDay(now.minusDays(getRandom(getDaysTrading())), ccy);
    final Tenor forward = getRandom(FORWARDS);
    final ZonedDateTime forwardDate = nextWorkingDay(now.plus(forward.getPeriod()), ccy);
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
    final HistoricalTimeSeries initialRateSeries = getHistoricalSource().getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, tsIdentifier.toBundle(), null, tradeDate.toLocalDate(),
        true, tradeDate.toLocalDate(), true);
    if (initialRateSeries == null || initialRateSeries.getTimeSeries().isEmpty()) {
      s_logger.error("couldn't get series for {} on {}", tsIdentifier, tradeDate);
      return null;
    }
    Double initialRate = initialRateSeries.getTimeSeries().getEarliestValue();
    // get the identifier for the swap rate for the maturity we're interested in (assuming the fixed rate will be =~ swap rate)
    final ExternalId swapRateForMaturityIdentifier = getSwapRateFor(ccy, tradeDate.toLocalDate(), maturity, forward);
    if (swapRateForMaturityIdentifier == null) {
      s_logger.error("Couldn't get swap rate identifier for {} [{}] from {}", new Object[] {ccy, maturity, tradeDate });
      return null;
    }
    final HistoricalTimeSeries fixedRateSeries = getHistoricalSource().getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, swapRateForMaturityIdentifier.toBundle(),
        null, tradeDate.toLocalDate(), true,
        tradeDate.toLocalDate(), true);
    if (fixedRateSeries == null) {
      s_logger.error("can't find time series for {} on {}", swapRateForMaturityIdentifier, tradeDate);
      return null;
    }
    Double fixedRate = (fixedRateSeries.getTimeSeries().getEarliestValue() + getRandom().nextDouble()) / 100d;
    Double notional = (double) getRandom(100000) * 1000;
    ZonedDateTime maturityDate = forwardDate.plus(maturity.getPeriod());
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
    final ForwardSwapSecurity swap = new ForwardSwapSecurity(tradeDate, tradeDate, maturityDate, counterparty, payLeg, receiveLeg, forwardDate);
    swap.setName("IR Forward Swap " + ccy + " " + NOTIONAL_FORMATTER.format(notional) + " " + maturity.getPeriod() + " from " + forwardDate.toString(DATE_FORMATTER) + " - " + payLegDescription +
        " / " + receiveLegDescription);
    return swap;
  }

  @Override
  public ManageableTrade createSecurityTrade(final QuantityGenerator quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    final ForwardSwapSecurity swap = createSecurity();
    if (swap != null) {
      return new ManageableTrade(quantity.createQuantity(), persister.storeSecurity(swap), swap.getTradeDate().toLocalDate(), swap.getTradeDate().toOffsetDateTime().toOffsetTime(), 
          ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
    } else {
      return null;
    }
  }

}
