/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

/**
 * Marker interface for a target resolver that supports lazy operations.
 */
public interface LazyResolver {

  // TODO: should be package visible

  LazyResolveContext getLazyResolveContext();

  void setLazyResolveContext(LazyResolveContext context);

}
