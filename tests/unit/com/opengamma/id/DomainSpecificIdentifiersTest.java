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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.junit.Test;


/**
 * 
 *
 * @author kirk
 */
public class DomainSpecificIdentifiersTest {
  private DomainSpecificIdentifier _id11 = new DomainSpecificIdentifier(new IdentificationDomain("D1"), "V1");
  private DomainSpecificIdentifier _id21 = new DomainSpecificIdentifier(new IdentificationDomain("D2"), "V1");
  private DomainSpecificIdentifier _id12 = new DomainSpecificIdentifier(new IdentificationDomain("D1"), "V2");
  private DomainSpecificIdentifier _id22 = new DomainSpecificIdentifier(new IdentificationDomain("D2"), "V2");

  @Test
  public void noIdentifiers() {
    DomainSpecificIdentifiers dsi1 = new DomainSpecificIdentifiers();
    DomainSpecificIdentifiers dsi2 = new DomainSpecificIdentifiers();
    
    assertTrue(dsi1.equals(dsi1));
    assertTrue(dsi1.equals(dsi2));
  }
  
  @Test
  public void singleIdentifier() {
    assertTrue(new DomainSpecificIdentifiers(_id11).equals(new DomainSpecificIdentifiers(_id11)));
    assertFalse(new DomainSpecificIdentifiers(_id11).equals(new DomainSpecificIdentifiers(_id21)));
    assertFalse(new DomainSpecificIdentifiers(_id11).equals(new DomainSpecificIdentifiers(_id12)));
    assertFalse(new DomainSpecificIdentifiers(_id11).equals(new DomainSpecificIdentifiers(_id22)));
  }

  @Test
  public void singleIdentifierDifferentConstructors() {
    assertTrue(new DomainSpecificIdentifiers(_id11).equals(new DomainSpecificIdentifiers(Collections.singleton(_id11))));
  }

  @Test
  public void singleVersusMultipleIdentifier() {
    assertFalse(new DomainSpecificIdentifiers(_id11).equals(new DomainSpecificIdentifiers(_id11, _id12)));
    assertFalse(new DomainSpecificIdentifiers(_id11, _id12).equals(new DomainSpecificIdentifiers(_id11)));
  }

  @Test
  public void multipleIdentifier() {
    assertTrue(new DomainSpecificIdentifiers(_id11, _id12).equals(new DomainSpecificIdentifiers(_id11, _id12)));
    assertFalse(new DomainSpecificIdentifiers(_id11, _id22).equals(new DomainSpecificIdentifiers(_id11, _id12)));
    assertFalse(new DomainSpecificIdentifiers(_id21, _id22).equals(new DomainSpecificIdentifiers(_id11, _id12)));
  }
  
  @Test
  public void fudgeEncoding() {
    DomainSpecificIdentifiers input = new DomainSpecificIdentifiers(
        new DomainSpecificIdentifier(new IdentificationDomain("id1"), "value1"),
        new DomainSpecificIdentifier(new IdentificationDomain("id2"), "value2")
      );
    FudgeFieldContainer msg = input.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    
    DomainSpecificIdentifiers decoded = new DomainSpecificIdentifiers(msg);
    assertEquals(input, decoded);
  }
}
