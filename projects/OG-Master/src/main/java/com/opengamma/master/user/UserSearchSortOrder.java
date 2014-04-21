/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import java.util.Comparator;

import org.apache.commons.lang.ObjectUtils;

/**
 * Available sort orders for the user search.
 */
public enum UserSearchSortOrder implements Comparator<ManageableUser> {
  // this design is simple and perhaps not ideal, but it is effective for most use cases at the moment

  /**
   * Sort by object id ascending.
   */
  OBJECT_ID_ASC {
    @Override
    public int compare(ManageableUser obj1, ManageableUser obj2) {
      return obj1.getObjectId().compareTo(obj2.getObjectId());
    }
  },
  /**
   * Sort by object id ascending.
   */
  OBJECT_ID_DESC {
    @Override
    public int compare(ManageableUser obj1, ManageableUser obj2) {
      return obj2.getObjectId().compareTo(obj1.getObjectId());
    }
  },
  /**
   * Sort by name ascending.
   */
  NAME_ASC {
    @Override
    public int compare(ManageableUser obj1, ManageableUser obj2) {
      return ObjectUtils.compare(obj1.getUserName(), obj2.getUserName(), true);
    }
  },
  /**
   * Sort by name descending.
   */
  NAME_DESC {
    @Override
    public int compare(ManageableUser obj1, ManageableUser obj2) {
      return ObjectUtils.compare(obj2.getUserName(), obj1.getUserName(), true);
    }
  };

}
