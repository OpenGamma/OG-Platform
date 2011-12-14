/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.util.Collection;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.joda.beans.JodaBeanUtils;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.BeforeMethod;

import com.google.common.collect.Maps;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.web.MockUriInfo;

/**
 * 
 */
public abstract class AbstractWebSecurityResourceTestCase {

  protected HistoricalTimeSeriesMaster _htsMaster;
  protected ConfigSource _cfgSource;
  protected SecurityMaster _secMaster;
  protected SecurityLoader _secLoader;
  protected WebSecuritiesResource _webSecuritiesResource;
  protected Map<FinancialSecurity, UniqueId> _sec2UniqueId = Maps.newHashMap();
  protected UriInfo _uriInfo;

  @BeforeMethod
  public void setUp() throws Exception {
    _uriInfo = new MockUriInfo();
    _secMaster = new InMemorySecurityMaster();
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
    
    _htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    _cfgSource = new MasterConfigSource(new InMemoryConfigMaster());
    
    addSecurity(WebSecuritiesResourceTestUtils.getEquitySecurity());
    addSecurity(WebSecuritiesResourceTestUtils.getBondFutureSecurity());
        
    _webSecuritiesResource = new WebSecuritiesResource(_secMaster, _secLoader, _htsMaster, _cfgSource);
    _webSecuritiesResource.setServletContext(new MockServletContext("/web-engine", new FileSystemResourceLoader()));
    _webSecuritiesResource.setUriInfo(_uriInfo);
    
  }

  private void addSecurity(FinancialSecurity security) {
    FinancialSecurity clone = JodaBeanUtils.clone(security);
    SecurityDocument secDoc = _secMaster.add(new SecurityDocument(security));
    _sec2UniqueId.put(clone, secDoc.getUniqueId());
  }

}
