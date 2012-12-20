/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import com.opengamma.core.Link;
import com.opengamma.util.PublicAPI;

/**
 * A flexible link between an object and a region.
 * <p>
 * The region link represents a connection from an entity to a region.
 * The connection can be held by an {@code ObjectId} or an {@code ExternalIdBundle}.
 * To obtain the target region, the link must be resolved.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface RegionLink extends Link<Region> {

}
