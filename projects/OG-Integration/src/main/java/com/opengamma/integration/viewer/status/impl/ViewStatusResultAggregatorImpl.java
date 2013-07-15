/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.integration.viewer.status.AggregateType;
import com.opengamma.integration.viewer.status.ViewColumnType;
import com.opengamma.integration.viewer.status.ViewStatus;
import com.opengamma.integration.viewer.status.ViewStatusKey;
import com.opengamma.integration.viewer.status.ViewStatusModel;
import com.opengamma.integration.viewer.status.ViewStatusResultAggregator;
import com.opengamma.integration.viewer.status.impl.ViewStatusKeyBean.Meta;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link ViewStatusResultAggregator}
 */
public class ViewStatusResultAggregatorImpl implements ViewStatusResultAggregator {
  
  private static final Logger s_logger = LoggerFactory.getLogger(ViewStatusResultAggregatorImpl.class);
  
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
  /**
   * Header for Target type
   */
  public static final String TARGET_TYPE_HEADER = "Target Type";
  /**
   * Header for status
   */
  public static final String STATUS = "Status";
  
  private Map<ViewStatusKey, ViewStatus> _viewStatusResult = Maps.newConcurrentMap();
  
  private static final Map<MetaProperty<?>, String> HEADERS = Maps.newHashMap();
  static {
    Meta statusKeyMeta = ViewStatusKeyBean.meta();
    HEADERS.put(statusKeyMeta.securityType(), SECURITY_HEADER);
    HEADERS.put(statusKeyMeta.valueRequirementName(), VALUE_REQUIREMENT_NAME_HEADER);
    HEADERS.put(statusKeyMeta.currency(), CURRENCY_HEADER);
    HEADERS.put(statusKeyMeta.targetType(), TARGET_TYPE_HEADER);
  }
  
  private static final String[] DEFAULT_HEADERS = {TARGET_TYPE_HEADER, SECURITY_HEADER, VALUE_REQUIREMENT_NAME_HEADER, CURRENCY_HEADER, STATUS};
  
  private static final String EMPTY_STR = StringUtils.EMPTY;
  
  @Override
  public ViewStatusModel aggregate(AggregateType aggregateType) {
    ArgumentChecker.notNull(aggregateType, "aggregateType");
    if (aggregateType == AggregateType.NO_AGGREGATION) {
      return defaultModel();
    }
    return aggregate(aggregateType.getColumnTypes());
  }

  private ViewStatusModel aggregate(final List<ViewColumnType> columnTypes) {
    ArgumentChecker.notNull(columnTypes, "aggregations");
 
    if (columnTypes.isEmpty()) {
      return defaultModel();
    }
    
    Iterable<ViewColumnType> fixedColumnTypes = Iterables.limit(columnTypes, columnTypes.size() - 1);
    final Map<List<String>, Set<String>> fixedRow2Columns = Maps.newHashMap();
    
    for (ViewStatusKey viewStatusKey : _viewStatusResult.keySet()) {
      ViewStatusKeyBean viewStatusKeyBean = new ViewStatusKeyBean(viewStatusKey.getSecurityType(), viewStatusKey.getValueRequirementName(), 
          viewStatusKey.getCurrency(), viewStatusKey.getTargetType());
      List<String> key = viewStatusKey(viewStatusKeyBean, fixedColumnTypes);
      
      Set<String> columnValues = fixedRow2Columns.get(key);
      if (columnValues == null) {
        columnValues = Sets.newHashSet();
        fixedRow2Columns.put(key, columnValues);
      }
      columnValues.addAll(viewStatusKey(viewStatusKeyBean, Collections.singletonList(Iterables.getLast(columnTypes))));
    }
    
    Set<String> extraColumns = getExtraColumns(fixedRow2Columns);
       
    List<List<String>> columnHeaders = makeHeaders(columnTypes, extraColumns);
    List<List<Object>> rowData = createRowData(fixedRow2Columns, extraColumns, columnTypes);
    
    return new SimpleViewStatusModel(columnHeaders, rowData, _viewStatusResult);
  }

