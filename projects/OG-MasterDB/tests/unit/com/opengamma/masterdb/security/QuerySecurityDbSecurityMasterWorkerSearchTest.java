/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.test.DBTest;

/**
 * Tests QuerySecurityDbSecurityMasterWorker.
 */
public class QuerySecurityDbSecurityMasterWorkerSearchTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public QuerySecurityDbSecurityMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchSecurities_documents() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(_totalSecurities, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(PagingRequest.of(1, 2));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(PagingRequest.of(2, 2));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_identifier() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentifierValue("B");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }
    
  @Test
  public void test_search_identifier_case() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentifierValue("hi");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_identifier_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentifierValue("FooBar");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }
  
  @Test
  public void test_search_identifier_wildcard() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentifierValue("H*");
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }
  
  @Test
  public void test_search_identifier_wildcardCase() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentifierValue("h*");
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
    assertEquals(UniqueIdentifier.of("DbSec", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc2.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_securityIds_none() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityIds(new ArrayList<ObjectIdentifier>());
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_securityIds() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityId(ObjectIdentifier.of("DbSec", "101"));
    request.addSecurityId(ObjectIdentifier.of("DbSec", "201"));
    request.addSecurityId(ObjectIdentifier.of("DbSec", "9999"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_securityIds_badSchemeValidOid() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityId(ObjectIdentifier.of("Rubbish", "120"));
    _secMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityKeys(new IdentifierSearch());
    request.getSecurityKeys().setSearchType(IdentifierSearchType.EXACT);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityKeys(new IdentifierSearch());
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityKeys(new IdentifierSearch());
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ANY);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityKeys(new IdentifierSearch());
    request.getSecurityKeys().setSearchType(IdentifierSearchType.NONE);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(_totalSecurities, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_AB() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("A", "B"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("C", "D"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_Any_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("E", "F"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_GHI() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("G", "HI"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("A", "HI"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_AB_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(Identifier.of("A", "B"), Identifier.of("C", "D"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_EF_GHI() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(Identifier.of("E", "F"), Identifier.of("G", "HI"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_Any_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(Identifier.of("E", "HI"), Identifier.of("A", "D"));
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_AB() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("A", "B"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("C", "D"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_All_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("E", "F"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_GHI() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("G", "HI"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("A", "HI"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_AB_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(Identifier.of("A", "B"), Identifier.of("C", "D"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_CD_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(Identifier.of("C", "D"), Identifier.of("E", "F"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "HI")));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_All_AB_CD_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_AB_CD_GHI() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("G", "HI"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F"), Identifier.of("A", "HI")));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None_AB() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("A", "B"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.NONE);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_None_CD_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(Identifier.of("C", "D"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.NONE);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact_AB_CD_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.EXACT);
    SecuritySearchResult test = _secMaster.search(request);
    
    System.out.println(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_AB_CD_GHI() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("G", "HI"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.EXACT);
    SecuritySearchResult test = _secMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKeys(Identifier.of("A", "B"), Identifier.of("C", "D"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.EXACT);
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
    assertEquals(UniqueIdentifier.of("DbSec", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueIdentifier.of("DbSec", "201", "0"), doc2.getUniqueId());  // old version
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
    assertEquals(UniqueIdentifier.of("DbSec", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc2.getUniqueId());  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_secMaster.getClass().getSimpleName() + "[DbSec]", _secMaster.toString());
  }

}
