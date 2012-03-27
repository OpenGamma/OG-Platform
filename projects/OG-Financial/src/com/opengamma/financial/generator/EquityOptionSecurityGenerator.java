/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Pair;

/**
 * Source of random, but reasonable, equity option security instances.
 */
public class EquityOptionSecurityGenerator extends SecurityGenerator<EquityOptionSecurity> {

  private final EquitySecurity _underlying;
  private final Sequence _sequence = new Sequence();

  public EquityOptionSecurityGenerator(final EquitySecurity underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected EquitySecurity getUnderlying() {
    return _underlying;
  }

  protected Sequence.Entry getSequence() {
    return _sequence.entry();
  }

  protected String createName(final EquitySecurity security, final ZonedDateTime expiry, final OptionType type, final double strike) {
    final StringBuilder sb = new StringBuilder((security.getShortName() != null) ? security.getShortName() : security.getName());
    sb.append(' ').append(expiry.toString(DATE_FORMATTER));
    if (type == OptionType.CALL) {
      sb.append(" C ");
    } else {
      assert type == OptionType.PUT;
      sb.append(" P ");
    }
    sb.append(strike);
    return sb.toString();
  }
  
  @Override
  public EquityOptionSecurity createSecurity() {
    final Sequence.Entry seq = getSequence();
    final EquitySecurity underlying = getUnderlying();
    final OptionType optionType = (seq.next(2) == 0) ? OptionType.CALL : OptionType.PUT;
    final Pair<LocalDate, Double> lastValue = getHistoricalSource().getLatestDataPoint(MarketDataRequirementNames.MARKET_VALUE, underlying.getExternalIdBundle(), null);
    if (lastValue == null) {
      return null;
    }
    final int atm = (int) (lastValue.getSecond() * 10);
    final int tick;
    if (atm < 250) {
      tick = 25;
    } else if (atm < 1000) {
      tick = 50;
    } else if (atm < 2500) {
      tick = 100;
    } else if (atm < 5000) {
      tick = 200;
    } else {
      tick = 500;
    }
    final double strike = (double) (atm - (atm % tick) + (seq.next(8) - 3) * tick) / 10;
    if (strike <= 0) {
      return null;
    }
    final Currency currency = underlying.getCurrency();
    ExternalId underlyingIdentifier = underlying.getExternalIdBundle().getExternalId(SecurityUtils.OG_SYNTHETIC_TICKER);
    if (underlyingIdentifier == null) {
      underlyingIdentifier = underlying.getExternalIdBundle().iterator().next();
    }
    final ExerciseType exerciseType = Currency.GBP.equals(currency) ? new EuropeanExerciseType() : new AmericanExerciseType();
    final Expiry expiry = new Expiry(ZonedDateTime.now().plusMonths(seq.next(3) * 3 + 2));
    final double pointValue = 5; // TODO: vary this
    final String exchange = underlying.getExchangeCode();
    final EquityOptionSecurity security = new EquityOptionSecurity(optionType, strike, currency, underlyingIdentifier, exerciseType, expiry, pointValue, exchange);
    security.setName(createName(underlying, expiry.getExpiry(), optionType, strike));
    return security;
  }

}
