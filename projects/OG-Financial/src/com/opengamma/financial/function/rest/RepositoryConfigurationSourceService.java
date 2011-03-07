/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteRepositoryConfigurationSource}.
 */
@Path("repositoryConfigurationSource")
public class RepositoryConfigurationSourceService extends AbstractResourceService<RepositoryConfigurationSource, RepositoryConfigurationSourceResource> {

  public RepositoryConfigurationSourceService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected RepositoryConfigurationSourceResource createResource(RepositoryConfigurationSource underlying) {
    return new RepositoryConfigurationSourceResource(underlying, getFudgeContext());
  }

}
