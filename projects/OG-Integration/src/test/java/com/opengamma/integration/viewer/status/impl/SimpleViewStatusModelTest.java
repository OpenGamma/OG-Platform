/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.integration.viewer.status.ViewStatus;
import com.opengamma.integration.viewer.status.ViewStatusKey;
import com.opengamma.integration.viewer.status.ViewStatusModel;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link SimpleViewStatusModel}
 */
@Test(groups = TestGroup.UNIT)
public class SimpleViewStatusModelTest {
  
  private static final ViewStatus NO_VALUE = ViewStatus.NO_VALUE;
  private static final ViewStatus VALUE = ViewStatus.VALUE;
  private static final String SWAP = "SWAP";
  private static final String SWAPTION = "SWAPTION";
  private static final String PV = "PV";
  private static final String YIELD_CURVE = "YIELD_CURVE";
  private static final String VALUERHO = "VALUERHO";
  private static final String CREDIT_SPREAD_CURVE = "CREDIT_SPREAD_CURVE";
  private static final String HAZARD_RATE_CURVE = "HAZARD_RATE_CURVE";
  private static final String SEC_TYPE = ViewStatusResultAggregatorImpl.SECURITY_HEADER;
  private static final String VALUE_NAME = ViewStatusResultAggregatorImpl.VALUE_REQUIREMENT_NAME_HEADER;
  private static final String CURRENCY = ViewStatusResultAggregatorImpl.CURRENCY_HEADER;
  private static final String EMPTY_STR = StringUtils.EMPTY;
  private static final String VALUE_STR = VALUE.name();
  private static final String NO_VALUE_STR = NO_VALUE.name();
  private static final String USD = Currency.USD.getCode();
  private static final String GBP = Currency.GBP.getCode();
  private static final String EUR = Currency.EUR.getCode();
  private static final String POSITION_TARGET = ComputationTargetType.POSITION.getName();
  
  private static final List<List<String>> HEADERS = Lists.newArrayListWithCapacity(2);
  static {
    HEADERS.add(Lists.newArrayList(SEC_TYPE, VALUE_NAME, CURRENCY, EMPTY_STR));
    HEADERS.add(Lists.newArrayList(EMPTY_STR, EMPTY_STR, USD, GBP));
  }

  private static final List<List<Object>> ROWS = Lists.newArrayListWithCapacity(5);
  static {
    ROWS.add(makeRowList(SWAP, PV, VALUE_STR, VALUE_STR));
    ROWS.add(makeRowList(EMPTY_STR, YIELD_CURVE, NO_VALUE_STR, VALUE_STR));
    ROWS.add(makeRowList(EMPTY_STR, VALUERHO, VALUE_STR, VALUE_STR));
    ROWS.add(makeRowList(SWAPTION, CREDIT_SPREAD_CURVE, VALUE_STR, NO_VALUE_STR));
    ROWS.add(makeRowList(EMPTY_STR, HAZARD_RATE_CURVE, NO_VALUE_STR, NO_VALUE_STR));
  }
  
