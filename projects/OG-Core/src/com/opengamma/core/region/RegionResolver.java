/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Link;
import com.opengamma.core.LinkResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicAPI;

/**
 * Resolver capable of providing regions.
 * <p>
 * This resolver provides lookup of a {@link Region region} to the engine functions.
 * The lookup may require selecting a single "best match" from a set of potential options.
 * The best match behavior is the key part that distinguishes one implementation from another.
 * Best match selection may use a version-correction, configuration or code as appropriate.
 * Implementations of this interface must specify the rules they use to best match.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicAPI
public interface RegionResolver extends LinkResolver<Region> {

  /**
   * Resolves the link to the provide the target region.
   * <p>
   * A link contains both an object and an external identifier bundle, although
   * typically only one of these is populated. Since neither input exactly specifies
   * a single version of a single region a best match is required.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param link  the link to be resolver, not null
   * @return the resolved target, not null
   * @throws DataNotFoundException if the target could not be resolved
   * @throws RuntimeException if an error occurs
   */
  Region resolve(Link<Region> link);

  /**
   * Gets a region by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single region at a single version-correction.
   * As such, there should be no complex matching issues in this lookup.
   * However, if the underlying data store does not handle versioning correctly,
   * then a best match selection may be required.
   * 
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched region, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the region could not be found
   * @throws RuntimeException if an error occurs
   */
  Region getRegion(UniqueId uniqueId);

  /**
   * Gets a region by object identifier.
   * <p>
   * An object identifier exactly specifies a single region, but it provide no information
   * about the version-correction required.
   * As such, it is likely that multiple versions/corrections will match the object identifier.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param objectId  the object identifier to find, not null
   * @return the matched region, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the region could not be found
   * @throws RuntimeException if an error occurs
   */
  Region getRegion(ObjectId objectId);

  //-------------------------------------------------------------------------
  /**
   * Get a region based on the specified external identifier.
   * <p>
   * This will return the highest level region that matches the identifier.
   * For example 'US' will return the country USA rather than a state or city.
   * 
   * @param regionId  the region identifier to find, not null
   * @return the matched region, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the region could not be found
   * @throws RuntimeException if an error occurs
   */
  Region getHighestLevelRegion(ExternalId regionId);

  /**
   * Get a region based on the specified external identifier bundle.
   * <p>
   * This will return the highest level region that matches the bundle.
   * For example 'US' will return the country USA rather than a state or city.
   * Note that a bundle including 'US' and a city will still return the country USA.
   * 
   * @param bundle  the external identifier bundle to find, not null
   * @return the matched region, not null
   * @throws IllegalArgumentException if the identifier bundle is invalid
   * @throws DataNotFoundException if the region could not be found
   * @throws RuntimeException if an error occurs
   */
  Region getHighestLevelRegion(ExternalIdBundle bundle);

}
