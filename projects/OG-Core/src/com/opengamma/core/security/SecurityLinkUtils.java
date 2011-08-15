/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import com.google.common.base.Objects;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.util.PublicAPI;

/**
 * Utilities and constants for {@code SecurityLink}.
 * <p>
 * This is a thread-safe static utility class.
 */
@PublicAPI
public class SecurityLinkUtils {

  /**
   * Restricted constructor.
   */
  protected SecurityLinkUtils() {
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
  public static Object best(SecurityLink link) {
    Security target = link.getTarget();
    ObjectId objectId = link.getObjectId();
    ExternalIdBundle bundle = link.getExternalId();
    return Objects.firstNonNull(target, Objects.firstNonNull(objectId, bundle));
  }

  /**
   * Gets the best name for the object from the specified link.
   * <p>
   * This will return a name extracted from the external bundle or object identifier.
   * 
   * @param link  the link, not null
   * @return the best representative name, not null
   */
  public static String bestName(SecurityLink link) {
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

}
