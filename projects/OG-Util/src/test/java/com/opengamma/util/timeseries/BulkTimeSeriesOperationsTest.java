/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

@Test(groups = TestGroup.UNIT)
public class BulkTimeSeriesOperationsTest {

  private Logger s_logger = LoggerFactory.getLogger(BulkTimeSeriesOperationsTest.class);

  private static final LocalDate[] DATES1 = new LocalDate[] { 
    LocalDate.of(2010, Month.FEBRUARY, 18),
    LocalDate.of(2010, Month.FEBRUARY, 19),
    LocalDate.of(2010, Month.FEBRUARY, 20),
    LocalDate.of(2010, Month.FEBRUARY, 21),
    LocalDate.of(2010, Month.FEBRUARY, 25),
    LocalDate.of(2010, Month.FEBRUARY, 26),
    LocalDate.of(2010, Month.FEBRUARY, 27),
    LocalDate.of(2010, Month.MARCH, 1),
    LocalDate.of(2010, Month.MARCH, 2),
    LocalDate.of(2010, Month.MARCH, 5),
    LocalDate.of(2010, Month.MARCH, 6),
    LocalDate.of(2010, Month.MARCH, 7)
  };

  private static final double[] VALUES1 = { 18, 19, 20, 21, 25, 26, 27, 1, 2, 5, 6, 7 };

  private static final LocalDate[] DATES2 = new LocalDate[] { 
    LocalDate.of(2010, Month.FEBRUARY, 19),
    LocalDate.of(2010, Month.FEBRUARY, 20),
    LocalDate.of(2010, Month.FEBRUARY, 21),
    LocalDate.of(2010, Month.FEBRUARY, 22),
    LocalDate.of(2010, Month.FEBRUARY, 24),
    LocalDate.of(2010, Month.FEBRUARY, 25),
    LocalDate.of(2010, Month.FEBRUARY, 26),
    LocalDate.of(2010, Month.MARCH, 1),
    LocalDate.of(2010, Month.MARCH, 2),
    LocalDate.of(2010, Month.MARCH, 4),
    LocalDate.of(2010, Month.MARCH, 5),
    LocalDate.of(2010, Month.MARCH, 6),
    LocalDate.of(2010, Month.MARCH, 7)
  };

  private static final double[] VALUES2 = { 19 * 2, 20 * 2, 21 * 2, 22 * 2, 24 * 2, 25 * 2, 26 * 2, 1 * 2, 2 * 2, 4 * 2, 5 * 2, 6 * 2, 7 * 2 }; 

  private static final LocalDate[] DATES3 = new LocalDate[] { 
    LocalDate.of(2010, Month.FEBRUARY, 16),
    LocalDate.of(2010, Month.FEBRUARY, 18),
    LocalDate.of(2010, Month.FEBRUARY, 20),
    LocalDate.of(2010, Month.FEBRUARY, 21),
    LocalDate.of(2010, Month.FEBRUARY, 25),
    LocalDate.of(2010, Month.FEBRUARY, 26),
    LocalDate.of(2010, Month.FEBRUARY, 27),
    LocalDate.of(2010, Month.MARCH, 1),
    LocalDate.of(2010, Month.MARCH, 2),
    LocalDate.of(2010, Month.MARCH, 3),
    LocalDate.of(2010, Month.MARCH, 4),
    LocalDate.of(2010, Month.MARCH, 6),
    LocalDate.of(2010, Month.MARCH, 8)
  };

  private static final double[] VALUES3 = { 16 * 3, 18 * 3, 20 * 3, 21 * 3, 25 * 3, 26 * 3, 27 * 3, 1 * 3, 2 * 3, 3 * 3, 4 * 3, 6 * 3, 8 * 3 };

  private static final LocalDate[] DATES4 = new LocalDate[] { 
    LocalDate.of(2010, Month.FEBRUARY, 11),
    LocalDate.of(2010, Month.FEBRUARY, 20),
    LocalDate.of(2010, Month.FEBRUARY, 21),
    LocalDate.of(2010, Month.FEBRUARY, 25),
    LocalDate.of(2010, Month.FEBRUARY, 26),
    LocalDate.of(2010, Month.FEBRUARY, 27),
    LocalDate.of(2010, Month.MARCH, 2),
    LocalDate.of(2010, Month.MARCH, 5),
    LocalDate.of(2010, Month.MARCH, 6),
    LocalDate.of(2010, Month.MARCH, 7)
  };

  private static final double[] VALUES4 = { 11 * 4, 20 * 4, 21 * 4, 25 * 4, 26 * 4, 27 * 4, 2 * 4, 5 * 4, 6 * 4, 7 * 4 };

  private static final LocalDate[] RESULT = new LocalDate[] {
    LocalDate.of(2010, Month.FEBRUARY, 20),
    LocalDate.of(2010, Month.FEBRUARY, 21),
    LocalDate.of(2010, Month.FEBRUARY, 25),
    LocalDate.of(2010, Month.FEBRUARY, 26),
    LocalDate.of(2010, Month.MARCH, 2),
    LocalDate.of(2010, Month.MARCH, 6),   
  };

  public void testBulkIntersection() {
    LocalDateDoubleTimeSeries one = new ArrayLocalDateDoubleTimeSeries(DATES1, VALUES1);
    LocalDateDoubleTimeSeries two = new ArrayLocalDateDoubleTimeSeries(DATES2, VALUES2);
    LocalDateDoubleTimeSeries three = new ArrayLocalDateDoubleTimeSeries(DATES3, VALUES3);
    LocalDateDoubleTimeSeries four = new ArrayLocalDateDoubleTimeSeries(DATES4, VALUES4);

    LocalDateDoubleTimeSeries[] inputs = new LocalDateDoubleTimeSeries[] {one, two, three, four };
    DoubleTimeSeries<LocalDate>[] intersection = BulkTimeSeriesOperations.intersection(inputs);
    for (int i = 0; i < intersection.length; i++) {
      LocalDate[] timesArray = intersection[i].timesArray();
      assertEquals(Arrays.asList(RESULT), Arrays.asList(timesArray));
      Double[] valuesArray = intersection[i].valuesArray();
      s_logger.info(Arrays.toString(timesArray));
      s_logger.info(Arrays.toString(valuesArray));

      for (int j = 0; j < timesArray.length; j++) {
        s_logger.info("i+1 = " + (i + 1));
        s_logger.info("timesArray[" + j + "].getDayOfMonth() = " + timesArray[j].getDayOfMonth());
        s_logger.info("valuesArray[" + j + "] = " + valuesArray[j]);
        assertEquals((double) ((i + 1) * timesArray[j].getDayOfMonth()), (double) valuesArray[j], 1E-20);
      }
    }
    assertEquals(BulkTimeSeriesOperations.intersection(new LocalDateDoubleTimeSeries[] {one })[0], one);
  }

}
