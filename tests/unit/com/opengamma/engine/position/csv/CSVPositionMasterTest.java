/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.junit.Test;

import com.opengamma.engine.position.Position;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test CSVPositionMaster.
 */
public class CSVPositionMasterTest {

  private static UniqueIdentifier ID = UniqueIdentifier.of("A", "B");

  @Test
  public void parseLineEmpty() {
    assertNull(CSVPositionMaster.parseLine("", ID));
  }

  @Test
  public void parseLineTooShort() {
    assertNull(CSVPositionMaster.parseLine("foo,bar", ID));
  }

  @Test
  public void parseLineOneIdentifierTrim() {
    Position position = CSVPositionMaster.parseLine("    98.4 , KIRK   , MY-ID", ID);
    assertNotNull(position);
    
    assertEquals(ID, position.getUniqueIdentifier());
    
    assertNotNull(position.getQuantity());
    assertEquals(0, new BigDecimal(984).scaleByPowerOfTen(-1).compareTo(position.getQuantity()));
    
    assertNotNull(position.getSecurityKey());
    assertEquals(1, position.getSecurityKey().getIdentifiers().size());
    Identifier id = position.getSecurityKey().getIdentifiers().iterator().next();
    assertNotNull(id);
    assertNotNull(id.getScheme());
    assertEquals("KIRK", id.getScheme().getName());
    assertNotNull(id.getValue());
    assertEquals("MY-ID", id.getValue());
  }

  @Test
  public void parseLineThreeIdentifiers() {
    Position position = CSVPositionMaster.parseLine("98.4,Domain1,Value1,Domain2,Value2,Domain3,Value3", ID);
    assertNotNull(position);
    
    assertNotNull(position.getQuantity());
    assertEquals(0, new BigDecimal(984).scaleByPowerOfTen(-1).compareTo(position.getQuantity()));
    
    assertNotNull(position.getSecurityKey());
    assertEquals(3, position.getSecurityKey().getIdentifiers().size());
    
    for (Identifier id : position.getSecurityKey().getIdentifiers()) {
      assertNotNull(id);
      assertNotNull(id.getScheme());
      assertNotNull(id.getValue());
      assertEquals(id.getScheme().getName().charAt(6), id.getValue().charAt(5));
    }
  }

}
