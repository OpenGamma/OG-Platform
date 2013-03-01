/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.math.BigDecimal;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.AbstractFxOptionTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.BuySell;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Calculator class allowing calculations performed for FxOptions to be defined once. Not
 * intended for use outside of the TradeSecurityExtractors for FxOption.
 */
/* package */
class FxOptionCalculator {

  private final Currency _callCurrency;
  private final Currency _putCurrency;
  private final double _callAmount;
  private final double _putAmount;
  private final Expiry _expiry;
  private final ZonedDateTime _settlementDate;
  private final boolean _long;

  public FxOptionCalculator(AbstractFxOptionTrade trade, BigDecimal amount, Currency currency) {

    CurrencyPair cp = CurrencyPair.parse(trade.getCurrencyPair());

    Currency optionCurrency = trade.getOptionCurrency();
    Currency notionalCurrency = currency;

    ArgumentChecker.isTrue(cp.contains(optionCurrency), "Option currency must appear in the currency pair");
    ArgumentChecker.isTrue(cp.contains(notionalCurrency), "Notional currency must appear in the currency pair");
    ArgumentChecker.notNull(trade.getOptionType(), "Option type");

    boolean isCall = trade.getOptionType() == OptionType.CALL;
    Currency other = cp.getComplement(optionCurrency);
    _callCurrency = isCall ? optionCurrency : other;
    _putCurrency = isCall ? other : optionCurrency;

    BigDecimal strike = trade.getStrike();

    // Depending on the currency, either call amount or put amount equals the supplied amount
    // Then use the fact that Strike = (call amount / put amount) to calculate the other value
    _callAmount = (_callCurrency.equals(notionalCurrency) ? amount : amount.multiply(strike)).doubleValue();
    _putAmount = (_putCurrency.equals(notionalCurrency) ? amount : amount.divide(strike)).doubleValue();

    _expiry = new Expiry(trade.getFxExpiry().getExpiryDate().atStartOfDay(ZoneOffset.UTC));
    _settlementDate = trade.getPremiumSettlementDate().atStartOfDay(ZoneOffset.UTC);
    _long = trade.getBuySell() == BuySell.BUY;
  }

  public Currency getCallCurrency() {
    return _callCurrency;
  }

  public Currency getPutCurrency() {
    return _putCurrency;
  }

  public double getCallAmount() {
    return _callAmount;
  }

  public double getPutAmount() {
    return _putAmount;
  }

  public Expiry getExpiry() {
    return _expiry;
  }

  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  public boolean isLong() {
    return _long;
  }
}
