/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean to provide SQL database connectors.
 * <p>
 * This class provides a simple-to-setup and simple-to-use way to access databases.
 * It can be configured for access via JDBC, Hibernate or both.
 * The main benefit is simpler configuration, especially if that configuration is in XML.
 * <p>
 * There are multiple options for some elements.
 * Set either the dialect name or the dialect itself.
 * Set either the transaction details or the transaction manager itself.
 * Set the Hibernate factory bean, the Hibernate settings or the session factory itself.
 */
public class DbConnectorFactoryBean extends SingletonFactoryBean<DbConnector> {

  /**
   * The name that this source is known by.
   */
  private String _name;
  /**
   * The underlying data source.
   */
  private DataSource _dataSource;
  /**
   * The database type, used to create a helper class.
   */
  private String _databaseDialectClass;
  /**
   * The database dialect.
   */
  private DbDialect _databaseDialect;
  /**
   * Factory bean to create Hibernate.
   * This can be used if more control is needed than the properties exposed on this factory bean.
   */
  private LocalSessionFactoryBean _hibernateFactoryBean;
  /**
   * The Hibernate mapping file configuration classes.
   */
  private HibernateMappingFiles[] _mappingConfigurations;
  /**
   * The Hibernate mapping resource locations.
   */
  private String[] _mappingResources;
  /**
   * The Hibernate configuration to show the SQL.
   */
  private boolean _hibernateShowSql;
  /**
   * Set to true if you want to use Hibernate thread-bound auto-create sessions 
   */
  private boolean _allowHibernateThreadBoundSession;
  /**
   * The Hibernate session factory.
   */
  private SessionFactory _hibernateSessionFactory;
  /**
   * The transaction isolation level.
   * See {@link DefaultTransactionDefinition}.
   */
  private String _transactionIsolationLevelName;
  /**
   * The transaction isolation level.
   * See {@link DefaultTransactionDefinition}.
   */
  private String _transactionPropagationBehaviorName;
  /**
   * The transaction timeout in seconds.
   * See {@link DefaultTransactionDefinition}.
   */
  private int _transactionTimeoutSecs;
  /**
   * The transaction manager.
   * This can be left null, and an appropriate one will be created.
   */
  private PlatformTransactionManager _transactionManager;

  /**
   * Creates an instance.
   */
  public DbConnectorFactoryBean() {
  }

  /**
   * Creates an instance based on an existing source.
   * <p>
   * This copies the name, dialect, data source, session factory and transaction manager.
   * 
   * @param base  the base source to copy, not null
   */
  public DbConnectorFactoryBean(DbConnector base) {
    setName(base.getName());
    setDialect(base.getDialect());
    setDataSource(base.getDataSource());
    setHibernateSessionFactory(base.getHibernateSessionFactory());
    setTransactionManager(base.getTransactionManager());
  }

  //-------------------------------------------------------------------------
  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public DataSource getDataSource() {
    return _dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    _dataSource = dataSource;
  }

  public String getDialectName() {
    return _databaseDialectClass;
  }

  public void setDialectName(String databaseDialectClass) {
    _databaseDialectClass = databaseDialectClass;
  }

  public DbDialect getDialect() {
    return _databaseDialect;
  }

  public void setDialect(DbDialect dialect) {
    _databaseDialect = dialect;
  }

  public LocalSessionFactoryBean getHibernateFactoryBean() {
    return _hibernateFactoryBean;
  }

  public void setHibernateFactoryBean(LocalSessionFactoryBean hibernateFactoryBean) {
    _hibernateFactoryBean = hibernateFactoryBean;
  }

  public HibernateMappingFiles[] getHibernateMappingFiles() {
    return _mappingConfigurations;
  }

  public void setHibernateMappingFiles(HibernateMappingFiles[] mappingConfigurations) {
    _mappingConfigurations = mappingConfigurations;
  }

