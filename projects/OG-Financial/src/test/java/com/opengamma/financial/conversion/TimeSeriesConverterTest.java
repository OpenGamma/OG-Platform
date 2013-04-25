/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TimeSeriesConverterTest {
  
  private final TimeSeriesConverter _converter = new TimeSeriesConverter();
  
  @Test
  public void convertEmpty() {
    Map<String, Double> expected = new HashMap<String, Double>();
    
    Map<String, Double> actual = _converter.convert("Foo", ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    
    assertEquals(expected, actual);
  }
  
  @Test
  public void convertNonEmpty() {
    Map<String, Double> expected = new HashMap<String, Double>();
    expected.put("Foo[2005-04-04]", 5.5);
    expected.put("Foo[2005-04-05]", 6.6);
    
    Map<String, Double> actual = _converter.convert("Foo", 
        ImmutableLocalDateDoubleTimeSeries.of(
            new LocalDate[] { LocalDate.of(2005, 4, 4), LocalDate.of(2005, 4, 5) },
            new double[] { 5.5, 6.6 }));
    
    assertEquals(expected, actual);
  }

  @Test
  public void getConvertedClass() {
    assertEquals(DoubleTimeSeries.class, _converter.getConvertedClass());  
  }

}
