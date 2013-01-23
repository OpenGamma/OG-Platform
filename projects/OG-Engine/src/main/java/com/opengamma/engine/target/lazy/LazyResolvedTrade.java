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
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.util.money.Currency;

/**
 * A trade implementation that may not be fully resolved at construction but will appear fully resolved when used.
 */
public final class LazyResolvedTrade extends LazyResolvedPositionOrTrade<Trade> implements Trade {

  private static final long serialVersionUID = 1L;

  public LazyResolvedTrade(final LazyResolveContext.AtVersionCorrection context, final Trade underlying) {
    super(context, underlying);
  }

  @Override
  public Map<String, String> getAttributes() {
    return getUnderlying().getAttributes();
  }

  @Override
  public void setAttributes(Map<String, String> attributes) {
    getUnderlying().setAttributes(attributes);
  }

  @Override
  public void addAttribute(String key, String value) {
    getUnderlying().addAttribute(key, value);
  }

  @Override
  public Counterparty getCounterparty() {
    return getUnderlying().getCounterparty();
  }

  @Override
  public LocalDate getTradeDate() {
    return getUnderlying().getTradeDate();
  }

  @Override
  public OffsetTime getTradeTime() {
    return getUnderlying().getTradeTime();
  }

  @Override
  public Double getPremium() {
    return getUnderlying().getPremium();
  }

  @Override
  public Currency getPremiumCurrency() {
    return getUnderlying().getPremiumCurrency();
  }

  @Override
  public LocalDate getPremiumDate() {
    return getUnderlying().getPremiumDate();
  }

  @Override
  public OffsetTime getPremiumTime() {
    return getUnderlying().getPremiumTime();
  }

  @Override
  protected TargetResolverTrade targetResolverObject(final ComputationTargetResolver.AtVersionCorrection resolver) {
    return new TargetResolverTrade(resolver, this);
  }

  @Override
  protected SimpleTrade simpleObject() {
    return new SimpleTrade(this);
  }
}
