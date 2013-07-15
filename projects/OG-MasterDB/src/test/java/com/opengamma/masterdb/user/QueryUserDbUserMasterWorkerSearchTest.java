/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

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
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryUserDbUserMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryUserDbUserMasterWorkerSearchTest extends AbstractDbUserMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryUserDbUserMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryUserDbUserMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents() {
    UserSearchRequest request = new UserSearchRequest();
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalUsers, test.getPaging().getTotalItems());
    
    assertEquals(_totalUsers, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    UserSearchRequest request = new UserSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(1, 2));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalUsers, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    UserSearchRequest request = new UserSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(2, 2));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(3, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalUsers, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_stringUserId_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.setUserId("FooBar");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_stringUserId() {
    UserSearchRequest request = new UserSearchRequest();
    request.setUserId("Test102");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_stringUserId_case() {
    UserSearchRequest request = new UserSearchRequest();
    request.setUserId("TEST102");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_stringUserId_wildcard() {
    UserSearchRequest request = new UserSearchRequest();
    request.setUserId("Test1*");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_stringUserId_wildcardCase() {
    UserSearchRequest request = new UserSearchRequest();
    request.setUserId("TEST1*");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.setName("FooBar");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    UserSearchRequest request = new UserSearchRequest();
    request.setName("TestUser102");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    UserSearchRequest request = new UserSearchRequest();
    request.setName("TESTUser102");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    UserSearchRequest request = new UserSearchRequest();
    request.setName("TestUser1*");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    UserSearchRequest request = new UserSearchRequest();
    request.setName("TESTUser1*");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_timeZone_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.setTimeZone("FooBar");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_timeZone() {
    UserSearchRequest request = new UserSearchRequest();
    request.setTimeZone("Europe/Paris");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_timeZone_case() {
    UserSearchRequest request = new UserSearchRequest();
    request.setTimeZone("EUROPE/PARIS");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_timeZone_wildcard() {
    UserSearchRequest request = new UserSearchRequest();
    request.setTimeZone("Europe/*");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_timeZone_wildcardCase() {
    UserSearchRequest request = new UserSearchRequest();
    request.setTimeZone("EUROPE/**");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_emailAddress_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.setEmailAddress("FooBar");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_emailAddress() {
    UserSearchRequest request = new UserSearchRequest();
    request.setEmailAddress("email102@email.com");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_emailAddress_case() {
    UserSearchRequest request = new UserSearchRequest();
    request.setEmailAddress("EMAIL102@EMAIL.COM");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_emailAddress_wildcard() {
    UserSearchRequest request = new UserSearchRequest();
    request.setEmailAddress("email1*");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_emailAddress_wildcardCase() {
    UserSearchRequest request = new UserSearchRequest();
    request.setEmailAddress("EMAIL1*");
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_userIds_none() {
    UserSearchRequest request = new UserSearchRequest();
    request.setObjectIds(new ArrayList<ObjectId>());
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_userIds() {
    UserSearchRequest request = new UserSearchRequest();
    request.addObjectId(ObjectId.of("DbUsr", "101"));
    request.addObjectId(ObjectId.of("DbUsr", "201"));
    request.addObjectId(ObjectId.of("DbUsr", "9999"));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_userIds_badSchemeValidOid() {
    UserSearchRequest request = new UserSearchRequest();
    request.addObjectId(ObjectId.of("Rubbish", "120"));
    _usrMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.setExternalIdSearch(new ExternalIdSearch());
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.EXACT);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.setExternalIdSearch(new ExternalIdSearch());
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.setExternalIdSearch(new ExternalIdSearch());
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ANY);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_allMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.setExternalIdSearch(new ExternalIdSearch());
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.NONE);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(_totalUsers, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_AB() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_CD() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_Any_EF() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_GH() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("G", "H"));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("A", "H"));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_AB_CD() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_EF_GH() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalId.of("E", "F"), ExternalId.of("G", "H"));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_Any_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalId.of("E", "H"), ExternalId.of("A", "D"));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_AB() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_CD() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_All_EF() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_GH() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("G", "H"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("A", "H"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_AB_CD() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_CD_EF() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "H")));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_All_AB_CD_EF() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_AB_CD_GH() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("G", "H"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F"), ExternalId.of("A", "H")));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.ALL);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None_AB() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.NONE);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_None_CD_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.NONE);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact_AB_CD_EF() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.EXACT);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_AB_CD_GH() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("G", "H"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.EXACT);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    UserSearchRequest request = new UserSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.getExternalIdSearch().setSearchType(ExternalIdSearchType.EXACT);
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_oneId_AB() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKey(ExternalId.of("A", "B"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_oneId_CD() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKey(ExternalId.of("C", "D"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(3, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//    assert202(test.getDocuments().get(2));
//  }
//
//  @Test
//  public void test_search_oneId_EF() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKey(ExternalId.of("E", "F"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert202(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_oneId_GH() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKey(ExternalId.of("G", "H"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(1, test.getDocuments().size());
//    assert102(test.getDocuments().get(0));
//  }
//
//  @Test
//  public void test_search_oneId_noMatch() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKey(ExternalId.of("A", "H"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(0, test.getDocuments().size());
//  }
//
//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_twoIds_AB_CD() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKeys(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_twoIds_CD_EF() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKeys(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert202(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_twoIds_noMatch() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKeys(ExternalId.of("C", "D"), ExternalId.of("E", "H"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(0, test.getDocuments().size());
//  }
//
//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_threeIds_AB_CD_EF() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKeys(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(1, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//  }
//
//  @Test
//  public void test_search_threeIds_AB_CD_GH() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKeys(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("G", "H"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(1, test.getDocuments().size());
//    assert102(test.getDocuments().get(0));
//  }
//
//  @Test
//  public void test_search_threeIds_noMatch() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKeys(ExternalId.of("C", "D"), ExternalId.of("E", "F"), ExternalId.of("A", "H"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(0, test.getDocuments().size());
//  }
//
//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_ids_AB_or_CD() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKey(ExternalId.of("A", "B"));
//    request.addUserKey(ExternalId.of("C", "D"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(3, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//    assert202(test.getDocuments().get(2));
//  }
//
//  @Test
//  public void test_search_ids_EF_or_GH() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKey(ExternalId.of("E", "F"));
//    request.addUserKey(ExternalId.of("G", "H"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(3, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//    assert202(test.getDocuments().get(2));
//  }
//
//  @Test
//  public void test_search_ids_or_noMatch() {
//    UserSearchRequest request = new UserSearchRequest();
//    request.addUserKey(ExternalId.of("E", "H"));
//    request.addUserKey(ExternalId.of("A", "D"));
//    UserSearchResult test = _worker.search(request);
//    
//    assertEquals(0, test.getDocuments().size());
//  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    UserSearchRequest request = new UserSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    UserSearchRequest request = new UserSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    UserSearchRequest request = new UserSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    UserSearchResult test = _usrMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

}
