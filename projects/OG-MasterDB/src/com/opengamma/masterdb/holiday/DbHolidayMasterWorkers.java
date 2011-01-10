/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

/**
 * Configuration object defining the workers in use for {@code DbHolidayMaster}.
 * <p>
 * This class is designed for injection of alternative implementations.
 */
public class DbHolidayMasterWorkers {

  /** Worker. */
  private DbHolidayMasterWorker _searchWorker = new QueryHolidayDbHolidayMasterWorker();
  /** Worker. */
  private DbHolidayMasterWorker _getWorker = new QueryHolidayDbHolidayMasterWorker();
  /** Worker. */
  private DbHolidayMasterWorker _addWorker = new ModifyHolidayDbHolidayMasterWorker();
  /** Worker. */
  private DbHolidayMasterWorker _updateWorker = new ModifyHolidayDbHolidayMasterWorker();
  /** Worker. */
  private DbHolidayMasterWorker _removeWorker = new ModifyHolidayDbHolidayMasterWorker();
  /** Worker. */
  private DbHolidayMasterWorker _historyWorker = new QueryHolidayDbHolidayMasterWorker();
  /** Worker. */
  private DbHolidayMasterWorker _correctWorker = new ModifyHolidayDbHolidayMasterWorker();

  /**
   * Creates an instance.
   */
  public DbHolidayMasterWorkers() {
  }

  /**
   * Initializes the instance.
   * @param master  the holiday master, not null
   */
  protected void init(final DbHolidayMaster master) {
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
  public DbHolidayMasterWorker getSearchWorker() {
    return _searchWorker;
  }

  /**
   * Sets the searchWorker field.
   * @param searchWorker  the searchWorker
   */
  public void setSearchWorker(DbHolidayMasterWorker searchWorker) {
    _searchWorker = searchWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the getWorker field.
   * @return the getWorker
   */
  public DbHolidayMasterWorker getGetWorker() {
    return _getWorker;
  }

  /**
   * Sets the getWorker field.
   * @param getWorker  the getWorker
   */
  public void setGetWorker(DbHolidayMasterWorker getWorker) {
    _getWorker = getWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the addWorker field.
   * @return the addWorker
   */
  public DbHolidayMasterWorker getAddWorker() {
    return _addWorker;
  }

  /**
   * Sets the addWorker field.
   * @param addWorker  the addWorker
   */
  public void setAddWorker(DbHolidayMasterWorker addWorker) {
    _addWorker = addWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the updateWorker field.
   * @return the updateWorker
   */
  public DbHolidayMasterWorker getUpdateWorker() {
    return _updateWorker;
  }

  /**
   * Sets the updateWorker field.
   * @param updateWorker  the updateWorker
   */
  public void setUpdateWorker(DbHolidayMasterWorker updateWorker) {
    _updateWorker = updateWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the removeWorker field.
   * @return the removeWorker
   */
  public DbHolidayMasterWorker getRemoveWorker() {
    return _removeWorker;
  }

  /**
   * Sets the removeWorker field.
   * @param removeWorker  the removeWorker
   */
  public void setRemoveWorker(DbHolidayMasterWorker removeWorker) {
    _removeWorker = removeWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the historyWorker field.
   * @return the historyWorker
   */
  public DbHolidayMasterWorker getHistoryWorker() {
    return _historyWorker;
  }

  /**
   * Sets the historyWorker field.
   * @param historyWorker  the historyWorker
   */
  public void setHistoryWorker(DbHolidayMasterWorker historyWorker) {
    _historyWorker = historyWorker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the correctWorker field.
   * @return the correctWorker
   */
  public DbHolidayMasterWorker getCorrectWorker() {
    return _correctWorker;
  }

  /**
   * Sets the correctWorker field.
   * @param correctWorker  the correctWorker
   */
  public void setCorrectWorker(DbHolidayMasterWorker correctWorker) {
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