  public String[] getHibernateMappingResources() {
    return _mappingResources;
  }

  public void setHibernateMappingResources(String[] mappingResources) {
    _mappingResources = mappingResources;
  }

  public boolean isHibernateShowSql() {
    return _hibernateShowSql;
  }

  public void setHibernateShowSql(boolean hibernateShowSql) {
    _hibernateShowSql = hibernateShowSql;
  }

  public boolean isAllowHibernateThreadBoundSession() {
    return _allowHibernateThreadBoundSession;
  }

  public void setAllowHibernateThreadBoundSession(boolean allowHibernateThreadBoundSession) {
    _allowHibernateThreadBoundSession = allowHibernateThreadBoundSession;
  }

  public SessionFactory getHibernateSessionFactory() {
    return _hibernateSessionFactory;
  }

  public void setHibernateSessionFactory(SessionFactory sessionFactory) {
    _hibernateSessionFactory = sessionFactory;
  }

  public String getTransactionIsolationLevelName() {
    return _transactionIsolationLevelName;
  }

  public void setTransactionIsolationLevelName(String transactionIsolationLevelName) {
    _transactionIsolationLevelName = transactionIsolationLevelName;
  }

  public String getTransactionPropagationBehaviorName() {
    return _transactionPropagationBehaviorName;
  }

  public void setTransactionPropagationBehaviorName(String transactionPropagationBehaviorName) {
    _transactionPropagationBehaviorName = transactionPropagationBehaviorName;
  }

  public int getTransactionTimeout() {
    return _transactionTimeoutSecs;
  }

  public void setTransactionTimeout(int transactionTimeoutSecs) {
    _transactionTimeoutSecs = transactionTimeoutSecs;
  }

  public PlatformTransactionManager getTransactionManager() {
    return _transactionManager;
  }

  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    _transactionManager = transactionManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public DbConnector createObject() {
    ArgumentChecker.notNull(getName(), "name");
    ArgumentChecker.notNull(getDataSource(), "dataSource");
    DbDialect dialect = createDialect();
    NamedParameterJdbcTemplate jdbcTemplate = createNamedParameterJdbcTemplate(dialect);
    SessionFactory hbFactory = createSessionFactory(dialect);
    HibernateTemplate hbTemplate = createHibernateTemplate(hbFactory);
    TransactionTemplate transTemplate = createTransactionTemplate(hbFactory);
    return new DbConnector(getName(), dialect, getDataSource(), jdbcTemplate, hbTemplate, transTemplate);
  }

