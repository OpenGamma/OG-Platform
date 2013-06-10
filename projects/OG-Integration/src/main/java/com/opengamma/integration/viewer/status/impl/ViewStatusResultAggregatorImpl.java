/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import static com.opengamma.integration.viewer.status.impl.ViewStatusKeyBean.getMetaProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.integration.viewer.status.ViewAggregationType;
import com.opengamma.integration.viewer.status.ViewStatusKey;
import com.opengamma.integration.viewer.status.ViewStatusModel;
import com.opengamma.integration.viewer.status.ViewStatusResultAggregator;
import com.opengamma.integration.viewer.status.impl.ViewStatusKeyBean.Meta;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link ViewStatusResultAggregator}
 */
public class ViewStatusResultAggregatorImpl implements ViewStatusResultAggregator {
  
  private static final Map<MetaProperty<?>, String> HEADERS = Maps.newHashMap();
  
  /**
   * Header for Security Type
   */
  public static final String SECURITY_HEADER = "SecurityType";
  /**
   * Header for Value Requirement Name
   */
  public static final String VALUE_REQUIREMENT_NAME_HEADER = "ValueRequirementName";
  /**
   * Header for Currency
   */
  public static final String CURRENCY_HEADER = "Currency";
  
  private Map<ViewStatusKey, Boolean> _viewStatusResult = Maps.newConcurrentMap();
  
  static {
    Meta statusKeyMeta = ViewStatusKeyBean.meta();
    HEADERS.put(statusKeyMeta.securityType(), SECURITY_HEADER);
    HEADERS.put(statusKeyMeta.valueRequirementName(), VALUE_REQUIREMENT_NAME_HEADER);
    HEADERS.put(statusKeyMeta.currency(), CURRENCY_HEADER);
  }

  public ViewStatusModel aggregate(final ViewAggregationType columnType, final ViewAggregationType rowType, final ViewAggregationType subRowType) {
    ArgumentChecker.notNull(columnType, "columnType");
    ArgumentChecker.notNull(rowType, "rowType");
    ArgumentChecker.notNull(subRowType, "rowType");
    
    validateAggregationParameters(columnType, rowType, subRowType);
    
    final MetaProperty<String> colPropertyMeta = getMetaProperty(columnType);
    final MetaProperty<String> rowPropertyMeta = getMetaProperty(rowType);
    final MetaProperty<String> subRowPropertyMeta = getMetaProperty(subRowType);
    String[][] columnHeaders = buildColumnHeaders(colPropertyMeta, rowPropertyMeta, subRowPropertyMeta);
    Object[][] rowData = buildRowValues(columnHeaders[0].length, colPropertyMeta, rowPropertyMeta, subRowPropertyMeta);
    
    return new SimpleViewStatusModel(columnHeaders, rowData, ImmutableMap.copyOf(_viewStatusResult));
  }

  private Object[][] buildRowValues(int colSize, final MetaProperty<String> colPropertyMeta, final MetaProperty<String> rowPropertyMeta, final MetaProperty<String> subRowPropertyMeta) {
    List<List<Object>> results = Lists.newArrayList();
    
    Set<String> rows = Sets.newTreeSet(getKeyComponentEntries(rowPropertyMeta));
    Set<String> subRows = Sets.newTreeSet(getKeyComponentEntries(subRowPropertyMeta));
    Set<String> cols = Sets.newTreeSet(getKeyComponentEntries(colPropertyMeta));
    
    for (String row : rows) {
      int count = 0;
      for (String subRow : subRows) {
        if (includeRow(rowPropertyMeta, row, subRowPropertyMeta, subRow, colPropertyMeta, cols)) {
          List<Object> rowData = Lists.newArrayList();
          if (count == 0) {
            rowData.add(row);
          } else {
            rowData.add(StringUtils.EMPTY);
          }
          count++;
          rowData.add(subRow);
          for (String col : cols) {
            Object columnValue = getColumnValue(rowPropertyMeta, row, subRowPropertyMeta, subRow, colPropertyMeta, col);
            columnValue = columnValue == null ? StringUtils.EMPTY : columnValue;
            rowData.add(columnValue);
          }
          results.add(rowData);
        }
      }
    }
    return rowValuesToArray(results);
  }

  private Object[][] rowValuesToArray(final List<List<Object>> rowsAsList) {
    Object[][] rows = new Object[rowsAsList.size()][];
    int rowCounter = 0;
    for (List<Object> rowList : rowsAsList) {
      Object[] row = new Object[rowList.size()];
      int colCounter = 0;
      for (Object col : rowList) {
        row[colCounter++] = col;
      }
      rows[rowCounter++] = row; 
    }
    return rows;
  }

  private Object getColumnValue(MetaProperty<?> rowPropertyMeta, String row, MetaProperty<?> subRowPropertyMeta, String subRow, MetaProperty<?> colPropertyMeta, String col) {
    Meta keyMeta = ViewStatusKeyBean.meta();
    BeanBuilder<? extends ViewStatusKeyBean> beanBuilder = keyMeta.builder();
    beanBuilder.set(rowPropertyMeta, row);
    beanBuilder.set(subRowPropertyMeta, subRow);
    beanBuilder.set(colPropertyMeta, col);
    ViewStatusKeyBean key = beanBuilder.build();    
    return _viewStatusResult.get(ImmutableViewStatusKey.of(key));
  }

