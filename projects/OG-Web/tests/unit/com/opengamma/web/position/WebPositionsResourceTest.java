/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.ZoneOffset;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Counterparty;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.util.money.Currency;

/**
 * Test {@link WebPositionsResource}.
 */
public class WebPositionsResourceTest {
  
  private static final ExternalId SEC_ID = ExternalId.of("A", "B");
  private static final ExternalId COUNTER_PARTY = ExternalId.of(Counterparty.DEFAULT_SCHEME, "BACS");
  
  private SecurityMaster _secMaster;
  private SecurityLoader _secLoader;
  private HistoricalTimeSeriesMaster _htsMaster;
  private ConfigSource _cfgSource;
  private WebPositionsResource _webPositionsResource;
  private MockSecuritySource _securitySource;
  private PositionMaster _positionMaster;
  private List<ManageableTrade> _trades = Lists.newArrayList();
  
  @BeforeMethod
  public void setUp() throws Exception {
    buildTrades();
    _secMaster = new InMemorySecurityMaster(new ObjectIdSupplier("Mock"));
    _positionMaster = new InMemoryPositionMaster();
    _htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    _cfgSource = new MasterConfigSource(new InMemoryConfigMaster());
    _securitySource = new MockSecuritySource();
    _secLoader = new SecurityLoader() {
      
      @Override
      public Map<ExternalIdBundle, UniqueId> loadSecurity(Collection<ExternalIdBundle> identifiers) {
        throw new UnsupportedOperationException("load security not supported");
      }
      
      @Override
      public SecurityMaster getSecurityMaster() {
        return _secMaster;
      }
    };
    populateSecMaster();
    _webPositionsResource = new WebPositionsResource(_positionMaster, _secLoader,  _securitySource, _htsMaster, _cfgSource);
    _webPositionsResource.setServletContext(new MockServletContext("/web-engine", new FileSystemResourceLoader()));
    _webPositionsResource.setUriInfo(new MockUriInfo());
  }

  private void buildTrades() {
    ManageableTrade trade1 = new ManageableTrade(BigDecimal.valueOf(50), SEC_ID, LocalDate.parse("2011-12-07"), OffsetTime.of(LocalTime.of(15, 4), ZoneOffset.UTC), COUNTER_PARTY);
    trade1.setPremium(10.0);
    trade1.setPremiumCurrency(Currency.USD);
    trade1.setPremiumDate(LocalDate.parse("2011-12-08"));
    trade1.setPremiumTime(OffsetTime.of(LocalTime.of(15, 4), ZoneOffset.UTC));
    _trades.add(trade1);
    
    ManageableTrade trade2 = new ManageableTrade(BigDecimal.valueOf(60), SEC_ID, LocalDate.parse("2011-12-08"), OffsetTime.of(LocalTime.of(16, 4), ZoneOffset.UTC), COUNTER_PARTY);
    trade2.setPremium(20.0);
    trade2.setPremiumCurrency(Currency.USD);
    trade2.setPremiumDate(LocalDate.parse("2011-12-09"));
    trade2.setPremiumTime(OffsetTime.of(LocalTime.of(16, 4), ZoneOffset.UTC));
    _trades.add(trade2);
    
    ManageableTrade trade3 = new ManageableTrade(BigDecimal.valueOf(70), SEC_ID, LocalDate.parse("2011-12-09"), OffsetTime.of(LocalTime.of(17, 4), ZoneOffset.UTC), COUNTER_PARTY);
    trade3.setPremium(30.0);
    trade3.setPremiumCurrency(Currency.USD);
    trade3.setPremiumDate(LocalDate.parse("2011-12-10"));
    trade3.setPremiumTime(OffsetTime.of(LocalTime.of(17, 4), ZoneOffset.UTC));
    _trades.add(trade3);
  }

  private void populateSecMaster() {
    RawSecurity rawSecurity = new RawSecurity();
    rawSecurity.setExternalIdBundle(ExternalIdBundle.of(SEC_ID));
    SecurityDocument added = _secMaster.add(new SecurityDocument(rawSecurity));
    _securitySource.addSecurity(added.getSecurity());
  }

  @Test
  public void testAddPositionWithTrades() throws Exception {
    String tradesJson = getTradesJson();
    Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    assertPositionAndTrades();
  }

  private void assertPositionAndTrades() {
    PositionSearchRequest request = new PositionSearchRequest();
    PositionSearchResult searchResult = _positionMaster.search(request);
    assertNotNull(searchResult);
    List<PositionDocument> docs = searchResult.getDocuments();
    assertNotNull(docs);
    assertEquals(1, docs.size());
    ManageablePosition position = docs.get(0).getPosition();
    assertEquals(BigDecimal.TEN, position.getQuantity());
    assertEquals(new ManageableSecurityLink(SEC_ID), position.getSecurityLink());
    
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(3, trades.size());
    for (ManageableTrade trade : trades) {
      trade.setUniqueId(null);
      trade.setParentPositionId(null);
      assertTrue(_trades.contains(trade));
    }
   
  }

  private String getTradesJson() throws Exception {
    URL resource = getClass().getResource("tradesJson.txt");
    assertNotNull(resource);
    return FileUtils.readFileToString(new File(resource.getFile()));
  }
}
