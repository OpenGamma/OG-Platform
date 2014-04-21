/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.bean;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests querying.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryDbConventionBeanMasterTest extends AbstractDbConventionBeanMasterTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryDbConventionBeanMasterTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryDbConventionBeanMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getConvention_nullUID() {
    _cnvMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getConvention_versioned_notFoundId() {
    UniqueId uniqueId = UniqueId.of("DbCnv", "0", "0");
    _cnvMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getConvention_versioned_notFoundVersion() {
    UniqueId uniqueId = UniqueId.of("DbCnv", "101", "1");
    _cnvMaster.get(uniqueId);
  }

  @Test
  public void test_getConvention_versioned_oneConventionKey() {
    UniqueId uniqueId = UniqueId.of("DbCnv", "101", "0");
    ConventionDocument test = _cnvMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getConvention_versioned_twoConventionKeys() {
    UniqueId uniqueId = UniqueId.of("DbCnv", "102", "0");
    ConventionDocument test = _cnvMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getConvention_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbCnv", "201", "0");
    ConventionDocument test = _cnvMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getConvention_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbCnv", "201", "1");
    ConventionDocument test = _cnvMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getConvention_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbCnv", "0");
    _cnvMaster.get(uniqueId);
  }

  @Test
  public void test_getConvention_unversioned() {
    UniqueId oid = UniqueId.of("DbCnv", "201");
    ConventionDocument test = _cnvMaster.get(oid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test
  public void test_searchSecurities_documents() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    ConventionSearchResult test = _cnvMaster.search(request);
    
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
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setPagingRequest(pr);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    PagingRequest pr = PagingRequest.ofPage(2, 2);
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setPagingRequest(pr);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_pageAtEnd() {
    PagingRequest pr = PagingRequest.ofIndex(3, 2);
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setPagingRequest(pr);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdValue("B");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }
    
  @Test
  public void test_search_identifier_case() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdValue("hi");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_identifier_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdValue("FooBar");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }
  
  @Test
  public void test_search_identifier_wildcard() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdValue("H*");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_identifier_wildcardCase() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdValue("h*");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_scheme() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdScheme("A");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }
    
  @Test
  public void test_search_scheme_case() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdScheme("gh");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_scheme_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdScheme("FooBar");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }
  
  @Test
  public void test_search_scheme_wildcard() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdScheme("G*");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_scheme_wildcardCase() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdScheme("g*");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_name_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("FooBar");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("TestConvention102");
    ConventionSearchResult test = _cnvMaster.search(request);
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_name_case() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("TESTConvention102");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("TestConvention1*");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("TESTConvention1*");
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_type() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setConventionType(ConventionType.of("MOCK"));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    ConventionDocument doc0 = test.getDocuments().get(0);
    ConventionDocument doc1 = test.getDocuments().get(1);
    ConventionDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbCnv", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "201", "1"), doc2.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_securityIds_none() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setObjectIds(new ArrayList<ObjectId>());
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_securityIds() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addObjectId(ObjectId.of("DbCnv", "101"));
    request.addObjectId(ObjectId.of("DbCnv", "201"));
    request.addObjectId(ObjectId.of("DbCnv", "9999"));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_securityIds_badSchemeValidOid() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addObjectId(ObjectId.of("Rubbish", "120"));
    _cnvMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ANY);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(_totalSecurities, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_AB() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_CD() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_Any_EF() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_GHI() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_AB_CD() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_EF_GHI() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("E", "F"), ExternalId.of("GH", "HI"));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_Any_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("E", "HI"), ExternalId.of("A", "D"));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_AB() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_CD() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_All_EF() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_GHI() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_AB_CD() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_CD_EF() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_All_AB_CD_EF() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_AB_CD_GHI() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F"), ExternalId.of("A", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None_AB() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_None_CD_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact_AB_CD_EF() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_AB_CD_GHI() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    ConventionDocument doc0 = test.getDocuments().get(0);
    ConventionDocument doc1 = test.getDocuments().get(1);
    ConventionDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbCnv", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "201", "0"), doc2.getUniqueId());  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    ConventionSearchResult test = _cnvMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    ConventionDocument doc0 = test.getDocuments().get(0);
    ConventionDocument doc1 = test.getDocuments().get(1);
    ConventionDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbCnv", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "201", "1"), doc2.getUniqueId());  // new version
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    ObjectId oid = ObjectId.of("DbCnv", "201");
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documentCountWhenMultipleConventions() {
    ObjectId oid = ObjectId.of("DbCnv", "102");
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    ObjectId oid = ObjectId.of("DbCnv", "201");
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    ObjectId oid = ObjectId.of("DbCnv", "201");
    PagingRequest pr = PagingRequest.ofPage(1, 1);
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setPagingRequest(pr);
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    ObjectId oid = ObjectId.of("DbCnv", "201");
    PagingRequest pr = PagingRequest.ofPage(2, 1);
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setPagingRequest(pr);
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsFrom_preFirst() {
    ObjectId oid = ObjectId.of("DbCnv", "201");
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    ObjectId oid = ObjectId.of("DbCnv", "201");
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    ObjectId oid = ObjectId.of("DbCnv", "201");
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    ObjectId oid = ObjectId.of("DbCnv", "201");
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    ObjectId oid = ObjectId.of("DbCnv", "201");
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    ObjectId oid = ObjectId.of("DbCnv", "201");
    ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    ConventionHistoryResult test = _cnvMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

}