  /**
   * Creates the database dialect, using the dialect object, then the string.
   * 
   * @return the dialect, not null
   */
  protected DbDialect createDialect() {
    DbDialect dialect = getDialect();
    if (dialect == null) {
      String dialectStr = getDialectName();
      ArgumentChecker.notNull(dialectStr, "dialectStr");
      if (dialectStr.contains(".") == false) {
        dialectStr = "org.opengamma.util." + dialectStr;
      }
      try {
        dialect = (DbDialect) getClass().getClassLoader().loadClass(dialectStr).newInstance();
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    return dialect;
  }

  /**
   * Creates the JDBC template, using the data source.
   * 
   * @param dialect  the dialect instance, not null
   * @return the JDBC template, not null
   */
  protected NamedParameterJdbcTemplate createNamedParameterJdbcTemplate(DbDialect dialect) {
    return dialect.getNamedParameterJdbcTemplate(getDataSource());
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the Hibernate session factory.
   * 
   * @param dialect  the dialect instance, not null
   * @return the session factory, may be null
   */
  protected SessionFactory createSessionFactory(DbDialect dialect) {
    SessionFactory result = getHibernateSessionFactory();
    if (result != null) {
      return result;
    }
    LocalSessionFactoryBean factory = getHibernateFactoryBean();
    if (factory == null) {
      String[] files = createHibernateFiles();
      if (files.length == 0) {
        return null; // Hibernate not required
      }
      factory = new LocalSessionFactoryBean();
      factory.setMappingResources(files);
      factory.setDataSource(getDataSource());
      Properties props = new Properties();
      props.setProperty("hibernate.dialect", dialect.getHibernateDialect().getClass().getName());
      props.setProperty("hibernate.show_sql", String.valueOf(isHibernateShowSql()));
      props.setProperty("hibernate.connection.release_mode", "on_close");
      if (isAllowHibernateThreadBoundSession()) {
        props.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        props.setProperty(Environment.TRANSACTION_STRATEGY, "org.hibernate.transaction.JDBCTransactionFactory");
      }
      factory.setHibernateProperties(props);
      factory.setLobHandler(dialect.getLobHandler());
    }
    try {
      factory.afterPropertiesSet();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return factory.getObject();
  }

  /**
   * Creates the complete list of Hibernate configuration files.
   * 
   * @return the set of Hibernate files, not null
   */
  protected String[] createHibernateFiles() {
    String[] nameArray = getHibernateMappingResources();
    HibernateMappingFiles[] filesArray = getHibernateMappingFiles();
    if (nameArray == null && filesArray == null) {
      return new String[0];
    }
    Set<String> config = new HashSet<String>();
    if (nameArray != null) {
      config.addAll(Arrays.asList(nameArray));
    }
    if (filesArray != null) {
      for (HibernateMappingFiles files : filesArray) {
        for (Class<?> cls : files.getHibernateMappingFiles()) {
          String hbm = cls.getName().replace('.', '/') + ".hbm.xml";
          config.add(hbm);
        }
      }
    }
    return (String[]) config.toArray(new String[config.size()]);
  }

  /**
   * Creates the Hibernate template, using the session factory.
   * 
   * @param sessionFactory  the Hibernate session factory, may be null
   * @return the Hibernate template, not null
   */
  protected HibernateTemplate createHibernateTemplate(SessionFactory sessionFactory) {
    if (sessionFactory != null) {
      return new HibernateTemplate(sessionFactory);
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the transaction template.
   * 
   * @param sessionFactory  the Hibernate session factory, may be null
   * @return the transaction template, not null
   */
  protected TransactionTemplate createTransactionTemplate(SessionFactory sessionFactory) {
    DefaultTransactionDefinition transDefn = createTransactionDefinition();
    PlatformTransactionManager transMgr = createTransactionManager(sessionFactory);
    return new TransactionTemplate(transMgr, transDefn);
  }

  /**
   * Creates the transaction definition.
   * 
   * @return the transaction definition, not null
   */
  protected DefaultTransactionDefinition createTransactionDefinition() {
    DefaultTransactionDefinition transDefn = new DefaultTransactionDefinition();
    transDefn.setName(getName());
    if (getTransactionIsolationLevelName() != null) {
      transDefn.setIsolationLevelName(getTransactionIsolationLevelName());
    }
    if (getTransactionPropagationBehaviorName() != null) {
      transDefn.setPropagationBehaviorName(getTransactionPropagationBehaviorName());
    }
    if (getTransactionTimeout() != 0) {
      transDefn.setTimeout(getTransactionTimeout());
    }
    return transDefn;
  }

  /**
   * Creates the transaction manager.
   * 
   * @param sessionFactory  the Hibernate session factory, may be null
   * @return the transaction manager, not null
   */
  protected PlatformTransactionManager createTransactionManager(SessionFactory sessionFactory) {
    PlatformTransactionManager transMgr = getTransactionManager();
    if (transMgr == null) {
      AbstractPlatformTransactionManager newTransMgr;
      if (sessionFactory != null) {
        newTransMgr = new HibernateTransactionManager(sessionFactory);
      } else {
        newTransMgr = new DataSourceTransactionManager(getDataSource());
      }
      newTransMgr.setNestedTransactionAllowed(true);
      transMgr = newTransMgr;
    }
    return transMgr;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
