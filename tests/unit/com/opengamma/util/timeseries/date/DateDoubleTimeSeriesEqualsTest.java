/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date;


import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 *
 * @author yomi
 */
@Ignore
public class DateDoubleTimeSeriesEqualsTest {
  
  private ArrayDateDoubleTimeSeries _arrayTS;
  private ListDateDoubleTimeSeries _listTS;
  private MapDateDoubleTimeSeries _mapTS;

  private double[] _values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    _arrayTS = new ArrayDateDoubleTimeSeries(testDates(), _values);
    _listTS = new ListDateDoubleTimeSeries(testDates(), _values);
    _mapTS = new MapDateDoubleTimeSeries(testDates(), _values);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }
  
  @Test
  public void equalsDateDoubleTimeSeries() throws Exception {
    assertEquals(_arrayTS, _arrayTS);
    assertEquals(_arrayTS, _listTS);
    assertEquals(_arrayTS, _mapTS);
    assertEquals(_listTS, _listTS);
    assertEquals(_listTS, _arrayTS);
    assertEquals(_listTS, _mapTS);
    assertEquals(_mapTS, _arrayTS);
    assertEquals(_mapTS, _listTS);
    assertEquals(_mapTS, _mapTS);
  }
  
  public Date[] testDates() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2010, 1, 8); 
    Date one = cal.getTime();
    cal.set(2010, 1, 9);
    Date two = cal.getTime();
    cal.set(2010, 1, 10);
    Date three = cal.getTime();
    cal.set(2010, 1, 11);
    Date four = cal.getTime();
    cal.set(2010, 1, 12);
    Date five = cal.getTime();
    cal.set(2010, 1, 13);
    Date six = cal.getTime();
    return new Date[] { one, two, three, four, five, six };
  }
  

}
