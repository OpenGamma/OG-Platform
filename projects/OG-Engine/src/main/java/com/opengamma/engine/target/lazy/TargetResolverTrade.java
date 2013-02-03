/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.util.money.Currency;

/**
 * A trade implementation that defers to a target resolver for the component parts.
 */
public class TargetResolverTrade extends TargetResolverPositionOrTrade implements Trade {

  private final Map<String, String> _attributes;
  private final Counterparty _counterparty;
  private final LocalDate _tradeDate;
  private final OffsetTime _tradeTime;
  private final Double _premium;
  private final Currency _premiumCurrency;
  private final LocalDate _premiumDate;
  private final OffsetTime _premiumTime;

  public TargetResolverTrade(final ComputationTargetResolver.AtVersionCorrection targetResolver, final Trade copyFrom) {
    super(targetResolver, copyFrom);
    _attributes = copyFrom.getAttributes();
    _counterparty = copyFrom.getCounterparty();
    _tradeDate = copyFrom.getTradeDate();
    _tradeTime = copyFrom.getTradeTime();
    _premium = copyFrom.getPremium();
    _premiumCurrency = copyFrom.getPremiumCurrency();
    _premiumDate = copyFrom.getPremiumDate();
    _premiumTime = copyFrom.getPremiumTime();
  }

  @Override
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  @Override
  public void setAttributes(final Map<String, String> attributes) {
    _attributes.clear();
    _attributes.putAll(attributes);
  }

  @Override
  public void addAttribute(String key, String value) {
    _attributes.put(key, value);
  }

  @Override
  public Counterparty getCounterparty() {
    return _counterparty;
  }

  @Override
  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  @Override
  public OffsetTime getTradeTime() {
    return _tradeTime;
  }

  @Override
  public Double getPremium() {
    return _premium;
  }

  @Override
  public Currency getPremiumCurrency() {
    return _premiumCurrency;
  }

  @Override
  public LocalDate getPremiumDate() {
    return _premiumDate;
  }

  @Override
  public OffsetTime getPremiumTime() {
    return _premiumTime;
  }

}
