/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.trade;

import java.math.BigDecimal;
import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Immutable trade, needed for remote serialization .
 */
public class ImmutableTrade implements Trade {

  private static ImmutableTradeBundle s_tradeBundle;

  public static ImmutableTrade of(ImmutableTradeBundle tradeBundle) {
    return new ImmutableTrade(tradeBundle);
  }

  public ImmutableTrade(ImmutableTradeBundle tradeBundle) {
    s_tradeBundle = tradeBundle;
  }

  @Override
  public Counterparty getCounterparty() {
    return s_tradeBundle.getCounterparty();
  }

  @Override
  public LocalDate getTradeDate() {
    return s_tradeBundle.getTradeDate();
  }

  @Override
  public OffsetTime getTradeTime() {
    return s_tradeBundle.getTradeTime();
  }

  @Override
  public Double getPremium() {
    return s_tradeBundle.getPremium();
  }

  @Override
  public Currency getPremiumCurrency() {
    return s_tradeBundle.getPremiumCurrency();
  }

  @Override
  public LocalDate getPremiumDate() {
    return s_tradeBundle.getPremiumDate();
  }

  @Override
  public OffsetTime getPremiumTime() {
    return s_tradeBundle.getPremiumTime();
  }

  @Override
  public Map<String, String> getAttributes() {
    return s_tradeBundle.getAttributes();
  }

  @Override
  public void setAttributes(Map<String, String> attributes) {
    throw new UnsupportedOperationException("Mutation not allowed");
  }

  @Override
  public void addAttribute(String key, String value) {
    throw new UnsupportedOperationException("Mutation not allowed");
  }

  @Override
  public UniqueId getUniqueId() {
    return s_tradeBundle.getUniqueId();
  }

  @Override
  public BigDecimal getQuantity() {
    return s_tradeBundle.getQuantity();
  }

  @Override
  public SecurityLink getSecurityLink() {
    throw new UnsupportedOperationException("Links not supported");
  }

  @Override
  public Security getSecurity() {
    return s_tradeBundle.getSecurity();
  }
}
