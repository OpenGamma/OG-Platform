/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Test InMemorySecurityMaster.
 */
public class InMemorySecurityMasterTest {
 
  @Test
  public void empty() {
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    IdentifierBundle secKey = new IdentifierBundle(new Identifier(new IdentificationScheme("d1"), "v1"));
    assertNull(secMaster.getSecurity(secKey));
    Collection<Security> securities = secMaster.getSecurities(secKey);
    assertNotNull(securities);
    assertTrue(securities.isEmpty());
  }
  
  @Test
  public void singleSecurity() {
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    Identifier secId1 = new Identifier(new IdentificationScheme("d1"), "v1");
    Identifier secId2 = new Identifier(new IdentificationScheme("d1"), "v2");
    IdentifierBundle secKey1 = new IdentifierBundle(secId1);
    IdentifierBundle secKey2 = new IdentifierBundle(secId2);
    IdentifierBundle secKey3 = new IdentifierBundle(secId1, secId2);
    
    DefaultSecurity sec = new DefaultSecurity();
    sec.addIdentifier(secId1);
    secMaster.addSecurity(sec);
    
    assertSame(sec, secMaster.getSecurity(secKey1));
    assertNull(secMaster.getSecurity(secKey2));
    assertSame(sec, secMaster.getSecurity(secKey3));
    
    Collection<Security> securities = null;
    securities = secMaster.getSecurities(secKey1);
    assertNotNull(securities);
    assertTrue(securities.contains(sec));
    securities = secMaster.getSecurities(secKey2);
    assertNotNull(securities);
    assertFalse(securities.contains(sec));
    securities = secMaster.getSecurities(secKey3);
    assertNotNull(securities);
    assertTrue(securities.contains(sec));
  }
  
  @Test
  public void multipleSecurities() {
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    Identifier secId1 = new Identifier(new IdentificationScheme("d1"), "v1");
    Identifier secId2 = new Identifier(new IdentificationScheme("d1"), "v2");
    IdentifierBundle secKey1 = new IdentifierBundle(secId1);
    IdentifierBundle secKey2 = new IdentifierBundle(secId2);
    IdentifierBundle secKey3 = new IdentifierBundle(secId1, secId2);
    
    DefaultSecurity sec1 = new DefaultSecurity();
    sec1.addIdentifier(secId1);
    secMaster.addSecurity(sec1);
    
    DefaultSecurity sec2 = new DefaultSecurity();
    sec2.addIdentifier(secId2);
    secMaster.addSecurity(sec2);
    
    assertSame(sec1, secMaster.getSecurity(secKey1));
    assertSame(sec2, secMaster.getSecurity(secKey2));
    assertSame(sec1, secMaster.getSecurity(secKey3));

    Collection<Security> securities = null;
    securities = secMaster.getSecurities(secKey1);
    assertNotNull(securities);
    assertTrue(securities.contains(sec1));
    securities = secMaster.getSecurities(secKey2);
    assertNotNull(securities);
    assertTrue(securities.contains(sec2));
    securities = secMaster.getSecurities(secKey3);
    assertNotNull(securities);
    assertTrue(securities.contains(sec1));
    assertTrue(securities.contains(sec2));
  }

}
