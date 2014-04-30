/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import com.opengamma.util.db.HibernateMappingFiles;

/**
 * Hibernate UserManager configuration.
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
