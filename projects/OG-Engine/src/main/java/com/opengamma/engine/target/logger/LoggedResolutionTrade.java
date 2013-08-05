/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.logger;

import java.math.BigDecimal;
import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.util.money.Currency;

/**
 * Wrapper around a {@link Trade} instance that will log any deep resolution calls.
 */
public class LoggedResolutionTrade extends AbstractLoggedResolution<Trade> implements Trade {

  public LoggedResolutionTrade(final Trade underlying, final ResolutionLogger logger) {
    super(underlying, logger);
  }

  // Trade

  @Override
  public BigDecimal getQuantity() {
    return getUnderlying().getQuantity();
  }

  @Override
  public SecurityLink getSecurityLink() {
    return getUnderlying().getSecurityLink();
  }

  @Override
  public Security getSecurity() {
    final Security security = getUnderlying().getSecurity();
    if (security != null) {
      final SecurityLink link = getSecurityLink();
      if ((link.getExternalId() != null) && !link.getExternalId().isEmpty()) {
        log(new ComputationTargetRequirement(ComputationTargetType.SECURITY, link.getExternalId()), security.getUniqueId());
      }
      if (link.getObjectId() != null) {
        log(ComputationTargetType.SECURITY, security);
      }
    }
    return security;
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

}
