/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

/**
 * Configuration object defining the workers in use for {@code DbPortfolioMaster}.
 * <p>
 * This class is designed for injection of alternative implementations.
 */
public class DbPortfolioMasterWorkers {

  /** Worker. */
  private DbPortfolioMasterWorker _searchWorker = new QueryPortfolioDbPortfolioMasterWorker();
  /** Worker. */
  private DbPortfolioMasterWorker _getWorker = new QueryPortfolioDbPortfolioMasterWorker();
  /** Worker. */
  private DbPortfolioMasterWorker _addWorker = new ModifyPortfolioDbPortfolioMasterWorker();
  /** Worker. */
  private DbPortfolioMasterWorker _updateWorker = new ModifyPortfolioDbPortfolioMasterWorker();
  /** Worker. */
  private DbPortfolioMasterWorker _removeWorker = new ModifyPortfolioDbPortfolioMasterWorker();
  /** Worker. */
  private DbPortfolioMasterWorker _historysWorker = new QueryPortfolioDbPortfolioMasterWorker();
  /** Worker. */
  private DbPortfolioMasterWorker _correctWorker = new ModifyPortfolioDbPortfolioMasterWorker();
  /** Worker. */
  private DbPortfolioMasterWorker _getNodeWorker = new QueryPortfolioDbPortfolioMasterWorker();

  /**
   * Creates an instance.
   */
  public DbPortfolioMasterWorkers() {
  }

  /**
   * Initializes the instance.
   * @param master  the portfolio master, not null
   */
  protected void init(final DbPortfolioMaster master) {
    _searchWorker.init(master);
    _getWorker.init(master);
    _addWorker.init(master);
    _updateWorker.init(master);
    _removeWorker.init(master);
    _historysWorker.init(master);
    _correctWorker.init(master);
    _getNodeWorker.init(master);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the searchWorker field.
   * @return the searchWorker
   */
  public DbPortfolioMasterWorker getSearchWorker() {
    return _searchWorker;
  }

  /**
   * Sets the searchWorker field.
   * @param searchWorker  the searchWorker
   */
  public void setSearchWorker(DbPortfolioMasterWorker searchWorker) {
    _searchWorker = searchWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the getWorker field.
   * @return the getWorker
   */
  public DbPortfolioMasterWorker getGetWorker() {
    return _getWorker;
  }

  /**
   * Sets the getWorker field.
   * @param getWorker  the getWorker
   */
  public void setGetWorker(DbPortfolioMasterWorker getWorker) {
    _getWorker = getWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the addWorker field.
   * @return the addWorker
   */
  public DbPortfolioMasterWorker getAddWorker() {
    return _addWorker;
  }

  /**
   * Sets the addWorker field.
   * @param addWorker  the addWorker
   */
  public void setAddWorker(DbPortfolioMasterWorker addWorker) {
    _addWorker = addWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the updateWorker field.
   * @return the updateWorker
   */
  public DbPortfolioMasterWorker getUpdateWorker() {
    return _updateWorker;
  }

  /**
   * Sets the updateWorker field.
   * @param updateWorker  the updateWorker
   */
  public void setUpdateWorker(DbPortfolioMasterWorker updateWorker) {
    _updateWorker = updateWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the removeWorker field.
   * @return the removeWorker
   */
  public DbPortfolioMasterWorker getRemoveWorker() {
    return _removeWorker;
  }

  /**
   * Sets the removeWorker field.
   * @param removeWorker  the removeWorker
   */
  public void setRemoveWorker(DbPortfolioMasterWorker removeWorker) {
    _removeWorker = removeWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the historysWorker field.
   * @return the historysWorker
   */
  public DbPortfolioMasterWorker getHistorysWorker() {
    return _historysWorker;
  }

  /**
   * Sets the historysWorker field.
   * @param historysWorker  the historysWorker
   */
  public void setHistorysWorker(DbPortfolioMasterWorker historysWorker) {
    _historysWorker = historysWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the correctWorker field.
   * @return the correctWorker
   */
  public DbPortfolioMasterWorker getCorrectWorker() {
    return _correctWorker;
  }

  /**
   * Sets the correctWorker field.
   * @param correctWorker  the correctWorker
   */
  public void setCorrectWorker(DbPortfolioMasterWorker correctWorker) {
    _correctWorker = correctWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the getNodeWorker field.
   * @return the getNodeWorker
   */
  public DbPortfolioMasterWorker getGetNodeWorker() {
    return _getNodeWorker;
  }

  /**
   * Sets the getNodeWorker field.
   * @param getNodeWorker  the getNodeWorker
   */
  public void setGetNodeWorker(DbPortfolioMasterWorker getNodeWorker) {
    _getNodeWorker = getNodeWorker;
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
