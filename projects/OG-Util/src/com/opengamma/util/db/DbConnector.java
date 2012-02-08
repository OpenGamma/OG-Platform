/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.io.Closeable;
import java.sql.Timestamp;

import javax.sql.DataSource;
import javax.time.Instant;
import javax.time.TimeSource;

import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ReflectionUtils;
import com.opengamma.util.time.DateUtils;

/**
 * Connector used to access SQL databases.
 * <p>
 * This class provides a simple-to-setup and simple-to-use way to access databases.
 * It can be configured for access via JDBC, Hibernate or both.
 * The main benefit is simpler configuration, especially if that configuration is in XML.
 * <p>
 * This class is usually configured using the associated factory bean.
 */
public class DbConnector implements Closeable {

  static {
    DateUtils.initTimeZone();
  }

  /**
   * The configuration name.
   */
  private final String _name;
  /**
   * The data source.
   */
  private final DataSource _dataSource;
  /**
   * The dialect.
   */
  private final DbDialect _dialect;
  /**
   * The JDBC template.
   */
  private final SimpleJdbcTemplate _jdbcTemplate;
  /**
   * The Hibernate template.
   */
  private final HibernateTemplate _hibernateTemplate;
  /**
   * The transaction template.
   */
  private final TransactionTemplate _transactionTemplate;

  /**
   * Creates an instance.
   * 
   * @param name  the configuration name, not null
   * @param dialect  the database dialect, not null
   * @param dataSource  the data source, not null
   * @param jdbcTemplate  the JDBC template, not null
   * @param hibernateTemplate  the Hibernate template, may be null
   * @param transactionTemplate  the transaction template, not null
   */
  public DbConnector(
      String name, DbDialect dialect, DataSource dataSource,
      SimpleJdbcTemplate jdbcTemplate, HibernateTemplate hibernateTemplate, TransactionTemplate transactionTemplate) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(dialect, "dialect");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(jdbcTemplate, "JDBC template");
    ArgumentChecker.notNull(transactionTemplate, "transactionTemplate");
    _name = name;
    _dataSource = dataSource;
    _dialect = dialect;
    _jdbcTemplate = jdbcTemplate;
    _hibernateTemplate = hibernateTemplate;
    _transactionTemplate = transactionTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the display name of the connector.
   * 
   * @return a name usable for display, not null
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the data source.
   * 
   * @return the data source, not null
   */
  public DataSource getDataSource() {
    return _dataSource;
  }

  /**
   * Gets the database dialect.
   * 
   * @return the database dialect, not null
   */
  public DbDialect getDialect() {
    return _dialect;
  }

  /**
   * Gets the JDBC template.
   * 
   * @return the JDBC template, not null
   */
  public SimpleJdbcTemplate getJdbcTemplate() {
    return _jdbcTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Hibernate session factory.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the Hibernate session factory, may be null
   */
  public SessionFactory getHibernateSessionFactory() {
    if (_hibernateTemplate == null) {
      return null;
    }
    return _hibernateTemplate.getSessionFactory();
  }

  /**
   * Gets the shared Hibernate template.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the Hibernate template, null if the session factory is null
   */
  public HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the transaction manager.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the transaction manager, may be null
   */
  public PlatformTransactionManager getTransactionManager() {
    return _transactionTemplate.getTransactionManager();
  }

  /**
   * Gets the transaction template.
   * <p>
   * This is shared between all users of this object and must not be further configured.
   * 
   * @return the transaction template, may be null
   */
  public TransactionTemplate getTransactionTemplate() {
    return _transactionTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the current instant using the database clock.
   * 
   * @return the current database instant, may be null
   */
  public Instant now() {
    Timestamp ts = getJdbcTemplate().queryForObject(getDialect().sqlSelectNow(), Timestamp.class);
    return DbDateUtils.fromSqlTimestamp(ts);
  }

  /**
   * Returns a time-source based on the current database clock.
   * <p>
   * This can be used to obtain the current instant by calling {@link Instant#now(TimeSource)}.
   * 
   * @return the database time-source, may be null
   */
  public TimeSource timeSource() {
    return new TimeSource() {
      @Override
      public Instant instant() {
        return now();
      }
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public void close() {
    ReflectionUtils.close(getDataSource());
    ReflectionUtils.close(getTransactionManager());
    ReflectionUtils.close(getHibernateSessionFactory());
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a description of this object suitable for debugging.
   * 
   * @return the description, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _name + "]";
  }

}
