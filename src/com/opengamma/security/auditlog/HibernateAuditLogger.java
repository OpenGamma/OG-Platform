/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.util.Date;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author pietari
 */
public class HibernateAuditLogger extends HibernateDaoSupport implements AuditLogger {
  
  @Override
  public void log(String user, String object, String operation, String description, boolean success) {
    ArgumentChecker.checkNotNull(user, "User ID");
    ArgumentChecker.checkNotNull(object, "Object ID");
    ArgumentChecker.checkNotNull(operation, "Operation name");
    
    AuditLogEntry auditLogEntry = new AuditLogEntry(user, object, operation, description, success, new Date());
    getHibernateTemplate().save(auditLogEntry);
  }

  @Override
  public void log(String user, String object, String operation, boolean success) {
    log(user, object, operation, null, success);     
  }
  
  @SuppressWarnings("unchecked")
  List<AuditLogEntry> findLogEntry(String user, Date start, Date end) {
    return (List<AuditLogEntry>) getHibernateTemplate().find(
        "from AuditLogEntry where user = ? and timestamp >= ? and timestamp < ?", 
        new Object[] { user, start, end });
  }

}
