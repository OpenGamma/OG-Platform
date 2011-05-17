/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.test.DBTest;

/**
 * Tests QueryConfigDbConfigMasterWorker.
 */
public class QueryConfigDbConfigMasterWorkerSearchTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public QueryConfigDbConfigMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_invalid_searchRequest() {
    ConfigSearchRequest<Identifier> request = new ConfigSearchRequest<Identifier>();
    _cfgMaster.search(request);
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_search_all_documents() {
    ConfigSearchRequest<Object> request = new ConfigSearchRequest<Object>(Object.class);
    
    ConfigSearchResult<Object> test = _cfgMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalConfigs, test.getPaging().getTotalItems());
    
    assertEquals(_totalConfigs, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_typed_documents() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalIdentifiers, test.getPaging().getTotalItems());
    
    assertEquals(_totalIdentifiers, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert101(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setPagingRequest(PagingRequest.of(1, 2));
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalIdentifiers, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setPagingRequest(PagingRequest.of(2, 2));
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalIdentifiers, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setName("FooBar");
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setName("TestConfig102");
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setName("TESTConfig102");
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setName("TestConfig1*");
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
    assert101(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setName("TESTConfig1*");
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
    assert101(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_configIds_none() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setConfigIds(new ArrayList<ObjectIdentifier>());
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_configIds() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.addConfigId(ObjectIdentifier.of("DbCfg", "101"));
    request.addConfigId(ObjectIdentifier.of("DbCfg", "201"));
    request.addConfigId(ObjectIdentifier.of("DbCfg", "9999"));
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert101(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_configIds_badSchemeValidOid() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.addConfigId(ObjectIdentifier.of("Rubbish", "102"));
    _cfgMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1aInstant.minusSeconds(5)));
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1cInstant.plusSeconds(5)));
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert201(test.getDocuments().get(0));  // old version
    assert102(test.getDocuments().get(1));
    assert101(test.getDocuments().get(2));
  }

  @Test
  public void test_search_versionAsOf_above() {
    ConfigSearchRequest<Identifier> request = createIdentifierSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    ConfigSearchResult<Identifier> test = _cfgMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert202(test.getDocuments().get(0));  // new version
    assert102(test.getDocuments().get(1));
    assert101(test.getDocuments().get(2));
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_cfgMaster.getClass().getSimpleName() + "[DbCfg]", _cfgMaster.toString());
  }
  
  private ConfigSearchRequest<Identifier> createIdentifierSearchRequest() {
    return new ConfigSearchRequest<Identifier>(Identifier.class);
  }

}
