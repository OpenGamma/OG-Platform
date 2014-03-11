/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.cli;

import static org.mockito.Matchers.any;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;

/**
 * 
 */
@Test
public class MarketDataSourceCliTest {
  
  private static final UniqueId SNAPSHOT_ID = UniqueId.parse("DbSnp~1000");
  
  private Options _options;
  private MarketDataSourceCli _mktDataSourceCli;
  private CommandLineParser _parser;
  private MarketDataSnapshotMaster _snapshotMaster;
  
  @BeforeMethod
  public void setUp() {
    _options = new Options();
    _mktDataSourceCli = new MarketDataSourceCli();
    _parser = new PosixParser();
    _snapshotMaster = Mockito.mock(MarketDataSnapshotMaster.class);
    
    Mockito.when(_snapshotMaster.search(any(MarketDataSnapshotSearchRequest.class))).thenReturn(getMockSearchResult());
  }

  public void validDataSourceCliOptions() throws Exception {
    _options.addOption(_mktDataSourceCli.getOption());
    String[] args = {
        "--dataSource", "live:bbg", 
        "--dataSource", "live", 
        "--dataSource", "live:activ 123",
        "--dataSource", "historical",
        "--dataSource", "historical:defaultHts",
        "--dataSource", "historical:defaultHts:20140306",
        "--dataSource", "snapshot:testSnapshot"};
    
    CommandLine cmdLine = _parser.parse(_options, args);
    List<MarketDataSpecification> mktDataSpecs = _mktDataSourceCli.getMarketDataSpecs(cmdLine, _snapshotMaster);
    assertNotNull(mktDataSpecs);
    assertEquals(7, mktDataSpecs.size());
    assertEquals(LiveMarketDataSpecification.of("bbg"), mktDataSpecs.get(0));
    assertEquals(MarketData.live(), mktDataSpecs.get(1));
    assertEquals(LiveMarketDataSpecification.of("activ 123"), mktDataSpecs.get(2));
    assertEquals(new LatestHistoricalMarketDataSpecification(), mktDataSpecs.get(3));
    assertEquals(new LatestHistoricalMarketDataSpecification("defaultHts"), mktDataSpecs.get(4));
    assertEquals(new FixedHistoricalMarketDataSpecification("defaultHts", LocalDate.of(2014, 3, 6)), mktDataSpecs.get(5));
    assertEquals((UserMarketDataSpecification.of(SNAPSHOT_ID)), mktDataSpecs.get(6));
  }

  private MarketDataSnapshotSearchResult getMockSearchResult() {
    MarketDataSnapshotSearchResult searchResult = new MarketDataSnapshotSearchResult();
    searchResult.setDocuments(Collections.singletonList(new MarketDataSnapshotDocument(SNAPSHOT_ID, new ManageableMarketDataSnapshot())));
    return searchResult;
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void invalidOptionType() throws Exception {
    _options.addOption(_mktDataSourceCli.getOption());
    String[] args = {"--dataSource", "user:bbg"};
    CommandLine cmdLine = _parser.parse(_options, args);
    _mktDataSourceCli.getMarketDataSpecs(cmdLine, _snapshotMaster);
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void invalidSnapshotType() throws Exception {
    _options.addOption(_mktDataSourceCli.getOption());
    String[] args = {"--dataSource", "snapshot"};
    CommandLine cmdLine = _parser.parse(_options, args);
    _mktDataSourceCli.getMarketDataSpecs(cmdLine, _snapshotMaster);
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void invalidSnapshotTypeWithColon() throws Exception {
    _options.addOption(_mktDataSourceCli.getOption());
    String[] args = {"--dataSource", "snapshot:"};
    CommandLine cmdLine = _parser.parse(_options, args);
    _mktDataSourceCli.getMarketDataSpecs(cmdLine, _snapshotMaster);
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void invalidHistoricalType() throws Exception {
    _options.addOption(_mktDataSourceCli.getOption());
    String[] args = {"--dataSource", "historical:"};
    CommandLine cmdLine = _parser.parse(_options, args);
    _mktDataSourceCli.getMarketDataSpecs(cmdLine, _snapshotMaster);
  }
  
}
