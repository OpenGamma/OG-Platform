/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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
import java.util.Collections;

import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiers;
import com.opengamma.id.IdentificationDomain;

/**
 * 
 *
 * @author kirk
 */
public class InMemorySecurityMasterTest {
  
  @Test
  public void empty() {
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    DomainSpecificIdentifiers secKey = new DomainSpecificIdentifiers(new DomainSpecificIdentifier(new IdentificationDomain("d1"), "v1"));
    assertNull(secMaster.getSecurity(secKey));
    Collection<Security> securities = secMaster.getSecurities(secKey);
    assertNotNull(securities);
    assertTrue(securities.isEmpty());
  }
  
  @Test
  public void singleSecurity() {
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    DomainSpecificIdentifier secId1 = new DomainSpecificIdentifier(new IdentificationDomain("d1"), "v1");
    DomainSpecificIdentifier secId2 = new DomainSpecificIdentifier(new IdentificationDomain("d1"), "v2");
    DomainSpecificIdentifiers secKey1 = new DomainSpecificIdentifiers(secId1);
    DomainSpecificIdentifiers secKey2 = new DomainSpecificIdentifiers(secId2);
    DomainSpecificIdentifiers secKey3 = new DomainSpecificIdentifiers(secId1, secId2);
    
    DefaultSecurity sec = new DefaultSecurity();
    sec.setIdentifiers(Collections.singleton(secId1));
    secMaster.add(sec);
    
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
    DomainSpecificIdentifier secId1 = new DomainSpecificIdentifier(new IdentificationDomain("d1"), "v1");
    DomainSpecificIdentifier secId2 = new DomainSpecificIdentifier(new IdentificationDomain("d1"), "v2");
    DomainSpecificIdentifiers secKey1 = new DomainSpecificIdentifiers(secId1);
    DomainSpecificIdentifiers secKey2 = new DomainSpecificIdentifiers(secId2);
    DomainSpecificIdentifiers secKey3 = new DomainSpecificIdentifiers(secId1, secId2);
    
    DefaultSecurity sec1 = new DefaultSecurity();
    sec1.setIdentifiers(Collections.singleton(secId1));
    secMaster.add(sec1);
    
    DefaultSecurity sec2 = new DefaultSecurity();
    sec2.setIdentifiers(Collections.singleton(secId2));
    secMaster.add(sec2);
    
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
