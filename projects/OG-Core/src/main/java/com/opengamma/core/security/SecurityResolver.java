/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Link;
import com.opengamma.core.LinkResolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicAPI;

/**
 * Resolver capable of providing securities.
 * <p>
 * This resolver provides lookup of a {@link Security security} to the engine functions.
 * The lookup may require selecting a single "best match" from a set of potential options.
 * The best match behavior is the key part that distinguishes one implementation from another.
 * Best match selection may use a version-correction, configuration or code as appropriate.
 * Implementations of this interface must specify the rules they use to best match.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicAPI
public interface SecurityResolver extends LinkResolver<Security> {

  /**
   * Resolves the link to the provide the target security.
   * <p>
   * A link contains both an object and an external identifier bundle, although
   * typically only one of these is populated. Since neither input exactly specifies
   * a single version of a single security a best match is required.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param link  the link to be resolver, not null
   * @return the resolved target, not null
   * @throws DataNotFoundException if the target could not be resolved
   * @throws RuntimeException if an error occurs
   */
  @Override
  Security resolve(Link<Security> link);

  /**
   * Gets a security by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single security at a single version-correction.
   * As such, there should be no complex matching issues in this lookup.
   * However, if the underlying data store does not handle versioning correctly,
   * then a best match selection may be required.
   * 
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched security, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the security could not be found
   * @throws RuntimeException if an error occurs
   */
  Security getSecurity(UniqueId uniqueId);

  /**
   * Gets a security by object identifier.
   * <p>
   * An object identifier exactly specifies a single security, but it provide no information
   * about the version-correction required.
   * As such, it is likely that multiple versions/corrections will match the object identifier.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param objectId  the object identifier to find, not null
   * @return the matched security, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the security could not be found
   * @throws RuntimeException if an error occurs
   */
  Security getSecurity(ObjectId objectId);

  /**
   * Gets a security by external identifier bundle.
   * <p>
   * A bundle represents the set of external identifiers which in theory map to a single security.
   * Unfortunately, not all external identifiers uniquely identify a single version of a single security.
   * As such, it is likely that multiple versions/corrections of multiple different securities will match the bundle.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param bundle  the external identifier bundle to find, not null
   * @return the matched security, not null
   * @throws IllegalArgumentException if the identifier bundle is invalid
   * @throws DataNotFoundException if the security could not be found
   * @throws RuntimeException if an error occurs
   */
  Security getSecurity(ExternalIdBundle bundle);

}
