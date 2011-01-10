/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import com.opengamma.util.db.HibernateMappingFiles;

/**
 * HibernateSecurityMaster configuration.
 */
public final class HibernateUserManagerFiles implements HibernateMappingFiles {

  @Override
  public Class<?>[] getHibernateMappingFiles() {
    return new Class<?>[] {
      Authority.class,
      User.class,
      UserGroup.class,
    };
  }

}
