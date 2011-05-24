/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

/**
 * Compresses the source of a bundle of CSS/Javascript files.
 */
public interface CompressedBundleSource {

  /**
   * Gets the compressed bundle source by ID.
   * 
   * @param bundleId  the bundle ID, not null
   * @return the bundle source, not null
   */
  String getBundle(String bundleId);

}
