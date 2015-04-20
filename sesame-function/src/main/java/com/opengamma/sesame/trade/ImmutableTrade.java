/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.trade;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Immutable trade is an immutable representation of a trade.  It is paired with ImmutableTradeBundle, which the allows
 * fudge de/serialization. Once fudge is replaced these two classes can be merged.
 */
public class ImmutableTrade implements Trade {

  private final ImmutableTradeBundle _tradeBundle;

  public static ImmutableTrade of(ImmutableTradeBundle tradeBundle) {
    return new ImmutableTrade(tradeBundle);
  }

  public ImmutableTrade(ImmutableTradeBundle tradeBundle) {
    _tradeBundle = tradeBundle;
  }

  @Override
  public Counterparty getCounterparty() {
    return _tradeBundle.getCounterparty();
  }

  @Override
  public LocalDate getTradeDate() {
    return _tradeBundle.getTradeDate();
  }

  @Override
  public OffsetTime getTradeTime() {
    return _tradeBundle.getTradeTime();
  }

  @Override
  public Double getPremium() {
    return _tradeBundle.getPremium();
  }

  @Override
  public Currency getPremiumCurrency() {
    return _tradeBundle.getPremiumCurrency();
  }

  @Override
  public LocalDate getPremiumDate() {
    return _tradeBundle.getPremiumDate();
  }

  @Override
  public OffsetTime getPremiumTime() {
    return _tradeBundle.getPremiumTime();
  }

  /**
   * The returned map will be immutable, this is enforced by the ImmutableTradeBundle
   */
  @Override
  public Map<String, String> getAttributes() {
    return _tradeBundle.getAttributes();
  }

  /**
   * This is not supported as this would break immutability
   * @param attributes
   */
  @Override
  public void setAttributes(Map<String, String> attributes) {
    throw new UnsupportedOperationException("Mutation not allowed, ImmutableTrade is an immutable representation of " +
                                                "Trade and thus setting Attributes is not supported");
  }

  /**
   * This is not supported as this would break immutability
   * @param key
   * @param value
   */
  @Override
  public void addAttribute(String key, String value) {
    throw new UnsupportedOperationException("Mutation not allowed, ImmutableTrade is an immutable representation of " +
                                                "Trade and thus setting Attributes is not supported");
  }

  @Override
  public UniqueId getUniqueId() {
    return _tradeBundle.getUniqueId();
  }

  @Override
  public BigDecimal getQuantity() {
    return _tradeBundle.getQuantity();
  }

  @Override
  public SecurityLink getSecurityLink() {
    throw new UnsupportedOperationException("Links not supported in ImmutabeTrade as the security 'target' is " +
                                                "directly embedded");
  }

  @Override
  public Security getSecurity() {
    return _tradeBundle.getSecurity();
  }

  @Override
  public int hashCode() {
    return Objects.hash(_tradeBundle);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ImmutableTrade other = (ImmutableTrade) obj;
    return Objects.equals(this._tradeBundle, other._tradeBundle);
  }
}
