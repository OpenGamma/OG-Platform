/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Common interface for a resolver component to produce a unique identifier from a weaker reference (either an unversioned object identifier or an external identifier bundle).
 */
public interface IdentifierResolver {

  /**
   * Resolves the object loosely specified by an identifier bundle to an exact unique identifier.
   * 
   * @param identifiers the identifiers for the object, not null
   * @param versionCorrection the version/correction time to perform the resolution at, not null
   * @return the resolved object identifier or null if the object was not found
   */
  UniqueId resolve(ExternalIdBundle identifiers, VersionCorrection versionCorrection);

  /**
   * Resolves the object loosely specified by an object identifier to an exact unique identifier.
   * 
   * @param identifier the strong identifier for the object, not null
   * @param versionCorrection the version/correction time to perform the resolution at, not null
   * @return the resolved object identifier or null if the object was not found
   */
  UniqueId resolve(ObjectId identifier, VersionCorrection versionCorrection);

}
