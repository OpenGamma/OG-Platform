/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;


/**
 * Extension of the ReferenceDataProvider to indicate that caching is in place. This is
 * for the Live Data server that needs occasional access to the uncached data. 
 */
public interface CachingReferenceDataProvider extends ReferenceDataProvider {

  ReferenceDataProvider getUnderlying();
  
}
