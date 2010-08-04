/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test MasterSecuritySource.
 */
public class MasterSecuritySourceTest {

  private static final DefaultSecurity SECURITY = new DefaultSecurity("TEST");
  private static final SecurityDocument DOCUMENT = new SecurityDocument();

  static {
    DOCUMENT.setSecurity(SECURITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_nullMaster() {
    new MasterSecuritySource(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getSecurity_UniqueIdentifier() {
    MasterSecuritySource test = new MasterSecuritySource(new Mock());
    assertSame(SECURITY, test.getSecurity(UniqueIdentifier.of("A", "B")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getSecurity_UniqueIdentifier_nullUID() {
    MasterSecuritySource test = new MasterSecuritySource(new Mock());
    test.getSecurity((UniqueIdentifier) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getSecurities_IdentifierBundle() {
    MasterSecuritySource test = new MasterSecuritySource(new Mock());
    assertEquals(Arrays.asList(SECURITY), test.getSecurities(IdentifierBundle.of(Identifier.of("A", "B"))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getSecurities_IdentifierBundle_nullBundle() {
    MasterSecuritySource test = new MasterSecuritySource(new Mock());
    test.getSecurities((IdentifierBundle) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getSecurity_IdentifierBundle() {
    MasterSecuritySource test = new MasterSecuritySource(new Mock());
    assertEquals(SECURITY, test.getSecurity(IdentifierBundle.of(Identifier.of("A", "B"))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getSecurity_IdentifierBundle_nullBundle() {
    MasterSecuritySource test = new MasterSecuritySource(new Mock());
    test.getSecurity((IdentifierBundle) null);
  }

  //-------------------------------------------------------------------------
  static class Mock implements SecurityMaster {
    @Override
    public SecuritySearchResult search(SecuritySearchRequest request) {
      SecuritySearchResult result = new SecuritySearchResult();
      result.addDocument(DOCUMENT);
      return result;
    }
    @Override
    public SecurityDocument get(UniqueIdentifier uid) {
      return DOCUMENT;
    }
    @Override
    public SecurityDocument add(SecurityDocument document) {
      throw new UnsupportedOperationException();
    }
    @Override
    public SecurityDocument update(SecurityDocument document) {
      throw new UnsupportedOperationException();
    }
    @Override
    public void remove(UniqueIdentifier uid) {
      throw new UnsupportedOperationException();
    }
    @Override
    public SecuritySearchHistoricResult searchHistoric(SecuritySearchHistoricRequest request) {
      throw new UnsupportedOperationException();
    }
    @Override
    public SecurityDocument correct(SecurityDocument document) {
      throw new UnsupportedOperationException();
    }
  }

}
