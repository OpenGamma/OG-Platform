package com.opengamma.financial.analytics.test.unittest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.impl.NonVersionedRedisHistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.AbstractRedisTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION, enabled = true)
public class CurveFixingTSLoaderTest extends AbstractRedisTestCase {

  public void testOperation() {
    NonVersionedRedisHistoricalTimeSeriesSource source = new NonVersionedRedisHistoricalTimeSeriesSource(getJedisPool(), getRedisPrefix());
    CurveFixingTSLoader loader = new CurveFixingTSLoader(source);
    loader.loadCurveFixingCSVFile("classpath:com/opengamma/financial/analytics/test/Base_Curves_20131014_Clean.csv");

    HistoricalTimeSeries historicalTimeSeries = source.getHistoricalTimeSeries(UniqueId.of(ExternalSchemes.ISDA.getName(), "CHF-LIBOR-BBA-6M"));
    assertNotNull(historicalTimeSeries);
    LocalDateDoubleTimeSeries timeSeries = historicalTimeSeries.getTimeSeries();
    assertNotNull(timeSeries);
    assertEquals(5996, timeSeries.size());
  }

}
