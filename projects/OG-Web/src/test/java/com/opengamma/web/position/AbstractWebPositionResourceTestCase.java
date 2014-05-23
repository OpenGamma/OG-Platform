/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.BeforeMethod;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;

import com.google.common.collect.Lists;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.MasterHistoricalTimeSeriesSource;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.AbstractSecurityLoader;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestUtils;
import com.opengamma.web.FreemarkerOutputter;
import com.opengamma.web.MockUriInfo;
import com.opengamma.web.WebResourceTestUtils;

import freemarker.template.Configuration;

/**
 * Test base class for WebPositionResource tests
 */
public abstract class AbstractWebPositionResourceTestCase {

  protected static final ExternalId COUNTER_PARTY = ExternalId.of(Counterparty.DEFAULT_SCHEME, "BACS");
  protected static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("+0100");
  protected static final EquitySecurity EQUITY_SECURITY = WebResourceTestUtils.getEquitySecurity();
  protected static final ExternalId SEC_ID = EQUITY_SECURITY.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
  protected static final ManageableSecurityLink SECURITY_LINK = new ManageableSecurityLink(EQUITY_SECURITY.getExternalIdBundle());
  protected static final String EMPTY_TRADES = "{\"trades\" : []}";
  protected static final Long QUANTITY = Long.valueOf(100);

  protected SecurityMaster _secMaster;
  protected SecurityLoader _secLoader;
  protected HistoricalTimeSeriesSource _htsSource;
  protected WebPositionsResource _webPositionsResource;
  protected InMemorySecuritySource _securitySource;
  protected PositionMaster _positionMaster;
  protected List<ManageableTrade> _trades;
  protected UriInfo _uriInfo;
  protected Map<ExternalScheme, String> _externalSchemes;

  @BeforeMethod(groups = TestGroup.UNIT)
  public void setUp() throws Exception {
    TestUtils.initSecurity();
    _uriInfo = new MockUriInfo(true);
    _trades = getTrades();
    _secMaster = new InMemorySecurityMaster(new ObjectIdSupplier("Mock"));
    _positionMaster = new InMemoryPositionMaster();
    final MasterConfigSource configSource = new MasterConfigSource(new InMemoryConfigMaster());
    final InMemoryHistoricalTimeSeriesMaster htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesResolver htsResolver = new DefaultHistoricalTimeSeriesResolver(new DefaultHistoricalTimeSeriesSelector(configSource), htsMaster);
    _htsSource = new MasterHistoricalTimeSeriesSource(htsMaster, htsResolver);
    _securitySource = new InMemorySecuritySource();
    _secLoader = new AbstractSecurityLoader() {
      @Override
      protected SecurityLoaderResult doBulkLoad(SecurityLoaderRequest request) {
        throw new UnsupportedOperationException("load security not supported");
      }
    };
    populateSecMaster();
    _externalSchemes = new HashMap<>();
    _externalSchemes.put(ExternalSchemes.OG_SYNTHETIC_TICKER, ExternalSchemes.OG_SYNTHETIC_TICKER.getName());
    _webPositionsResource = new WebPositionsResource(_positionMaster, _secLoader, _securitySource, _htsSource, _externalSchemes);
    final MockServletContext sc = new MockServletContext("/web-engine", new FileSystemResourceLoader());
    final Configuration cfg = FreemarkerOutputter.createConfiguration();
    cfg.setServletContextForTemplateLoading(sc, "WEB-INF/pages");
    FreemarkerOutputter.init(sc, cfg);
    _webPositionsResource.setServletContext(sc);
    _webPositionsResource.setUriInfo(_uriInfo);
  }

