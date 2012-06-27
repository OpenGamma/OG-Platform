/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Publishes a {@link FunctionBlacklistProvider} to remote clients
 */
public class DataFunctionBlacklistProviderResource extends AbstractDataResource {

  private final FunctionBlacklistProvider _underlying;
  private final FudgeContext _fudgeContext;

  public DataFunctionBlacklistProviderResource(final FunctionBlacklistProvider underlying, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected FunctionBlacklistProvider getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected DataFunctionBlacklistResource createResource(final FunctionBlacklist blacklist) {
    return new DataFunctionBlacklistResource(blacklist, getFudgeContext());
  }

  @Path("name/{name}")
  public DataFunctionBlacklistResource get(@PathParam("name") final String name) {
    final FunctionBlacklist blacklist = getUnderlying().getBlacklist(name);
    if (blacklist == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return createResource(blacklist);
  }

}
