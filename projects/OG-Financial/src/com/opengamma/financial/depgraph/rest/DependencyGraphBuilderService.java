/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.rest.AbstractResourceService;

/**
 * REST resource for exposing diagnostic state from the dependency graph builder.
 */
@Path("dependencyGraphBuilder")
public class DependencyGraphBuilderService extends AbstractResourceService<DependencyGraphBuilderResourceContextBean, DependencyGraphBuilderResource> {

  public DependencyGraphBuilderService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected DependencyGraphBuilderResource createResource(DependencyGraphBuilderResourceContextBean context) {
    return new DependencyGraphBuilderResource(context, getFudgeContext());
  }

}
