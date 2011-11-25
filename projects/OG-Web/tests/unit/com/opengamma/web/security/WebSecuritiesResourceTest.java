/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.joda.beans.JodaBeanUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;

/**
 * Test {@link WebSecuritiesResource}.
 */
public class WebSecuritiesResourceTest {
  
  private SecurityMaster _secMaster;
  
  private SecurityLoader _secLoader;
  
  private WebSecuritiesResource _webSecuritiesResource;
  private Map<FinancialSecurity, UniqueId> _sec2UniqueId = Maps.newHashMap();
  
  @BeforeMethod
  public void setUp() throws Exception {
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
    
    addSecurity(WebSecuritiesResourceTestUtils.getEquitySecurity());
    addSecurity(WebSecuritiesResourceTestUtils.getBondFutureSecurity());
        
    _webSecuritiesResource = new WebSecuritiesResource(_secMaster, _secLoader);
    _webSecuritiesResource.setServletContext(new MockServletContext("/web-engine", new FileSystemResourceLoader()));
  }
  
  private void addSecurity(FinancialSecurity security) {
    FinancialSecurity clone = JodaBeanUtils.clone(security);
    SecurityDocument secDoc = _secMaster.add(new SecurityDocument(security));
    _sec2UniqueId.put(clone, secDoc.getUniqueId());
  }

  @Test
  public void testGetEquitySecurity() throws Exception {
    assertFinancialSecurity(WebSecuritiesResourceTestUtils.getEquitySecurity());
  }
  
  @Test
  public void testGetBondFutureSecurity() throws Exception {
    assertFinancialSecurity(WebSecuritiesResourceTestUtils.getBondFutureSecurity());
  }
  
  private void assertFinancialSecurity(FinancialSecurity finSecurity) throws Exception {
    assertNotNull(finSecurity);
    UniqueId uniqueId = getUniqueId(finSecurity);
    assertNotNull(uniqueId);
    
    WebSecurityResource securityResource = _webSecuritiesResource.findSecurity(uniqueId.toString());
    assertNotNull(securityResource);
    String json = securityResource.getJSON();
    assertNotNull(json);
    JSONObject actualJson = new JSONObject(json); 
    
    JSONObject expectedJson = finSecurity.accept(new ExpectedSecurityJsonProvider());
    assertNotNull(expectedJson);
    assertEquals(expectedJson.toString(), actualJson.toString());
  }

  private UniqueId getUniqueId(FinancialSecurity finSecurity) {
    return _sec2UniqueId.get(finSecurity);
  }

  @Test
  public void testGetMetaDataJSON() throws Exception {
    JSONObject expectedJson = getExceptedMetaDataJson();
    String metaDataJSON = _webSecuritiesResource.getMetaDataJSON();
    assertNotNull(metaDataJSON);
    JSONObject actualJson = new JSONObject(metaDataJSON); 
    assertEquals(expectedJson.toString(), actualJson.toString());
  }

  private JSONObject getExceptedMetaDataJson() throws IOException, JSONException {
    URL jsonResource = getClass().getResource("securitiesMetaDataJson.txt");
    assertNotNull(jsonResource);
    String expectedJsonStr = FileUtils.readFileToString(new File(jsonResource.getPath()));
    JSONObject expectedJson = new JSONObject(expectedJsonStr);
    return expectedJson;
  }
  
}
