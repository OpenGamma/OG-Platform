/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.jaxrs.FudgeFieldContainerBrowser;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for accessing available outputs from a portfolio
 */
public class DataAvailablePortfolioOutputsResource extends AbstractDataResource {

  /**
   * The provider.
   */
  private final AvailableOutputsProvider _provider;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  //-------------------------------------------------------------------------
  /**
   * Builder-style RESTful resource for accessing available outputs from a portfolio
   */
  public static final class Instance extends AbstractDataResource {

    private final AvailableOutputsProvider _provider;
    private final FudgeContext _fudgeContext;
    private final Instant _instant;
    private final Integer _maxNodes;
    private final Integer _maxPositions;

    private Instance(AvailableOutputsProvider provider, FudgeContext fudgeContext, Instant instant) {
      this(provider, fudgeContext, instant, null, null);
    }
    
    private Instance(AvailableOutputsProvider provider, FudgeContext fudgeContext, Instant instant, Integer maxNodes, Integer maxPositions) {
      _provider = provider;
      _fudgeContext = fudgeContext;
      _instant = instant;
      _maxNodes = maxNodes;
      _maxPositions = maxPositions;
    }

    @Path("nodes/{count}")
    public Instance nodes(@PathParam("count") int maxNodes) {
      return new Instance(_provider, _fudgeContext, _instant, maxNodes, _maxPositions);
    }

    @Path("positions/{count}")
    public Instance positions(@PathParam("count") int maxPositions) {
      return new Instance(_provider, _fudgeContext, _instant, _maxNodes, maxPositions);
    }

    @Path("{portfolioId}")
    public FudgeFieldContainerBrowser portfolioOutputsByPortfolioId(@PathParam("portfolioId") String portfolioUid) {
      try {
        final AvailableOutputs outputs = _provider.getPortfolioOutputs(UniqueId.parse(portfolioUid), _instant, _maxNodes, _maxPositions);
        final FudgeSerializer serializer = new FudgeSerializer(_fudgeContext);
        return new FudgeFieldContainerBrowser(serializer.objectToFudgeMsg(outputs));
      } catch (DataNotFoundException e) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    }

    @POST
    @Consumes(FudgeRest.MEDIA)
    @Produces(FudgeRest.MEDIA)
    public FudgeMsg portfolioOutputsByPortfolio(Portfolio portfolio) {
      try {
        FudgeSerializer serializer = new FudgeSerializer(_fudgeContext);
        AvailableOutputs availableOutputs = _provider.getPortfolioOutputs(portfolio, _instant, _maxNodes, _maxPositions);
        return serializer.objectToFudgeMsg(availableOutputs);
      } catch (DataNotFoundException e) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param provider  the provider, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public DataAvailablePortfolioOutputsResource(final AvailableOutputsProvider provider, final FudgeContext fudgeContext) {
    _provider = provider;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  private AvailableOutputsProvider getProvider() {
    return _provider;
  }

  private FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  //-------------------------------------------------------------------------
  @Path("now")
  public Instance now() {
    return new Instance(getProvider(), getFudgeContext(), null);
  }

  @Path("{timestamp}")
  public Instance timestamp(@PathParam("timestamp") String timestamp) {
    Instant instant = Instant.parse(timestamp);
    return new Instance(getProvider(), getFudgeContext(), instant);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param instant  the instant, may be null
   * @param maxNodes  the maximum nodes, may be null
   * @param maxPositions  the maximum positions, may be null
   * @param portfolioId  the portfolio identifier, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, Instant instant, Integer maxNodes, Integer maxPositions, UniqueId portfolioId) {
    UriBuilder bld = UriBuilder.fromUri(DataAvailableOutputsProviderResource.uriPortfolio(baseUri));
    bld.path(instant != null ? instant.toString() : "now");
    if (maxNodes != null && maxNodes > 0) {
      bld.path("nodes").path(Integer.toString(maxNodes));
    }
    if (maxPositions != null && maxPositions > 0) {
      bld.path("positions").path(Integer.toString(maxPositions));
    }
    if (portfolioId != null) {
      bld.path(portfolioId.toString());
    }
    return bld.build();
  }

}