  protected List<ManageableTrade> getTrades() {
    final List<ManageableTrade> trades = Lists.newArrayList();
    final ManageableTrade trade1 = new ManageableTrade(BigDecimal.valueOf(50), SEC_ID, LocalDate.parse("2011-12-07"), OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET), COUNTER_PARTY);
    trade1.setPremium(10.0);
    trade1.setPremiumCurrency(Currency.USD);
    trade1.setPremiumDate(LocalDate.parse("2011-12-08"));
    trade1.setPremiumTime(OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET));
    trades.add(trade1);

    final ManageableTrade trade2 = new ManageableTrade(BigDecimal.valueOf(60), SEC_ID, LocalDate.parse("2011-12-08"), OffsetTime.of(LocalTime.of(16, 4), ZONE_OFFSET), COUNTER_PARTY);
    trade2.setPremium(20.0);
    trade2.setPremiumCurrency(Currency.USD);
    trade2.setPremiumDate(LocalDate.parse("2011-12-09"));
    trade2.setPremiumTime(OffsetTime.of(LocalTime.of(16, 4), ZONE_OFFSET));
    trades.add(trade2);

    final ManageableTrade trade3 = new ManageableTrade(BigDecimal.valueOf(70), SEC_ID, LocalDate.parse("2011-12-09"), OffsetTime.of(LocalTime.of(17, 4), ZONE_OFFSET), COUNTER_PARTY);
    trade3.setPremium(30.0);
    trade3.setPremiumCurrency(Currency.USD);
    trade3.setPremiumDate(LocalDate.parse("2011-12-10"));
    trade3.setPremiumTime(OffsetTime.of(LocalTime.of(17, 4), ZONE_OFFSET));
    trades.add(trade3);
    return trades;
  }

  protected void populateSecMaster() {
    final SecurityDocument added = _secMaster.add(new SecurityDocument(EQUITY_SECURITY));
    _securitySource.addSecurity(added.getSecurity());
  }

  protected void populatePositionMaster() {
    for (final ManageableTrade trade : _trades) {
      final ManageablePosition manageablePosition = new ManageablePosition(trade.getQuantity(), SEC_ID);
      manageablePosition.addTrade(trade);
      final PositionDocument positionDocument = new PositionDocument(manageablePosition);
      _positionMaster.add(positionDocument);
    }
  }

  protected String getTradesJson() throws Exception {
    return WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesJson.txt").toString();
  }

  protected void assertPositionWithNoTrades() {
    final PositionSearchRequest request = new PositionSearchRequest();
    final PositionSearchResult searchResult = _positionMaster.search(request);
    assertNotNull(searchResult);
    final List<PositionDocument> docs = searchResult.getDocuments();
    assertNotNull(docs);
    assertEquals(1, docs.size());
    final ManageablePosition position = docs.get(0).getPosition();
    assertEquals(BigDecimal.TEN, position.getQuantity());
    assertEquals(SECURITY_LINK, position.getSecurityLink());
    assertTrue(position.getTrades().isEmpty());
  }

  protected void assertPositionAndTrades() {
    final PositionSearchRequest request = new PositionSearchRequest();
    final PositionSearchResult searchResult = _positionMaster.search(request);
    assertNotNull(searchResult);
    final List<PositionDocument> docs = searchResult.getDocuments();
    assertNotNull(docs);
    assertEquals(1, docs.size());
    final ManageablePosition position = docs.get(0).getPosition();
    assertEquals(BigDecimal.TEN, position.getQuantity());
    assertEquals(SECURITY_LINK, position.getSecurityLink());

    final List<ManageableTrade> trades = position.getTrades();
    assertEquals(3, trades.size());
    for (final ManageableTrade trade : trades) {
      assertEquals(SECURITY_LINK, trade.getSecurityLink());
      trade.setUniqueId(null);
      trade.setSecurityLink(new ManageableSecurityLink(SEC_ID));
      trade.setParentPositionId(null);
      assertTrue(_trades.contains(trade));
    }
  }

  protected UniqueId addPosition() {
    final ManageableTrade origTrade = new ManageableTrade(BigDecimal.valueOf(50), SEC_ID, LocalDate.parse("2011-12-07"), OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET), COUNTER_PARTY);
    origTrade.setPremium(10.0);
    origTrade.setPremiumCurrency(Currency.USD);
    origTrade.setPremiumDate(LocalDate.parse("2011-12-08"));
    origTrade.setPremiumTime(OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET));

    final ManageablePosition manageablePosition = new ManageablePosition(origTrade.getQuantity(), EQUITY_SECURITY.getExternalIdBundle());
    manageablePosition.addTrade(origTrade);
    final PositionDocument addedPos = _positionMaster.add(new PositionDocument(manageablePosition));
    final UniqueId uid = addedPos.getUniqueId();
    return uid;
  }

  protected String getActualURL(final Response response) {
    return response.getMetadata().getFirst("Location").toString();
  }

}
