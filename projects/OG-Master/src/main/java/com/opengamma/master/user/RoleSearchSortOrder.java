/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import com.opengamma.util.PublicSPI;
import org.apache.commons.lang.ObjectUtils;

import java.util.Comparator;

/**
 * Available sort orders for the role search.
 */
@PublicSPI
public enum RoleSearchSortOrder implements Comparator<RoleDocument> {
  // this design is simple and perhaps not ideal, but it is effective for most use cases at the moment

  /**
   * Sort by object id ascending.
   */
  OBJECT_ID_ASC {
    @Override
    public int compare(RoleDocument obj1, RoleDocument obj2) {
      return obj1.getObjectId().compareTo(obj2.getObjectId());
    }
  },
  /**
   * Sort by object id ascending.
   */
  OBJECT_ID_DESC {
    @Override
    public int compare(RoleDocument obj1, RoleDocument obj2) {
      return obj2.getObjectId().compareTo(obj1.getObjectId());
    }
  },
  /**
   * Sort by version from instant ascending.
   */
  VERSION_FROM_INSTANT_ASC {
    @Override
    public int compare(RoleDocument obj1, RoleDocument obj2) {
      return obj1.getVersionFromInstant().compareTo(obj2.getVersionFromInstant());
    }
  },
  /**
   * Sort by version from instant descending.
   */
  VERSION_FROM_INSTANT_DESC {
    @Override
    public int compare(RoleDocument obj1, RoleDocument obj2) {
      return obj2.getVersionFromInstant().compareTo(obj1.getVersionFromInstant());
    }
  },
  /**
   * Sort by name ascending.
   */
  NAME_ASC {
    @Override
    public int compare(RoleDocument obj1, RoleDocument obj2) {
      return ObjectUtils.compare(obj1.getName(), obj2.getName(), true);
    }
  },
  /**
   * Sort by name descending.
   */
  NAME_DESC {
    @Override
    public int compare(RoleDocument obj1, RoleDocument obj2) {
      return ObjectUtils.compare(obj2.getName(), obj1.getName(), true);
    }
  };

}
