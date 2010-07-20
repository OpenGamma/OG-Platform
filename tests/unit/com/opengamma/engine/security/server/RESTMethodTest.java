/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security.server;

import static com.opengamma.engine.security.server.SecuritySourceServiceNames.DEFAULT_SECURITYSOURCE_NAME;
import static com.opengamma.engine.security.server.SecuritySourceServiceNames.SECURITYSOURCE_SECURITIES;
import static com.opengamma.engine.security.server.SecuritySourceServiceNames.SECURITYSOURCE_SECURITY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgFormatter;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.MockSecuritySource;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

public class RESTMethodTest {
  
  private final SecuritySourceService _securitySourceService = new SecuritySourceService();
  private UniqueIdentifier _uid1;
  //private UniqueIdentifier _uid2;

  protected SecuritySourceService getSecuritySourceService () {
    return _securitySourceService;
  }

  protected SecuritySourceResource getSecuritySourceResource () {
    return getSecuritySourceService().findSecuritySource(DEFAULT_SECURITYSOURCE_NAME);
  }

  @Before
  public void configureService() {
    MockSecuritySource securitySource = new MockSecuritySource();
    Identifier secId1 = new Identifier(new IdentificationScheme("d1"), "v1");
    Identifier secId2 = new Identifier(new IdentificationScheme("d2"), "v2");
    DefaultSecurity sec1 = new DefaultSecurity("t1", new IdentifierBundle(secId1));
    securitySource.addSecurity(sec1);
    DefaultSecurity sec2 = new DefaultSecurity("t2", new IdentifierBundle(secId2));
    securitySource.addSecurity(sec2);
    getSecuritySourceService().setSecuritySource(securitySource);
    _uid1 = sec1.getUniqueIdentifier();
    // _uid2 = sec2.getUniqueIdentifier();
  }

  @Test
  public void testFindSecuritySource() {
    assertNull(getSecuritySourceService().findSecuritySource("woot"));
    assertNotNull(getSecuritySourceResource());
  }

//  private <T> List<T> assertIsList (final Class<T> clazz, final FudgeFieldContainer msg) {
//    if (msg.getNumFields () == 0) {
//      return Collections.emptyList ();
//    }
//    final List<T> list = new ArrayList<T> (msg.getNumFields ());
//    for (FudgeField f : msg) {
//      assertEquals (null, f.getName ());
//      assertTrue ((f.getOrdinal () == null) || (f.getOrdinal () == 1));
//      T value = msg.getFieldValue (clazz, f);
//      assertNotNull (value);
//      list.add (value);
//    }
//    return list;
//  }
  
  @Test
  public void testGetSecurityByIdentifier() {
    final FudgeMsgEnvelope fme = getSecuritySourceResource().getSecurity(_uid1.toString());
    assertNotNull(fme);
    final FudgeFieldContainer msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    final FudgeFieldContainer security = msg.getFieldValue(FudgeFieldContainer.class, msg.getByName(SECURITYSOURCE_SECURITY));
    assertNotNull(security);
  }

  @Test
  public void testGetSecurityByBundle() {
    final FudgeMsgEnvelope fme = getSecuritySourceResource().getSecurity(Arrays.asList("d1::v1"));
    assertNotNull(fme);
    final FudgeFieldContainer msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    final FudgeFieldContainer security = msg.getFieldValue(FudgeFieldContainer.class, msg.getByName(SECURITYSOURCE_SECURITY));
    assertNotNull(security);
  }

  @Test
  public void testGetSecurities() {
    final FudgeMsgEnvelope fme = getSecuritySourceResource().getSecurities(Arrays.asList("d1::v1", "d2::v2"));
    assertNotNull(fme);
    final FudgeFieldContainer msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    final FudgeFieldContainer securities = msg.getFieldValue(FudgeFieldContainer.class, msg.getByName(SECURITYSOURCE_SECURITIES));
    assertNotNull(securities);
    assertEquals(2, securities.getNumFields());
  }

}