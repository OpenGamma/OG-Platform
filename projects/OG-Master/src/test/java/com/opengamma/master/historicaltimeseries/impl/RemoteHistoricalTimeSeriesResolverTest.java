/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.fudgemsg.FudgeMsg;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolverWithBasicChangeManager;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterface;

/**
 * Tests the {@link RemoteHistoricalTimeSeriesResolver} and {@link DataHistoricalTimeSeriesResolverResource} classes.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteHistoricalTimeSeriesResolverTest {

  private static class MockResolver extends HistoricalTimeSeriesResolverWithBasicChangeManager {

    private ExternalIdBundle _identifierBundle;
    private LocalDate _identifierValidityDate;
    private String _dataSource;
    private String _dataProvider;
    private String _dataField;
    private String _resolutionKey;
    private HistoricalTimeSeriesResolutionResult _result;

    @Override
    public HistoricalTimeSeriesResolutionResult resolve(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
        final String dataSource, final String dataProvider, final String dataField, final String resolutionKey) {
      assertEquals(identifierBundle, _identifierBundle);
      assertEquals(identifierValidityDate, _identifierValidityDate);
      assertEquals(dataSource, _dataSource);
      assertEquals(dataProvider, _dataProvider);
      assertEquals(dataField, _dataField);
      assertEquals(resolutionKey, _resolutionKey);
      return _result;
    }

    public void setIdentifierBundle(final ExternalIdBundle identifierBundle) {
      _identifierBundle = identifierBundle;
    }

    public void setIdentifierValidityDate(final LocalDate identifierValidityDate) {
      _identifierValidityDate = identifierValidityDate;
    }

    public void setDataSource(final String dataSource) {
      _dataSource = dataSource;
    }

    public void setDataProvider(final String dataProvider) {
      _dataProvider = dataProvider;
    }

    public void setDataField(final String dataField) {
      _dataField = dataField;
    }

    public void setResolutionKey(final String resolutionKey) {
      _resolutionKey = resolutionKey;
    }

    public void setResult(final HistoricalTimeSeriesResolutionResult result) {
      _result = result;
    }

  }

  private static class MockAdjuster implements HistoricalTimeSeriesAdjuster {

    private ExternalIdBundle _identifierBundle;
    private HistoricalTimeSeriesAdjustment _result;

    @Override
    public HistoricalTimeSeries adjust(final ExternalIdBundle securityIdBundle, final HistoricalTimeSeries timeSeries) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeriesAdjustment getAdjustment(final ExternalIdBundle identifierBundle) {
      assertEquals(identifierBundle, _identifierBundle);
      return _result;
    }

    public void setIdentifierBundle(final ExternalIdBundle identifierBundle) {
      _identifierBundle = identifierBundle;
    }

    public void setResult(final HistoricalTimeSeriesAdjustment result) {
      _result = result;
    }

  }

  private HistoricalTimeSeriesResolver mockConnection(final HistoricalTimeSeriesResolver resolver) {
    final DataHistoricalTimeSeriesResolverResource server = new DataHistoricalTimeSeriesResolverResource(resolver, OpenGammaFudgeContext.getInstance());
    final HistoricalTimeSeriesResolver client = new RemoteHistoricalTimeSeriesResolver(URI.create("http://localhost/")) {
      @Override
      protected UniformInterface accessRemote(final URI uri) {
        assertTrue(uri.getPath().startsWith("/resolve/"));
        final UniformInterface builder = Mockito.mock(UniformInterface.class);
        Mockito.when(builder.get(FudgeMsg.class)).thenAnswer(new Answer<FudgeMsg>() {
          @Override
          public FudgeMsg answer(final InvocationOnMock invocation) throws Throwable {
            try {
              String[] str = uri.getPath().substring(9).split("/");
              DataHistoricalTimeSeriesResolverResource.Resolve resolve = server.resolve();
              for (int i = 0; i < str.length; i++) {
                if ("adjustment".equals(str[i])) {
                  final String[] params = uri.getQuery().split("&");
                  final List<String> ids = new ArrayList<String>(params.length);
                  for (String param : params) {
                    ids.add(param.substring(3));
                  }
                  return (FudgeMsg) resolve.adjustment(ids).getEntity();
                } else if ("dataField".equals(str[i])) {
                  resolve = resolve.dataField(str[++i]);
                } else if ("dataProvider".equals(str[i])) {
                  resolve = resolve.dataProvider(str[++i]);
                } else if ("dataSource".equals(str[i])) {
                  resolve = resolve.dataSource(str[++i]);
                } else if ("id".equals(str[i])) {
                  resolve = resolve.id(str[++i]);
                } else if ("identifierValidityDate".equals(str[i])) {
                  resolve = resolve.identifierValidityDate(str[++i]);
                } else if ("resolutionKey".equals(str[i])) {
                  resolve = resolve.resolutionKey(str[++i]);
                } else {
                  fail(uri + " - " + str[i]);
                }
              }
              return (FudgeMsg) resolve.get().getEntity();
            } catch (WebApplicationException e) {
              assertEquals(e.getResponse().getStatus(), 404);
              throw new UniformInterfaceException404NotFound(new ClientResponse(404, null, null, null), false);
            }
          }
        });
        return builder;
      }
    };
    return client;
  }

  public void testResolve() {
    final MockResolver mock = new MockResolver();
    final HistoricalTimeSeriesResolver client = mockConnection(mock);
    mock.setIdentifierBundle(ExternalId.of("Test", "Foo").toBundle());
    mock.setDataField("PX_LAST");
    mock.setResult(new HistoricalTimeSeriesResolutionResult(new ManageableHistoricalTimeSeriesInfo()));
    assertNotNull(client.resolve(ExternalId.of("Test", "Foo").toBundle(), null, null, null, "PX_LAST", null), null);
  }

  public void testResolveWithValidityDate() {
    final LocalDate now = LocalDate.now();
    final MockResolver mock = new MockResolver();
    final HistoricalTimeSeriesResolver client = mockConnection(mock);
    mock.setIdentifierBundle(ExternalId.of("Test", "Foo").toBundle());
    mock.setIdentifierValidityDate(now);
    mock.setDataField("PX_LAST");
    mock.setResult(new HistoricalTimeSeriesResolutionResult(new ManageableHistoricalTimeSeriesInfo()));
    assertNotNull(client.resolve(ExternalId.of("Test", "Foo").toBundle(), now, null, null, "PX_LAST", null), null);
  }

  public void testResolveWithDataSourceAndProvider() {
    final MockResolver mock = new MockResolver();
    final HistoricalTimeSeriesResolver client = mockConnection(mock);
    mock.setIdentifierBundle(ExternalId.of("Test", "Foo").toBundle());
    mock.setDataSource("datasource");
    mock.setDataProvider("dataprovider");
    mock.setDataField("PX_LAST");
    mock.setResult(new HistoricalTimeSeriesResolutionResult(new ManageableHistoricalTimeSeriesInfo()));
    assertNotNull(client.resolve(ExternalId.of("Test", "Foo").toBundle(), null, "datasource", "dataprovider", "PX_LAST", null), null);
  }

  public void testResolveWithResolutionKey() {
    final MockResolver mock = new MockResolver();
    final HistoricalTimeSeriesResolver client = mockConnection(mock);
    mock.setIdentifierBundle(ExternalId.of("Test", "Foo").toBundle());
    mock.setDataField("PX_LAST");
    mock.setResolutionKey("resolutionkey");
    mock.setResult(new HistoricalTimeSeriesResolutionResult(new ManageableHistoricalTimeSeriesInfo()));
    assertNotNull(client.resolve(ExternalId.of("Test", "Foo").toBundle(), null, null, null, "PX_LAST", "resolutionkey"), null);
  }

  public void testGetAdjustment() {
    final MockResolver resolver = new MockResolver();
    final HistoricalTimeSeriesResolver client = mockConnection(resolver);
    resolver.setIdentifierBundle(ExternalIdBundle.of(ExternalId.of("Test1", "Foo"), ExternalId.of("Test2", "Foo")));
    resolver.setDataField("PX_LAST");
    final MockAdjuster adjuster = new MockAdjuster();
    adjuster.setIdentifierBundle(ExternalIdBundle.of(ExternalId.of("Test1", "Foo"), ExternalId.of("Test2", "Foo")));
    adjuster.setResult(new HistoricalTimeSeriesAdjustment.DivideBy(100d));
    resolver.setResult(new HistoricalTimeSeriesResolutionResult(new ManageableHistoricalTimeSeriesInfo(), adjuster));
    final HistoricalTimeSeriesResolutionResult result = client.resolve(ExternalIdBundle.of(ExternalId.of("Test1", "Foo"), ExternalId.of("Test2", "Foo")), null, null, null, "PX_LAST", null);
    assertNotNull(result);
    adjuster.setIdentifierBundle(ExternalIdBundle.of(ExternalId.of("Test1", "Foo")));
    adjuster.setResult(HistoricalTimeSeriesAdjustment.NoOp.INSTANCE);
    HistoricalTimeSeriesAdjustment adjustment = result.getAdjuster().getAdjustment(ExternalIdBundle.of(ExternalId.of("Test1", "Foo"), ExternalId.of("Test2", "Foo")));
    assertEquals(adjustment.toString(), "100.0 /");
    adjustment = result.getAdjuster().getAdjustment(ExternalIdBundle.of(ExternalId.of("Test1", "Foo")));
    assertEquals(adjustment.toString(), "");
  }

  public void testNotFound() {
    final MockResolver resolver = new MockResolver();
    final HistoricalTimeSeriesResolver client = mockConnection(resolver);
    resolver.setIdentifierBundle(ExternalIdBundle.of(ExternalId.of("Test1", "Foo"), ExternalId.of("Test2", "Foo")));
    resolver.setDataField("PX_LAST");
    resolver.setResult(null);
    assertNull(client.resolve(ExternalIdBundle.of(ExternalId.of("Test1", "Foo"), ExternalId.of("Test2", "Foo")), null, null, null, "PX_LAST", null));
  }

}
