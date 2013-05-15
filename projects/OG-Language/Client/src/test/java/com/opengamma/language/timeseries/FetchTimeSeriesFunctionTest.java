/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.language.DataUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.Converters;
import com.opengamma.language.identifier.IdentifierConverter;
import com.opengamma.language.invoke.AggregatingTypeConverterProvider;
import com.opengamma.language.invoke.TypeConverterProviderBean;
import com.opengamma.language.test.TestUtils;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the {@link FetchTimeSeriesFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class FetchTimeSeriesFunctionTest {

  private static class TestHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean inclusiveStart, LocalDate end, boolean inclusiveEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean inclusiveStart,
        LocalDate end, boolean inclusiveEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
        boolean inclusiveStart, LocalDate end, boolean inclusiveEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean inclusiveStart, LocalDate end,
        boolean inclusiveEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
        boolean inclusiveStart, LocalDate end, boolean inclusiveEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
        boolean inclusiveStart, LocalDate end, boolean inclusiveEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
        boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
        boolean includeEnd, int maxPoints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
        boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
        boolean includeStart, LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
        LocalDate end, boolean includeEnd, int maxPoints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
        LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
        boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
        boolean includeStart, LocalDate end, boolean includeEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalIdBundle getExternalIdBundle(UniqueId uniqueId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ChangeManager changeManager() {
      throw new UnsupportedOperationException();
    }
  }

  private SessionContext createSessionContext(final HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    final TestUtils testUtils = new TestUtils();
    testUtils.setHistoricalTimeSeriesSource(historicalTimeSeriesSource);
    final Converters converters = new Converters();
    converters.setFudgeContext(OpenGammaFudgeContext.getInstance());
    final TypeConverterProviderBean extra = new TypeConverterProviderBean();
    extra.setConverters(Arrays.asList(IdentifierConverter.INSTANCE));
    final AggregatingTypeConverterProvider agg = new AggregatingTypeConverterProvider();
    agg.addTypeConverterProvider(converters);
    agg.addTypeConverterProvider(extra);
    testUtils.setTypeConverters(agg);
    return testUtils.createSessionContext();
  }

  private HistoricalTimeSeries result() {
    return new HistoricalTimeSeries() {

      @Override
      public LocalDateDoubleTimeSeries getTimeSeries() {
        return null;
      }

      @Override
      public UniqueId getUniqueId() {
        return null;
      }
    };
  }

  public void testUniqueId() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final UniqueId uniqueId) {
        assertEquals(uniqueId, UniqueId.of("Foo", "Bar"));
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), null, null, null, null, null, null, null, null, null, null
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

  public void testUniqueIdMaxPoints() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final UniqueId uniqueId, final LocalDate start, final boolean inclusiveStart, final LocalDate end, final boolean inclusiveEnd, final int maxPoints) {
        assertEquals(uniqueId, UniqueId.of("Foo", "Bar"));
        assertEquals(-1, maxPoints);
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), null, null, null, null, null, null, null, null, null, -1
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

  public void testUniqueIdSubset() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final UniqueId uniqueId, final LocalDate start, final boolean inclusiveStart, final LocalDate end, final boolean inclusiveEnd) {
        assertEquals(uniqueId, UniqueId.of("Foo", "Bar"));
        assertEquals(start, LocalDate.of(2011, 4, 1));
        assertTrue(inclusiveStart);
        assertEquals(end, null);
        assertTrue(inclusiveEnd);
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), LocalDate.of(2011, 4, 1), null, null, null, Boolean.TRUE, Boolean.TRUE, null, null, null, null
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

  public void testBundleSourceProviderAndField() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final String dataSource, final String dataProvider, final String dataField) {
        assertEquals(identifierBundle, ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
        assertEquals(dataSource, "source");
        assertEquals(dataProvider, "provider");
        assertEquals(dataField, "field");
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), null, null, "field", null, null, null, "source", "provider", null, null
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

  public void testBundleSourceProviderAndFieldWithValidity() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider, final String dataField) {
        assertEquals(identifierBundle, ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
        assertEquals(identifierValidityDate, LocalDate.of(2011, 5, 1));
        assertEquals(dataSource, "source");
        assertEquals(dataProvider, "provider");
        assertEquals(dataField, "field");
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), null, null, "field", null, null, null, "source", "provider", LocalDate.of(2011, 5, 1), null
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

  public void testBundleSourceProviderAndFieldSubset() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final String dataSource, final String dataProvider, final String dataField, final LocalDate start,
          final boolean inclusiveStart, final LocalDate end, final boolean inclusiveEnd) {
        assertEquals(identifierBundle, ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
        assertEquals(dataSource, "source");
        assertEquals(dataProvider, "provider");
        assertEquals(dataField, "field");
        assertEquals(start, LocalDate.of(2011, 4, 1));
        assertTrue(inclusiveStart);
        assertEquals(end, null);
        assertTrue(inclusiveEnd);
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), LocalDate.of(2011, 4, 1), null, "field", null, Boolean.TRUE, Boolean.TRUE, "source", "provider", null, null
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

  public void testBundleSourceProviderAndFieldSubsetWithValidity() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider,
          final String dataField, final LocalDate start, final boolean inclusiveStart, final LocalDate end, final boolean inclusiveEnd) {
        assertEquals(identifierBundle, ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
        assertEquals(identifierValidityDate, LocalDate.of(2011, 5, 1));
        assertEquals(dataSource, "source");
        assertEquals(dataProvider, "provider");
        assertEquals(dataField, "field");
        assertEquals(start, LocalDate.of(2011, 4, 1));
        assertTrue(inclusiveStart);
        assertEquals(end, null);
        assertTrue(inclusiveEnd);
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), LocalDate.of(2011, 4, 1), null, "field", null, Boolean.TRUE, Boolean.TRUE, "source", "provider", LocalDate.of(2011, 5, 1), null
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

  public void testBundleUsingConfiguration() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final String resolutionKey) {
        assertEquals(dataField, "field");
        assertEquals(identifierBundle, ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
        assertEquals(resolutionKey, "key");
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), null, null, "field", "key", null, null, null, null, null, null
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

  public void testBundleUsingConfigurationWithValidity() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String resolutionKey) {
        assertEquals(dataField, "field");
        assertEquals(identifierBundle, ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
        assertEquals(identifierValidityDate, LocalDate.of(2011, 5, 1));
        assertEquals(resolutionKey, "key");
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), null, null, "field", "key", null, null, null, null, LocalDate.of(2011, 5, 1), null
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

  public void testBundleUsingConfigurationSubset() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final String resolutionKey, final LocalDate start,
          final boolean inclusiveStart, final LocalDate end, final boolean inclusiveEnd) {
        assertEquals(dataField, "field");
        assertEquals(identifierBundle, ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
        assertEquals(resolutionKey, "key");
        assertEquals(start, null);
        assertTrue(inclusiveStart);
        assertEquals(end, LocalDate.of(2011, 4, 1));
        assertFalse(inclusiveEnd);
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), null, LocalDate.of(2011, 4, 1), "field", "key", Boolean.TRUE, Boolean.FALSE, null, null, null, null
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

  public void testBundleUsingConfigurationSubsetWithValidity() {
    final SessionContext sessionContext = createSessionContext(new TestHistoricalTimeSeriesSource() {
      @Override
      public HistoricalTimeSeries getHistoricalTimeSeries(final String dataField, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String resolutionKey,
          final LocalDate start, final boolean inclusiveStart, final LocalDate end, final boolean inclusiveEnd) {
        assertEquals(dataField, "field");
        assertEquals(identifierBundle, ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
        assertEquals(identifierValidityDate, LocalDate.of(2011, 5, 1));
        assertEquals(resolutionKey, "key");
        assertEquals(start, null);
        assertTrue(inclusiveStart);
        assertEquals(end, LocalDate.of(2011, 4, 1));
        assertFalse(inclusiveEnd);
        return result();
      }
    });
    final FetchTimeSeriesFunction function = new FetchTimeSeriesFunction();
    final Object result = function.invokeImpl(sessionContext, new Object[] {
        DataUtils.of("Foo~Bar"), null, LocalDate.of(2011, 4, 1), "field", "key", Boolean.TRUE, Boolean.FALSE, null, null, LocalDate.of(2011, 5, 1), null
    });
    assertTrue(result instanceof HistoricalTimeSeries);
  }

}
