/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import com.google.common.base.Objects;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.util.PublicAPI;

/**
 * Utilities and constants for {@code Link}.
 * <p>
 * This is a thread-safe static utility class.
 */
@PublicAPI
public class LinkUtils {

  /**
   * Restricted constructor.
   */
  protected LinkUtils() {
    super();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the best representative object from the specified link.
   * <p>
   * This will return either the object identifier or the external bundle.
   * 
   * @param link  the link, not null
   * @return the best representative object, not null
   */
  public static Object best(Link<?> link) {
    ObjectId objectId = link.getObjectId();
    ExternalIdBundle bundle = link.getExternalId();
    return Objects.firstNonNull(objectId, bundle);
  }

  /**
   * Gets the best name for the object from the specified link.
   * <p>
   * This will return a name extracted from the external bundle or object identifier.
   * 
   * @param link  the link, not null
   * @return the best representative name, not null
   */
  public static String bestName(Link<?> link) {
    ObjectId objectId = link.getObjectId();
    ExternalIdBundle bundle = link.getExternalId();
    if (bundle != null && bundle.size() > 0) {
      if (bundle.getValue(SecurityUtils.BLOOMBERG_TICKER) != null) {
        return bundle.getValue(SecurityUtils.BLOOMBERG_TICKER);
      } else if (bundle.getValue(SecurityUtils.RIC) != null) {
        return bundle.getValue(SecurityUtils.RIC);
      } else if (bundle.getValue(SecurityUtils.ACTIVFEED_TICKER) != null) {
        return bundle.getValue(SecurityUtils.ACTIVFEED_TICKER);
      } else {
        return bundle.getExternalIds().iterator().next().getValue();
      }
    }
    if (objectId != null) {
      return objectId.toString();
    }
    return "";
  }

  /**
   * Tests if the link is "valid" - i.e. it contains either (or both of) an object
   * reference or an external identifier bundle.
   * 
   * @param link link to check
   * @return true if valid, false if not
   */
  public static boolean isValid(final Link<?> link) {
    if (link.getObjectId() != null) {
      return true;
    }
    final ExternalIdBundle externalId = link.getExternalId();
    if (externalId == null) {
      return false;
    }
    return !externalId.isEmpty();
  }

}
