/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

/**
 * Configuration object defining the workers in use for {@code DbConfigMaster}.
 * <p>
 * This class is designed for injection of alternative implementations.
 * 
 * @param <T>  the configuration element type
 */
public class DbConfigTypeMasterWorkers<T> {

  /** Worker. */
  private DbConfigTypeMasterWorker<T> _searchWorker = new QueryConfigDbConfigTypeMasterWorker<T>();
  /** Worker. */
  private DbConfigTypeMasterWorker<T> _getWorker = new QueryConfigDbConfigTypeMasterWorker<T>();
  /** Worker. */
  private DbConfigTypeMasterWorker<T> _addWorker = new ModifyConfigDbConfigTypeMasterWorker<T>();
  /** Worker. */
  private DbConfigTypeMasterWorker<T> _updateWorker = new ModifyConfigDbConfigTypeMasterWorker<T>();
  /** Worker. */
  private DbConfigTypeMasterWorker<T> _removeWorker = new ModifyConfigDbConfigTypeMasterWorker<T>();
  /** Worker. */
  private DbConfigTypeMasterWorker<T> _historyWorker = new QueryConfigDbConfigTypeMasterWorker<T>();
  /** Worker. */
  private DbConfigTypeMasterWorker<T> _correctWorker = new ModifyConfigDbConfigTypeMasterWorker<T>();

  /**
   * Creates an instance.
   */
  public DbConfigTypeMasterWorkers() {
  }

  /**
   * Initializes the instance.
   * @param master  the security master, non-null
   */
  protected void init(final DbConfigTypeMaster<T> master) {
    _searchWorker.init(master);
    _getWorker.init(master);
    _addWorker.init(master);
    _updateWorker.init(master);
    _removeWorker.init(master);
    _historyWorker.init(master);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the searchWorker field.
   * @return the searchWorker
   */
  public DbConfigTypeMasterWorker<T> getSearchWorker() {
    return _searchWorker;
  }

  /**
   * Sets the searchWorker field.
   * @param searchWorker  the searchWorker
   */
  public void setSearchWorker(DbConfigTypeMasterWorker<T> searchWorker) {
    _searchWorker = searchWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the getWorker field.
   * @return the getWorker
   */
  public DbConfigTypeMasterWorker<T> getGetWorker() {
    return _getWorker;
  }

  /**
   * Sets the getWorker field.
   * @param getWorker  the getWorker
   */
  public void setGetWorker(DbConfigTypeMasterWorker<T> getWorker) {
    _getWorker = getWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the addWorker field.
   * @return the addWorker
   */
  public DbConfigTypeMasterWorker<T> getAddWorker() {
    return _addWorker;
  }

  /**
   * Sets the addWorker field.
   * @param addWorker  the addWorker
   */
  public void setAddWorker(DbConfigTypeMasterWorker<T> addWorker) {
    _addWorker = addWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the updateWorker field.
   * @return the updateWorker
   */
  public DbConfigTypeMasterWorker<T> getUpdateWorker() {
    return _updateWorker;
  }

  /**
   * Sets the updateWorker field.
   * @param updateWorker  the updateWorker
   */
  public void setUpdateWorker(DbConfigTypeMasterWorker<T> updateWorker) {
    _updateWorker = updateWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the removeWorker field.
   * @return the removeWorker
   */
  public DbConfigTypeMasterWorker<T> getRemoveWorker() {
    return _removeWorker;
  }

  /**
   * Sets the removeWorker field.
   * @param removeWorker  the removeWorker
   */
  public void setRemoveWorker(DbConfigTypeMasterWorker<T> removeWorker) {
    _removeWorker = removeWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the historyWorker field.
   * @return the historyWorker
   */
  public DbConfigTypeMasterWorker<T> getHistoryWorker() {
    return _historyWorker;
  }

  /**
   * Sets the historyWorker field.
   * @param historyWorker  the historyWorker
   */
  public void setHistoryWorker(DbConfigTypeMasterWorker<T> historyWorker) {
    _historyWorker = historyWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the correctWorker field.
   * @return the correctWorker
   */
  public DbConfigTypeMasterWorker<T> getCorrectWorker() {
    return _correctWorker;
  }

  /**
   * Sets the correctWorker field.
   * @param correctWorker  the correctWorker
   */
  public void setCorrectWorker(DbConfigTypeMasterWorker<T> correctWorker) {
    _correctWorker = correctWorker;
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
