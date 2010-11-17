/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 * Test InMemorySecurityMaster.
 */
public class InMemorySecurityMasterTest {

  // TODO Move the logical tests from here to the generic SecurityMasterTestCase then we can just extend from that

  private static final UniqueIdentifier OTHER_UID = UniqueIdentifier.of("U", "1");
  private static final Identifier ID1 = Identifier.of("A", "B");
  private static final Identifier ID2 = Identifier.of("A", "C");
  private static final IdentifierBundle BUNDLE1 = IdentifierBundle.of(ID1);
  private static final IdentifierBundle BUNDLE2 = IdentifierBundle.of(ID2);
  private static final ManageableSecurity SEC1 = new ManageableSecurity(UniqueIdentifier.of("Test", "sec1"), "Test 1", "TYPE1", BUNDLE1);
  private static final ManageableSecurity SEC2 = new ManageableSecurity(UniqueIdentifier.of("Test", "sec2"), "Test 2", "TYPE2", BUNDLE2);

  private InMemorySecurityMaster testEmpty;
  private InMemorySecurityMaster testPopulated;
  private SecurityDocument doc1;
  private SecurityDocument doc2;

  @Before
  public void setUp() {
    testEmpty = new InMemorySecurityMaster(new UniqueIdentifierSupplier("Test"));
    testPopulated = new InMemorySecurityMaster(new UniqueIdentifierSupplier("Test"));
    doc1 = new SecurityDocument();
    doc1.setSecurity(SEC1);
    doc1 = testPopulated.add(doc1);
    doc2 = new SecurityDocument();
    doc2.setSecurity(SEC2);
    doc2 = testPopulated.add(doc2);
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemorySecurityMaster(null);
  }

  @Test
  public void test_defaultSupplier() {
    InMemorySecurityMaster master = new InMemorySecurityMaster();
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    SecurityDocument added = master.add(doc);
    assertEquals("MemSec", added.getUniqueId().getScheme());
  }

  @Test
  public void test_alternateSupplier() {
    InMemorySecurityMaster master = new InMemorySecurityMaster(new UniqueIdentifierSupplier("Hello"));
    SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    SecurityDocument added = master.add(doc);
    assertEquals("Hello", added.getUniqueId().getScheme());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_emptyMaster() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    SecuritySearchResult result = testEmpty.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  @Test
  public void test_search_populatedMaster_all() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    List<SecurityDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(doc1));
    assertEquals(true, docs.contains(doc2));
  }

  @Test
  public void test_search_populatedMaster_filterByBundle() {
    SecuritySearchRequest request = new SecuritySearchRequest(BUNDLE1);
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(doc1));
  }

  @Test
  public void test_search_populatedMaster_filterByBundle_both() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(BUNDLE1);
    request.addIdentifierBundle(BUNDLE2);
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    List<SecurityDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(doc1));
    assertEquals(true, docs.contains(doc2));
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_get_emptyMaster() {
    assertNull(testEmpty.get(OTHER_UID));
  }

  @Test
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
  @Test(expected = DataNotFoundException.class)
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
    assertEquals(false, doc1.getVersionFromInstant().equals(updated.getVersionFromInstant()));
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    testEmpty.remove(OTHER_UID);
  }

  @Test
  public void test_remove_populatedMaster() {
    testPopulated.remove(doc1.getUniqueId());
    SecuritySearchRequest request = new SecuritySearchRequest();
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

}