  private List<List<Object>> createRowData(final Map<List<String>, Set<String>> fixedRow2Columns, final Set<String> extraColumns, List<ViewColumnType> columnTypes) {
    
    List<List<String>> rows = Lists.newArrayList(fixedRow2Columns.keySet());
    Comparator<List<String>> rowComparator = new Comparator<List<String>>() {

      @Override
      public int compare(List<String> left, List<String> right) {
        int compare = 0;
        for (int i = 0; i < left.size(); i++) {
          compare = left.get(i).compareTo(right.get(i));
          if (compare != 0) {
            return compare;
          }
        }
        return compare;
      }
    };

    Collections.sort(rows, rowComparator);
    
    List<List<Object>> processedRows = Lists.newArrayListWithCapacity(rows.size());

    String[] currentRow = new String[Iterables.getFirst(rows, Lists.newArrayList()).size()];
    for (List<String> row : rows) {
      List<Object> processedRow = Lists.newArrayList();
      Iterable<String> columns = Iterables.limit(row, row.size() - 1);
      int count = 0;
      for (String col : columns) {
        if (currentRow[count] == null || !col.equals(currentRow[count])) {
          currentRow[count] = col;
          processedRow.add(col);
        } else {
          processedRow.add(EMPTY_STR);
        }
        count++;
      }
      processedRow.add(Iterables.getLast(row));
            
      for (String col : extraColumns) {
        List<String> keyMemebers = Lists.newArrayList(row);
        keyMemebers.add(col);
        ViewStatus status = getStatus(keyFromRowValues(keyMemebers, columnTypes));
        if (status == null) {
          processedRow.add(EMPTY_STR);
        } else {
          processedRow.add(status);
        }
      }
      processedRows.add(processedRow);
    }
    return processedRows;
  }

  private ViewStatusKey keyFromRowValues(List<String> keyValues, List<ViewColumnType> columnTypes) {

    Meta keyMeta = ViewStatusKeyBean.meta();
    BeanBuilder<? extends ViewStatusKeyBean> beanBuilder = keyMeta.builder();
    Iterator<String> keyItr = keyValues.iterator();
    Iterator<ViewColumnType> colTypeItr = columnTypes.iterator();
    
    while (keyItr.hasNext() && colTypeItr.hasNext()) {
      beanBuilder.set(colTypeItr.next().getMetaProperty(), keyItr.next());
    }
    ViewStatusKeyBean result = beanBuilder.build();
    s_logger.debug("{} built from properties: {} and types: {}", result, keyValues, columnTypes);
    return result;
  }

  private Set<String> getExtraColumns(Map<List<String>, Set<String>> fixedRow2Columns) {
    Set<String> extraColumns = Sets.newTreeSet();
    Iterables.addAll(extraColumns, Iterables.concat(fixedRow2Columns.values()));
    return extraColumns;
  }

  private List<List<String>> makeHeaders(List<ViewColumnType> columnTypes, Set<String> extraColumns) {
    List<List<String>> headers = Lists.newArrayListWithCapacity(2);
    int colSize = columnTypes.size() + extraColumns.size() - 1;
    headers.add(topColumnHeaders(columnTypes, colSize));
    headers.add(subColumnHeaders(extraColumns, colSize)); 
    return headers;
  }

