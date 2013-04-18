/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import java.util.Collection;
import java.util.Map;

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
  UniqueId resolveExternalId(ExternalIdBundle identifiers, VersionCorrection versionCorrection);

  /**
   * Resolves the objects loosely specified by identifier bundles to exact unique identifiers. This is a bulk version of {@link #resolveExternalId} that should be more efficient than calling it
   * repeatedly.
   * 
   * @param identifiers the identifiers to resolve, not null and not containing nulls
   * @param versionCorrection the version/correction time to perform the resolution at, not null
   * @return the map of identifier bundles to resolved object identifiers, not null
   */
  Map<ExternalIdBundle, UniqueId> resolveExternalIds(Collection<ExternalIdBundle> identifiers, VersionCorrection versionCorrection);

  /**
   * Resolves the object loosely specified by an object identifier to an exact unique identifier.
   * 
   * @param identifier the strong identifier for the object, not null
   * @param versionCorrection the version/correction time to perform the resolution at, not null
   * @return the resolved object identifier or null if the object was not found
   */
  UniqueId resolveObjectId(ObjectId identifier, VersionCorrection versionCorrection);

  /**
   * Resolves the objects loosely specified by object identifiers to exact unique identifiers. This is a bulk version of {@link #resolveObjectId} that should be more efficient than calling it
   * repeatedly.
   * 
   * @param identifiers the identifiers to resolve, not null and not containing nulls
   * @param versionCorrection the version/correction time to perform the resolution at, not null
   * @return the map of object identifiers to unique identifiers, not null
   */
  Map<ObjectId, UniqueId> resolveObjectIds(Collection<ObjectId> identifiers, VersionCorrection versionCorrection);

}
