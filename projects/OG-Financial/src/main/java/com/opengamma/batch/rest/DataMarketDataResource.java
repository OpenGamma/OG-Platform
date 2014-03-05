/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.batch.BatchMasterWriter;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.id.ObjectId;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.tuple.Pair;

/**
 * RESTful resource for a live data snapshot within batch.
 */
public class DataMarketDataResource extends AbstractDataResource {

  /**
   * The batch master.
   */
  private final BatchMasterWriter _batchMaster;
  /**
   * The id of the market data
   */
  private final ObjectId _marketDataId;

  /**
   * Creates the resource.
   *
   * @param batchMaster  the underlying batch master, not null
   * @param marketDataId  the id of market data, not null
   */
  public DataMarketDataResource(final ObjectId marketDataId, final BatchMasterWriter batchMaster) {
    _batchMaster = batchMaster;
    _marketDataId = marketDataId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the batch master.
   *
   * @return the batch master, not null
   */
  public BatchMasterWriter getMaster() {
    return _batchMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get() {
    MarketData result = getMaster().getMarketDataById(_marketDataId);
    return responseOkObject(result);
  }

  @DELETE
  public void delete() {
    getMaster().deleteMarketData(_marketDataId);
  }

  @GET
  @Path("values")
  public Response getDataValues(PagingRequest paging) {
    Pair<List<MarketDataValue>, Paging> result = getMaster().getMarketDataValues(_marketDataId, paging);
    return responseOkObject(result);
  }

  @PUT
  @Path("values")
  @Consumes(FudgeRest.MEDIA)
  public void addDataValues(Set<MarketDataValue> dataValues) {
    getMaster().addValuesToMarketData(_marketDataId, dataValues);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the values of a given market data.
   *
   * @param baseUri  the base URI, not null
   * @param marketDataId  the id of market data
   * @return the URI, not null
   */
  public static URI uriMarketDataValues(URI baseUri, ObjectId marketDataId) {
    return UriBuilder.fromUri(baseUri).path("/marketDataSnapshot/{id}/values").build(marketDataId);  
  }

  /**
   * Builds a URI for all market data snapshots.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriMarketData(URI baseUri) {
    return UriBuilder.fromUri(baseUri).path("/marketDataSnapshot").build();
  }

  /**
   * Builds a URI for a market data snapshot of given id.
   *
   * @param baseUri  the base URI, not null
   * @param marketDataId  the id of market data
   * @return the URI, not null
   */
  public static URI uriMarketData(URI baseUri, ObjectId marketDataId) {
    return UriBuilder.fromUri(baseUri).path("/marketDataSnapshot/{id}").build(marketDataId);
  }

}