  private List<String> viewStatusKey(ViewStatusKeyBean viewStatusKeyBean, Iterable<ViewColumnType> fixedColumnTypes) {
    
    List<String> result = Lists.newArrayList();
    for (ViewColumnType keyType : fixedColumnTypes) {
      MetaProperty<String> metaProperty = keyType.getMetaProperty();
      
      if (ViewStatusKeyBean.meta().currency().equals(metaProperty)) {
        result.add(viewStatusKeyBean.getCurrency());
      } else if (ViewStatusKeyBean.meta().securityType().equals(metaProperty)) {
        result.add(viewStatusKeyBean.getSecurityType());
      } else if (ViewStatusKeyBean.meta().targetType().equals(metaProperty)) {
        result.add(viewStatusKeyBean.getTargetType());
      } else if (ViewStatusKeyBean.meta().valueRequirementName().equals(metaProperty)) {
        result.add(viewStatusKeyBean.getValueRequirementName());
      }
    }
    return ImmutableList.copyOf(result);
  }



  private List<String> subColumnHeaders(Set<String> extraColumnHeaders, int colsize) {
    List<String> subHeader = Lists.newArrayListWithCapacity(colsize);
    int emptySize = colsize - extraColumnHeaders.size();
    for (int i = 0; i < emptySize; i++) {
      subHeader.add(StringUtils.EMPTY);
    }
    Iterables.addAll(subHeader, extraColumnHeaders);
    return subHeader;
  }

  private List<String> topColumnHeaders(List<ViewColumnType> columnTypes, int colsize) {
    List<String> topHeader = Lists.newArrayListWithCapacity(colsize);
    for (ViewColumnType columnType : columnTypes) {
      topHeader.add(HEADERS.get(columnType.getMetaProperty()));
    }
    int emptySize = colsize - columnTypes.size();
    for (int i = 0; i < emptySize; i++) {
      topHeader.add(StringUtils.EMPTY);
    }
    return topHeader;
  }
    
  @Override
  public void putStatus(ViewStatusKey key, ViewStatus status) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(status, "status");
    
    _viewStatusResult.put(ImmutableViewStatusKey.of(key), status);
  }
  
  @Override
  public ViewStatus getStatus(ViewStatusKey key) {
    if (key == null) {
      return null;
    } else {
      return _viewStatusResult.get(ImmutableViewStatusKey.of(key)); 
    }
  }
  
  @Override
  public Set<ViewStatusKey> keySet() {
    return Sets.newHashSet(_viewStatusResult.keySet());
  }
  
  private ViewStatusModel defaultModel() {
    List<List<String>> columnHeaders = Lists.newArrayListWithCapacity(1);
    columnHeaders.add(Arrays.asList(DEFAULT_HEADERS));
    
    List<List<Object>> rowData = Lists.newArrayListWithCapacity(_viewStatusResult.size());
    for (ViewStatusKey key : _viewStatusResult.keySet()) {
      List<Object> row = Lists.newArrayList();
      ViewStatus status = _viewStatusResult.get(key);
      
      row.add(key.getTargetType());
      row.add(key.getSecurityType());
      row.add(key.getValueRequirementName());
      row.add(key.getCurrency());
      row.add(status.getValue());
      rowData.add(row);
    }    
    return new SimpleViewStatusModel(columnHeaders, rowData, _viewStatusResult);
  }
  
  /**
   * Immutable key into view status result map
   */
  static class ImmutableViewStatusKey implements ViewStatusKey  {
    
    private final String _securityType;
    
    private final String _valueName;
    
    private final String _currency;
    
    private final String _targetType;
    
    public ImmutableViewStatusKey(String securityType, String valueName, String currency, String targetType) {
      ArgumentChecker.notNull(securityType, "securityType");
      ArgumentChecker.notNull(valueName, "valueName");
      ArgumentChecker.notNull(currency, "currency");
      ArgumentChecker.notNull(targetType, "targetType");
      
      _securityType = securityType;
      _valueName = valueName;
      _currency = currency;
      _targetType = targetType;
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
    
    @Override
    public String getTargetType() {
      return _targetType;
    }
    
    public static ImmutableViewStatusKey of(final ViewStatusKey key) {
      ArgumentChecker.notNull(key, "key");
      return new ImmutableViewStatusKey(key.getSecurityType(), key.getValueRequirementName(), key.getCurrency(), key.getTargetType());
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
