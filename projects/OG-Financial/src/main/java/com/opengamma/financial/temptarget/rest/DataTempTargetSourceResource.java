/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.financial.temptarget.TempTargetSource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a {@link TempTargetSource}
 */
@Path("tempTarget")
public class DataTempTargetSourceResource extends AbstractDataResource {

  private final TempTargetSource _underlying;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param underlying the underlying source, not null
   */
  public DataTempTargetSourceResource(final TempTargetSource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected TempTargetSource getUnderlying() {
    return _underlying;
  }

  public static URI uriGet(final URI baseUri, final UniqueId uid) {
    return UriBuilder.fromUri(baseUri).path("/target/{uid}").build(uid);
  }

  @GET
  @Path("target/{uid}")
  public Response get(@PathParam("uid") final String uid) {
    final TempTarget target = getUnderlying().get(UniqueId.parse(uid));
    if (target == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    return responseOk(FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(target), target.getClass(), TempTarget.class));
  }

}
