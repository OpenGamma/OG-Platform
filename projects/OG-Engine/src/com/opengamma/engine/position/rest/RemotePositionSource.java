/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.position.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link PositionSource}.
 */
public class RemotePositionSource implements PositionSource {

  /**
   * The base URI to call.
   */
  private final RestTarget _target;

  /**
   * The client API.
   */
  private final RestClient _client;

  public RemotePositionSource(final FudgeContext fudgeContext, final RestTarget restTarget) {
    _client = RestClient.getInstance(fudgeContext, null);
    _target = restTarget;
  }

  // -------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    Portfolio result = _client.getSingleValue(Portfolio.class, DataPositionSourceResource.targetPortfolio(_target, uid), "portfolio");
    return result;
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    PortfolioNode result = _client.getSingleValue(PortfolioNode.class, DataPositionSourceResource.targetPortfolioNode(_target, uid), "node");
    return result;
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    Position result = _client.getSingleValue(Position.class, DataPositionSourceResource.targetPosition(_target, uid), "position");
    return result;
  }

  @Override
  public Trade getTrade(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    Trade result = _client.getSingleValue(Trade.class, DataPositionSourceResource.targetTrade(_target, uid), "trade");
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * Returns a string summary of this object.
   * 
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _target + "]";
  }

}
