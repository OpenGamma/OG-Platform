/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Map;
import java.util.WeakHashMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Publishes a {@link FunctionBlacklistProvider} to remote clients
 */
public class DataFunctionBlacklistProviderResource extends AbstractDataResource {

  private final FunctionBlacklistProvider _underlying;
  private final FudgeContext _fudgeContext;
  private final JmsConnector _jmsConnector;
  private final Map<FunctionBlacklist, DataFunctionBlacklistResource> _blacklists = new WeakHashMap<FunctionBlacklist, DataFunctionBlacklistResource>();

  public DataFunctionBlacklistProviderResource(final FunctionBlacklistProvider underlying, final FudgeContext fudgeContext, final JmsConnector jmsConnector) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(jmsConnector, "jmsConnector");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
    _jmsConnector = jmsConnector;
  }

  protected FunctionBlacklistProvider getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  protected DataFunctionBlacklistResource createResource(final FunctionBlacklist blacklist) {
    return new DataFunctionBlacklistResource(blacklist, getFudgeContext(), getJmsConnector());
  }

  @Path("name/{name}")
  public DataFunctionBlacklistResource get(@PathParam("name") final String name) {
    final FunctionBlacklist blacklist = getUnderlying().getBlacklist(name);
    if (blacklist == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    synchronized (this) {
      DataFunctionBlacklistResource resource = _blacklists.get(blacklist);
      if (resource == null) {
        resource = createResource(blacklist);
        _blacklists.put(blacklist, resource);
      }
      return resource;
    }
  }

}
