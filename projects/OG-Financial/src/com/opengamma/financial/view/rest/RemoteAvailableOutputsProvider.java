/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote representation of {@link AvailableOutputs} data.
 */
public class RemoteAvailableOutputsProvider implements AvailableOutputsProvider {

  private final FudgeContext _fudgeContext;
  private final RestTarget _serviceTarget;

  public RemoteAvailableOutputsProvider(final FudgeContext fudgeContext, final RestTarget serviceTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(serviceTarget, "serviceTarget");
    _fudgeContext = fudgeContext;
    _serviceTarget = serviceTarget;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected RestTarget getServiceTarget() {
    return _serviceTarget;
  }

  //-------------------------------------------------------------------------
  @Override
  public AvailableOutputs getPortfolioOutputs(Portfolio portfolio, InstantProvider instantProvider) {
    return getPortfolioOutputs(portfolio, instantProvider, null, null);
  }
  
  @Override
  public AvailableOutputs getPortfolioOutputs(Portfolio portfolio, InstantProvider instantProvider, Integer maxNodes, Integer maxPositions) {
    final RestClient client = RestClient.getInstance(getFudgeContext(), null);
    FudgeSerializer serializer = new FudgeSerializer(_fudgeContext);
    FudgeMsg portfolioMsg = serializer.objectToFudgeMsg(portfolio);
    RestTarget target = getServiceTarget(instantProvider, maxNodes, maxPositions);
    final FudgeMsg msg = client.post(target, portfolioMsg).getMessage();
    if (msg == null) {
      return null;
    }
    final FudgeDeserializer fd = new FudgeDeserializer(getFudgeContext());
    return fd.fudgeMsgToObject(AvailableOutputs.class, msg);
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(UniqueId portfolioId, InstantProvider instantProvider) {
    return getPortfolioOutputs(portfolioId, instantProvider, null, null);
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(UniqueId portfolioId, InstantProvider instantProvider, Integer maxNodes, Integer maxPositions) {
    final RestClient client = RestClient.getInstance(getFudgeContext(), null);
    RestTarget target = getServiceTarget(instantProvider, maxNodes, maxPositions);
    final FudgeMsg msg = client.getMsg(target.resolve(portfolioId.toString()));
    if (msg == null) {
      return null;
    }
    final FudgeDeserializer fd = new FudgeDeserializer(getFudgeContext());
    return fd.fudgeMsgToObject(AvailableOutputs.class, msg);
  }

  //-------------------------------------------------------------------------
  private RestTarget getServiceTarget(InstantProvider instantProvider, Integer maxNodes, Integer maxPositions) {
    Instant instant = instantProvider != null ? instantProvider.toInstant() : null;
    String instantString = instant != null ? Long.toString(instant.toInstant().toEpochMillisLong()) : "now";
    RestTarget target = getServiceTarget().resolveBase("portfolio").resolveBase(instantString);
    if (maxNodes != null && maxNodes > 0) {
      target = target.resolveBase("nodes").resolveBase(Integer.toString(maxNodes));
    }
    if (maxPositions != null && maxPositions > 0) {
      target = target.resolveBase("positions").resolveBase(Integer.toString(maxPositions));
    }
    return target;
  }

}
