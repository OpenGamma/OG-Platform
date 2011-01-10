/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

/**
 * Configuration object defining the workers in use for {@code DbPositionMaster}.
 * <p>
 * This class is designed for injection of alternative implementations.
 */
public class DbPositionMasterWorkers {

  /** Worker. */
  private DbPositionMasterWorker _searchWorker = new QueryPositionDbPositionMasterWorker();
  /** Worker. */
  private DbPositionMasterWorker _getWorker = new QueryPositionDbPositionMasterWorker();
  /** Worker. */
  private DbPositionMasterWorker _addWorker = new ModifyPositionDbPositionMasterWorker();
  /** Worker. */
  private DbPositionMasterWorker _updateWorker = new ModifyPositionDbPositionMasterWorker();
  /** Worker. */
  private DbPositionMasterWorker _removeWorker = new ModifyPositionDbPositionMasterWorker();
  /** Worker. */
  private DbPositionMasterWorker _historyWorker = new QueryPositionDbPositionMasterWorker();
  /** Worker. */
  private DbPositionMasterWorker _correctWorker = new ModifyPositionDbPositionMasterWorker();
  /** Worker. */
  private DbPositionMasterWorker _getTradeWorker = new QueryPositionDbPositionMasterWorker();

  /**
   * Creates an instance.
   */
  public DbPositionMasterWorkers() {
  }

  /**
   * Initializes the instance.
   * @param master  the position master, not null
   */
  protected void init(final DbPositionMaster master) {
    _searchWorker.init(master);
    _getWorker.init(master);
    _addWorker.init(master);
    _updateWorker.init(master);
    _removeWorker.init(master);
    _historyWorker.init(master);
    _correctWorker.init(master);
    _getTradeWorker.init(master);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the searchWorker field.
   * @return the searchWorker
   */
  public DbPositionMasterWorker getSearchWorker() {
    return _searchWorker;
  }

  /**
   * Sets the searchWorker field.
   * @param searchWorker  the searchWorker
   */
  public void setSearchWorker(DbPositionMasterWorker searchWorker) {
    _searchWorker = searchWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the getWorker field.
   * @return the getWorker
   */
  public DbPositionMasterWorker getGetWorker() {
    return _getWorker;
  }

  /**
   * Sets the getWorker field.
   * @param getWorker  the getWorker
   */
  public void setGetWorker(DbPositionMasterWorker getWorker) {
    _getWorker = getWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the addWorker field.
   * @return the addWorker
   */
  public DbPositionMasterWorker getAddWorker() {
    return _addWorker;
  }

  /**
   * Sets the addWorker field.
   * @param addWorker  the addWorker
   */
  public void setAddWorker(DbPositionMasterWorker addWorker) {
    _addWorker = addWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the updateWorker field.
   * @return the updateWorker
   */
  public DbPositionMasterWorker getUpdateWorker() {
    return _updateWorker;
  }

  /**
   * Sets the updateWorker field.
   * @param updateWorker  the updateWorker
   */
  public void setUpdateWorker(DbPositionMasterWorker updateWorker) {
    _updateWorker = updateWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the removeWorker field.
   * @return the removeWorker
   */
  public DbPositionMasterWorker getRemoveWorker() {
    return _removeWorker;
  }

  /**
   * Sets the removeWorker field.
   * @param removeWorker  the removeWorker
   */
  public void setRemoveWorker(DbPositionMasterWorker removeWorker) {
    _removeWorker = removeWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the historyWorker field.
   * @return the historyWorker
   */
  public DbPositionMasterWorker getHistoryWorker() {
    return _historyWorker;
  }

  /**
   * Sets the historyWorker field.
   * @param historyWorker  the historyWorker
   */
  public void setHistoryWorker(DbPositionMasterWorker historyWorker) {
    _historyWorker = historyWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the correctWorker field.
   * @return the correctWorker
   */
  public DbPositionMasterWorker getCorrectWorker() {
    return _correctWorker;
  }

  /**
   * Sets the correctWorker field.
   * @param correctWorker  the correctWorker
   */
  public void setCorrectWorker(DbPositionMasterWorker correctWorker) {
    _correctWorker = correctWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the getTradeWorker field.
   * @return the getTradeWorker
   */
  public DbPositionMasterWorker getGetTradeWorker() {
    return _getTradeWorker;
  }

  /**
   * Sets the getTradeWorker field.
   * @param getTradeWorker  the getTradeWorker
   */
  public void setGetTradeWorker(DbPositionMasterWorker getTradeWorker) {
    _getTradeWorker = getTradeWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of these workers.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "@" + System.identityHashCode(this);
  }

}
