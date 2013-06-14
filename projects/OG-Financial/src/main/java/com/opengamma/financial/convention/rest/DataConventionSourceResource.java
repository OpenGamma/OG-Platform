/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource exposing a {@link ConventionSource} to remote clients.
 */
public class DataConventionSourceResource extends AbstractDataResource {
  /** The underlying source */
  private final ConventionSource _underlying;

  /**
   * @param underlying The underlying source, not null
   */
  public DataConventionSourceResource(final ConventionSource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  /**
   * Gets the underlying source.
   * @return The underlying source
   */
  protected ConventionSource getUnderlying() {
    return _underlying;
  }

  private FudgeMsg encodeBundle(final Convention convention) {
    if (convention == null) {
      return null;
    }
    return new FudgeSerializer(OpenGammaFudgeContext.getInstance()).objectToFudgeMsg(convention);
  }

  @GET
  @Path("identifier/{id}")
  public Response getByIdentifier(@PathParam("id") final String idStr) {
    final ExternalId id = ExternalId.parse(idStr);
    return responseOkFudge(encodeBundle(getUnderlying().getConvention(id)));
  }

  public static URI uriGetByIdentifier(final URI baseUri, final ExternalId id) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("identifier/{id}");
    return bld.buildFromEncoded(id.toString().replace("/", "%2F"));
  }

  @GET
  @Path("bundle")
  public Response getByBundle(@QueryParam("id") final List<String> idStrs) {
    final ExternalIdBundle ids = ExternalIdBundle.parse(idStrs);
    return responseOkFudge(encodeBundle(getUnderlying().getConvention(ids)));
  }

  public static URI uriGetByBundle(final URI baseUri, final ExternalIdBundle ids) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("bundle");
    bld.queryParam("id", ids.toStringList().toArray());
    return bld.build();
  }

  @GET
  @Path("unique/{id}")
  public Response getByUniqueId(@PathParam("id") final String idStr) {
    final UniqueId id = UniqueId.parse(idStr);
    return responseOkFudge(encodeBundle(getUnderlying().getConvention(id)));
  }

  public static URI uriGetByUniqueId(final URI baseUri, final UniqueId id) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("unique/{id}");
    return bld.buildFromEncoded(id.toString().replace("/", "%2F"));
  }

}
