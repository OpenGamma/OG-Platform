/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import java.util.Comparator;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.PublicSPI;

/**
 * Available sort orders for the user search.
 */
@PublicSPI
public enum UserSearchSortOrder implements Comparator<UserDocument> {
  // this design is simple and perhaps not ideal, but it is effective for most use cases at the moment

  /**
   * Sort by object id ascending.
   */
  OBJECT_ID_ASC {
    @Override
    public int compare(UserDocument obj1, UserDocument obj2) {
      return obj1.getObjectId().compareTo(obj2.getObjectId());
    }
  },
  /**
   * Sort by object id ascending.
   */
  OBJECT_ID_DESC {
    @Override
    public int compare(UserDocument obj1, UserDocument obj2) {
      return obj2.getObjectId().compareTo(obj1.getObjectId());
    }
  },
  /**
   * Sort by version from instant ascending.
   */
  VERSION_FROM_INSTANT_ASC {
    @Override
    public int compare(UserDocument obj1, UserDocument obj2) {
      return obj1.getVersionFromInstant().compareTo(obj2.getVersionFromInstant());
    }
  },
  /**
   * Sort by version from instant descending.
   */
  VERSION_FROM_INSTANT_DESC {
    @Override
    public int compare(UserDocument obj1, UserDocument obj2) {
      return obj2.getVersionFromInstant().compareTo(obj1.getVersionFromInstant());
    }
  },
  /**
   * Sort by name ascending.
   */
  NAME_ASC {
    @Override
    public int compare(UserDocument obj1, UserDocument obj2) {
      return ObjectUtils.compare(obj1.getName(), obj2.getName(), true);
    }
  },
  /**
   * Sort by name descending.
   */
  NAME_DESC {
    @Override
    public int compare(UserDocument obj1, UserDocument obj2) {
      return ObjectUtils.compare(obj2.getName(), obj1.getName(), true);
    }
  },
  /**
   * Sort by email ascending.
   */
  EMAIL_ASC {
    @Override
    public int compare(UserDocument obj1, UserDocument obj2) {
      return ObjectUtils.compare(obj1.getUser().getEmailAddress(), obj2.getUser().getEmailAddress(), true);
    }
  },
  /**
   * Sort by email descending.
   */
  EMAIL_DESC {
    @Override
    public int compare(UserDocument obj1, UserDocument obj2) {
      return ObjectUtils.compare(obj2.getUser().getEmailAddress(), obj1.getUser().getEmailAddress(), true);
    }
  };

}
