/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QuerySecurityDbSecurityMasterWorker.
 */
public class QuerySecurityDbSecurityMasterWorkerHistoryTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorkerHistoryTest.class);

  public QuerySecurityDbSecurityMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documentCountWhenMultipleSecuritys() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "102");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
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
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(1, 1));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(2, 1));
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
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
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
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
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
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
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
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    SecurityHistoryResult test = _secMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
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
