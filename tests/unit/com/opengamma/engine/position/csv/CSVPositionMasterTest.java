/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Iterator;

import org.junit.Test;

import com.opengamma.DomainSpecificIdentifier;
import com.opengamma.engine.position.Position;

/**
 * 
 *
 * @author kirk
 */
public class CSVPositionMasterTest {
  
  @Test
  public void parseLineNull() {
    assertNull(CSVPositionMaster.parseLine(null));
  }

  @Test
  public void parseLineEmpty() {
    assertNull(CSVPositionMaster.parseLine(""));
  }

  @Test
  public void parseLineTooShort() {
    assertNull(CSVPositionMaster.parseLine("foo,bar"));
  }
  
  @Test
  public void parseLineOneIdentifierTrim() {
    Position position = CSVPositionMaster.parseLine("    98.4 , KIRK   , MY-ID");
    assertNotNull(position);
    
    assertNotNull(position.getQuantity());
    assertEquals(0, new BigDecimal(984).scaleByPowerOfTen(-1).compareTo(position.getQuantity()));
    
    assertNotNull(position.getSecurityKey());
    assertEquals(1, position.getSecurityKey().getIdentifiers().size());
    DomainSpecificIdentifier id = position.getSecurityKey().getIdentifiers().iterator().next();
    assertNotNull(id);
    assertNotNull(id.getDomain());
    assertEquals("KIRK", id.getDomain().getDomainName());
    assertNotNull(id.getValue());
    assertEquals("MY-ID", id.getValue());
  }

  @Test
  public void parseLineThreeIdentifiers() {
    Position position = CSVPositionMaster.parseLine("98.4,Domain1,Value1,Domain2,Value2,Domain3,Value3");
    assertNotNull(position);
    
    assertNotNull(position.getQuantity());
    assertEquals(0, new BigDecimal(984).scaleByPowerOfTen(-1).compareTo(position.getQuantity()));
    
    assertNotNull(position.getSecurityKey());
    assertEquals(3, position.getSecurityKey().getIdentifiers().size());
    
    Iterator<DomainSpecificIdentifier> idIter = position.getSecurityKey().getIdentifiers().iterator();
    while(idIter.hasNext()) {
      DomainSpecificIdentifier id = idIter.next();
      assertNotNull(id);
      assertNotNull(id.getDomain());
      assertNotNull(id.getValue());
      assertEquals(id.getDomain().getDomainName().charAt(6), id.getValue().charAt(5));
    }
  }

}
