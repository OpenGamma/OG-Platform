/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import java.util.Set;


/**
 * View status model
 */
public interface ViewStatusModel {
  /**
   * Gets all the view status result keys
   * 
   * @return the set of keys for collected view status, empty set if no result.
   */
  Set<ViewStatusKey> keySet();
  /**
   * Gets the status value stored for a key
   * 
   * @param key the view status key, can be null
   * @return the stored status value for a given key or null if no matching key.
   */
  ViewStatus getStatus(ViewStatusKey key);
  /**
   * Gets all the supported value requirement names.
   * 
   * @return the set of value requirement names, empty set if none.
   */
  Set<String> getValueRequirementNames();
  /**
   * Gets all supported currencies.
   * 
   * @return the set of currencies, empty set if none.
   */
  Set<String> getCurrencies();
  /**
   * Gets all supported security types.
   * 
   * @return the set of security types, empty set if none.
   */
  Set<String> getSecurityTypes();
  /**
   * Gets all supported computation target types.
   * 
   * @return the set of computation target types, empty set if none.
   */
  Set<String> getComputationTargetTypes();
  /**
   * Returns the number of rows in the model.
   *
   * @return the number of rows in the model
   */
  int getRowCount();
  /**
   * Returns the number of columns in the model.
   *
   * @return the number of columns in the model
   */
  int getColumnCount();
  /**
   * Returns the value for the cell at <code>columnIndex</code> and
   * <code>rowIndex</code>.
   *
   * @param   rowIndex        the row whose value is to be queried
   * @param   columnIndex     the column whose value is to be queried
   * @return  the value Object at the specified cell
   * @throws IllegalArgumentException if rowIndex or columnIndex is out of range
   */
  Object getRowValueAt(int rowIndex, int columnIndex);
  /**
   * Return the number of rows in the header for the model.
   * 
   * @return the number of rows in the header.
   */
  int getHeaderRowCount();
  /**
   * Returns the name of the column at <code>columnIndex</code> and <code>rowIndex</code>.
   * 
   * @param rowIndex      the row whose value is to be queried
   * @param columnIndex   the column whose value is to be queried
   * @return the name of column header at specified cell
   * @throws IllegalArgumentException if rowIndex or columnIndex is out of range
   */
  String getColumnNameAt(int rowIndex, int columnIndex);
  
}
