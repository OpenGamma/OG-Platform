/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * An audit logger that uses JDBC batch insert (through Hibernate) to insert
 * audit log entries into a database. If there is a server crash, audit log
 * entries stored in memory that have not yet been committed into
 * the database may be lost. 
 *
 */
public class HibernateAuditLogger extends AbstractAuditLogger implements Closeable {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateAuditLogger.class);
  
  private HibernateTemplate _hibernateTemplate;

  private final int _batchSize;
  
  /** Keeps track of audit log entries to flush */ 
  private List<AuditLogEntry> _auditLogCache;
  
  private final Timer _timer;
  
  public HibernateAuditLogger() {
    this(getDefaultOriginatingSystem());
  }
  
  public HibernateAuditLogger(String originatingSystem) {
    this(originatingSystem, 50, 5);
  }
  
  public HibernateAuditLogger(int batchSize, int maxSecondsToKeepInMemory) {
    this(getDefaultOriginatingSystem(), batchSize, maxSecondsToKeepInMemory);
  }
  
  public HibernateAuditLogger(String originatingSystem, int batchSize, int maxSecondsToKeepInMemory) {
    super(originatingSystem);
    
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
  
  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
  }
  
  private Session getSession() {
    return SessionFactoryUtils.getSession(
            _hibernateTemplate.getSessionFactory(),
            _hibernateTemplate.getEntityInterceptor(),
            _hibernateTemplate.getJdbcExceptionTranslator());
  }

  //-------------------------------------------------------------------------
  /**
   * The <code>Flusher</code> background thread ensures that all log entries are written into 
   * the DB in a timely fashion, even if the flow of new log entries from clients stops abruptly.  
   */
  private class Flusher extends TimerTask {
    @Override
    public void run() {
      try {
        flushCache();
      } catch (RuntimeException e) {
        // see http://manikandakumar.blogspot.com/2006/09/drawbacks-of-timertask.html
      }
    }
  }
  
  @Override
  public void log(String user, String originatingSystem, String object, String operation, String description, boolean success) {
    ArgumentChecker.notNull(user, "User ID");
    ArgumentChecker.notNull(user, "Originating system name");
    ArgumentChecker.notNull(object, "Object ID");
    ArgumentChecker.notNull(operation, "Operation name");
    
    AuditLogEntry auditLogEntry = new AuditLogEntry(user, originatingSystem, object, operation, description, success, new Date());
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
  
  @Override
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
      // If this happens, for now, assume that there was something wrong 
      // with one of the log messages. Therefore do NOT re-insert 
      // the messages into _auditLogCache.
      s_logger.error("Failed to commit batch to Hibernate", e);
      if (tx != null) {
        tx.rollback();
      }
      throw e;
    } finally {
      session.close();
    }
  }

  List<AuditLogEntry> findAll() {
    return _hibernateTemplate.loadAll(AuditLogEntry.class);
  }

  @Override
  public void close() {
    Timer timer = _timer;
    if (timer != null) {
      try {
        _timer.cancel();
      } catch (Throwable ex) {
        s_logger.info("Error during timer cancellation", ex);
      }
      try {
        flushCache();
      } catch (Throwable ex) {
        s_logger.info("Error during flush", ex);
      }
    }
  }

}
