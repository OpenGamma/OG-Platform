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

import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Source of random, but reasonable, Cap/Floor securities.
 */
public class CapFloorSecurityGenerator extends SecurityGenerator<CapFloorSecurity> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(CapFloorSecurityGenerator.class);

  private static final DayCount[] DAY_COUNT = new DayCount[] {DayCounts.ACT_360, DayCounts.THIRTY_U_360 };
  private static final Frequency[] FREQUENCY = new Frequency[] {SimpleFrequency.QUARTERLY, SimpleFrequency.SEMI_ANNUAL, SimpleFrequency.ANNUAL };
  private static final Tenor[] TENORS = new Tenor[] {Tenor.TWO_YEARS, Tenor.FIVE_YEARS, Tenor.ofYears(10), Tenor.ofYears(20) };
  private static final Tenor[] IBOR_TENORS = new Tenor[] {Tenor.ONE_DAY, Tenor.TWO_DAYS, Tenor.THREE_DAYS, Tenor.ofDays(7), Tenor.ofDays(14), Tenor.ofDays(21), 
    Tenor.ONE_MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, Tenor.SEVEN_MONTHS, Tenor.EIGHT_MONTHS, 
    Tenor.NINE_MONTHS, Tenor.TEN_MONTHS, Tenor.ELEVEN_MONTHS};

  public static String createName(final boolean ibor, final boolean cap, final double strike, final ZonedDateTime startDate, final ZonedDateTime maturityDate, final Frequency frequency,
      final Currency currency, final double notional) {
    final StringBuilder sb = new StringBuilder();
    sb.append(ibor ? "Ibor " : "CMS ");
    sb.append(cap ? "cap " : "floor");
    sb.append(" @ ").append(strike).append(" [").append(startDate.toString(DATE_FORMATTER)).append(" - ").append(maturityDate.toString(DATE_FORMATTER)).append("], ");
    sb.append(frequency.getName()).append(", ").append(currency.getCode()).append(' ').append(NOTIONAL_FORMATTER.format(notional));
    return sb.toString();
  }

  private ExternalId getUnderlying(final Currency ccy, final LocalDate tradeDate, final Tenor tenor, final boolean ibor) {
    final CurveSpecificationBuilderConfiguration curveSpecConfig = getCurrencyCurveConfig(ccy);
    if (curveSpecConfig == null) {
      return null;
    }
    if (ibor) {
      return curveSpecConfig.getLiborSecurity(tradeDate, tenor);
    } else {
      if (ccy.equals(Currency.USD)) {
        return curveSpecConfig.getSwap3MSecurity(tradeDate, tenor);
      } else {
        return curveSpecConfig.getSwap6MSecurity(tradeDate, tenor);
      }
    }
  }

  @Override
  public CapFloorSecurity createSecurity() {
    final Currency currency = getRandomCurrency();
    final boolean payer = getRandom().nextBoolean();
    final boolean cap = getRandom().nextBoolean();
    final boolean ibor = getRandom().nextBoolean();
    final ZonedDateTime startDate = previousWorkingDay(ZonedDateTime.now().minusDays(getRandom(365) + 7), currency);
    final int length = getRandom(22) + 3;
    final ZonedDateTime maturityDate = nextWorkingDay(startDate.plusYears(length), currency);
    final double notional = (double) getRandom(100000) * 1000;
    ExternalId underlyingIdentifier = null;
    Tenor tenor = ibor ? getRandom(IBOR_TENORS) : getRandom(TENORS);
    try {
      underlyingIdentifier = getUnderlying(currency, startDate.toLocalDate(), tenor, ibor);
      if (underlyingIdentifier == null) {
        return null;
      }
    } catch (Exception ex) {
      s_logger.warn("Unable to obtain underlying id for " + currency + " " + startDate.toLocalDate() + " " + tenor, ex);
      return null;
    }
    final double strike = 0.01 + (double) getRandom(6) / 200;
    final Frequency frequency = getRandom(FREQUENCY);
    final DayCount dayCount = getRandom(DAY_COUNT);
    final CapFloorSecurity capFloor = new CapFloorSecurity(startDate, maturityDate, notional, underlyingIdentifier, strike, frequency, currency, dayCount, payer, cap, ibor);
    capFloor.setName(createName(ibor, cap, strike, startDate, maturityDate, frequency, currency, notional));
    return capFloor;
  }

}
