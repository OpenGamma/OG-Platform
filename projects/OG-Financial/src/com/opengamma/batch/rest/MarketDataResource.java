/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.tuple.Pair;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Providers;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * RESTful resource for a live data snapshot.
 */
public class MarketDataResource extends AbstractDataResource {

  /**
   * The batch master.
   */
  private final BatchMaster _batchMaster;

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
  public MarketDataResource(final ObjectId marketDataId, final BatchMaster batchMaster) {
    _batchMaster = batchMaster;
    _marketDataId = marketDataId;
  }

  /**
   * Gets the batch master.
   *
   * @return the batch master, not null
   */
  public BatchMaster getMaster() {
    return _batchMaster;
  }

  @GET
  public Response get() {
    MarketData result = getMaster().getMarketDataById(_marketDataId);
    return Response.ok(result).build();
  }

  @DELETE
  public void delete() {
    getMaster().deleteMarketData(_marketDataId);
  }


  @GET
  @Path("values")
  public Response getDataValues(PagingRequest paging) {
    Pair<List<MarketDataValue>, Paging> result = getMaster().getMarketDataValues(_marketDataId, paging);
    return Response.ok(result).build();
  }

  @PUT
  @Path("values")
  @Consumes(FudgeRest.MEDIA)
  public void addDataValues(Set<MarketDataValue> dataValues) {
    getMaster().addValuesToMarketData(_marketDataId, dataValues);
  }


  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param snapshotId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectId snapshotId) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/batch/snapshots/{snapshotId}");
    return b.build(snapshotId);
  }

  /**
   * Builds a URI for all market data snapshots.
   *
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI marketDataSnapshotUri(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/marketDataSnapshot");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

  public static URI marketDataSnapshotUri(URI baseUri, UniqueId marketDataSnapshotUid) {
    return UriBuilder.fromUri(baseUri).path("/marketDataSnapshot/{uid}").build(marketDataSnapshotUid);
  }


}
