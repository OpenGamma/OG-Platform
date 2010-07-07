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

import java.util.Collection;

import org.junit.Test;

import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.security.AddSecurityRequest;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test InMemorySecurityMaster.
 */
public class InMemorySecurityMasterTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("U", "1");
  private static final Identifier ID1 = Identifier.of("A", "B");
  private static final Identifier ID2 = Identifier.of("A", "C");
  private static final IdentifierBundle BUNDLE1 = IdentifierBundle.of(ID1);
  private static final IdentifierBundle BUNDLE2 = IdentifierBundle.of(ID2);
  private static final IdentifierBundle BUNDLE1AND2 = IdentifierBundle.of(ID1, ID2);

  @Test
  public void test_empty() {
    InMemorySecurityMaster test = new InMemorySecurityMaster();
    assertNull(test.getSecurity(UID));
    
    assertNull(test.getSecurity(BUNDLE1));
    
    Collection<Security> secs = test.getSecurities(BUNDLE1);
    assertNotNull(secs);
    assertEquals(true, secs.isEmpty());
    
    Collection<String> types = test.getAllSecurityTypes();
    assertNotNull(types);
    assertEquals(true, types.isEmpty());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_singleSecurity() {
    InMemorySecurityMaster test = new InMemorySecurityMaster();
    DefaultSecurity sec = new DefaultSecurity("TestType");
    sec.addIdentifier(ID1);
    test.addSecurity(new AddSecurityRequest(sec));
    
    assertSame(sec, test.getSecurity(BUNDLE1));
    assertNull(test.getSecurity(BUNDLE2));
    assertSame(sec, test.getSecurity(BUNDLE1AND2));
    
    Collection<Security> securities = null;
    securities = test.getSecurities(BUNDLE1);
    assertNotNull(securities);
    assertEquals(true, securities.contains(sec));
    securities = test.getSecurities(BUNDLE2);
    assertNotNull(securities);
    assertEquals(false, securities.contains(sec));
    securities = test.getSecurities(BUNDLE1AND2);
    assertNotNull(securities);
    assertEquals(true, securities.contains(sec));
  }

  @Test
  public void test_multipleSecurities() {
    InMemorySecurityMaster test = new InMemorySecurityMaster();
    
    DefaultSecurity sec1 = new DefaultSecurity();
    sec1.addIdentifier(ID1);
    test.addSecurity(new AddSecurityRequest(sec1));
    
    DefaultSecurity sec2 = new DefaultSecurity();
    sec2.addIdentifier(ID2);
    test.addSecurity(new AddSecurityRequest(sec2));
    
    assertSame(sec1, test.getSecurity(BUNDLE1));
    assertSame(sec2, test.getSecurity(BUNDLE2));
    assertSame(sec1, test.getSecurity(BUNDLE1AND2));
    
    Collection<Security> securities = null;
    securities = test.getSecurities(BUNDLE1);
    assertNotNull(securities);
    assertEquals(true, securities.contains(sec1));
    assertEquals(false, securities.contains(sec2));
    securities = test.getSecurities(BUNDLE2);
    assertNotNull(securities);
    assertEquals(false, securities.contains(sec1));
    assertEquals(true, securities.contains(sec2));
    securities = test.getSecurities(BUNDLE1AND2);
    assertNotNull(securities);
    assertEquals(true, securities.contains(sec1));
    assertEquals(true, securities.contains(sec2));
  }

}
