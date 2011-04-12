/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
