/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.opengamma.util.ArgumentChecker;

/**
 * An audit logger that uses JDBC batch insert (through Hibernate) to insert
 * audit log entries into a database. If there is a server crash, audit log
 * entries stored in memory that have not yet been committed into
 * the database may be lost. 
 *
 * @author pietari
 */
public class HibernateAuditLogger extends HibernateDaoSupport implements AuditLogger {
  
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateAuditLogger.class);
  
  private final int _batchSize;
  
  /** Keeps track of audit log entries to flush */ 
  private List<AuditLogEntry> _auditLogCache;
  
  private final Timer _timer;
  
  public HibernateAuditLogger() {
    this(50, 5);
  }
  
  public HibernateAuditLogger(int batchSize, int maxSecondsToKeepInMemory) {
    if (batchSize <= 0) {
      throw new IllegalArgumentException("Please give positive batch size");
    }
    if (maxSecondsToKeepInMemory <= 0) {
      throw new IllegalArgumentException("Please give positive max seconds to keep in memory");
    }
    
    _batchSize = batchSize;
    _auditLogCache = new ArrayList<AuditLogEntry>(_batchSize);
    
    Flusher flusher = new Flusher();
    _timer = new Timer("hibernate-audit-log-flusher");
    _timer.schedule(flusher, 1000 * maxSecondsToKeepInMemory, 1000 * maxSecondsToKeepInMemory);
  }
  
  /**
   * The <code>Flusher</code> background thread ensures that all log entries are written into 
   * the DB in a timely fashion, even if the flow of new log entries from clients stops abruptly.  
   */
  private class Flusher extends TimerTask {
    @Override
    public void run() {
      flushCache();
    }
  }
  
  @Override
  public void log(String user, String object, String operation, String description, boolean success) {
    ArgumentChecker.checkNotNull(user, "User ID");
    ArgumentChecker.checkNotNull(object, "Object ID");
    ArgumentChecker.checkNotNull(operation, "Operation name");
    
    AuditLogEntry auditLogEntry = new AuditLogEntry(user, object, operation, description, success, new Date());
    boolean flushCache = false;
    synchronized (this) {
      _auditLogCache.add(auditLogEntry);
      if (_auditLogCache.size() >= _batchSize) {
        flushCache = true;
      }
    }
    
    if (flushCache) {
      flushCache();
    }
  }
  
  /** 
   * Flushes the log entries stored in memory into the database.
   */
  public void flushCache() {
    
    List<AuditLogEntry> auditLogCache;
    synchronized (this) {
      auditLogCache = _auditLogCache;
      _auditLogCache = new ArrayList<AuditLogEntry>(_batchSize);
    }

    Session session = getSession();
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      for (int i = 0; i < auditLogCache.size(); i++) {
        AuditLogEntry auditLogEntry = auditLogCache.get(i);
        session.save(auditLogEntry);
        
        if (i != 0 && i % _batchSize == 0) {
          session.flush();
          session.clear();
        }
      }
      
      tx.commit();
    } catch (RuntimeException e) {
      s_logger.error("Failed to commit batch to Hibernate", e);
      if (tx != null) {
        tx.rollback();
      }
      throw e;
    } finally {
      session.close();
    }
  }

  @Override
  public void log(String user, String object, String operation, boolean success) {
    log(user, object, operation, null, success);     
  }
  
  @SuppressWarnings("unchecked")
  List<AuditLogEntry> findAll() {
    return (List<AuditLogEntry>) getHibernateTemplate().loadAll(AuditLogEntry.class);
  }
  
  @SuppressWarnings("unchecked")
  List<AuditLogEntry> findLogEntries(String user, Date start, Date end) {
    return (List<AuditLogEntry>) getHibernateTemplate().find(
        "from AuditLogEntry where user = ? and timestamp >= ? and timestamp < ?", 
        new Object[] { user, start, end });
  }

}
