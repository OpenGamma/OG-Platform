/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.math.BigDecimal;

import org.junit.Test;

import com.opengamma.core.security.MockSecurity;
import com.opengamma.core.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test PositionImpl.
 */
public class PositionImplTest {

  @Test
  public void test_construction_BigDecimal_Identifier() {
    PositionImpl test = new PositionImpl(BigDecimal.ONE, Identifier.of("A", "B"));
    assertEquals(null, test.getUniqueIdentifier());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals("Position[, 1 Bundle[A::B]]", test.toString());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_BigDecimal_Identifier_nullBigDecimal() {
    new PositionImpl(null, Identifier.of("A", "B"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_BigDecimal_Identifier_nullIdentifier() {
    new PositionImpl(BigDecimal.ONE, (Identifier) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_BigDecimal_IdentifierBundle() {
    PositionImpl test = new PositionImpl(BigDecimal.ONE, IdentifierBundle.of(Identifier.of("A", "B")));
    assertEquals(null, test.getUniqueIdentifier());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals("Position[, 1 Bundle[A::B]]", test.toString());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_BigDecimal_IdentifierBundle_nullBigDecimal() {
    new PositionImpl(null, IdentifierBundle.of(Identifier.of("A", "B")));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_BigDecimal_IdentifierBundle_nullIdentifier() {
    new PositionImpl(BigDecimal.ONE, (IdentifierBundle) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_UniqueIdentifier_BigDecimal_Identifier() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertEquals(UniqueIdentifier.of("B", "C"), test.getUniqueIdentifier());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals("Position[B::C, 1 Bundle[A::B]]", test.toString());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Identifier_nullUniqueIdentifier() {
    new PositionImpl(null, BigDecimal.ONE, Identifier.of("A", "B"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Identifier_nullBigDecimal() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), null, Identifier.of("A", "B"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Identifier_nullIdentifier() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, (Identifier) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_UniqueIdentifier_BigDecimal_IdentifierBundle() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, IdentifierBundle.of(Identifier.of("A", "B")));
    assertEquals(UniqueIdentifier.of("B", "C"), test.getUniqueIdentifier());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals("Position[B::C, 1 Bundle[A::B]]", test.toString());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_IdentifierBundle_nullUniqueIdentifier() {
    new PositionImpl(null, BigDecimal.ONE, Identifier.of("A", "B"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_IdentifierBundle_nullBigDecimal() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), null, IdentifierBundle.of(Identifier.of("A", "B")));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_IdentifierBundle_nullIdentifierBundle() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, (IdentifierBundle) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_UniqueIdentifier_BigDecimal_Security() {
    MockSecurity sec = new MockSecurity("A");
    sec.setIdentifiers(IdentifierBundle.of(Identifier.of("A", "B")));
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, sec);
    assertEquals(UniqueIdentifier.of("B", "C"), test.getUniqueIdentifier());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals(true, test.toString().startsWith("Position[B::C, 1"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Security_nullUniqueIdentifier() {
    new PositionImpl(null, BigDecimal.ONE, Identifier.of("A", "B"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Security_nullBigDecimal() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), null, IdentifierBundle.of(Identifier.of("A", "B")));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Security_nullSecurity() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, (Security) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setUniqueIdentifier() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setUniqueIdentifier(UniqueIdentifier.of("B", "D"));
    assertEquals(UniqueIdentifier.of("B", "D"), test.getUniqueIdentifier());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_setUniqueIdentifier_null() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setUniqueIdentifier(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setQuantity() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setQuantity(BigDecimal.ZERO);
    assertSame(BigDecimal.ZERO, test.getQuantity());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_setQuantity_null() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setQuantity(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setSecurityKey() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setSecurityKey(IdentifierBundle.EMPTY);
    assertSame(IdentifierBundle.EMPTY, test.getSecurityKey());
  }

  @Test
  public void test_setSecurityKey_nullAllowed() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setSecurityKey(null);
    assertEquals(null, test.getSecurityKey());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setSecurity() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    Security sec = new MockSecurity("");
    test.setSecurity(sec);
    assertSame(sec, test.getSecurity());
  }

  @Test
  public void test_setSecurity_nullAllowed() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setSecurity(null);
    assertEquals(null, test.getSecurity());
  }

}
