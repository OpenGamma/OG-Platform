/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.util.ArgumentChecker;

/**
 * General purpose source of access to databases.
 * <p>
 * This class provides a simple-to-setup and simple-to-use way to access databases.
 * It can be configured for access via JDBC, Hibernate or both.
 * The main benefit is simpler configuration, especially if that configuration is in XML.
 * <p>
 * This class is usually configured using the associated factory bean.
 */
public class DbSource {

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
  private final DbHelper _dialect;
  /**
   * The JDBC template.
   */
  private final SimpleJdbcTemplate _jdbcTemplate;
  /**
   * The transaction manager.
   */
  private final PlatformTransactionManager _transactionManager;
  /**
   * The Hibernate session factory.
   */
  private final SessionFactory _sessionFactory;
  /**
   * The Hibernate template.
   */
  private final HibernateTemplate _hibernateTemplate;
  /**
   * The transaction definition.
   */
  private final TransactionDefinition _transactionDefinition;
  /**
   * The transaction template.
   */
  private final TransactionTemplate _transactionTemplate;

  /**
   * Creates an instance.
   * @param name  the configuration name, not null
   * @param dataSource  the data source, not null
   * @param helper  the database helper, not null
   * @param sessionFactory  the Hibernate session factory
   * @param transactionDefinition  the transaction definition, not null
   * @param transactionManager  the transaction manager, not null
   */
  public DbSource(
      String name, DataSource dataSource, DbHelper helper, SessionFactory sessionFactory,
      TransactionDefinition transactionDefinition, PlatformTransactionManager transactionManager) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(helper, "helper");
    ArgumentChecker.notNull(transactionDefinition, "transactionDefinition");
    ArgumentChecker.notNull(transactionManager, "transactionManager");
    _name = name;
    _dataSource = dataSource;
    _dialect = helper;
    _jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    _sessionFactory = sessionFactory;
    if (sessionFactory != null) {
      _hibernateTemplate = new HibernateTemplate(sessionFactory);
    } else {
      _hibernateTemplate = null;
    }
    _transactionDefinition = transactionDefinition;
    _transactionManager = transactionManager;
    _transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
  }

  //-------------------------------------------------------------------------

  public String getName() {
    return _name;
  }

  /**
   * Gets the data source.
   * @return the data source, not null
   */
  public DataSource getDataSource() {
    return _dataSource;
  }

  /**
   * Gets the database dialect.
   * @return the database dialect, not null
   */
  public DbHelper getDialect() {
    return _dialect;
  }

  /**
   * Gets the JDBC template.
   * @return the JDBC template, not null
   */
  public SimpleJdbcTemplate getJdbcTemplate() {
    return _jdbcTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Hibernate session factory.
   * @return the Hibernate session factory, may be null
   */
  public SessionFactory getHibernateSessionFactory() {
    return _sessionFactory;
  }

  /**
   * Gets the Hibernate template.
   * @return the Hibernate template, null if the session factory is null
   */
  public HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the transaction manager.
   * @return the transaction manager, may be null
   */
  public PlatformTransactionManager getTransactionManager() {
    return _transactionManager;
  }

  /**
   * Gets the transaction definition.
   * @return the transaction definition, may be null
   */
  public TransactionDefinition getTransactionDefinition() {
    return _transactionDefinition;
  }

  /**
   * Gets the transaction template.
   * @return the transaction template, may be null
   */
  public TransactionTemplate getTransactionTemplate() {
    return _transactionTemplate;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _name + "]";
  }

}
