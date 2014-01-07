/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;

/**
 * Wrapper around a {@link Position} instance that will log any deep resolution calls.
 */
public class LoggedResolutionPosition extends AbstractLoggedResolution<Position> implements Position {

  public LoggedResolutionPosition(final Position underlying, final ResolutionLogger logger) {
    super(underlying, logger);
  }

  // Position

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
    final SecurityLink link = getSecurityLink();
    if ((link.getExternalId() != null) && !link.getExternalId().isEmpty()) {
      log(new ComputationTargetRequirement(ComputationTargetType.SECURITY, link.getExternalId()), security.getUniqueId());
    }
    if (link.getObjectId() != null) {
      log(ComputationTargetType.SECURITY, security);
    }
    return security;
  }

  @Override
  public Collection<Trade> getTrades() {
    final Collection<Trade> trades = getUnderlying().getTrades();
    final Collection<Trade> result = new ArrayList<Trade>(trades.size());
    for (Trade trade : trades) {
      // log(ComputationTargetType.TRADE, trade); // [PLAT-4491] Trades are linked to positions by UID, not OID
      result.add(new LoggedResolutionTrade(trade, getLogger()));
    }
    return result;
  }

  @Override
  public Map<String, String> getAttributes() {
    return getUnderlying().getAttributes();
  }

}
