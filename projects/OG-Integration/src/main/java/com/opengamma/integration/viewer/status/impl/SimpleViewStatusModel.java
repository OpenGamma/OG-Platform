/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.integration.viewer.status.ViewStatus;
import com.opengamma.integration.viewer.status.ViewStatusKey;
import com.opengamma.integration.viewer.status.ViewStatusModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple implementation of {@link ViewStatusModel}
 */
public class SimpleViewStatusModel implements ViewStatusModel {
  /**
   * Column headers
   */
  private final List<List<String>> _columnHeaders;
  /**
   * The rows
   */
  private final List<List<Object>> _rows;
  /**
   * The unaggregated result.
   */
  private final Map<ViewStatusKey, ViewStatus> _viewStatusResult;
    
  public SimpleViewStatusModel(List<List<String>> columnHeaders, List<List<Object>> rows, Map<ViewStatusKey, ViewStatus> viewStatusResult) {
    ArgumentChecker.notNull(columnHeaders, "columnHeaders");
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(viewStatusResult, "viewStatusResult");
    
    _columnHeaders = deepCopyHeaders(columnHeaders);
    _rows = deepCopyRows(rows);
    _viewStatusResult = ImmutableMap.copyOf(viewStatusResult);
  }

  private List<List<Object>> deepCopyRows(List<List<Object>> rows) {
    List<List<Object>> result = Lists.newArrayListWithCapacity(rows.size());
    for (List<Object> row : rows) {
      result.add(Lists.newArrayList(row));
    }
    return result;
  }

  private List<List<String>> deepCopyHeaders(final List<List<String>> columnHeaders) {
    List<List<String>> headers = Lists.newArrayListWithCapacity(columnHeaders.size());
    for (List<String> headings : columnHeaders) {
      headers.add(Lists.newArrayList(headings));
    }
    return headers;
  }

  @Override
  public ViewStatus getStatus(ViewStatusKey entry) {
    return _viewStatusResult.get(entry);
  }
  
  @Override
  public Set<ViewStatusKey> keySet() {
    return ImmutableSet.copyOf(_viewStatusResult.keySet());
  }

  @Override
  public Set<String> getValueRequirementNames() {
    Set<String> result = Sets.newHashSetWithExpectedSize(_viewStatusResult.size());
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      result.add(key.getValueRequirementName());
    }
    return result;
  }

  @Override
  public Set<String> getCurrencies() {
    Set<String> result = Sets.newHashSetWithExpectedSize(_viewStatusResult.size());
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      result.add(key.getCurrency());
    }
    return result;
  }
  
  @Override
  public Set<String> getComputationTargetTypes() {
    Set<String> result = Sets.newHashSetWithExpectedSize(_viewStatusResult.size());
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      result.add(key.getTargetType());
    }
    return result;
  }

  @Override
  public Set<String> getSecurityTypes() {
    Set<String> result = Sets.newHashSetWithExpectedSize(_viewStatusResult.size());
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      result.add(key.getSecurityType());
    }
    return result;
  }

  @Override
  public int getRowCount() {
    return _rows.size();
  }

  @Override
  public int getColumnCount() {
    return Iterables.getFirst(_columnHeaders, Lists.newArrayList()).size();
  }


  @Override
  public Object getRowValueAt(int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= _rows.size()) {
      throw new IllegalArgumentException("RowIndex must be in range 0 >= rowIndex < " + _rows.size());
    }
    if (columnIndex < 0 || columnIndex >= getColumnCount()) {
      throw new IllegalArgumentException("ColumnIndex must be in range 0 >= columnIndex < " + getColumnCount());
    }
    return Iterables.get(Iterables.get(_rows, rowIndex), columnIndex);
  }

  @Override
  public int getHeaderRowCount() {
    return _columnHeaders.size();
  }

  @Override
  public String getColumnNameAt(int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= _columnHeaders.size()) {
      throw new IllegalArgumentException("RowIndex must be in range 0 >= rowIndex < " + _columnHeaders.size());
    }
    if (columnIndex < 0 || columnIndex >= getColumnCount()) {
      throw new IllegalArgumentException("ColumnIndex must be in range 0 >= columnIndex < " + getColumnCount());
    }
    return Iterables.get(Iterables.get(_columnHeaders, rowIndex), columnIndex);
  }

}
