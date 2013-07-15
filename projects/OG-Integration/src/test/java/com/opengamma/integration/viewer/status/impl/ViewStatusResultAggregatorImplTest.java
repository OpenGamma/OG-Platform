/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import static com.opengamma.integration.viewer.status.impl.ViewStatusResultAggregatorImpl.CURRENCY_HEADER;
import static com.opengamma.integration.viewer.status.impl.ViewStatusResultAggregatorImpl.SECURITY_HEADER;
import static com.opengamma.integration.viewer.status.impl.ViewStatusResultAggregatorImpl.STATUS;
import static com.opengamma.integration.viewer.status.impl.ViewStatusResultAggregatorImpl.TARGET_TYPE_HEADER;
import static com.opengamma.integration.viewer.status.impl.ViewStatusResultAggregatorImpl.VALUE_REQUIREMENT_NAME_HEADER;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.integration.viewer.status.AggregateType;
import com.opengamma.integration.viewer.status.ViewStatus;
import com.opengamma.integration.viewer.status.ViewStatusModel;
import com.opengamma.integration.viewer.status.ViewStatusResultAggregator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link ViewStatusResultAggregatorImpl}
 */
@Test(groups = TestGroup.UNIT)
public class ViewStatusResultAggregatorImplTest {
  
  
  private static final ViewStatus NO_VALUE = ViewStatus.NO_VALUE;
  private static final ViewStatus VALUE = ViewStatus.VALUE;
  private static final String SWAP = "SWAP";
  private static final String SWAPTION = "SWAPTION";
  private static final String PV = "PV";
  private static final String YIELD_CURVE = "YIELD_CURVE";
  private static final String VALUERHO = "VALUERHO";
  private static final String CREDIT_SPREAD_CURVE = "CREDIT_SPREAD_CURVE";
  private static final String HAZARD_RATE_CURVE = "HAZARD_RATE_CURVE";
  private static final String QUANTITY = "QUANTITY";
  private static final String FAIR_VALUE = "FAIR_VALUE";
  private static final String PRESENT_VALUE = "PRESENT_VALUE";
  
  
  private static final String MIXED_SEC = "Mixed_SEC";
  private static final String MIXED_CUR = "Mixed_CUR";

  private static final String EMPTY_STR = StringUtils.EMPTY;
  private static final String USD = Currency.USD.getCode();
  private static final String GBP = Currency.GBP.getCode();
  private static final String EUR = Currency.EUR.getCode();
  private static final String POSITION_TARGET = ComputationTargetType.POSITION.getName();
  private static final String NODE_TARGET = ComputationTargetType.PORTFOLIO_NODE.getName();
  
  private ViewStatusResultAggregator _aggregator;
  
