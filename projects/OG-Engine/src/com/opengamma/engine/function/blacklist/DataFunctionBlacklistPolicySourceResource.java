/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Publishes a {@link FunctionBlacklistPolicySource} to remote clients
 */
public class DataFunctionBlacklistPolicySourceResource extends AbstractDataResource {

  private final FunctionBlacklistPolicySource _underlying;
  private final FudgeContext _fudgeContext;

  public DataFunctionBlacklistPolicySourceResource(final FunctionBlacklistPolicySource underlying, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected FunctionBlacklistPolicySource getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @GET
  @Path("uid/{uniqueId}")
  public Response getByUniqueId(@PathParam("uniqueId") final String uniqueId) {
    final FunctionBlacklistPolicy policy = getUnderlying().getPolicy(UniqueId.parse(uniqueId));
    if (policy == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializer fsc = new FudgeSerializer(getFudgeContext());
    return responseOk(fsc.objectToFudgeMsg(policy));
  }

  @GET
  @Path("name/{name}")
  public Response getByName(@PathParam("name") final String name) {
    final FunctionBlacklistPolicy policy = getUnderlying().getPolicy(name);
    if (policy == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializer fsc = new FudgeSerializer(getFudgeContext());
    return responseOk(fsc.objectToFudgeMsg(policy));
  }

}
