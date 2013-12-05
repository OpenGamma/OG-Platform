/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Source of random, but reasonable, Cap/Floor CMS spread securities.
 */
public class CapFloorCMSSpreadSecurityGenerator extends SecurityGenerator<CapFloorCMSSpreadSecurity> {

  private static final DayCount[] DAY_COUNT = new DayCount[] {DayCounts.ACT_360, DayCounts.THIRTY_U_360 };
  private static final Frequency[] FREQUENCY = new Frequency[] {SimpleFrequency.QUARTERLY, SimpleFrequency.SEMI_ANNUAL, SimpleFrequency.ANNUAL };
  private static final Tenor[] SHORT_TENORS = new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.FIVE_YEARS, Tenor.ofYears(8) };
  private static final Tenor[] LONG_TENORS = new Tenor[] {Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10), Tenor.ofYears(20) };
  private static final double[] STRIKES = new double[] {0.001, 0.0015, 0.002, 0.0025, 0.003, 0.004, 0.005, 0.0075, 0.01, 0.015, 0.02, 0.03, 0.04 };

  protected String createName(final boolean cap, final Tenor tenor1, final Tenor tenor2, final double strike, final ZonedDateTime startDate, final ZonedDateTime maturityDate,
      final Frequency frequency, final Currency currency, final double notional) {
    final StringBuilder sb = new StringBuilder("CMS ");
    sb.append(cap ? "cap" : "floor");
    sb.append(" spread on ").append(tenor1.getPeriod().toString().substring(1)).append(" and ").append(tenor2.getPeriod().toString().substring(1));
    sb.append(" @ ").append(strike).append(" [").append(startDate.toString(DATE_FORMATTER)).append(" - ").append(maturityDate.toString(DATE_FORMATTER)).append("], ");
    sb.append(frequency.getName()).append(", ").append(currency.getCode()).append(' ').append(NOTIONAL_FORMATTER.format(notional));
    return sb.toString();
  }

  private ExternalId getUnderlying(final Currency ccy, final LocalDate tradeDate, final Tenor tenor) {
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
  public CapFloorCMSSpreadSecurity createSecurity() {
    final Currency currency = getRandomCurrency();
    final boolean payer = getRandom().nextBoolean();
    final boolean cap = getRandom().nextBoolean();
    final ZonedDateTime startDate = previousWorkingDay(ZonedDateTime.now().minusDays(getRandom(365) + 7), currency);
    final double notional = (double) getRandom(100000) * 1000;
    Tenor tenor1;
    Tenor tenor2;
    do {
      tenor1 = getRandom(SHORT_TENORS);
      tenor2 = getRandom(LONG_TENORS);
    } while (tenor1.compareTo(tenor2) >= 0);
    final int length = getRandom(tenor2.getPeriod().getYears() - 5) + 3;
    final ZonedDateTime maturityDate = nextWorkingDay(startDate.plusYears(length), currency);
    final ExternalId shortIdentifier = getUnderlying(currency, startDate.toLocalDate(), tenor1);
    final ExternalId longIdentifier = getUnderlying(currency, startDate.toLocalDate(), tenor2);
    final double strike = getRandom(STRIKES);
    final Frequency frequency = getRandom(FREQUENCY);
    final DayCount dayCount = getRandom(DAY_COUNT);
    CapFloorCMSSpreadSecurity security = null;
    if (shortIdentifier != null && longIdentifier != null) {
      security = new CapFloorCMSSpreadSecurity(startDate, maturityDate, notional, longIdentifier, shortIdentifier, strike, frequency, currency, dayCount, payer, cap);
      security.setName(createName(cap, tenor1, tenor2, strike, startDate, maturityDate, frequency, currency, notional));
    }
    return security;
  }

}
