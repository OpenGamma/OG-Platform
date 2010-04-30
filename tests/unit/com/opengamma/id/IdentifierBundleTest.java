/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.junit.Test;

/**
 * Test IdentifierBundle.
 *
 * @author kirk
 */
public class IdentifierBundleTest {

  private final Identifier _id11 = new Identifier(new IdentificationScheme("D1"), "V1");
  private final Identifier _id21 = new Identifier(new IdentificationScheme("D2"), "V1");
  private final Identifier _id12 = new Identifier(new IdentificationScheme("D1"), "V2");
  private final Identifier _id22 = new Identifier(new IdentificationScheme("D2"), "V2");

  @Test
  public void noIdentifiers() {
    IdentifierBundle dsi1 = new IdentifierBundle();
    IdentifierBundle dsi2 = new IdentifierBundle();
    
    assertTrue(dsi1.equals(dsi1));
    assertTrue(dsi1.equals(dsi2));
  }

  @Test
  public void singleIdentifier() {
    assertTrue(new IdentifierBundle(_id11).equals(new IdentifierBundle(_id11)));
    assertFalse(new IdentifierBundle(_id11).equals(new IdentifierBundle(_id21)));
    assertFalse(new IdentifierBundle(_id11).equals(new IdentifierBundle(_id12)));
    assertFalse(new IdentifierBundle(_id11).equals(new IdentifierBundle(_id22)));
  }

  @Test
  public void singleIdentifierDifferentConstructors() {
    assertTrue(new IdentifierBundle(_id11).equals(new IdentifierBundle(Collections.singleton(_id11))));
  }

  @Test
  public void singleVersusMultipleIdentifier() {
    assertFalse(new IdentifierBundle(_id11).equals(new IdentifierBundle(_id11, _id12)));
    assertFalse(new IdentifierBundle(_id11, _id12).equals(new IdentifierBundle(_id11)));
  }

  @Test
  public void multipleIdentifier() {
    assertTrue(new IdentifierBundle(_id11, _id12).equals(new IdentifierBundle(_id11, _id12)));
    assertFalse(new IdentifierBundle(_id11, _id22).equals(new IdentifierBundle(_id11, _id12)));
    assertFalse(new IdentifierBundle(_id21, _id22).equals(new IdentifierBundle(_id11, _id12)));
  }

  @Test
  public void fudgeEncoding() {
    IdentifierBundle input = new IdentifierBundle(
        new Identifier(new IdentificationScheme("id1"), "value1"),
        new Identifier(new IdentificationScheme("id2"), "value2")
      );
    FudgeFieldContainer msg = input.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    
    IdentifierBundle decoded = IdentifierBundle.fromFudgeMsg(msg);
    assertEquals(input, decoded);
  }
  
  @Test
  public void nullIdentifierConstructor() {
    IdentifierBundle bundle = new IdentifierBundle((Identifier)null);
    assertNotNull(bundle.getIdentifiers());
    assertTrue(bundle.getIdentifiers().isEmpty());
  }

  @Test
  public void emptyIdentifierArrayConstructor() {
    IdentifierBundle bundle = new IdentifierBundle(new Identifier[0]);
    assertNotNull(bundle.getIdentifiers());
    assertTrue(bundle.getIdentifiers().isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void nullIdentifierCollectionConstructor() {
    IdentifierBundle bundle = new IdentifierBundle((List)null);
    assertNotNull(bundle.getIdentifiers());
    assertTrue(bundle.getIdentifiers().isEmpty());
  }
  
  @Test
  public void mapForm() {
    IdentifierBundle input = new IdentifierBundle(_id11, _id22);
    
    assertEquals("V1", input.getIdentifier(new IdentificationScheme("D1")));
    assertEquals("V2", input.getIdentifier(new IdentificationScheme("D2")));
    assertNull(input.getIdentifier(new IdentificationScheme("Kirk Wylie")));
    assertNull(input.getIdentifier(null));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_size() {
    assertEquals(0, new IdentifierBundle().size());
    assertEquals(1, new IdentifierBundle(_id11).size());
    assertEquals(2, new IdentifierBundle(_id11, _id12).size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals_same_empty() {
    IdentifierBundle a1 = new IdentifierBundle();
    IdentifierBundle a2 = new IdentifierBundle();
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  @Test
  public void test_equals_same_nonEmpty() {
    IdentifierBundle a1 = new IdentifierBundle(_id11, _id12);
    IdentifierBundle a2 = new IdentifierBundle(_id11, _id12);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  @Test
  public void test_equals_different() {
    IdentifierBundle a = new IdentifierBundle();
    IdentifierBundle b = new IdentifierBundle(_id11, _id12);
    
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    
    assertEquals(false, b.equals("Rubbish"));
    assertEquals(false, b.equals(null));
  }

  @Test
  public void test_hashCode() {
    IdentifierBundle a = new IdentifierBundle(_id11, _id12);
    IdentifierBundle b = new IdentifierBundle(_id11, _id12);
    
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void test_toString_empty() {
    IdentifierBundle test = new IdentifierBundle();
    assertEquals("Bundle[]", test.toString());
  }

  @Test
  public void test_toString_nonEmpty() {
    IdentifierBundle test = new IdentifierBundle(_id11, _id12);
    assertEquals("Bundle[" + _id11.toString() + ", " + _id12.toString() + "]", test.toString());
  }

}
