/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.push.TestConnectionManager;
import com.opengamma.web.server.push.Viewport;
import com.opengamma.web.server.push.WebPushTestUtils;
import org.eclipse.jetty.server.Server;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

public class ViewportResourceTest {

  private static final String VIEWPORT_ID = "123";
  private static final String PORTFOLIO_CSV_FILE_NAME = "portfolioCsvFileName";
  private static final String PORTFOLIO_CSV_CONTENT = "portfolioCsvContent";
  private static final String PRIMITIVES_CSV_FILE_NAME = "primitivesCsvFileName";
  private static final String PRIMITIVES_CSV_CONTENT = "primitivesCsvContent";
  private static final String PORTFOLIO_DEP_GRAPH_CSV_FILE_NAME = "portfolioDepGraphCsvFileName";
  private static final String PORTFOLIO_DEP_GRAPH_CSV_CONTENT = "portfolioDepGraphCsvContent";
  private static final String PRIMITIVES_DEP_GRAPH_CSV_FILE_NAME = "primitivesDepGraphCsvFileName";
  private static final String PRIMITIVES_DEP_GRAPH_CSV_CONTENT = "primitivesDepGraphCsvContent";
  private static final String CONTENT_DISPOSITION = "Content-Disposition";

  private Server _server;

  @BeforeClass
  public void setUp() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/server/push/viewportresource-test.xml");
    _server = serverAndContext.getFirst();
    WebApplicationContext context = serverAndContext.getSecond();
    TestConnectionManager connectionManager = context.getBean("connectionManager", TestConnectionManager.class);
    Viewport viewport = mock(Viewport.class);
    when(viewport.getPortfolioCsv()).thenReturn(Pair.of(PORTFOLIO_CSV_FILE_NAME, PORTFOLIO_CSV_CONTENT));
    when(viewport.getPrimitivesCsv()).thenReturn(Pair.of(PRIMITIVES_CSV_FILE_NAME, PRIMITIVES_CSV_CONTENT));
    when(viewport.getPortfolioCsv(2, 3)).thenReturn(Pair.of(PORTFOLIO_DEP_GRAPH_CSV_FILE_NAME, PORTFOLIO_DEP_GRAPH_CSV_CONTENT));
    when(viewport.getPrimitivesCsv(4, 5)).thenReturn(Pair.of(PRIMITIVES_DEP_GRAPH_CSV_FILE_NAME, PRIMITIVES_DEP_GRAPH_CSV_CONTENT));
    connectionManager.addViewport(VIEWPORT_ID, viewport);
  }

  @AfterClass
  public void tearDown() throws Exception {
    _server.stop();
  }

  @Test
  public void portfolioCsvOverHttp() throws Exception {
    HttpURLConnection connection = WebPushTestUtils.connectToPath("/jax/viewports/" + VIEWPORT_ID + "/portfolio");
    assertEquals("text/csv", connection.getContentType());
    assertEquals("attachment; filename=" + PORTFOLIO_CSV_FILE_NAME, connection.getHeaderField(CONTENT_DISPOSITION));
    assertEquals(PORTFOLIO_CSV_CONTENT, WebPushTestUtils.readAndClose(connection));
  }

  @Test
  public void primitivesCsvOverHttp() throws IOException {
    HttpURLConnection connection = WebPushTestUtils.connectToPath("/jax/viewports/" + VIEWPORT_ID + "/primitives");
    assertEquals("text/csv", connection.getContentType());
    assertEquals("attachment; filename=" + PRIMITIVES_CSV_FILE_NAME, connection.getHeaderField(CONTENT_DISPOSITION));
    assertEquals(PRIMITIVES_CSV_CONTENT, WebPushTestUtils.readAndClose(connection));
  }

  @Test
  public void portfolioDepGraphCsvOverHttp() throws IOException {
    HttpURLConnection connection = WebPushTestUtils.connectToPath("/jax/viewports/" + VIEWPORT_ID + "/portfolio/2/3");
    assertEquals("text/csv", connection.getContentType());
    assertEquals("attachment; filename=" + PORTFOLIO_DEP_GRAPH_CSV_FILE_NAME, connection.getHeaderField(CONTENT_DISPOSITION));
    assertEquals(PORTFOLIO_DEP_GRAPH_CSV_CONTENT, WebPushTestUtils.readAndClose(connection));
  }

  @Test
  public void primitivesDepGraphCsvOverHttp() throws IOException {
    HttpURLConnection connection = WebPushTestUtils.connectToPath("/jax/viewports/" + VIEWPORT_ID + "/primitives/4/5");
    assertEquals("text/csv", connection.getContentType());
    assertEquals("attachment; filename=" + PRIMITIVES_DEP_GRAPH_CSV_FILE_NAME, connection.getHeaderField(CONTENT_DISPOSITION));
    assertEquals(PRIMITIVES_DEP_GRAPH_CSV_CONTENT, WebPushTestUtils.readAndClose(connection));
  }
}
