/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.integration.viewer.status.ViewAggregationType;
import com.opengamma.integration.viewer.status.ViewStatusModel;
import com.opengamma.integration.viewer.status.ViewStatusResultAggregator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link ViewStatusResultAggregatorImpl}
 */
@Test(groups = TestGroup.UNIT, enabled=false)
public class ViewStatusResultAggregatorImplTest {
  
  private static final String SWAP = "SWAP";
  private static final String SWAPTION = "SWAPTION";
  private static final String PV = "PV";
  private static final String YIELD_CURVE = "YIELD_CURVE";
  private static final String VALUERHO = "VALUERHO";
  private static final String CREDIT_SPREAD_CURVE = "CREDIT_SPREAD_CURVE";
  private static final String HAZARD_RATE_CURVE = "HAZARD_RATE_CURVE";

  private static final String EMPTY_STR = StringUtils.EMPTY;
  private static final String TRUE_STR = String.valueOf(true);
  private static final String FALSE_STR = String.valueOf(false);
  private static final String USD = Currency.USD.getCode();
  private static final String GBP = Currency.GBP.getCode();
  private static final String EUR = Currency.EUR.getCode();
  private static final String POSITION_TARGET = ComputationTargetType.POSITION.getName();
  
  private ViewStatusResultAggregator _aggregator;
  
  @BeforeMethod
  public void setUp() {
    _aggregator = new ViewStatusResultAggregatorImpl();
    
    _aggregator.put(new ViewStatusKeyBean(SWAP, PV, USD, POSITION_TARGET), true);
    _aggregator.put(new ViewStatusKeyBean(SWAP, PV, GBP, POSITION_TARGET), true);
    _aggregator.put(new ViewStatusKeyBean(SWAP, PV, EUR, POSITION_TARGET), true);
    _aggregator.put(new ViewStatusKeyBean(SWAP, YIELD_CURVE, USD, POSITION_TARGET), false);
    _aggregator.put(new ViewStatusKeyBean(SWAP, YIELD_CURVE, GBP, POSITION_TARGET), true);
    _aggregator.put(new ViewStatusKeyBean(SWAP, VALUERHO, USD, POSITION_TARGET), true);
    _aggregator.put(new ViewStatusKeyBean(SWAP, VALUERHO, GBP, POSITION_TARGET), true);
    _aggregator.put(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, USD, POSITION_TARGET), true);
    _aggregator.put(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, GBP, POSITION_TARGET), false);
    _aggregator.put(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, USD, POSITION_TARGET), false);
    _aggregator.put(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, GBP, POSITION_TARGET), false);
  }
  
  public void aggregateByCurrencySecurityValueName() {
    ViewStatusModel statusModel = _aggregator.aggregate(ViewAggregationType.CURRENCY, ViewAggregationType.SECURITY, ViewAggregationType.VALUE_REQUIREMENT_NAME);
    assertNotNull(statusModel);
    
    //check header
    assertEquals(2, statusModel.getHeaderRowCount());
    assertEquals(5, statusModel.getColumnCount());
    
    assertEquals(ViewStatusResultAggregatorImpl.SECURITY_HEADER, statusModel.getColumnNameAt(0, 0));
    assertEquals(ViewStatusResultAggregatorImpl.VALUE_REQUIREMENT_NAME_HEADER, statusModel.getColumnNameAt(0, 1));
    assertEquals(ViewStatusResultAggregatorImpl.CURRENCY_HEADER, statusModel.getColumnNameAt(0, 2));
    assertEmptyString(statusModel.getColumnNameAt(0, 3));
    assertEmptyString(statusModel.getColumnNameAt(0, 4));
    
    assertEmptyString(statusModel.getColumnNameAt(1, 0));
    assertEmptyString(statusModel.getColumnNameAt(1, 1));
    assertEquals(Currency.EUR.getCode(), statusModel.getColumnNameAt(1, 2));
    assertEquals(Currency.GBP.getCode(), statusModel.getColumnNameAt(1, 3));
    assertEquals(Currency.USD.getCode(), statusModel.getColumnNameAt(1, 4));
    
    //check rows
    assertEquals(5, statusModel.getRowCount());
    
    //check row1
    assertEquals(SWAP, statusModel.getRowValueAt(0, 0));
    assertEquals(PV, statusModel.getRowValueAt(0, 1));
    assertEquals(TRUE_STR, String.valueOf(statusModel.getRowValueAt(0, 2)));
    assertEquals(TRUE_STR, String.valueOf(statusModel.getRowValueAt(0, 3)));
    assertEquals(TRUE_STR, String.valueOf(statusModel.getRowValueAt(0, 4)));
    
    //check row2
    assertEmptyString(statusModel.getRowValueAt(1, 0));
    assertEquals(VALUERHO, statusModel.getRowValueAt(1, 1));
    assertEmptyString(statusModel.getRowValueAt(1, 2));
    assertEquals(TRUE_STR, String.valueOf(statusModel.getRowValueAt(1, 3)));
    assertEquals(TRUE_STR, String.valueOf(statusModel.getRowValueAt(1, 4)));
    
    //check row3
    assertEmptyString(statusModel.getRowValueAt(2, 0));
    assertEquals(YIELD_CURVE, statusModel.getRowValueAt(2, 1));
    assertEmptyString(statusModel.getRowValueAt(2, 2));
    assertEquals(TRUE_STR, String.valueOf(statusModel.getRowValueAt(2, 3)));
    assertEquals(FALSE_STR, String.valueOf(statusModel.getRowValueAt(2, 4)));
    
    //check row4
    assertEquals(SWAPTION, statusModel.getRowValueAt(3, 0));
    assertEquals(CREDIT_SPREAD_CURVE, statusModel.getRowValueAt(3, 1));
    assertEmptyString(statusModel.getRowValueAt(3, 2));
    assertEquals(FALSE_STR, String.valueOf(statusModel.getRowValueAt(3, 3)));
    assertEquals(TRUE_STR, String.valueOf(statusModel.getRowValueAt(3, 4)));
    
    //check row5
    assertEmptyString(statusModel.getRowValueAt(4, 0));
    assertEquals(HAZARD_RATE_CURVE, statusModel.getRowValueAt(4, 1));
    assertEmptyString(statusModel.getRowValueAt(4, 2));
    assertEquals(FALSE_STR, String.valueOf(statusModel.getRowValueAt(4, 3)));
    assertEquals(FALSE_STR, String.valueOf(statusModel.getRowValueAt(4, 4)));
    
  }

  private void assertEmptyString(final Object content) {
    assertEquals(EMPTY_STR, String.valueOf(content));
  }
  
 

}
