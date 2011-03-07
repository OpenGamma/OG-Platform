/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;



/**
 * BundleCompressor compresses the given bundleId
 */
public interface CompressedBundleSource {
  
  String getBundle(String bundleId);
}
