/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.opengamma.integration.viewer.status.ViewStatusKey;
import com.opengamma.integration.viewer.status.ViewStatusModel;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Simple implementation of {@link ViewStatusModel}
 */
public class SimpleViewStatusModel implements ViewStatusModel {
  /**
   * Column headers
   */
  private String[][] _columnHeaders;
  /**
   * The rows
   */
  private Object[][] _rows;
  /**
   * The unstructured underlying result set.
   */
  private Map<ViewStatusKey, Boolean> _viewStatusResult;
    
  public SimpleViewStatusModel(String[][] columnHeaders, Object[][] rows, Map<ViewStatusKey, Boolean> viewStatusResult) {
    ArgumentChecker.notNull(columnHeaders, "columnHeaders");
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(viewStatusResult, "viewStatusResult");
    
    _columnHeaders = columnHeaders;
    _rows = rows;
    _viewStatusResult = ImmutableMap.copyOf(viewStatusResult);
  }

  @Override
  public Boolean getStatus(ViewStatusKey entry) {
    return _viewStatusResult.get(entry);
  }

  @Override
  public Set<String> getValueRequirementNames() {
    Set<String> result = Sets.newTreeSet();
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      result.add(key.getValueRequirementName());
    }
    return result;
  }

  @Override
  public Set<Currency> getCurrencies() {
    Set<Currency> result = Sets.newTreeSet();
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      result.add(key.getCurrency());
    }
    return result;
  }

  @Override
  public Set<String> getSecurityTypes() {
    Set<String> result = Sets.newTreeSet();
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      result.add(key.getSecurityType());
    }
    return result;
  }

  @Override
  public int getRowCount() {
    return _rows.length;
  }

  @Override
  public int getColumnCount() {
    return _columnHeaders[0].length;
  }


  @Override
  public Object getRowValueAt(int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= _rows.length) {
      throw new IllegalArgumentException("RowIndex must be in range 0 >= rowIndex < " + _rows.length);
    }
    if (columnIndex < 0 || columnIndex >= _rows[0].length) {
      throw new IllegalArgumentException("ColumnIndex must be in range 0 >= columnIndex < " + _rows[0].length);
    }
    return _rows[rowIndex][columnIndex];
  }

  @Override
  public int getHeaderRowCount() {
    return _columnHeaders.length;
  }

  @Override
  public String getColumnNameAt(int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= _columnHeaders.length) {
      throw new IllegalArgumentException("RowIndex must be in range 0 >= rowIndex < " + _columnHeaders.length);
    }
    if (columnIndex < 0 || columnIndex >= _columnHeaders[0].length) {
      throw new IllegalArgumentException("ColumnIndex must be in range 0 >= columnIndex < " + _columnHeaders[0].length);
    }
    return _columnHeaders[rowIndex][columnIndex];
  }
  
}
