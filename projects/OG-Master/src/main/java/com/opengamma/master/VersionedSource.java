/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * Trait for a "source" backed by a "master" that allows the version to be fixed externally.
 * 
 * @deprecated [PLAT-2237]
 */
@PublicSPI
@Deprecated
public interface VersionedSource {
  // TODO need a remote version of this to support ViewProcessorManager running on a different node to the actual masters and sources

  /**
   * Sets the version-correction locator to search at.
   * 
   * @param versionCorrection  the version-correction locator to search at, null for latest version
   */
  void setVersionCorrection(final VersionCorrection versionCorrection);

}
