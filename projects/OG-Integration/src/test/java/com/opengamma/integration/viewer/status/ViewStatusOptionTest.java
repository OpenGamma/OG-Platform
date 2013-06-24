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

import java.io.File;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.viewer.status.ViewStatusOption.ResultFormat;
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
  
  private static final String PORTFOLIO_NAME = "PORTFOLIO_NAME";
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
 
  public void defaultOptions() throws Exception {
    String[] args = {"-n", PORTFOLIO_NAME};
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
    assertNotNull(statusOption);
    
    assertEquals(PORTFOLIO_NAME, statusOption.getPortfolioName());
    assertEquals(UserPrincipal.getLocalUser(), statusOption.getUser());
    assertEquals(ResultFormat.HTML, statusOption.getFormat());
    assertEquals(MarketData.live(), statusOption.getMarketDataSpecification());
    assertEquals(AggregateType.NO_AGGREGATION, statusOption.getAggregateType());
    assertEquals(new File(ViewStatusOption.DEFAULT_OUTPUT_NAME + "." + ResultFormat.HTML.getExtension()), statusOption.getOutputFile());
    
  }
  
  public void userOption() throws Exception {
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(
        s_parser.parse(s_options, new String[] {"-n", PORTFOLIO_NAME, "-u", "test/127.0.0.1"}), s_toolContext);
    assertNotNull(statusOption);
    assertNotNull(statusOption.getUser());
    assertEquals("test", statusOption.getUser().getUserName());
    assertEquals("127.0.0.1", statusOption.getUser().getIpAddress());
    
    statusOption = ViewStatusOption.getViewStatusReporterOption(
        s_parser.parse(s_options, new String[] {"-n", PORTFOLIO_NAME, "-u", "A/B"}), s_toolContext);
    assertNotNull(statusOption);
    assertNotNull(statusOption.getUser());
    assertEquals("A", statusOption.getUser().getUserName());
    assertEquals("B", statusOption.getUser().getIpAddress());
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void invalidSeparatorForUserOption() throws Exception {
    String[] args = {"-n", PORTFOLIO_NAME, "-u", "test~127.0.0.1"};
    ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void blankUsernamePasswordForUserOption() throws Exception {
    String[] args = {"-n", PORTFOLIO_NAME, "-u", "/"};
    ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
  }
  
  public void liveMarketDataOption() throws Exception {
    String[] args = {"-n", PORTFOLIO_NAME, "--live", "liveMarketData"};
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
    assertNotNull(statusOption);
    
    assertNotNull(statusOption.getMarketDataSpecification());
    assertTrue(statusOption.getMarketDataSpecification() instanceof LiveMarketDataSpecification);
    LiveMarketDataSpecification marketDataSpecification = (LiveMarketDataSpecification) statusOption.getMarketDataSpecification();
    assertEquals("liveMarketData", marketDataSpecification.getDataSource());
  }
  
  public void historicalMarketDataOption() throws Exception {
    String[] args = {"-n", PORTFOLIO_NAME, "--historical", "2013-06-20/timeSeriesResolverKey"};
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
    String[] args = {"-n", PORTFOLIO_NAME, "--historical", "2013-06-20~timeSeriesResolverKey"};
    ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void invalidDateHistoricalMarketDataOption() throws Exception {
    String[] args = {"-n", PORTFOLIO_NAME, "--historical", "xxxx/timeSeriesResolverKey"};
    ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
  }
  
  public void userMarketDataOption() throws Exception {
    String[] args = {"-n", PORTFOLIO_NAME, "--snapshot", "snaphshotName"};
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args), s_toolContext);
    assertNotNull(statusOption);
    
    assertTrue(statusOption.getMarketDataSpecification() instanceof UserMarketDataSpecification);
    UserMarketDataSpecification marketDataSpecification = (UserMarketDataSpecification) statusOption.getMarketDataSpecification();
    assertEquals(s_mockUniqueId, marketDataSpecification.getUserSnapshotId());
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
}