  @BeforeMethod
  public void setUp() {
    _aggregator = new ViewStatusResultAggregatorImpl();
    _aggregator.putStatus(new ViewStatusKeyBean(SWAP, PV, USD, POSITION_TARGET), VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(SWAP, PV, GBP, POSITION_TARGET), VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(SWAP, PV, EUR, POSITION_TARGET), VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(SWAP, YIELD_CURVE, USD, POSITION_TARGET), NO_VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(SWAP, YIELD_CURVE, GBP, POSITION_TARGET), VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(SWAP, VALUERHO, USD, POSITION_TARGET), VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(SWAP, VALUERHO, GBP, POSITION_TARGET), VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, USD, POSITION_TARGET), VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(SWAPTION, CREDIT_SPREAD_CURVE, GBP, POSITION_TARGET), NO_VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, USD, POSITION_TARGET), NO_VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(SWAPTION, HAZARD_RATE_CURVE, GBP, POSITION_TARGET), NO_VALUE);
    
    _aggregator.putStatus(new ViewStatusKeyBean(MIXED_SEC, QUANTITY, MIXED_CUR, NODE_TARGET), NO_VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(MIXED_SEC, FAIR_VALUE, MIXED_CUR, NODE_TARGET), NO_VALUE);
    _aggregator.putStatus(new ViewStatusKeyBean(MIXED_SEC, PRESENT_VALUE, MIXED_CUR, NODE_TARGET), NO_VALUE);
     
  }
  
  public void aggregateByTargetTypeSecurityValueNameCurrency() {
    ViewStatusModel statusModel = _aggregator.aggregate(AggregateType.of("TSVC"));
    assertNotNull(statusModel);
    
    //check header
    assertEquals(2, statusModel.getHeaderRowCount());
    assertEquals(7, statusModel.getColumnCount());
    assertEquals(TARGET_TYPE_HEADER, statusModel.getColumnNameAt(0, 0));
    assertEquals(SECURITY_HEADER, statusModel.getColumnNameAt(0, 1));
    assertEquals(VALUE_REQUIREMENT_NAME_HEADER, statusModel.getColumnNameAt(0, 2));
    assertEquals(CURRENCY_HEADER, statusModel.getColumnNameAt(0, 3));
    assertEmptyString(statusModel.getColumnNameAt(0, 4));
    assertEmptyString(statusModel.getColumnNameAt(0, 5));
    assertEmptyString(statusModel.getColumnNameAt(0, 6));
    
    assertEmptyString(statusModel.getColumnNameAt(1, 0));
    assertEmptyString(statusModel.getColumnNameAt(1, 1));
    assertEmptyString(statusModel.getColumnNameAt(1, 2));
    assertEquals(EUR, statusModel.getColumnNameAt(1, 3));
    assertEquals(GBP, statusModel.getColumnNameAt(1, 4));
    assertEquals(MIXED_CUR, statusModel.getColumnNameAt(1, 5));
    assertEquals(USD, statusModel.getColumnNameAt(1, 6));
    
    //check rows
    assertEquals(8, statusModel.getRowCount());
    
    //check row1
    assertEquals(NODE_TARGET, statusModel.getRowValueAt(0, 0));
    assertEquals(MIXED_SEC, statusModel.getRowValueAt(0, 1));
    assertEquals(FAIR_VALUE, String.valueOf(statusModel.getRowValueAt(0, 2)));
    assertEmptyString(statusModel.getRowValueAt(0, 3));
    assertEmptyString(statusModel.getRowValueAt(0, 4));
    assertEquals(NO_VALUE, statusModel.getRowValueAt(0, 5));
    assertEmptyString(statusModel.getRowValueAt(0, 6));
    
    //check row2
    assertEmptyString(statusModel.getRowValueAt(1, 0));
    assertEmptyString(statusModel.getRowValueAt(1, 1));
    assertEquals(PRESENT_VALUE, statusModel.getRowValueAt(1, 2));
    assertEmptyString(statusModel.getRowValueAt(1, 3));
    assertEmptyString(statusModel.getRowValueAt(1, 4));
    assertEquals(NO_VALUE, statusModel.getRowValueAt(1, 5));
    assertEmptyString(statusModel.getRowValueAt(1, 6));
    
    //check row3
    assertEmptyString(statusModel.getRowValueAt(2, 0));
    assertEmptyString(statusModel.getRowValueAt(2, 1));
    assertEquals(QUANTITY, statusModel.getRowValueAt(2, 2));
    assertEmptyString(statusModel.getRowValueAt(2, 3));
    assertEmptyString(statusModel.getRowValueAt(2, 4));
    assertEquals(NO_VALUE, statusModel.getRowValueAt(2, 5));
    assertEmptyString(statusModel.getRowValueAt(2, 6));
  
    //check row4
    assertEquals(POSITION_TARGET, statusModel.getRowValueAt(3, 0));
    assertEquals(SWAP, statusModel.getRowValueAt(3, 1));
    assertEquals(PV, statusModel.getRowValueAt(3, 2));
    assertEquals(VALUE, statusModel.getRowValueAt(3, 3));
    assertEquals(VALUE, statusModel.getRowValueAt(3, 4));
    assertEmptyString(statusModel.getRowValueAt(3, 5));
    assertEquals(VALUE, statusModel.getRowValueAt(3, 6));
   
    //check row5
    assertEmptyString(statusModel.getRowValueAt(4, 0));
    assertEmptyString(statusModel.getRowValueAt(4, 1));
    assertEquals(VALUERHO, statusModel.getRowValueAt(4, 2));
    assertEmptyString(statusModel.getRowValueAt(4, 3));
    assertEquals(VALUE, statusModel.getRowValueAt(4, 4));
    assertEmptyString(statusModel.getRowValueAt(4, 5));
    assertEquals(VALUE, statusModel.getRowValueAt(4, 6));
    
    //check row6
    assertEmptyString(statusModel.getRowValueAt(5, 0));
    assertEmptyString(statusModel.getRowValueAt(5, 1));
    assertEquals(YIELD_CURVE, statusModel.getRowValueAt(5, 2));
    assertEmptyString(statusModel.getRowValueAt(5, 3));
    assertEquals(VALUE, statusModel.getRowValueAt(5, 4));
    assertEmptyString(statusModel.getRowValueAt(5, 5));
    assertEquals(NO_VALUE, statusModel.getRowValueAt(5, 6));
    
    //check row7
    assertEmptyString(statusModel.getRowValueAt(6, 0));
    assertEquals(SWAPTION, statusModel.getRowValueAt(6, 1));
    assertEquals(CREDIT_SPREAD_CURVE, statusModel.getRowValueAt(6, 2));
    assertEmptyString(statusModel.getRowValueAt(6, 3));
    assertEquals(NO_VALUE, statusModel.getRowValueAt(6, 4));
    assertEmptyString(statusModel.getRowValueAt(6, 5));
    assertEquals(VALUE, statusModel.getRowValueAt(6, 6));
    
    //check row8
    assertEmptyString(statusModel.getRowValueAt(7, 0));
    assertEmptyString(statusModel.getRowValueAt(7, 1));
    assertEquals(HAZARD_RATE_CURVE, statusModel.getRowValueAt(7, 2));
    assertEmptyString(statusModel.getRowValueAt(7, 3));
    assertEquals(NO_VALUE, statusModel.getRowValueAt(7, 4));
    assertEmptyString(statusModel.getRowValueAt(7, 5));
    assertEquals(NO_VALUE, statusModel.getRowValueAt(7, 6));
    
  }
  
  public void aggregateBySecurityCurrencyValueNameTargetType() {
    ViewStatusModel statusModel = _aggregator.aggregate(AggregateType.of("SCVT"));
    assertNotNull(statusModel);
    
    //check header
    assertEquals(2, statusModel.getHeaderRowCount());
    assertEquals(5, statusModel.getColumnCount());
    assertEquals(SECURITY_HEADER, statusModel.getColumnNameAt(0, 0));
    assertEquals(CURRENCY_HEADER, statusModel.getColumnNameAt(0, 1));
    assertEquals(VALUE_REQUIREMENT_NAME_HEADER, statusModel.getColumnNameAt(0, 2));
    assertEquals(TARGET_TYPE_HEADER, statusModel.getColumnNameAt(0, 3));
    assertEmptyString(statusModel.getColumnNameAt(0, 4));
    
    assertEmptyString(statusModel.getColumnNameAt(1, 0));
    assertEmptyString(statusModel.getColumnNameAt(1, 1));
    assertEmptyString(statusModel.getColumnNameAt(1, 2));
    assertEquals(NODE_TARGET, statusModel.getColumnNameAt(1, 3));
    assertEquals(POSITION_TARGET, statusModel.getColumnNameAt(1, 4));
    
    //check rows
    assertEquals(14, statusModel.getRowCount());
    
    //check row1
    assertEmptyString(statusModel.getRowValueAt(6, 0));
    assertEmptyString(statusModel.getRowValueAt(6, 1));
    assertEquals(YIELD_CURVE, String.valueOf(statusModel.getRowValueAt(6, 2)));
    assertEmptyString(statusModel.getRowValueAt(6, 3));
    assertEquals(VALUE, statusModel.getRowValueAt(6, 4));
    
    //check row7
    assertEquals(MIXED_SEC, statusModel.getRowValueAt(0, 0));
    assertEquals(MIXED_CUR, statusModel.getRowValueAt(0, 1));
    assertEquals(FAIR_VALUE, String.valueOf(statusModel.getRowValueAt(0, 2)));
    assertEquals(NO_VALUE, statusModel.getRowValueAt(0, 3));
    assertEmptyString(statusModel.getRowValueAt(0, 4));
    
    //check row14
    assertEmptyString(statusModel.getRowValueAt(13, 0));
    assertEmptyString(statusModel.getRowValueAt(13, 1));
    assertEquals(HAZARD_RATE_CURVE, statusModel.getRowValueAt(13, 2));
    assertEmptyString(statusModel.getRowValueAt(13, 3));
    assertEquals(NO_VALUE, statusModel.getRowValueAt(13, 4));
      
  }
  
  public void targetSecurityValueCurrencyHeaders() {
    ViewStatusModel model = _aggregator.aggregate(AggregateType.of("TSVC"));
    assertNotNull(model);
    
    assertEquals(2, model.getHeaderRowCount());
    assertEquals(7, model.getColumnCount());
    
    //check top header
    assertEquals(ViewStatusResultAggregatorImpl.TARGET_TYPE_HEADER, model.getColumnNameAt(0, 0));
    assertEquals(ViewStatusResultAggregatorImpl.SECURITY_HEADER, model.getColumnNameAt(0, 1));
    assertEquals(ViewStatusResultAggregatorImpl.VALUE_REQUIREMENT_NAME_HEADER, model.getColumnNameAt(0, 2));
    assertEquals(ViewStatusResultAggregatorImpl.CURRENCY_HEADER, model.getColumnNameAt(0, 3));
    assertEmptyString(model.getColumnNameAt(0, 4));
    assertEmptyString(model.getColumnNameAt(0, 5));
    assertEmptyString(model.getColumnNameAt(0, 6));
    
    //check sub header
    assertEmptyString(model.getColumnNameAt(1, 0));
    assertEmptyString(model.getColumnNameAt(1, 1));
    assertEmptyString(model.getColumnNameAt(1, 2));
    assertEquals(EUR, model.getColumnNameAt(1, 3));
    assertEquals(GBP, model.getColumnNameAt(1, 4));
    assertEquals(MIXED_CUR, model.getColumnNameAt(1, 5));
    assertEquals(USD, model.getColumnNameAt(1, 6));
  }
  
  public void targetSecurityCurrencyValueHeaders() {
    ViewStatusModel model = _aggregator.aggregate(AggregateType.of("TSCV"));
    assertNotNull(model);
    
    assertEquals(2, model.getHeaderRowCount());
    assertEquals(11, model.getColumnCount());
    
    //check top header
    assertEquals(ViewStatusResultAggregatorImpl.TARGET_TYPE_HEADER, model.getColumnNameAt(0, 0));
    assertEquals(ViewStatusResultAggregatorImpl.SECURITY_HEADER, model.getColumnNameAt(0, 1));
    assertEquals(ViewStatusResultAggregatorImpl.CURRENCY_HEADER, model.getColumnNameAt(0, 2));
    assertEquals(ViewStatusResultAggregatorImpl.VALUE_REQUIREMENT_NAME_HEADER, model.getColumnNameAt(0, 3));
    assertEmptyString(model.getColumnNameAt(0, 4));
    assertEmptyString(model.getColumnNameAt(0, 5));
    assertEmptyString(model.getColumnNameAt(0, 6));
    assertEmptyString(model.getColumnNameAt(0, 7));
    assertEmptyString(model.getColumnNameAt(0, 8));
    assertEmptyString(model.getColumnNameAt(0, 9));
    assertEmptyString(model.getColumnNameAt(0, 10));
    
    //check sub header
    assertEmptyString(model.getColumnNameAt(1, 0));
    assertEmptyString(model.getColumnNameAt(1, 1));
    assertEmptyString(model.getColumnNameAt(1, 2));
    assertEquals(CREDIT_SPREAD_CURVE, model.getColumnNameAt(1, 3));
    assertEquals(FAIR_VALUE, model.getColumnNameAt(1, 4));
    assertEquals(HAZARD_RATE_CURVE, model.getColumnNameAt(1, 5));
    assertEquals(PRESENT_VALUE, model.getColumnNameAt(1, 6));
    assertEquals(PV, model.getColumnNameAt(1, 7));
    assertEquals(QUANTITY, model.getColumnNameAt(1, 8));
    assertEquals(VALUERHO, model.getColumnNameAt(1, 9));
    assertEquals(YIELD_CURVE, model.getColumnNameAt(1, 10));
  }
  
  public void defaultHeaders() {
    ViewStatusModel model = _aggregator.aggregate(AggregateType.NO_AGGREGATION);
    
    assertNotNull(model);
    assertEquals(1, model.getHeaderRowCount());
    assertEquals(5, model.getColumnCount());
    
    assertEquals(TARGET_TYPE_HEADER, model.getColumnNameAt(0, 0));
    assertEquals(SECURITY_HEADER, model.getColumnNameAt(0, 1));
    assertEquals(VALUE_REQUIREMENT_NAME_HEADER, model.getColumnNameAt(0, 2));
    assertEquals(CURRENCY_HEADER, model.getColumnNameAt(0, 3));
    assertEquals(STATUS, model.getColumnNameAt(0, 4));    
  }

  private void assertEmptyString(final Object content) {
    assertEquals(EMPTY_STR, String.valueOf(content));
  }
  
 

}
