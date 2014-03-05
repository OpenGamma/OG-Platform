/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource exposing a {@link ConventionBundleSource} to remote clients.
 * <p>
 * This is the server to {@link RemoteConventionBundleSource}.
 */
public class DataConventionBundleSourceResource extends AbstractDataResource {

  private final ConventionBundleSource _underlying;

  public DataConventionBundleSourceResource(final ConventionBundleSource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected ConventionBundleSource getUnderlying() {
    return _underlying;
  }

  private FudgeMsg encodeBundle(final ConventionBundle bundle) {
    if (bundle == null) {
      return null;
    }
    return new FudgeSerializer(OpenGammaFudgeContext.getInstance()).objectToFudgeMsg(bundle);
  }

  @GET
  @Path("identifier/{id}")
  public Response getByIdentifier(@PathParam("id") final String idStr) {
    final ExternalId id = ExternalId.parse(idStr);
    return responseOkObject(encodeBundle(getUnderlying().getConventionBundle(id)));
  }

  public static URI uriGetByIdentifier(final URI baseUri, final ExternalId id) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("identifier/{id}");
    return bld.buildFromEncoded(id.toString().replace("/", "%2F"));
  }

  @GET
  @Path("bundle")
  public Response getByBundle(@QueryParam("id") final List<String> idStrs) {
    final ExternalIdBundle ids = ExternalIdBundle.parse(idStrs);
    return responseOkObject(encodeBundle(getUnderlying().getConventionBundle(ids)));
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
    return responseOkObject(encodeBundle(getUnderlying().getConventionBundle(id)));
  }

  public static URI uriGetByUniqueId(final URI baseUri, final UniqueId id) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("unique/{id}");
    return bld.buildFromEncoded(id.toString().replace("/", "%2F"));
  }

}
