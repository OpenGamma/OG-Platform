/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import org.testng.annotations.Test;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link AggregateType}
 */
@Test(groups = TestGroup.UNIT)
public class AggregateTypeTest {
  
  public void noAggregation() {
    AggregateType noAggregation = AggregateType.NO_AGGREGATION;
    assertNotNull(noAggregation);
    List<ViewColumnType> columnTypes = noAggregation.getColumnTypes();
    assertNotNull(columnTypes);
    assertTrue(columnTypes.isEmpty());
  }
  
  public void typeSecurityValueCurrencyWithUppercase() {
    AggregateType aggregateType = AggregateType.of("TSVC");
    assertNotNull(aggregateType);
    List<ViewColumnType> columnTypes = aggregateType.getColumnTypes();
    assertNotNull(columnTypes);
    assertEquals(4, columnTypes.size());
    assertEquals(ViewColumnType.TARGET_TYPE, columnTypes.get(0));
    assertEquals(ViewColumnType.SECURITY, columnTypes.get(1));
    assertEquals(ViewColumnType.VALUE_REQUIREMENT_NAME, columnTypes.get(2));
    assertEquals(ViewColumnType.CURRENCY, columnTypes.get(3));
  }
  
  public void typeSecurityValueCurrencyWithLowercase() {
    AggregateType aggregateType = AggregateType.of("tsvc");
    assertNotNull(aggregateType);
    List<ViewColumnType> columnTypes = aggregateType.getColumnTypes();
    assertNotNull(columnTypes);
    assertEquals(4, columnTypes.size());
    assertEquals(ViewColumnType.TARGET_TYPE, columnTypes.get(0));
    assertEquals(ViewColumnType.SECURITY, columnTypes.get(1));
    assertEquals(ViewColumnType.VALUE_REQUIREMENT_NAME, columnTypes.get(2));
    assertEquals(ViewColumnType.CURRENCY, columnTypes.get(3));
  }
  
  public void typeSecurityCurrencyValueWithUppercase() {
    AggregateType aggregateType = AggregateType.of("TSCV");
    assertNotNull(aggregateType);
    List<ViewColumnType> columnTypes = aggregateType.getColumnTypes();
    assertNotNull(columnTypes);
    assertEquals(4, columnTypes.size());
    assertEquals(ViewColumnType.TARGET_TYPE, columnTypes.get(0));
    assertEquals(ViewColumnType.SECURITY, columnTypes.get(1));
    assertEquals(ViewColumnType.CURRENCY, columnTypes.get(2));
    assertEquals(ViewColumnType.VALUE_REQUIREMENT_NAME, columnTypes.get(3));
  }
  
  public void typeSecurityCurrencyValueWithLowercase() {
    AggregateType aggregateType = AggregateType.of("tscv");
    assertNotNull(aggregateType);
    List<ViewColumnType> columnTypes = aggregateType.getColumnTypes();
    assertNotNull(columnTypes);
    assertEquals(4, columnTypes.size());
    assertEquals(ViewColumnType.TARGET_TYPE, columnTypes.get(0));
    assertEquals(ViewColumnType.SECURITY, columnTypes.get(1));
    assertEquals(ViewColumnType.CURRENCY, columnTypes.get(2));
    assertEquals(ViewColumnType.VALUE_REQUIREMENT_NAME, columnTypes.get(3));
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void duplicateChar() {
    AggregateType.of("TTCV");
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void lessThan4CharsType() {
    AggregateType.of("TCV");
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void moreThan4CharsType() {
    AggregateType.of("TSCVT");
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void invalidChars() {
    AggregateType.of("TSCA");
  }
  
  public void testToString() {
    AggregateType aggregateType = AggregateType.of("TSVC");
    assertEquals("AggregateType [TSVC]", aggregateType.toString());
  }
  
}
