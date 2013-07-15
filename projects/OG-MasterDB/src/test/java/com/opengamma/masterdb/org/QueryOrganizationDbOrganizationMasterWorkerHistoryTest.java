/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.master.orgs.OrganizationHistoryRequest;
import com.opengamma.master.orgs.OrganizationHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryOrganizationDbOrganizationMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryOrganizationDbOrganizationMasterWorkerHistoryTest extends AbstractDbOrganizationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryOrganizationDbOrganizationMasterWorkerHistoryTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryOrganizationDbOrganizationMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }


  @Test
  public void test_history_documents() {
    ObjectId oid = ObjectId.of("DbOrg", "201");
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documentCountWhenMultipleOrganizations() {
    ObjectId oid = ObjectId.of("DbOrg", "102");
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }


  @Test
  public void test_history_noInstants() {
    ObjectId oid = ObjectId.of("DbOrg", "201");
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }


  @Test
  public void test_history_noInstants_pageOne() {
    ObjectId oid = ObjectId.of("DbOrg", "201");
    PagingRequest pr = PagingRequest.ofPage(1, 1);
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    request.setPagingRequest(pr);
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    ObjectId oid = ObjectId.of("DbOrg", "201");
    PagingRequest pr = PagingRequest.ofPage(2, 1);
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    request.setPagingRequest(pr);
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }


  @Test
  public void test_history_versionsFrom_preFirst() {
    ObjectId oid = ObjectId.of("DbOrg", "201");
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    ObjectId oid = ObjectId.of("DbOrg", "201");
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    ObjectId oid = ObjectId.of("DbOrg", "201");
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }


  @Test
  public void test_history_versionsTo_preFirst() {
    ObjectId oid = ObjectId.of("DbOrg", "201");
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertEquals(0, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    ObjectId oid = ObjectId.of("DbOrg", "201");
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    ObjectId oid = ObjectId.of("DbOrg", "201");
    OrganizationHistoryRequest request = new OrganizationHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    OrganizationHistoryResult test = _orgMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

}
