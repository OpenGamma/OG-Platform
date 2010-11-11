/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

/**
 * Configuration object defining the workers in use for {@code DbExchangeMaster}.
 * <p>
 * This class is designed for injection of alternative implementations.
 */
public class DbExchangeMasterWorkers {

  /** Worker. */
  private DbExchangeMasterWorker _searchWorker = new QueryExchangeDbExchangeMasterWorker();
  /** Worker. */
  private DbExchangeMasterWorker _getWorker = new QueryExchangeDbExchangeMasterWorker();
  /** Worker. */
  private DbExchangeMasterWorker _addWorker = new ModifyExchangeDbExchangeMasterWorker();
  /** Worker. */
  private DbExchangeMasterWorker _updateWorker = new ModifyExchangeDbExchangeMasterWorker();
  /** Worker. */
  private DbExchangeMasterWorker _removeWorker = new ModifyExchangeDbExchangeMasterWorker();
  /** Worker. */
  private DbExchangeMasterWorker _historyWorker = new QueryExchangeDbExchangeMasterWorker();
  /** Worker. */
  private DbExchangeMasterWorker _correctWorker = new ModifyExchangeDbExchangeMasterWorker();

  /**
   * Creates an instance.
   */
  public DbExchangeMasterWorkers() {
  }

  /**
   * Initializes the instance.
   * @param master  the exchange master, non-null
   */
  protected void init(final DbExchangeMaster master) {
    _searchWorker.init(master);
    _getWorker.init(master);
    _addWorker.init(master);
    _updateWorker.init(master);
    _removeWorker.init(master);
    _historyWorker.init(master);
    _correctWorker.init(master);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the searchWorker field.
   * @return the searchWorker
   */
  public DbExchangeMasterWorker getSearchWorker() {
    return _searchWorker;
  }

  /**
   * Sets the searchWorker field.
   * @param searchWorker  the searchWorker
   */
  public void setSearchWorker(DbExchangeMasterWorker searchWorker) {
    _searchWorker = searchWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the getWorker field.
   * @return the getWorker
   */
  public DbExchangeMasterWorker getGetWorker() {
    return _getWorker;
  }

  /**
   * Sets the getWorker field.
   * @param getWorker  the getWorker
   */
  public void setGetWorker(DbExchangeMasterWorker getWorker) {
    _getWorker = getWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the addWorker field.
   * @return the addWorker
   */
  public DbExchangeMasterWorker getAddWorker() {
    return _addWorker;
  }

  /**
   * Sets the addWorker field.
   * @param addWorker  the addWorker
   */
  public void setAddWorker(DbExchangeMasterWorker addWorker) {
    _addWorker = addWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the updateWorker field.
   * @return the updateWorker
   */
  public DbExchangeMasterWorker getUpdateWorker() {
    return _updateWorker;
  }

  /**
   * Sets the updateWorker field.
   * @param updateWorker  the updateWorker
   */
  public void setUpdateWorker(DbExchangeMasterWorker updateWorker) {
    _updateWorker = updateWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the removeWorker field.
   * @return the removeWorker
   */
  public DbExchangeMasterWorker getRemoveWorker() {
    return _removeWorker;
  }

  /**
   * Sets the removeWorker field.
   * @param removeWorker  the removeWorker
   */
  public void setRemoveWorker(DbExchangeMasterWorker removeWorker) {
    _removeWorker = removeWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the historyWorker field.
   * @return the historyWorker
   */
  public DbExchangeMasterWorker getHistoryWorker() {
    return _historyWorker;
  }

  /**
   * Sets the historyWorker field.
   * @param historyWorker  the historyWorker
   */
  public void setHistoryWorker(DbExchangeMasterWorker historyWorker) {
    _historyWorker = historyWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the correctWorker field.
   * @return the correctWorker
   */
  public DbExchangeMasterWorker getCorrectWorker() {
    return _correctWorker;
  }

  /**
   * Sets the correctWorker field.
   * @param correctWorker  the correctWorker
   */
  public void setCorrectWorker(DbExchangeMasterWorker correctWorker) {
    _correctWorker = correctWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of the workers.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "@" + System.identityHashCode(this);
  }

}
