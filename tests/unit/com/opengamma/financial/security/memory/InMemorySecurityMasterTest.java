/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.security.SecurityDocument;
import com.opengamma.financial.security.SecuritySearchRequest;
import com.opengamma.financial.security.SecuritySearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;

/**
 * Test InMemorySecurityMaster.
 */
public class InMemorySecurityMasterTest {

  private static final UniqueIdentifier OTHER_UID = UniqueIdentifier.of("U", "1");
  private static final Identifier ID1 = Identifier.of("A", "B");
  private static final Identifier ID2 = Identifier.of("A", "C");
  private static final IdentifierBundle BUNDLE1 = IdentifierBundle.of(ID1);
  private static final IdentifierBundle BUNDLE2 = IdentifierBundle.of(ID2);
  private static final IdentifierBundle BUNDLE1AND2 = IdentifierBundle.of(ID1, ID2);
  private static final Security SEC1 = new DefaultSecurity("TYPE1", BUNDLE1);
  private static final Security SEC2 = new DefaultSecurity("TYPE2", BUNDLE2);
  private static final SecurityDocument DOC1 = new SecurityDocument(SEC1);
  private static final SecurityDocument DOC2 = new SecurityDocument(SEC2);

  private InMemorySecurityMaster testEmpty;
  private InMemorySecurityMaster testPopulated;
  private SecurityDocument doc1;
  private SecurityDocument doc2;

  @Before
  public void setUp() {
    testEmpty = new InMemorySecurityMaster(new UniqueIdentifierSupplier("Test"));
    testPopulated = new InMemorySecurityMaster(new UniqueIdentifierSupplier("Test"));
    doc1 = testPopulated.add(DOC1);
    doc2 = testPopulated.add(DOC2);
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemorySecurityMaster(null);
  }

  @Test
  public void test_defaultSupplier() {
    InMemorySecurityMaster master = new InMemorySecurityMaster();
    SecurityDocument doc = new SecurityDocument(SEC1);
    SecurityDocument added = master.add(doc);
    assertEquals("Memory", added.getUniqueIdentifier().getScheme());
  }

  @Test
  public void test_alternateSupplier() {
    InMemorySecurityMaster master = new InMemorySecurityMaster(new UniqueIdentifierSupplier("Hello"));
    SecurityDocument doc = new SecurityDocument(SEC1);
    SecurityDocument added = master.add(doc);
    assertEquals("Hello", added.getUniqueIdentifier().getScheme());
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
    assertEquals(2, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(doc1));
    assertEquals(true, result.getDocuments().contains(doc2));
  }

  @Test
  public void test_search_populatedMaster_filterByBundle() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentifiers(BUNDLE1);
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(doc1));
  }

  @Test
  public void test_search_populatedMaster_filterByBundle_both() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentifiers(BUNDLE1AND2);
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    assertEquals(2, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(doc1));
    assertEquals(true, result.getDocuments().contains(doc2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_get_emptyMaster() {
    assertNull(testEmpty.get(OTHER_UID));
  }

  @Test
  public void test_get_populatedMaster() {
    assertSame(doc1, testPopulated.get(doc1.getUniqueIdentifier()));
    assertSame(doc2, testPopulated.get(doc2.getUniqueIdentifier()));
  }

  //-------------------------------------------------------------------------
  public void test_add_emptyMaster() {
    SecurityDocument doc = new SecurityDocument(SEC1);
    SecurityDocument added = testEmpty.add(doc);
    assertNotNull(added.getLastModifiedInstant());
    assertNotNull(added.getValidFromInstant());
    assertEquals(added.getLastModifiedInstant(), added.getValidFromInstant());
    assertEquals("Test", added.getUniqueIdentifier().getScheme());
    assertSame(SEC1, added.getSecurity());
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_update_emptyMaster() {
    SecurityDocument doc = new SecurityDocument(SEC1);
    doc.setUniqueIdentifier(OTHER_UID);
    testEmpty.update(doc);
  }

  public void test_update_populatedMaster() {
    SecurityDocument doc = new SecurityDocument(SEC1);
    doc.setUniqueIdentifier(doc1.getUniqueIdentifier());
    SecurityDocument updated = testPopulated.update(doc);
    assertEquals(doc1.getUniqueIdentifier(), updated.getUniqueIdentifier());
    assertNotNull(doc1.getLastModifiedInstant());
    assertNotNull(updated.getLastModifiedInstant());
    assertEquals(false, doc1.getLastModifiedInstant().equals(updated.getLastModifiedInstant()));
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    testEmpty.remove(OTHER_UID);
  }

  @Test
  public void test_remove_populatedMaster() {
    testPopulated.remove(doc1.getUniqueIdentifier());
    SecuritySearchRequest request = new SecuritySearchRequest();
    SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(doc2));
  }

}
