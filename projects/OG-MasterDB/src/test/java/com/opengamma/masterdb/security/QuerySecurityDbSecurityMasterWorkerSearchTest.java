/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QuerySecurityDbSecurityMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QuerySecurityDbSecurityMasterWorkerSearchTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QuerySecurityDbSecurityMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchSecurities_documents() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(_totalSecurities, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    PagingRequest pr = PagingRequest.ofPage(1, 2);
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(pr);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    PagingRequest pr = PagingRequest.ofPage(2, 2);
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(pr);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_pageAtEnd() {
    PagingRequest pr = PagingRequest.ofIndex(3, 2);
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(pr);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("B");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }
    
  @Test
  public void test_search_identifier_case() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("hi");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_identifier_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("FooBar");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }
  
  @Test
  public void test_search_identifier_wildcard() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("H*");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_identifier_wildcardCase() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("h*");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_scheme() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdScheme("A");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }
    
  @Test
  public void test_search_scheme_case() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdScheme("gh");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_scheme_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdScheme("FooBar");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }
  
  @Test
  public void test_search_scheme_wildcard() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdScheme("G*");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_scheme_wildcardCase() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdScheme("g*");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_name_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("FooBar");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TestSecurity102");
    SecuritySearchResult test = _secMaster.search(request);
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_name_case() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TESTSecurity102");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TestSecurity1*");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TESTSecurity1*");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_type() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityType("EQUITY");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    SecurityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbSec", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "201", "1"), doc2.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_securityIds_none() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setObjectIds(new ArrayList<ObjectId>());
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_securityIds() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addObjectId(ObjectId.of("DbSec", "101"));
    request.addObjectId(ObjectId.of("DbSec", "201"));
    request.addObjectId(ObjectId.of("DbSec", "9999"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_securityIds_badSchemeValidOid() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addObjectId(ObjectId.of("Rubbish", "120"));
    _secMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ANY);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(_totalSecurities, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_AB() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_Any_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_GHI() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_AB_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_EF_GHI() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("E", "F"), ExternalId.of("GH", "HI"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_Any_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("E", "HI"), ExternalId.of("A", "D"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_AB() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_All_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_GHI() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_AB_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_CD_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_All_AB_CD_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_AB_CD_GHI() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F"), ExternalId.of("A", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None_AB() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_None_CD_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact_AB_CD_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_AB_CD_GHI() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    SecurityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbSec", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "201", "0"), doc2.getUniqueId());  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    SecurityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbSec", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "201", "1"), doc2.getUniqueId());  // new version
  }

}
