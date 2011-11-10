/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import java.util.Collections;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.util.money.Currency;

/* package */final class TradeInfo extends PositionOrTradeInfo<Trade> {

  private final Map<String, String> _attributes;

  public TradeInfo(final ComparisonContext context, final Trade trade) {
    super(context, trade);
    _attributes = context.isIgnoreTradeAttributes() ? Collections.<String, String>emptyMap() : trade.getAttributes();
  }

  public Counterparty getCounterparty() {
    return getUnderlying().getCounterparty();
  }

  public LocalDate getTradeDate() {
    return getUnderlying().getTradeDate();
  }

  public OffsetTime getTradeTime() {
    return getUnderlying().getTradeTime();
  }

  public Double getPremium() {
    return getUnderlying().getPremium();
  }

  public Currency getPremiumCurrency() {
    return getUnderlying().getPremiumCurrency();
  }

  public LocalDate getPremiumDate() {
    return getUnderlying().getPremiumDate();
  }

  public OffsetTime getPremiumTime() {
    return getUnderlying().getPremiumTime();
  }

  public Map<String, String> getAttributes() {
    return _attributes;
  }
  
  @Override
  public String toString() {
    return "TradeInfo[quantity=" + getQuantity() + ", security=" + getSecurity() + ", counterparty=" +
        getCounterparty() + ", tradeDate=" + getTradeDate() + ", tradeTime=" + getTradeTime() + ", premium=" + getPremium() +
        ", premiumCurrency=" + getPremiumCurrency() + ", premiumDate=" + getPremiumDate() + ", premiumTime=" + getPremiumTime() + ", attributes=" + getAttributes() + "]";
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof TradeInfo)) {
      return false;
    }
    final TradeInfo other = (TradeInfo) o;
    return equalsImpl(other)
        && ObjectUtils.equals(getCounterparty(), other.getCounterparty())
        && ObjectUtils.equals(getTradeDate(), other.getTradeDate())
        && ObjectUtils.equals(getTradeTime(), other.getTradeTime())
        && ObjectUtils.equals(getPremium(), other.getPremium())
        && ObjectUtils.equals(getPremiumCurrency(), other.getPremiumCurrency())
        && ObjectUtils.equals(getPremiumDate(), other.getPremiumDate())
        && ObjectUtils.equals(getPremiumTime(), other.getPremiumTime())
        && ObjectUtils.equals(getAttributes(), other.getAttributes());
  }

  @Override
  public int hashCode() {
    int hc = hashCodeImpl();
    hc += (hc << 4) + ObjectUtils.hashCode(getCounterparty());
    hc += (hc << 4) + ObjectUtils.hashCode(getTradeDate());
    hc += (hc << 4) + ObjectUtils.hashCode(getTradeTime());
    hc += (hc << 4) + ObjectUtils.hashCode(getPremium());
    hc += (hc << 4) + ObjectUtils.hashCode(getPremiumCurrency());
    hc += (hc << 4) + ObjectUtils.hashCode(getPremiumDate());
    hc += (hc << 4) + ObjectUtils.hashCode(getPremiumTime());
    hc += (hc << 4) + ObjectUtils.hashCode(getAttributes());
    return hc;
  }

}
