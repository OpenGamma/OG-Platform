/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security.server;

import static com.opengamma.engine.security.server.SecurityMasterServiceNames.DEFAULT_SECURITYMASTER_NAME;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_ALLSECURITYTYPES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITIES;
import static com.opengamma.engine.security.server.SecurityMasterServiceNames.SECURITYMASTER_SECURITY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgFormatter;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.MockSecurityMaster;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

public class RESTMethodTest {
  
  private final SecurityMasterService _securityMasterService = new SecurityMasterService();
  private UniqueIdentifier _uid1;
  //private UniqueIdentifier _uid2;
  
  protected SecurityMasterService getSecurityMasterService () {
    return _securityMasterService;
  }
  
  protected SecurityMasterResource getSecurityMasterResource () {
    return getSecurityMasterService ().findSecurityMaster (DEFAULT_SECURITYMASTER_NAME);
  }
  
  @Before
  public void configureService () {
    MockSecurityMaster secMaster = new MockSecurityMaster();
    Identifier secId1 = new Identifier(new IdentificationScheme("d1"), "v1");
    Identifier secId2 = new Identifier(new IdentificationScheme("d2"), "v2");
    DefaultSecurity sec1 = new DefaultSecurity("t1");
    sec1.setIdentifiers (new IdentifierBundle(secId1));
    secMaster.addSecurity(sec1);
    DefaultSecurity sec2 = new DefaultSecurity("t2");
    sec2.setIdentifiers (new IdentifierBundle(secId2));
    secMaster.addSecurity(sec2);
    getSecurityMasterService ().setSecurityMaster (secMaster);
    _uid1 = sec1.getUniqueIdentifier();
    //_uid2 = sec2.getUniqueIdentifier();
  }
  
  @Test
  public void testFindSecurityMaster () {
    assertNull (getSecurityMasterService ().findSecurityMaster ("woot"));
    assertNotNull (getSecurityMasterResource ());
  }
  
  private <T> List<T> assertIsList (final Class<T> clazz, final FudgeFieldContainer msg) {
    if (msg.getNumFields () == 0) {
      return Collections.emptyList ();
    }
    final List<T> list = new ArrayList<T> (msg.getNumFields ());
    for (FudgeField f : msg) {
      assertEquals (null, f.getName ());
      assertTrue ((f.getOrdinal () == null) || (f.getOrdinal () == 1));
      T value = msg.getFieldValue (clazz, f);
      assertNotNull (value);
      list.add (value);
    }
    return list;
  }
  
  @Test
  public void testGetSecurityByIdentifier() {
    final FudgeMsgEnvelope fme = getSecurityMasterResource().getSecurity(_uid1.toString());
    assertNotNull(fme);
    final FudgeFieldContainer msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    final FudgeFieldContainer security = msg.getFieldValue(FudgeFieldContainer.class, msg.getByName(SECURITYMASTER_SECURITY));
    assertNotNull(security);
  }

  @Test
  public void testGetSecurityByBundle() {
    final FudgeMsgEnvelope fme = getSecurityMasterResource().getSecurity(Arrays.asList("d1::v1"));
    assertNotNull(fme);
    final FudgeFieldContainer msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    final FudgeFieldContainer security = msg.getFieldValue(FudgeFieldContainer.class, msg.getByName(SECURITYMASTER_SECURITY));
    assertNotNull(security);
  }

  @Test
  public void testGetSecurities() {
    final FudgeMsgEnvelope fme = getSecurityMasterResource().getSecurities(Arrays.asList("d1::v1", "d2::v2"));
    assertNotNull(fme);
    final FudgeFieldContainer msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    final FudgeFieldContainer securities = msg.getFieldValue(FudgeFieldContainer.class, msg.getByName(SECURITYMASTER_SECURITIES));
    assertNotNull(securities);
    assertEquals(2, securities.getNumFields());
  }

  @Test
  public void testGetAllSecurityTypes() {
    final FudgeMsgEnvelope fme = getSecurityMasterResource().getAllSecurityTypes();
    assertNotNull(fme);
    final FudgeFieldContainer msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    List<String> types = assertIsList(String.class, msg.getFieldValue(FudgeFieldContainer.class, msg.getByName(SECURITYMASTER_ALLSECURITYTYPES)));
    assertEquals(2, types.size());
    assertTrue(types.contains("t1"));
    assertTrue(types.contains("t2"));
  }
  
}