  private static final Map<ViewStatusKey, ViewStatus> RESULT = Maps.newHashMap(); 
  static {
    RESULT.put(new ViewStatusKeyBean(SWAP, PV, USD, POSITION_TARGET), VALUE);
    RESULT.put(new ViewStatusKeyBean(SWAP, PV, GBP, POSITION_TARGET), VALUE);
    RESULT.put(new ViewStatusKeyBean(SWAP, YIELD_CURVE, USD, POSITION_TARGET), NO_VALUE);
    RESULT.put(new ViewStatusKeyBean(SWAP, YIELD_CURVE, GBP, POSITION_TARGET), VALUE);
    RESULT.put(new ViewStatusKeyBean(SWAP, VALUERHO, USD, POSITION_TARGET), VALUE);
    RESULT.put(new ViewStatusKeyBean(SWAP, VALUERHO, GBP, POSITION_TARGET), VALUE);
    RESULT.put(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, USD, POSITION_TARGET), VALUE);
    RESULT.put(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, GBP, POSITION_TARGET), NO_VALUE);
    RESULT.put(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, USD, POSITION_TARGET), NO_VALUE);
    RESULT.put(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, GBP, POSITION_TARGET), NO_VALUE);
  }
  
  private ViewStatusModel _viewStatusModel = new SimpleViewStatusModel(HEADERS, ROWS, RESULT);
  
  public void getColumnNameAt() {
    assertEquals(SEC_TYPE, _viewStatusModel.getColumnNameAt(0, 0));
    assertEquals(VALUE_NAME, _viewStatusModel.getColumnNameAt(0, 1));
    assertEquals(CURRENCY, _viewStatusModel.getColumnNameAt(0, 2));
    assertEquals(EMPTY_STR, _viewStatusModel.getColumnNameAt(0, 3));
    
    assertEquals(EMPTY_STR, _viewStatusModel.getColumnNameAt(1, 0));
    assertEquals(EMPTY_STR, _viewStatusModel.getColumnNameAt(1, 1));
    assertEquals(Currency.USD.getCode(), _viewStatusModel.getColumnNameAt(1, 2));
    assertEquals(Currency.GBP.getCode(), _viewStatusModel.getColumnNameAt(1, 3));
  }
  
  private static List<Object> makeRowList(String... colums) {
    List<Object> row = Lists.newArrayListWithCapacity(4);
    for (Object column : colums) {
      row.add(column);
    }
    return row;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getColumnNameAtWithNegativeRowIndex() {
    _viewStatusModel.getColumnNameAt(-1, 0);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getColumnNameAtWithOutOfRangeRowIndex() {
    _viewStatusModel.getColumnNameAt(2, 0);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getColumnNameAtWithNegativeColumnIndex() {
    _viewStatusModel.getColumnNameAt(0, -1);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getColumnNameAtWithOutOfRangeColumnIndex() {
    _viewStatusModel.getColumnNameAt(0, 4);
  }
  
  public void getHeaderRowCount() {
    assertEquals(2, _viewStatusModel.getHeaderRowCount());
  }
  
  public void getRowValueAt() {
    assertEquals(SWAP, _viewStatusModel.getRowValueAt(0, 0));
    assertEquals(PV, _viewStatusModel.getRowValueAt(0, 1));
    assertEquals(VALUE_STR, _viewStatusModel.getRowValueAt(0, 2));
    assertEquals(VALUE_STR, _viewStatusModel.getRowValueAt(0, 3));
    assertEquals(EMPTY_STR, _viewStatusModel.getRowValueAt(1, 0));
    assertEquals(YIELD_CURVE, _viewStatusModel.getRowValueAt(1, 1));
    assertEquals(NO_VALUE_STR, _viewStatusModel.getRowValueAt(1, 2));
    assertEquals(VALUE_STR, _viewStatusModel.getRowValueAt(1, 3));
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getRowValueAtWithNegativeRowIndex() {
    _viewStatusModel.getRowValueAt(-1, 0);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getRowValueAtWithOutOfRangeRowIndex() {
    _viewStatusModel.getRowValueAt(5, 0);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getRowValueAtWithNegativeColumnIndex() {
    _viewStatusModel.getRowValueAt(0, -1);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getRowValueAtWithOutOfRangeColumnIndex() {
    _viewStatusModel.getRowValueAt(0, 4);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullColumnHeadersNotAllowed() {
    new SimpleViewStatusModel(null, ROWS, RESULT);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullRowsNotAllowed() {
    new SimpleViewStatusModel(HEADERS, null, RESULT);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullResultNotAllowed() {
    new SimpleViewStatusModel(HEADERS, ROWS, null);
  }
  
  public void getStatusWithNullKey() {
    assertNull(_viewStatusModel.getStatus(null));
  }
  
  public void getStatusByKey() {
    assertEquals(VALUE, _viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, PV, USD, POSITION_TARGET)));
    assertEquals(VALUE, _viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, PV, GBP, POSITION_TARGET)));
    assertEquals(NO_VALUE, _viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, YIELD_CURVE, USD, POSITION_TARGET)));
    assertEquals(VALUE, _viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, YIELD_CURVE, GBP, POSITION_TARGET)));
    assertEquals(VALUE, _viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, VALUERHO, USD, POSITION_TARGET)));
    assertEquals(VALUE, _viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, VALUERHO, GBP, POSITION_TARGET)));
    assertEquals(VALUE, _viewStatusModel.getStatus(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, USD, POSITION_TARGET)));
    assertEquals(NO_VALUE, _viewStatusModel.getStatus(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, GBP, POSITION_TARGET)));
    assertEquals(NO_VALUE, _viewStatusModel.getStatus(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, USD, POSITION_TARGET)));
    assertEquals(NO_VALUE, _viewStatusModel.getStatus(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, GBP, POSITION_TARGET)));
  }
  
  public void getMissingStatusByKey() {
    assertNull(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, PV, EUR, POSITION_TARGET)));
  }
  
  public void getValueRequirementNames() {
    assertEquals(Sets.newHashSet(PV, YIELD_CURVE, VALUERHO, CREDIT_SPREAD_CURVE, HAZARD_RATE_CURVE), _viewStatusModel.getValueRequirementNames());
  }
  
  public void getCurrencies() {
    assertEquals(Sets.newHashSet(USD, GBP), _viewStatusModel.getCurrencies());
  }
  
  public void getSecurityTypes() {
    assertEquals(Sets.newHashSet(SWAP, SWAPTION), _viewStatusModel.getSecurityTypes());
  }
  
  public void getComputationTargetTypes() {
    assertEquals(Sets.newHashSet(POSITION_TARGET), _viewStatusModel.getComputationTargetTypes());
  }
  
  public void getRowCount() {
    assertEquals(5, _viewStatusModel.getRowCount());
  }
  
  public void getColumnCount() {
    assertEquals(4, _viewStatusModel.getColumnCount());
  }
  
  public void getKeySet() {
    Set<ViewStatusKey> keySet = _viewStatusModel.keySet();
    assertNotNull(keySet);
    assertEquals(RESULT.size(), keySet.size());
    for (ViewStatusKey viewStatusKey : keySet) {
      if (RESULT.get(viewStatusKey) == null) {
        fail();
      }
    }
  }
}
