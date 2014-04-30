/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.management.jmx;

import javax.sql.DataSource;

import org.springframework.jmx.export.annotation.ManagedAttribute;

import com.opengamma.util.ArgumentChecker;

/**
 * JMX management of a database connection.
 */
public class DatabaseMBean {

  /**
   * Local access to the MBean instance. This avoids making various methods public.
   */
  public static class Local {

    private final DatabaseMBean _mbean;

    /**
     * Creates a new MBean instance of a type corresponding to the JDBC driver being used.
     * <p>
     * If the class is recognized then a sub-class of DatabaseMBean will be created with management operations specific to that type. Otherwise a trivial stub exposing basic/common data will be
     * returned.
     * 
     * @param driverClassName the driver class name, not null
     * @param dataSource the data source being managed that any operations should be applied to, not null
     */
    public Local(final String driverClassName, final DataSource dataSource) {
      _mbean = DatabaseMBean.of(driverClassName);
      _mbean._dataSource = ArgumentChecker.notNull(dataSource, "dataSource");
    }

    /**
     * Sets the value returned by {@link DatabaseMBean#getDriver}.
     * 
     * @param localJdbc the local JDBC connection string
     */
    public void setLocalJdbc(final String localJdbc) {
      _mbean._localJdbc = localJdbc;
    }

    /**
     * Sets the value returned by {@link DatabaseMBean#getUsername}.
     * 
     * @param username the username used to connect to the source
     */
    public void setUsername(final String username) {
      _mbean._username = username;
    }

    /**
     * Returns the underlying MBean instance for registration with the local MBean server.
     * 
     * @return the MBean instance, not null
     */
    public DatabaseMBean mbean() {
      return _mbean;
    }

  }

  /**
   * The name of the local driver. This might be a presentable name, if the JDBC driver class is recognized, otherwise it will be the full JDBC class name. This is the value returned by
   * {@link #getDriver}.
   */
  private final String _driver;
  /**
   * The local JDBC path returned by {@link #getLocalJdbc}.
   */
  private String _localJdbc;
  /**
   * The username used for the connection returned by {@link #getUsername}.
   */
  private String _username;
  /**
   * The data-source this bean should perform any management operations on.
   */
  private DataSource _dataSource;

  /**
   * Creates a new instance.
   * 
   * @param driver the database driver name, not null. If possible this should be a presentable name that the database technology is commonly known as (eg HSQLDB). Otherwise the class name of the JDBC
   *          driver will suffice.
   */
  public DatabaseMBean(final String driver) {
    _driver = ArgumentChecker.notNull(driver, "driver");
  }

  /**
   * Creates a new instance, matching the driver class name against a known list to produce a sub-class with specific management operations if possible.
   * <p>
   * This is wrapped by calls through {@link Local} so that the returned bean can be configured further before registration with the MBean server.
   * 
   * @param driverClassName the JDBC driver class name, as used in configuration files, not null
   * @return the MBean instance
   */
  private static DatabaseMBean of(final String driverClassName) {
    ArgumentChecker.notNull(driverClassName, "driverClassName");
    switch (driverClassName) {
      case HSQLDatabaseMBean.DRIVER_CLASS:
        return new HSQLDatabaseMBean();
      default:
        return new DatabaseMBean(driverClassName);
    }
  }

  @ManagedAttribute(description = "The database, or JDBC driver, type.")
  public String getDriver() {
    return _driver;
  }

  @ManagedAttribute(description = "The JDBC string used in the host process - may not work remotely.")
  public String getLocalJdbc() {
    return _localJdbc;
  }

  @ManagedAttribute(description = "The username used.")
  public String getUsername() {
    return _username;
  }

  /**
   * Returns the managed data source. Sub-classes may use this to perform the maintenance/management operations that they publish to JMX clients.
   * 
   * @return the datasource
   */
  protected DataSource getDataSource() {
    return _dataSource;
  }

}
