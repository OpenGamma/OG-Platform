/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.ZoneOffset;
import javax.ws.rs.core.UriInfo;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.BeforeMethod;

import com.google.common.collect.Lists;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.financial.security.equity.EquitySecurity;
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
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.web.MockUriInfo;
import com.opengamma.web.WebResourceTestUtils;

/**
 * Test base class for WebPositionResource tests
 */
public abstract class AbstractWebPositionResourceTestCase {
  
  protected static final ExternalId COUNTER_PARTY = ExternalId.of(Counterparty.DEFAULT_SCHEME, "BACS");
  protected static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("+0100");
  protected static final EquitySecurity EQUITY_SECURITY = WebResourceTestUtils.getEquitySecurity();
  protected static final ExternalId SEC_ID = EQUITY_SECURITY.getExternalIdBundle().getExternalId(SecurityUtils.BLOOMBERG_TICKER);
  
  protected SecurityMaster _secMaster;
  protected SecurityLoader _secLoader;
  protected HistoricalTimeSeriesMaster _htsMaster;
  protected ConfigSource _cfgSource;
  protected WebPositionsResource _webPositionsResource;
  protected MockSecuritySource _securitySource;
  protected PositionMaster _positionMaster;
  protected List<ManageableTrade> _trades;
  protected UriInfo _uriInfo;

  @BeforeMethod
  public void setUp() throws Exception {
    _uriInfo = new MockUriInfo();
    _trades = getTrades();
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
    _webPositionsResource.setUriInfo(_uriInfo);
  }

  protected List<ManageableTrade> getTrades() {
    List<ManageableTrade> trades = Lists.newArrayList();
    ManageableTrade trade1 = new ManageableTrade(BigDecimal.valueOf(50), SEC_ID, LocalDate.parse("2011-12-07"), OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET), COUNTER_PARTY);
    trade1.setPremium(10.0);
    trade1.setPremiumCurrency(Currency.USD);
    trade1.setPremiumDate(LocalDate.parse("2011-12-08"));
    trade1.setPremiumTime(OffsetTime.of(LocalTime.of(15, 4), ZONE_OFFSET));
    trades.add(trade1);
    
    ManageableTrade trade2 = new ManageableTrade(BigDecimal.valueOf(60), SEC_ID, LocalDate.parse("2011-12-08"), OffsetTime.of(LocalTime.of(16, 4), ZONE_OFFSET), COUNTER_PARTY);
    trade2.setPremium(20.0);
    trade2.setPremiumCurrency(Currency.USD);
    trade2.setPremiumDate(LocalDate.parse("2011-12-09"));
    trade2.setPremiumTime(OffsetTime.of(LocalTime.of(16, 4), ZONE_OFFSET));
    trades.add(trade2);
    
    ManageableTrade trade3 = new ManageableTrade(BigDecimal.valueOf(70), SEC_ID, LocalDate.parse("2011-12-09"), OffsetTime.of(LocalTime.of(17, 4), ZONE_OFFSET), COUNTER_PARTY);
    trade3.setPremium(30.0);
    trade3.setPremiumCurrency(Currency.USD);
    trade3.setPremiumDate(LocalDate.parse("2011-12-10"));
    trade3.setPremiumTime(OffsetTime.of(LocalTime.of(17, 4), ZONE_OFFSET));
    trades.add(trade3);
    return trades;
  }

  protected void populateSecMaster() {
    SecurityDocument added = _secMaster.add(new SecurityDocument(EQUITY_SECURITY));
    _securitySource.addSecurity(added.getSecurity());
  }
  
  protected void populatePositionMaster() {
    for (ManageableTrade trade : _trades) {
      ManageablePosition manageablePosition = new ManageablePosition(trade.getQuantity(), SEC_ID);
      manageablePosition.addTrade(trade);
      PositionDocument positionDocument = new PositionDocument(manageablePosition);
      _positionMaster.add(positionDocument);
    }
  }
  
  protected String getTradesJson() throws Exception {
    return WebResourceTestUtils.loadJson("com/opengamma/web/position/tradesJson.txt").toString();
  }

}
