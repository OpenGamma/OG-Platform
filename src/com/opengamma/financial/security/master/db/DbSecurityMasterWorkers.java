/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

/**
 * Configuration object defining the workers in use for {@code DbSecurityMaster}.
 * <p>
 * This class is designed for injection of alternative implementations.
 */
public class DbSecurityMasterWorkers {

  /** Worker. */
  private DbSecurityMasterWorker _searchWorker = new QuerySecurityDbSecurityMasterWorker();
  /** Worker. */
  private DbSecurityMasterWorker _getWorker = new QuerySecurityDbSecurityMasterWorker();
  /** Worker. */
  private DbSecurityMasterWorker _addWorker = new ModifySecurityDbSecurityMasterWorker();
  /** Worker. */
  private DbSecurityMasterWorker _updateWorker = new ModifySecurityDbSecurityMasterWorker();
  /** Worker. */
  private DbSecurityMasterWorker _removeWorker = new ModifySecurityDbSecurityMasterWorker();
  /** Worker. */
  private DbSecurityMasterWorker _searchHistoricWorker = new QuerySecurityDbSecurityMasterWorker();
  /** Worker. */
  private DbSecurityMasterWorker _correctWorker = new ModifySecurityDbSecurityMasterWorker();

  /**
   * Creates an instance.
   */
  public DbSecurityMasterWorkers() {
  }

  /**
   * Initializes the instance.
   * @param master  the security master, non-null
   */
  protected void init(final DbSecurityMaster master) {
    _searchWorker.init(master);
    _getWorker.init(master);
    _addWorker.init(master);
    _updateWorker.init(master);
    _removeWorker.init(master);
    _searchHistoricWorker.init(master);
    _correctWorker.init(master);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the searchWorker field.
   * @return the searchWorker
   */
  public DbSecurityMasterWorker getSearchWorker() {
    return _searchWorker;
  }

  /**
   * Sets the searchWorker field.
   * @param searchWorker  the searchWorker
   */
  public void setSearchWorker(DbSecurityMasterWorker searchWorker) {
    _searchWorker = searchWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the getWorker field.
   * @return the getWorker
   */
  public DbSecurityMasterWorker getGetWorker() {
    return _getWorker;
  }

  /**
   * Sets the getWorker field.
   * @param getWorker  the getWorker
   */
  public void setGetWorker(DbSecurityMasterWorker getWorker) {
    _getWorker = getWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the addWorker field.
   * @return the addWorker
   */
  public DbSecurityMasterWorker getAddWorker() {
    return _addWorker;
  }

  /**
   * Sets the addWorker field.
   * @param addWorker  the addWorker
   */
  public void setAddWorker(DbSecurityMasterWorker addWorker) {
    _addWorker = addWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the updateWorker field.
   * @return the updateWorker
   */
  public DbSecurityMasterWorker getUpdateWorker() {
    return _updateWorker;
  }

  /**
   * Sets the updateWorker field.
   * @param updateWorker  the updateWorker
   */
  public void setUpdateWorker(DbSecurityMasterWorker updateWorker) {
    _updateWorker = updateWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the removeWorker field.
   * @return the removeWorker
   */
  public DbSecurityMasterWorker getRemoveWorker() {
    return _removeWorker;
  }

  /**
   * Sets the removeWorker field.
   * @param removeWorker  the removeWorker
   */
  public void setRemoveWorker(DbSecurityMasterWorker removeWorker) {
    _removeWorker = removeWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the searchHistoricWorker field.
   * @return the searchHistoricWorker
   */
  public DbSecurityMasterWorker getSearchHistoricWorker() {
    return _searchHistoricWorker;
  }

  /**
   * Sets the searchHistoricWorker field.
   * @param searchHistoricWorker  the searchHistoricWorker
   */
  public void setSearchHistoricWorker(DbSecurityMasterWorker searchHistoricWorker) {
    _searchHistoricWorker = searchHistoricWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the correctWorker field.
   * @return the correctWorker
   */
  public DbSecurityMasterWorker getCorrectWorker() {
    return _correctWorker;
  }

  /**
   * Sets the correctWorker field.
   * @param correctWorker  the correctWorker
   */
  public void setCorrectWorker(DbSecurityMasterWorker correctWorker) {
    _correctWorker = correctWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this security master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "@" + System.identityHashCode(this);
  }

}
