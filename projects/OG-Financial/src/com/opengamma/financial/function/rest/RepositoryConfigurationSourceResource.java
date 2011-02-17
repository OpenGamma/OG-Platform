/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * 
 */
public class RepositoryConfigurationSourceResource {

  private final RepositoryConfigurationSource _underlying;
  private final FudgeContext _fudgeContext;

  public RepositoryConfigurationSourceResource(final RepositoryConfigurationSource underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected RepositoryConfigurationSource getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @GET
  @Path("/repositoryConfiguration")
  public FudgeMsgEnvelope getRepositoryConfiguration() {
    final RepositoryConfiguration configuration = getUnderlying().getRepositoryConfiguration();
    final MutableFudgeFieldContainer msg = getFudgeContext().newMessage();
    msg.add("repositoryConfiguration", configuration.toFudgeMsg(getFudgeContext()));
    return new FudgeMsgEnvelope(msg);
  }

}
