/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

/**
 * Compresses the source of a bundle of CSS/Javascript files.
 */
public interface BundleCompressor {

  /**
   * Compresses a bundle.
   * <p>
   * The ID must be unique across multiple calls to this method as it
   * may be used as a cache key.
   * 
   * @param bundle  the bundle to compress, not null
   * @return the compressed bundle fragment code, not null
   */
  String compressBundle(Bundle bundle);

}
