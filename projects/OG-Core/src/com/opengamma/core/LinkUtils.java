/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.base.Objects;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.util.ArgumentChecker;
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
   * This will return either the object identifier or the external bundle, which may be empty.
   * 
   * @param link  the link, not null
   * @return the best representative object, not null
   */
  public static Object best(Link<?> link) {
    ArgumentChecker.notNull(link, "link");
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
    ArgumentChecker.notNull(link, "link");
    ObjectId objectId = link.getObjectId();
    ExternalIdBundle bundle = link.getExternalId();
    if (bundle != null && bundle.size() > 0) {
      if (bundle.getValue(ExternalSchemes.BLOOMBERG_TICKER) != null) {
        return bundle.getValue(ExternalSchemes.BLOOMBERG_TICKER);
      } else if (bundle.getValue(ExternalSchemes.RIC) != null) {
        return bundle.getValue(ExternalSchemes.RIC);
      } else if (bundle.getValue(ExternalSchemes.ACTIVFEED_TICKER) != null) {
        return bundle.getValue(ExternalSchemes.ACTIVFEED_TICKER);
      } else {
        return bundle.getExternalIds().iterator().next().getValue();
      }
    }
    return ObjectUtils.toString(objectId);
  }

  /**
   * Tests if the link is "valid".
   * <p>
   * To be valid it must contain either an object identifier, a non-empty external
   * identifier bundle, or both.
   * 
   * @param link  the link to check, null returns false
   * @return true if valid, false if not
   */
  public static boolean isValid(final Link<?> link) {
    if (link == null) {
      return false;
    }
    if (link.getObjectId() != null) {
      return true;
    }
    final ExternalIdBundle externalId = link.getExternalId();
    return externalId != null && externalId.size() > 0;
  }

}
