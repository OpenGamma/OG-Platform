/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * Diagnostic RESTful backend for a {@link FunctionRepository}
 */
@Path("functionRepository")
public class FunctionRepositoryService extends AbstractResourceService<FunctionRepository, FunctionRepositoryResource> {

  public FunctionRepositoryService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected FunctionRepositoryResource createResource(FunctionRepository underlying) {
    return new FunctionRepositoryResource(underlying, getFudgeContext());
  }

}
