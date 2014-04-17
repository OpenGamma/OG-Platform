/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests querying.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryDbLegalEntityBeanMasterTest extends AbstractDbLegalEntityBeanMasterTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryDbLegalEntityBeanMasterTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryDbLegalEntityBeanMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getLegalEntity_nullUID() {
    _lenMaster.get((UniqueId) null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getLegalEntity_versioned_notFoundId() {
    UniqueId uniqueId = UniqueId.of("DbLen", "0", "0");
    _lenMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getLegalEntity_versioned_notFoundVersion() {
    UniqueId uniqueId = UniqueId.of("DbLen", "101", "1");
    _lenMaster.get(uniqueId);
  }

  @Test
  public void test_getLegalEntity_versioned_oneLegalEntityKey() {
    UniqueId uniqueId = UniqueId.of("DbLen", "101", "0");
    LegalEntityDocument test = _lenMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getLegalEntity_versioned_twoLegalEntityKeys() {
    UniqueId uniqueId = UniqueId.of("DbLen", "102", "0");
    LegalEntityDocument test = _lenMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getLegalEntity_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbLen", "201", "0");
    LegalEntityDocument test = _lenMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getLegalEntity_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbLen", "201", "1");
    LegalEntityDocument test = _lenMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getLegalEntity_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbLen", "0");
    _lenMaster.get(uniqueId);
  }

  @Test
  public void test_getLegalEntity_unversioned() {
    UniqueId oid = UniqueId.of("DbLen", "201");
    LegalEntityDocument test = _lenMaster.get(oid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test
  public void test_searchSecurities_documents() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    LegalEntitySearchResult test = _lenMaster.search(request);

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
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setPagingRequest(pr);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    PagingRequest pr = PagingRequest.ofPage(2, 2);
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setPagingRequest(pr);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_pageAtEnd() {
    PagingRequest pr = PagingRequest.ofIndex(3, 2);
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setPagingRequest(pr);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("B");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_identifier_case() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("hi");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("FooBar");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier_wildcard() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("H*");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_wildcardCase() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("h*");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_scheme() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("A");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_scheme_case() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("gh");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_scheme_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("FooBar");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_scheme_wildcard() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("G*");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_scheme_wildcardCase() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("g*");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_name_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("FooBar");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("TestLegalEntity102");
    LegalEntitySearchResult test = _lenMaster.search(request);
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("TESTLegalEntity102");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("TestLegalEntity1*");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("TESTLegalEntity1*");
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_type() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    LegalEntityDocument doc0 = test.getDocuments().get(0);
    LegalEntityDocument doc1 = test.getDocuments().get(1);
    LegalEntityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbLen", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "201", "1"), doc2.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_securityIds_none() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setObjectIds(new ArrayList<ObjectId>());
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_securityIds() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addObjectId(ObjectId.of("DbLen", "101"));
    request.addObjectId(ObjectId.of("DbLen", "201"));
    request.addObjectId(ObjectId.of("DbLen", "9999"));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_securityIds_badSchemeValidOid() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addObjectId(ObjectId.of("Rubbish", "120"));
    _lenMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ANY);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(_totalSecurities, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_AB() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_CD() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_Any_EF() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_GHI() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_AB_CD() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_EF_GHI() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("E", "F"), ExternalId.of("GH", "HI"));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_Any_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("E", "HI"), ExternalId.of("A", "D"));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_AB() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_CD() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_All_EF() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_GHI() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_AB_CD() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_CD_EF() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_All_AB_CD_EF() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_AB_CD_GHI() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F"), ExternalId.of("A", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None_AB() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_None_CD_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact_AB_CD_EF() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_AB_CD_GHI() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    LegalEntityDocument doc0 = test.getDocuments().get(0);
    LegalEntityDocument doc1 = test.getDocuments().get(1);
    LegalEntityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbLen", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "201", "0"), doc2.getUniqueId());  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    LegalEntityDocument doc0 = test.getDocuments().get(0);
    LegalEntityDocument doc1 = test.getDocuments().get(1);
    LegalEntityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbLen", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "201", "1"), doc2.getUniqueId());  // new version
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    ObjectId oid = ObjectId.of("DbLen", "201");
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documentCountWhenMultipleLegalEntities() {
    ObjectId oid = ObjectId.of("DbLen", "102");
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    ObjectId oid = ObjectId.of("DbLen", "201");
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    ObjectId oid = ObjectId.of("DbLen", "201");
    PagingRequest pr = PagingRequest.ofPage(1, 1);
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setPagingRequest(pr);
    LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    ObjectId oid = ObjectId.of("DbLen", "201");
    PagingRequest pr = PagingRequest.ofPage(2, 1);
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setPagingRequest(pr);
    LegalEntityHistoryResult test = _lenMaster.history(request);

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
    ObjectId oid = ObjectId.of("DbLen", "201");
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    ObjectId oid = ObjectId.of("DbLen", "201");
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    ObjectId oid = ObjectId.of("DbLen", "201");
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    ObjectId oid = ObjectId.of("DbLen", "201");
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(0, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    ObjectId oid = ObjectId.of("DbLen", "201");
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    ObjectId oid = ObjectId.of("DbLen", "201");
    LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

}
