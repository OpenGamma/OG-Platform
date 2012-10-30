/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.time.InstantProvider;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to an {@link AvailableOutputsProvider}.
 */
public class RemoteAvailableOutputsProvider extends AbstractRemoteClient implements AvailableOutputsProvider {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteAvailableOutputsProvider(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  public FudgeContext getFudgeContext() {
    return OpenGammaFudgeContext.getInstance();
  }

  //-------------------------------------------------------------------------
  @Override
  public AvailableOutputs getPortfolioOutputs(Portfolio portfolio, InstantProvider instantProvider) {
    return getPortfolioOutputs(portfolio, instantProvider, null, null);
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(Portfolio portfolio, InstantProvider instantProvider, Integer maxNodes, Integer maxPositions) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    
    URI uri = DataAvailablePortfolioOutputsResource.uri(getBaseUri(), instantProvider, maxNodes, maxPositions, null);
    return accessRemote(uri).post(AvailableOutputs.class, portfolio);
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(UniqueId portfolioId, InstantProvider instantProvider) {
    return getPortfolioOutputs(portfolioId, instantProvider, null, null);
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(UniqueId portfolioId, InstantProvider instantProvider, Integer maxNodes, Integer maxPositions) {
    ArgumentChecker.notNull(portfolioId, "portfolioId");
    
    URI uri = DataAvailablePortfolioOutputsResource.uri(getBaseUri(), instantProvider, maxNodes, maxPositions, portfolioId);
    return accessRemote(uri).get(AvailableOutputs.class);
  }

}
