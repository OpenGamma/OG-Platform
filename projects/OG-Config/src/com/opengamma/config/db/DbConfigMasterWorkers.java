/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;

/**
 * Configuration object defining the workers in use for {@code DbConfigMaster}.
 * <p>
 * This class is designed for injection of alternative implementations.
 * 
 * @param <T>  the configuration element type
 */
public class DbConfigMasterWorkers<T> {

  /** Worker. */
  private DbConfigMasterWorker<T> _searchWorker = new QueryConfigDbConfigMasterWorker<T>();
  /** Worker. */
  private DbConfigMasterWorker<T> _getWorker = new QueryConfigDbConfigMasterWorker<T>();
  /** Worker. */
  private DbConfigMasterWorker<T> _addWorker = new ModifyConfigDbConfigMasterWorker<T>();
  /** Worker. */
  private DbConfigMasterWorker<T> _updateWorker = new ModifyConfigDbConfigMasterWorker<T>();
  /** Worker. */
  private DbConfigMasterWorker<T> _removeWorker = new ModifyConfigDbConfigMasterWorker<T>();
  /** Worker. */
  private DbConfigMasterWorker<T> _searchHistoricWorker = new QueryConfigDbConfigMasterWorker<T>();

  /**
   * Creates an instance.
   */
  public DbConfigMasterWorkers() {
  }

  /**
   * Initializes the instance.
   * @param master  the security master, non-null
   */
  protected void init(final DbConfigMaster<T> master) {
    _searchWorker.init(master);
    _getWorker.init(master);
    _addWorker.init(master);
    _updateWorker.init(master);
    _removeWorker.init(master);
    _searchHistoricWorker.init(master);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the searchWorker field.
   * @return the searchWorker
   */
  public DbConfigMasterWorker<T> getSearchWorker() {
    return _searchWorker;
  }

  /**
   * Sets the searchWorker field.
   * @param searchWorker  the searchWorker
   */
  public void setSearchWorker(DbConfigMasterWorker<T> searchWorker) {
    _searchWorker = searchWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the getWorker field.
   * @return the getWorker
   */
  public DbConfigMasterWorker<T> getGetWorker() {
    return _getWorker;
  }

  /**
   * Sets the getWorker field.
   * @param getWorker  the getWorker
   */
  public void setGetWorker(DbConfigMasterWorker<T> getWorker) {
    _getWorker = getWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the addWorker field.
   * @return the addWorker
   */
  public DbConfigMasterWorker<T> getAddWorker() {
    return _addWorker;
  }

  /**
   * Sets the addWorker field.
   * @param addWorker  the addWorker
   */
  public void setAddWorker(DbConfigMasterWorker<T> addWorker) {
    _addWorker = addWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the updateWorker field.
   * @return the updateWorker
   */
  public DbConfigMasterWorker<T> getUpdateWorker() {
    return _updateWorker;
  }

  /**
   * Sets the updateWorker field.
   * @param updateWorker  the updateWorker
   */
  public void setUpdateWorker(DbConfigMasterWorker<T> updateWorker) {
    _updateWorker = updateWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the removeWorker field.
   * @return the removeWorker
   */
  public DbConfigMasterWorker<T> getRemoveWorker() {
    return _removeWorker;
  }

  /**
   * Sets the removeWorker field.
   * @param removeWorker  the removeWorker
   */
  public void setRemoveWorker(DbConfigMasterWorker<T> removeWorker) {
    _removeWorker = removeWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the searchHistoricWorker field.
   * @return the searchHistoricWorker
   */
  public DbConfigMasterWorker<T> getSearchHistoricWorker() {
    return _searchHistoricWorker;
  }

  /**
   * Sets the searchHistoricWorker field.
   * @param searchHistoricWorker  the searchHistoricWorker
   */
  public void setSearchHistoricWorker(DbConfigMasterWorker<T> searchHistoricWorker) {
    _searchHistoricWorker = searchHistoricWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this config master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "@" + System.identityHashCode(this);
  }

}
