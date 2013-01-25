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
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.util.money.Currency;

/**
 * A trade that may not be fully resolved at construction but will appear fully resolved when it is used.
 */
public class LazyTargetResolverTrade extends LazyTargetResolverPositionOrTrade implements Trade {

  public LazyTargetResolverTrade(final ComputationTargetResolver.AtVersionCorrection resolver, final ComputationTargetSpecification specification) {
    super(resolver, specification);
  }

  protected Trade getResolved() {
    return getResolvedTarget().getTrade();
  }

  @Override
  public Map<String, String> getAttributes() {
    return getResolved().getAttributes();
  }

  @Override
  public void setAttributes(Map<String, String> attributes) {
    getResolved().setAttributes(attributes);
  }

  @Override
  public void addAttribute(String key, String value) {
    getResolved().addAttribute(key, value);
  }

  @Override
  public Counterparty getCounterparty() {
    return getResolved().getCounterparty();
  }

  @Override
  public LocalDate getTradeDate() {
    return getResolved().getTradeDate();
  }

  @Override
  public OffsetTime getTradeTime() {
    return getResolved().getTradeTime();
  }

  @Override
  public Double getPremium() {
    return getResolved().getPremium();
  }

  @Override
  public Currency getPremiumCurrency() {
    return getResolved().getPremiumCurrency();
  }

  @Override
  public LocalDate getPremiumDate() {
    return getResolved().getPremiumDate();
  }

  @Override
  public OffsetTime getPremiumTime() {
    return getResolved().getPremiumTime();
  }

}
