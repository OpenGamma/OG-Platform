/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import java.util.Collection;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Source;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of users accessed by any OpenGamma application
 * <p>
 * This interface provides a simple view of users as used by most parts of the application.
 * This may be backed by a full-featured user master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface UserSource extends Source<OGUser> {

  /**
   * Gets all users at the given version-correction that match the specified
   * external identifier bundle.
   * <p>
   * A bundle represents the set of external identifiers which in theory map to a single user.
   * Unfortunately, not all external identifiers uniquely identify a single version of a single user.
   * This method returns all users that may match for {@link UserResolver} to choose from.
   * 
   * @param bundle  the bundle keys to match, not null
   * @param versionCorrection  the version-correction, not null
   * @return all users matching the specified key, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier bundle is invalid
   * @throws RuntimeException if an error occurs
   */
  Collection<? extends OGUser> getUsers(ExternalIdBundle bundle, VersionCorrection versionCorrection);

  //-------------------------------------------------------------------------
  /**
   * Gets the user by user id, specifying a version-correction.
   * 
   * @param userId  the user id to match, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched user, not null
   * @throws DataNotFoundException if the user could not be found
   * @throws RuntimeException if an error occurs
   */
  OGUser getUser(String userId, VersionCorrection versionCorrection);

}
