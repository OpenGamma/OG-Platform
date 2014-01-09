/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.currency;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencySeriesConversionFunctionTest {

  @Test
  public void testConvertLabelledMatrix() throws Exception {
    final double conversionFactor = 0.5;
    final Tenor[] tenors = new Tenor[] {Tenor.ONE_DAY, Tenor.TWO_DAYS, Tenor.THREE_DAYS};
    final LocalDateDoubleTimeSeries[] dateArray = new LocalDateDoubleTimeSeries[] { ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.now()}, new double[] { 2.0 }),
        ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.now()}, new double[] { 3.0 }), ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.now()}, new double[] { 4.0 })};
    final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D instance = new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(tenors, dateArray);
    final DoubleTimeSeries<LocalDate> conversionRates = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.now()},new double[] {conversionFactor} );

    final CurrencySeriesConversionFunction function = new CurrencySeriesConversionFunction("Blah");
    final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D result = function.convertLabelledMatrix(instance, conversionRates);
    assertEquals(instance.getValues()[0].getEarliestValueFast() / conversionFactor, result.getValues()[0].getEarliestValueFast());
    assertEquals(instance.getValues()[1].getEarliestValueFast() / conversionFactor, result.getValues()[1].getEarliestValueFast());
    assertEquals(instance.getValues()[2].getEarliestValueFast() / conversionFactor, result.getValues()[2].getEarliestValueFast());
  }
}
