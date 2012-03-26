/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * Source of random, but reasonable, cash security instances.
 */
public class CashSecurityGenerator extends SecurityGenerator<CashSecurity> {

  private static final double[] RATES = new double[] {0.001, 0.002, 0.005, 0.01, 0.015, 0.02, 0.03, 0.05 };
  private static final DayCount[] DAY_COUNT = new DayCount[] {
      DayCountFactory.INSTANCE.getDayCount("Act/360"),
      DayCountFactory.INSTANCE.getDayCount("Act/365"),
      DayCountFactory.INSTANCE.getDayCount("30/360")
  };

  protected String createName(final Currency currency, final double amount, final double rate, final ZonedDateTime maturity) {
    final StringBuilder sb = new StringBuilder();
    sb.append("Cash ").append(currency.getCode()).append(" ").append(NOTIONAL_FORMATTER.format(amount));
    sb.append(" @ ").append(RATE_FORMATTER.format(rate)).append(", maturity ").append(maturity.toString(DATE_FORMATTER));
    return sb.toString();
  }

  @Override
  public CashSecurity createSecurity() {
    final Currency currency = getRandomCurrency();
    final ExternalId region = RegionUtils.currencyRegionId(currency);
    final ZonedDateTime start = previousWorkingDay(ZonedDateTime.now().minusDays(getRandom(60) + 7), currency);
    final ZonedDateTime maturity = nextWorkingDay(start.plusMonths(getRandom(6) + 3), currency);
    final DayCount dayCount = getRandom(DAY_COUNT);
    final double rate = getRandom(RATES);
    final double amount = 10000 * (getRandom(1500) + 200);
    final CashSecurity security = new CashSecurity(currency, region, start, maturity, dayCount, rate, amount);
    security.setName(createName(currency, amount, rate, maturity));
    return security;
  }

}
