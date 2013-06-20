/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link ViewStatusOption}
 */
@Test(groups = TestGroup.UNIT)
public class ViewStatusOptionTest {
  
  private static final Options s_options = ViewStatusOption.createOptions();
  private static final CommandLineParser s_parser = new PosixParser();
  private static final ToolContext s_toolContext = new ToolContext();
  private static final UniqueId s_mockUniqueId = UniqueId.parse("Mock~12345~0");
  private static final MarketDataSnapshotSearchRequest s_snapshotRequest = makeRequest();
  private static final MarketDataSnapshotSearchResult s_snapshotSearchResult = makeSearchResult();
  
  static {
    MarketDataSnapshotMaster snapshotMaster = mock(MarketDataSnapshotMaster.class);
    when(snapshotMaster.search(s_snapshotRequest)).thenReturn(s_snapshotSearchResult);
    s_toolContext.setMarketDataSnapshotMaster(snapshotMaster);
  }

  public void defaultUser() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME"};
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
    assertNotNull(statusOption);
    assertNotNull(statusOption.getUser());
    assertEquals(UserPrincipal.getLocalUser(), statusOption.getUser());
  }
  
  private static MarketDataSnapshotSearchResult makeSearchResult() {
    MarketDataSnapshotSearchResult result = new MarketDataSnapshotSearchResult();
    result.setDocuments(Lists.newArrayList(new MarketDataSnapshotDocument(s_mockUniqueId, new ManageableMarketDataSnapshot())));
    return result;
  }

  private static MarketDataSnapshotSearchRequest makeRequest() {
    MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    request.setName("snaphshotName");
    return request;
  }

  public void userOption() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME", "-u", "test/127.0.0.1"};
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
    assertNotNull(statusOption);
    assertNotNull(statusOption.getUser());
    assertEquals("test", statusOption.getUser().getUserName());
    assertEquals("127.0.0.1", statusOption.getUser().getIpAddress());
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void invalidUserOption() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME", "-u", "test~127.0.0.1"};
    ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
  }
  
  public void liveMarketDataOption() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME", "--live", "liveMarketData"};
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
    assertNotNull(statusOption);
    
    assertNotNull(statusOption.getMarketDataSpecification());
    assertTrue(statusOption.getMarketDataSpecification() instanceof LiveMarketDataSpecification);
    LiveMarketDataSpecification marketDataSpecification = (LiveMarketDataSpecification) statusOption.getMarketDataSpecification();
    assertEquals("liveMarketData", marketDataSpecification.getDataSource());
  }
  
  public void historicalMarketDataOption() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME", "--historical", "2013-06-20/timeSeriesResolverKey"};
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
    assertNotNull(statusOption);
    assertTrue(statusOption.getMarketDataSpecification() instanceof FixedHistoricalMarketDataSpecification);
    FixedHistoricalMarketDataSpecification marketDataSpecification = (FixedHistoricalMarketDataSpecification) statusOption.getMarketDataSpecification();
    assertNotNull(marketDataSpecification.getSnapshotDate());
    assertEquals("2013-06-20", marketDataSpecification.getSnapshotDate().toString());
    assertEquals("timeSeriesResolverKey", marketDataSpecification.getTimeSeriesResolverKey());
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void invalidHistoricalMarketDataOption() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME", "--historical", "2013-06-20~timeSeriesResolverKey"};
    ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void invalidDateHistoricalMarketDataOption() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME", "--historical", "xxxx/timeSeriesResolverKey"};
    ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
  }
  
  public void userMarketDataOption() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME", "--snapshot", "snaphshotName"};
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
    assertNotNull(statusOption);
    
    assertTrue(statusOption.getMarketDataSpecification() instanceof UserMarketDataSpecification);
    UserMarketDataSpecification marketDataSpecification = (UserMarketDataSpecification) statusOption.getMarketDataSpecification();
    assertEquals(s_mockUniqueId, marketDataSpecification.getUserSnapshotId());
  }
}
