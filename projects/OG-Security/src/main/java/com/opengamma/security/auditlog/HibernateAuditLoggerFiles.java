/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import com.opengamma.util.db.HibernateMappingFiles;

/**
 * Hibernate AuditLogger configuration.
 */
public final class HibernateAuditLoggerFiles implements HibernateMappingFiles {

  @Override
  public Class<?>[] getHibernateMappingFiles() {
    return new Class<?>[] {
      AuditLogEntry.class,
    };
  }

}
