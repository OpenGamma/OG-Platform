/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Link;
import com.opengamma.core.LinkResolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicAPI;

/**
 * Resolver capable of providing users.
 * <p>
 * This resolver provides lookup of an {@link OGUser user} to the engine functions.
 * The lookup may require selecting a single "best match" from a set of potential options.
 * The best match behavior is the key part that distinguishes one implementation from another.
 * Best match selection may use a version-correction, configuration or code as appropriate.
 * Implementations of this interface must specify the rules they use to best match.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicAPI
public interface UserResolver extends LinkResolver<OGUser> {

  /**
   * Resolves the link to the provide the target user.
   * <p>
   * A link contains both an object and an external identifier bundle, although
   * typically only one of these is populated. Since neither input exactly specifies
   * a single version of a single user a best match is required.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param link  the link to be resolved, not null
   * @return the resolved target, not null
   * @throws DataNotFoundException if the target could not be resolved
   * @throws RuntimeException if an error occurs
   */
  @Override
  OGUser resolve(Link<OGUser> link);

  /**
   * Gets a user by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single user at a single version-correction.
   * As such, there should be no complex matching issues in this lookup.
   * However, if the underlying data store does not handle versioning correctly,
   * then a best match selection may be required.
   * 
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched user, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the user could not be found
   * @throws RuntimeException if an error occurs
   */
  OGUser getUser(UniqueId uniqueId);

  /**
   * Gets a user by object identifier.
   * <p>
   * An object identifier exactly specifies a single user, but it provides no information
   * about the version-correction required.
   * As such, it is likely that multiple versions/corrections will match the object identifier.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param objectId  the object identifier to find, not null
   * @return the matched user, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the user could not be found
   * @throws RuntimeException if an error occurs
   */
  OGUser getUser(ObjectId objectId);

  /**
   * Gets a user by external identifier bundle.
   * <p>
   * A bundle represents the set of external identifiers which in theory map to a single user.
   * Unfortunately, not all external identifiers uniquely identify a single version of a single user.
   * As such, it is likely that multiple versions/corrections of multiple different users will match the bundle.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param bundle  the external identifier bundle to find, not null
   * @return the matched user, not null
   * @throws IllegalArgumentException if the identifier bundle is invalid
   * @throws DataNotFoundException if the user could not be found
   * @throws RuntimeException if an error occurs
   */
  OGUser getUser(ExternalIdBundle bundle);

}
