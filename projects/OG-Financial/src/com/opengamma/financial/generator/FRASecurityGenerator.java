/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Source of random, but reasonable, FRA security instances.
 */
public class FRASecurityGenerator extends SecurityGenerator<FRASecurity> {

  protected String createName(final Currency currency, final double amount, final double rate, final ZonedDateTime maturity) {
    final StringBuilder sb = new StringBuilder();
    sb.append("FRA ").append(currency.getCode()).append(" ").append(NOTIONAL_FORMATTER.format(amount));
    sb.append(" @ ").append(RATE_FORMATTER.format(rate)).append(", maturity ").append(maturity.toString(DATE_FORMATTER));
    return sb.toString();
  }

  private ExternalId getUnderlyingRate(final Currency ccy, final LocalDate tradeDate, final Tenor tenor) {
    final CurveSpecificationBuilderConfiguration curveSpecConfig = getCurrencyCurveConfig(ccy);
    if (curveSpecConfig == null) {
      return null;
    }
    return curveSpecConfig.getLiborSecurity(tradeDate, tenor);
  }

  @Override
  public FRASecurity createSecurity() {
    final Currency currency = getRandomCurrency();
    final ExternalId region = RegionUtils.currencyRegionId(currency);
    final ZonedDateTime start = previousWorkingDay(ZonedDateTime.now().minusDays(getRandom(60) + 7), currency);
    final int length = getRandom(11) + 1;
    final ZonedDateTime maturity = nextWorkingDay(start.plusMonths(length), currency);
    final ZonedDateTime fixingDate = previousWorkingDay(maturity.minusDays(4), currency);
    final ExternalId underlyingIdentifier = getUnderlyingRate(currency, start.toLocalDate(), Tenor.ofMonths(length));
    final HistoricalTimeSeries underlyingSeries = getHistoricalSource().getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, underlyingIdentifier.toBundle(), null, start.toLocalDate(),
        true, start.toLocalDate(), true);
    if ((underlyingSeries == null) || underlyingSeries.getTimeSeries().isEmpty()) {
      return null;
    }
    final double rate = underlyingSeries.getTimeSeries().getEarliestValue() * getRandom(0.5, 1.5);
    final double amount = 10000 * (getRandom(1500) + 200);
    final FRASecurity security = new FRASecurity(currency, region, start, maturity, rate, amount, underlyingIdentifier, fixingDate);
    security.setName(createName(currency, amount, rate, maturity));
    return security;
  }

}
