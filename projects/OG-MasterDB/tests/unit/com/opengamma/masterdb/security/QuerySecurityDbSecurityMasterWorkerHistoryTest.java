/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectIdentifier;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.test.DBTest;

/**
 * Tests QuerySecurityDbSecurityMasterWorker.
 */
public class QuerySecurityDbSecurityMasterWorkerHistoryTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorkerHistoryTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public QuerySecurityDbSecurityMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documentCountWhenMultipleSecuritys() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "102");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setPagingRequest(PagingRequest.of(1, 1));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setPagingRequest(PagingRequest.of(2, 1));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsFrom_preFirst() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_secMaster.getClass().getSimpleName() + "[DbSec]", _secMaster.toString());
  }

}
