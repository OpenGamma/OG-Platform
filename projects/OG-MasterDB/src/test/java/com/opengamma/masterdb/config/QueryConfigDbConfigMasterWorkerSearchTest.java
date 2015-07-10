/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryConfigDbConfigMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryConfigDbConfigMasterWorkerSearchTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryConfigDbConfigMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_search_all_documents() {
    ConfigSearchRequest<Object> request = new ConfigSearchRequest<Object>(Object.class);
    
    ConfigSearchResult<Object> test = _cfgMaster.search(request);
    
    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(_totalConfigs, test.getPaging().getTotalItems());
    
    assertEquals(_totalConfigs, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_typed_documents() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(_totalExternalIds, test.getPaging().getTotalItems());
    
    assertEquals(_totalExternalIds, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert101(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    PagingRequest pr = PagingRequest.ofPage(1, 2);
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setPagingRequest(pr);
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalExternalIds, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    PagingRequest pr = PagingRequest.ofPage(2, 2);
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setPagingRequest(pr);
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalExternalIds, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setName("FooBar");
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setName("TestConfig102");
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setName("TESTConfig102");
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setName("TestConfig1*");
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
    assert101(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setName("TESTConfig1*");
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
    assert101(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_configIds_none() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setConfigIds(new ArrayList<ObjectId>());
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_configIds() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.addConfigId(ObjectId.of("DbCfg", "101"));
    request.addConfigId(ObjectId.of("DbCfg", "201"));
    request.addConfigId(ObjectId.of("DbCfg", "9999"));
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert101(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_configIds_badSchemeValidOid() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.addConfigId(ObjectId.of("Rubbish", "102"));
    _cfgMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1aInstant.minusSeconds(5)));
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1cInstant.plusSeconds(5)));
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert201(test.getDocuments().get(0));  // old version
    assert102(test.getDocuments().get(1));
    assert101(test.getDocuments().get(2));
  }

  @Test
  public void test_search_versionAsOf_above() {
    ConfigSearchRequest<ExternalId> request = createExternalIdSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    ConfigSearchResult<ExternalId> test = _cfgMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert202(test.getDocuments().get(0));  // new version
    assert102(test.getDocuments().get(1));
    assert101(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  private ConfigSearchRequest<ExternalId> createExternalIdSearchRequest() {
    return new ConfigSearchRequest<ExternalId>(ExternalId.class);
  }

}
