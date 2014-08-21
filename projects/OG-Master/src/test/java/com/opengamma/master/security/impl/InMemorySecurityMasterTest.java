/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemorySecurityMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemorySecurityMasterTest {

  // TODO Move the logical tests from here to the generic SecurityMasterTestCase then we can just extend from that

  private static final UniqueId OTHER_UID = UniqueId.of("U", "1");
  private static final ExternalId ID1 = ExternalId.of("A", "B");
  private static final ExternalId ID2 = ExternalId.of("A", "C");
  private static final ExternalIdBundle BUNDLE1 = ExternalIdBundle.of(ID1);
  private static final ExternalIdBundle BUNDLE2 = ExternalIdBundle.of(ID2);
  private static final ManageableSecurity SEC1 = new ManageableSecurity(UniqueId.of("Test", "sec1"), "Test 1", "TYPE1", BUNDLE1);
  private static final ManageableSecurity SEC2 = new ManageableSecurity(UniqueId.of("Test", "sec2"), "Test 2", "TYPE2", BUNDLE2);

  private InMemorySecurityMaster testEmpty;
  private InMemorySecurityMaster testPopulated;
  private SecurityDocument doc1;
  private SecurityDocument doc2;

  @BeforeMethod
  public void setUp() {
    testEmpty = new InMemorySecurityMaster(new ObjectIdSupplier("Test"));
    testPopulated = new InMemorySecurityMaster(new ObjectIdSupplier("Test"));
    doc1 = new SecurityDocument();
    doc1.setSecurity(SEC1);
    doc1 = testPopulated.add(doc1);
    doc2 = new SecurityDocument();
    doc2.setSecurity(SEC2);
    doc2 = testPopulated.add(doc2);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemorySecurityMaster((Supplier<ObjectId>) null);
  }

  public void test_defaultSupplier() {
    InMemorySecurityMaster master = new InMemorySecurityMaster();
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    SecurityDocument added = master.add(doc);
    assertEquals("MemSec", added.getUniqueId().getScheme());
  }

  public void test_alternateSupplier() {
    InMemorySecurityMaster master = new InMemorySecurityMaster(new ObjectIdSupplier("Hello"));
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    SecurityDocument added = master.add(doc);
    assertEquals("Hello", added.getUniqueId().getScheme());
  }

  //-------------------------------------------------------------------------
  public void test_search_emptyMaster() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    SecuritySearchResult result = testEmpty.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_populatedMaster_all() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    List<SecurityDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(doc1));
    assertEquals(true, docs.contains(doc2));
  }

  public void test_search_populatedMaster_filterByBundle() {
    SecuritySearchRequest request = new SecuritySearchRequest(BUNDLE1);
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(doc1));
  }

  public void test_search_populatedMaster_filterByBundle_both() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(BUNDLE1);
    request.addExternalIds(BUNDLE2);
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    List<SecurityDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(doc1));
    assertEquals(true, docs.contains(doc2));
  }

  public void test_search_populatedMaster_filterByName() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("*est 2");
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  public void test_search_populatedMaster_filterByType() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityType("TYPE2");
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }
  
  public void test_search_popluatedMaster_filterByExternalIdValue() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("B");
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc1));
  }
  
  public void test_search_popluatedMaster_filterByExternalIdValue_case() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("b");
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc1));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_emptyMaster() {
    assertNull(testEmpty.get(OTHER_UID));
  }

  public void test_get_populatedMaster() {
    assertSame(doc1, testPopulated.get(doc1.getUniqueId()));
    assertSame(doc2, testPopulated.get(doc2.getUniqueId()));
  }

  //-------------------------------------------------------------------------
  public void test_add_emptyMaster() {
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    SecurityDocument added = testEmpty.add(doc);
    assertNotNull(added.getVersionFromInstant());
    assertNotNull(added.getCorrectionFromInstant());
    assertEquals(added.getVersionFromInstant(), added.getCorrectionFromInstant());
    assertEquals("Test", added.getUniqueId().getScheme());
    assertSame(SEC1, added.getSecurity());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_emptyMaster() {
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    doc.setUniqueId(OTHER_UID);
    testEmpty.update(doc);
  }

  public void test_update_populatedMaster() {
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    doc.setUniqueId(doc1.getUniqueId());
    SecurityDocument updated = testPopulated.update(doc);
    assertEquals(doc1.getUniqueId(), updated.getUniqueId());
    assertNotNull(doc1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    testEmpty.remove(OTHER_UID);
  }

  public void test_remove_populatedMaster() {
    testPopulated.remove(doc1.getUniqueId());
    SecuritySearchRequest request = new SecuritySearchRequest();
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  public void test_replace_adds_uniqueid() {
    // remove UniqueId() from updated doc, search will use ExternalId and UniqueId of replaced version returned
    ManageableSecurity withoutId = SEC1.clone();
    withoutId.setUniqueId(null);
    SecurityDocument tempDoc = new SecurityDocument();
    tempDoc.setSecurity(withoutId);
    List<UniqueId> ids = testPopulated.replaceAllVersions(SEC1.getUniqueId().getObjectId(), Collections.singletonList(tempDoc));
    ArgumentChecker.noNulls(ids, "ids");
  }

}
