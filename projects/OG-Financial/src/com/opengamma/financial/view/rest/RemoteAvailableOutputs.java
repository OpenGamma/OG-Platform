/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.time.InstantProvider;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote representation of {@link AvailableOutputs} data.
 */
public class RemoteAvailableOutputs {

  private final FudgeContext _fudgeContext;
  private final RestTarget _serviceTarget;

  public RemoteAvailableOutputs(final FudgeContext fudgeContext, final RestTarget serviceTarget) {
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

  private AvailableOutputs getPortfolioOutputs(final UniqueId portfolioId, final String timestamp) {
    final RestClient client = RestClient.getInstance(getFudgeContext(), null);
    final FudgeMsg msg = client.getMsg(getServiceTarget().resolveBase("portfolio").resolveBase(timestamp).resolve(portfolioId.toString()));
    if (msg == null) {
      return null;
    }
    final FudgeDeserializer fd = new FudgeDeserializer(getFudgeContext());
    return fd.fudgeMsgToObject(AvailableOutputs.class, msg);
  }

  public AvailableOutputs getPortfolioOutputs(final UniqueId portfolioId) {
    return getPortfolioOutputs(portfolioId, "now");
  }

  public AvailableOutputs getPortfolioOutputs(final UniqueId portfolioId, final InstantProvider compilationInstant) {
    return getPortfolioOutputs(portfolioId, Long.toString(compilationInstant.toInstant().getEpochSeconds()));
  }

}