  private boolean includeRow(MetaProperty<?> rowPropertyMeta, String row, MetaProperty<?> subRowPropertyMeta, String subRow, MetaProperty<?> colPropertyMeta, Set<String> cols) {
    if (cols.isEmpty()) {
      return false;
    }
    Meta keyMeta = ViewStatusKeyBean.meta();
    BeanBuilder<? extends ViewStatusKeyBean> beanBuilder = keyMeta.builder();
    beanBuilder.set(rowPropertyMeta, row);
    beanBuilder.set(subRowPropertyMeta, subRow);
    
    for (String col : cols) {
      beanBuilder.set(colPropertyMeta, col);
      ViewStatusKeyBean key = beanBuilder.build();
      if (_viewStatusResult.get(ImmutableViewStatusKey.of(key)) != null) {
        return true;
      }
    }
    return false;
  }


  private void validateAggregationParameters(final ViewAggregationType columnType, final ViewAggregationType rowType, final ViewAggregationType subRowType) {
    
    if (getMetaProperty(columnType) == null) {
      throw new IllegalArgumentException(columnType + " does not exists in view status key");
    }
    if (getMetaProperty(rowType) == null) {
      throw new IllegalArgumentException(rowType + " does not exists in view status key");
    }
    if (getMetaProperty(subRowType) == null) {
      throw new IllegalArgumentException(subRowType + " does not exists in view status key");
    }
    
    Set<ViewAggregationType> dimensions = Sets.newHashSet();
    dimensions.add(columnType);
    dimensions.add(rowType);
    dimensions.add(subRowType);
    
    if (dimensions.size() != 3) {
      throw new IllegalArgumentException(String.format("ColumnType:%s  RowType:%s SubRowType:%s names must all be unique", columnType, rowType, subRowType));
    }
    
  }

  private String[][] buildColumnHeaders(MetaProperty<?> columnProperty, MetaProperty<?> rowProperty, MetaProperty<?> rowSubProperty) {
    String[][] result = new String[2][];
    
    Set<String> subColumnHeaders = Sets.newTreeSet(getKeyComponentEntries(columnProperty));
    
    int columnSize = 2 + subColumnHeaders.size();
    
    String[] topHeader = new String[columnSize];
    topHeader[0] = HEADERS.get(rowProperty);
    topHeader[1] = HEADERS.get(rowSubProperty);
    topHeader[2] = HEADERS.get(columnProperty);
    for (int i = 3; i < columnSize; i++) {
      topHeader[i] = StringUtils.EMPTY;
    }
    
    result[0] = topHeader;
    
    String[] subHeader = new String[columnSize];
    subHeader[0] = StringUtils.EMPTY;
    subHeader[1] = StringUtils.EMPTY;
    int counter = 2;
    for (String subColumn : subColumnHeaders) {
      subHeader[counter++] = subColumn;
    }
    
    result[1] = subHeader;
    
    return result;
  }
  
  private Set<String> getKeyComponentEntries(final MetaProperty<?> statusKey) {
    Meta meta = ViewStatusKeyBean.meta();
    if (meta.securityType().equals(statusKey)) {
      return getSecurityTypes();
    } else if (meta.valueRequirementName().equals(statusKey)) {
      return getValueNames();
    } else if (meta.currency().equals(statusKey)) {
      return getCurrencies();
    } else {
      return Sets.newHashSet();
    }
  }
  
  private Set<String> getSecurityTypes() {
    Set<String> result = Sets.newTreeSet();
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      result.add(key.getSecurityType());
    }
    return result;
  }

  private Set<String> getValueNames() {
    Set<String> result = Sets.newTreeSet();
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      result.add(key.getValueRequirementName());
    }
    return result;
  }

  private Set<String> getCurrencies() {
    Set<String> result = Sets.newTreeSet();
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      result.add(key.getCurrency());
    }
    return result;
  }
  
  @Override
  public void put(ViewStatusKey key, boolean status) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(status, "status");
    
    _viewStatusResult.put(ImmutableViewStatusKey.of(key), status);
  }
  
  @Override
  public Boolean get(ViewStatusKey key) {
    if (key == null) {
      return null;
    } else {
      return _viewStatusResult.get(ImmutableViewStatusKey.of(key)); 
    }
  }
    
  /**
   * Immutable key into view status result map
   */
  private static class ImmutableViewStatusKey implements ViewStatusKey  {
    
    private final String _securityType;
    
    private final String _valueName;
    
    private final String _currency;
    
    public ImmutableViewStatusKey(String securityType, String valueName, String currency) {
      ArgumentChecker.notNull(securityType, "securityType");
      ArgumentChecker.notNull(valueName, "valueName");
      ArgumentChecker.notNull(currency, "currency");
      
      _securityType = securityType;
      _valueName = valueName;
      _currency = currency;
      
    }

    @Override
    public String getSecurityType() {
      return _securityType;
    }

    @Override
    public String getValueRequirementName() {
      return _valueName;
    }

    @Override
    public String getCurrency() {
      return _currency;
    }
    
    public static ImmutableViewStatusKey of(final ViewStatusKey key) {
      ArgumentChecker.notNull(key, "key");
      return new ImmutableViewStatusKey(key.getSecurityType(), key.getValueRequirementName(), key.getCurrency());
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }
  }

}
