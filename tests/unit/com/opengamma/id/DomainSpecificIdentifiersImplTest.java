/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import com.opengamma.fudge.FudgeFieldContainer;

/**
 * 
 *
 * @author kirk
 */
public class DomainSpecificIdentifiersImplTest {
  private DomainSpecificIdentifier _id11 = new DomainSpecificIdentifier(new IdentificationDomain("D1"), "V1");
  private DomainSpecificIdentifier _id21 = new DomainSpecificIdentifier(new IdentificationDomain("D2"), "V1");
  private DomainSpecificIdentifier _id12 = new DomainSpecificIdentifier(new IdentificationDomain("D1"), "V2");
  private DomainSpecificIdentifier _id22 = new DomainSpecificIdentifier(new IdentificationDomain("D2"), "V2");

  @Test
  public void noIdentifiers() {
    DomainSpecificIdentifiersImpl dsi1 = new DomainSpecificIdentifiersImpl();
    DomainSpecificIdentifiersImpl dsi2 = new DomainSpecificIdentifiersImpl();
    
    assertTrue(dsi1.equals(dsi1));
    assertTrue(dsi1.equals(dsi2));
  }
  
  @Test
  public void singleIdentifier() {
    assertTrue(new DomainSpecificIdentifiersImpl(_id11).equals(new DomainSpecificIdentifiersImpl(_id11)));
    assertFalse(new DomainSpecificIdentifiersImpl(_id11).equals(new DomainSpecificIdentifiersImpl(_id21)));
    assertFalse(new DomainSpecificIdentifiersImpl(_id11).equals(new DomainSpecificIdentifiersImpl(_id12)));
    assertFalse(new DomainSpecificIdentifiersImpl(_id11).equals(new DomainSpecificIdentifiersImpl(_id22)));
  }

  @Test
  public void singleIdentifierDifferentConstructors() {
    assertTrue(new DomainSpecificIdentifiersImpl(_id11).equals(new DomainSpecificIdentifiersImpl(Collections.singleton(_id11))));
  }

  @Test
  public void singleVersusMultipleIdentifier() {
    assertFalse(new DomainSpecificIdentifiersImpl(_id11).equals(new DomainSpecificIdentifiersImpl(_id11, _id12)));
    assertFalse(new DomainSpecificIdentifiersImpl(_id11, _id12).equals(new DomainSpecificIdentifiersImpl(_id11)));
  }

  @Test
  public void multipleIdentifier() {
    assertTrue(new DomainSpecificIdentifiersImpl(_id11, _id12).equals(new DomainSpecificIdentifiersImpl(_id11, _id12)));
    assertFalse(new DomainSpecificIdentifiersImpl(_id11, _id22).equals(new DomainSpecificIdentifiersImpl(_id11, _id12)));
    assertFalse(new DomainSpecificIdentifiersImpl(_id21, _id22).equals(new DomainSpecificIdentifiersImpl(_id11, _id12)));
  }
  
  @Test
  public void fudgeEncoding() {
    DomainSpecificIdentifiersImpl input = new DomainSpecificIdentifiersImpl(
        new DomainSpecificIdentifier(new IdentificationDomain("id1"), "value1"),
        new DomainSpecificIdentifier(new IdentificationDomain("id2"), "value2")
      );
    FudgeFieldContainer msg = input.toFudgeMsg();
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    
    DomainSpecificIdentifiersImpl decoded = new DomainSpecificIdentifiersImpl(msg);
    assertEquals(input, decoded);
  }
}
