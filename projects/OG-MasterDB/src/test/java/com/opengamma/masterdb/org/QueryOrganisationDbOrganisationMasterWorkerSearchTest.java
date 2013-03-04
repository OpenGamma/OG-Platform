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

import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.orgs.OrganisationSearchRequest;
import com.opengamma.master.orgs.OrganisationSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;

/**
 * Tests QueryOrganisationDbOrganisationMasterWorker.
 */
public class QueryOrganisationDbOrganisationMasterWorkerSearchTest extends AbstractDbOrganisationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryOrganisationDbOrganisationMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryOrganisationDbOrganisationMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  
  @Test
  public void test_searchOrganisations_documents() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalOrganisations, test.getPaging().getTotalItems());

    assertEquals(_totalOrganisations, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  
  @Test
  public void test_search_pageOne() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(1, 2));
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalOrganisations, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(2, 2));
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(3, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalOrganisations, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  
  @Test
  public void test_search_name_noMatch() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setObligorShortName("FooBar");
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setObligorShortName("TestOrganisation102");
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setObligorShortName("TESTOrganisation102");
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setObligorShortName("TestOrganisation1*");
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setObligorShortName("TESTOrganisation1*");
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  
  @Test
  public void test_search_red_code() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setObligorREDCode("RED_code_201");
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  
  @Test
  public void test_search_tickerNoMatch() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setObligorTicker("NO_SUCH_TICKER");
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_tickerFound() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setObligorTicker("ticker_102");
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }  

  
  @Test
  public void test_search_organisationIds_none() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setOrganisationObjectIds(new ArrayList<ObjectId>());
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_organisationIds() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.addOrganisationObjectId(ObjectId.of("DbOrg", "101"));
    request.addOrganisationObjectId(ObjectId.of("DbOrg", "201"));
    request.addOrganisationObjectId(ObjectId.of("DbOrg", "9999"));
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_organisationIds_badSchemeValidOid() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.addOrganisationObjectId(ObjectId.of("Rubbish", "120"));
    _orgMaster.search(request);
  }

  
  @Test
  public void test_search_versionAsOf_below() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    OrganisationSearchRequest request = new OrganisationSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    OrganisationSearchResult test = _orgMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

}
