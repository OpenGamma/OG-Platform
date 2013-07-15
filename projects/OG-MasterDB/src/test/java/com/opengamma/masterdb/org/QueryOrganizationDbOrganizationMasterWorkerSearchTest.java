/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.OrganizationSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryOrganizationDbOrganizationMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryOrganizationDbOrganizationMasterWorkerSearchTest extends AbstractDbOrganizationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryOrganizationDbOrganizationMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryOrganizationDbOrganizationMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  
  @Test
  public void test_searchOrganizations_documents() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalOrganizations, test.getPaging().getTotalItems());

    assertEquals(_totalOrganizations, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  
  @Test
  public void test_search_pageOne() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(1, 2));
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalOrganizations, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(2, 2));
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(3, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalOrganizations, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  
  @Test
  public void test_search_name_noMatch() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setObligorShortName("FooBar");
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setObligorShortName("TestOrganization102");
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setObligorShortName("TestOrganization102");
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setObligorShortName("TestOrganization1*");
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setObligorShortName("TestOrganization1*");
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  
  @Test
  public void test_search_red_code() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setObligorREDCode("RED_code_201");
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  
  @Test
  public void test_search_tickerNoMatch() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setObligorTicker("NO_SUCH_TICKER");
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_tickerFound() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setObligorTicker("ticker_102");
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }  

  
  @Test
  public void test_search_OrganizationIds_none() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setOrganizationObjectIds(new ArrayList<ObjectId>());
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_OrganizationIds() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.addOrganizationObjectId(ObjectId.of("DbOrg", "101"));
    request.addOrganizationObjectId(ObjectId.of("DbOrg", "201"));
    request.addOrganizationObjectId(ObjectId.of("DbOrg", "9999"));
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_OrganizationIds_badSchemeValidOid() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.addOrganizationObjectId(ObjectId.of("Rubbish", "120"));
    _orgMaster.search(request);
  }

  
  @Test
  public void test_search_versionAsOf_below() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    OrganizationSearchRequest request = new OrganizationSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    OrganizationSearchResult test = _orgMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

}
