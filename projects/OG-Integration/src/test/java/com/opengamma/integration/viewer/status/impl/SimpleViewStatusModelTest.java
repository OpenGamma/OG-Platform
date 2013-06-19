/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.integration.viewer.status.ViewStatusKey;
import com.opengamma.integration.viewer.status.ViewStatusModel;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link SimpleViewStatusModel}
 */
@Test(groups = TestGroup.UNIT)
public class SimpleViewStatusModelTest {
  
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
  private static final String TRUE_STR = String.valueOf(true);
  private static final String FALSE_STR = String.valueOf(false);
  private static final String USD = Currency.USD.getCode();
  private static final String GBP = Currency.GBP.getCode();
  private static final String EUR = Currency.EUR.getCode();
  private static final String POSITION_TARGET = ComputationTargetType.POSITION.getName();
  
  private static final String[][] HEADERS = { {SEC_TYPE, VALUE_NAME, CURRENCY, EMPTY_STR},
      {EMPTY_STR, EMPTY_STR, Currency.USD.getCode(), Currency.GBP.getCode()} };

  private static final Object[][] ROWS = { {SWAP, PV, TRUE_STR, TRUE_STR},
      {EMPTY_STR, YIELD_CURVE, FALSE_STR, TRUE_STR},
      {EMPTY_STR, VALUERHO, TRUE_STR, TRUE_STR},
      {SWAPTION, CREDIT_SPREAD_CURVE, TRUE_STR, FALSE_STR},
      {EMPTY_STR, HAZARD_RATE_CURVE, FALSE_STR, FALSE_STR} };
  
  private static final Map<ViewStatusKey, Boolean> RESULT = Maps.newHashMap(); 
  static {
    RESULT.put(new ViewStatusKeyBean(SWAP, PV, USD, POSITION_TARGET), true);
    RESULT.put(new ViewStatusKeyBean(SWAP, PV, GBP, POSITION_TARGET), true);
    RESULT.put(new ViewStatusKeyBean(SWAP, YIELD_CURVE, USD, POSITION_TARGET), false);
    RESULT.put(new ViewStatusKeyBean(SWAP, YIELD_CURVE, GBP, POSITION_TARGET), true);
    RESULT.put(new ViewStatusKeyBean(SWAP, VALUERHO, USD, POSITION_TARGET), true);
    RESULT.put(new ViewStatusKeyBean(SWAP, VALUERHO, GBP, POSITION_TARGET), true);
    RESULT.put(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, USD, POSITION_TARGET), true);
    RESULT.put(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, GBP, POSITION_TARGET), false);
    RESULT.put(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, USD, POSITION_TARGET), false);
    RESULT.put(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, GBP, POSITION_TARGET), false);
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
    assertEquals(TRUE_STR, _viewStatusModel.getRowValueAt(0, 2));
    assertEquals(TRUE_STR, _viewStatusModel.getRowValueAt(0, 3));
    assertEquals(EMPTY_STR, _viewStatusModel.getRowValueAt(1, 0));
    assertEquals(YIELD_CURVE, _viewStatusModel.getRowValueAt(1, 1));
    assertEquals(FALSE_STR, _viewStatusModel.getRowValueAt(1, 2));
    assertEquals(TRUE_STR, _viewStatusModel.getRowValueAt(1, 3));
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
    assertTrue(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, PV, USD, POSITION_TARGET)));
    assertTrue(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, PV, GBP, POSITION_TARGET)));
    assertFalse(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, YIELD_CURVE, USD, POSITION_TARGET)));
    assertTrue(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, YIELD_CURVE, GBP, POSITION_TARGET)));
    assertTrue(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, VALUERHO, USD, POSITION_TARGET)));
    assertTrue(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAP, VALUERHO, GBP, POSITION_TARGET)));
    assertTrue(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, USD, POSITION_TARGET)));
    assertFalse(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, GBP, POSITION_TARGET)));
    assertFalse(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, USD, POSITION_TARGET)));
    assertFalse(_viewStatusModel.getStatus(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, GBP, POSITION_TARGET)));
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
  
  public void getRowCount() {
    assertEquals(5, _viewStatusModel.getRowCount());
  }
  
  public void getColumnCount() {
    assertEquals(4, _viewStatusModel.getColumnCount());
  }
}
