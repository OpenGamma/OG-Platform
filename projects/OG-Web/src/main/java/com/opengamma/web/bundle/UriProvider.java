/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.net.URI;

/**
 * Provides the URI of a {@link Fragment}.
 */
public interface UriProvider {

  /**
   * Gets the URI for a given resource.
   * 
   * @param resourceReference  the reference to the resource
   * 
   * @return the URI for the given resource, or null if the resource does not exist
   */
  URI getUri(String resourceReference);
  
}
