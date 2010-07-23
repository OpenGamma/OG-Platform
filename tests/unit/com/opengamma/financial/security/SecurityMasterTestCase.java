/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.security.Security;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Generic TestCase for a SecurityMaster implementation. Either inherit from it, or
 * delegate through the SecurityMasterTestCaseMethods interface.
 */
public class SecurityMasterTestCase extends SecurityTestCase {

  private static final Logger s_logger = LoggerFactory.getLogger(SecurityMasterTestCase.class);

  private final SecurityMaster _secMaster;

  public SecurityMasterTestCase(final SecurityMaster secMaster) {
    _secMaster = secMaster;
  }

  private UniqueIdentifier putSecurity(final Security security) {
    final SecurityDocument document = _secMaster.add(new SecurityDocument(security));
    assertNotNull(document);
    final UniqueIdentifier uid = document.getUniqueIdentifier();
    s_logger.debug("Security {} stored with identifier {}", security.getClass(), uid);
    return uid;
  }

  private Security getSecurity(final IdentifierBundle identifiers) {
    s_logger.debug("Search for security with identifiers {}", identifiers);
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentifiers(identifiers);
    final SecuritySearchResult result = _secMaster.search(request);
    assertNotNull(result);
    final List<SecurityDocument> documents = result.getDocuments();
    assertNotNull(documents);
    assertEquals(1, documents.size());
    final SecurityDocument document = documents.get(0);
    assertNotNull(document);
    final Security security = document.getSecurity();
    assertNotNull(security);
    return security;
  }

  private Security getSecurity(final UniqueIdentifier uniqueIdentifier) {
    s_logger.debug("Search for security with identifier {}", uniqueIdentifier);
    final SecurityDocument document = _secMaster.get(uniqueIdentifier);
    assertNotNull(document);
    final Security security = document.getSecurity();
    assertNotNull(security);
    return security;
  }

  private void retrieveByUniqueIdentifier(final Security expected, final UniqueIdentifier uid) {
    final Security security = getSecurity(uid);
    assertEquals(expected, security);
  }

  protected <T extends Security> void testSecurity(final Class<T> securityClass, final T security) {
    s_logger.debug("Testing {} instance {}", securityClass, security.hashCode());
    final UniqueIdentifier uid = putSecurity(security);
    assertNotNull(uid);
    retrieveByUniqueIdentifier(security, uid);
    // TODO retrieve by single identifier in a bundle
    // TODO retrieve by pair of valid identifiers in a bundle
    // TODO retrieve with one valid, one invalid identifier in a bundle
    // TODO permute randomly and update
    // TODO retrieve with the original unique identifier (should return original)
    // TODO retrieve with the updated unique identifier (should return updated)
    // TODO retrieve with the original unique identifier marked as toLatest (should return updated)
    // TODO retrieve by single identifier in a bundle (should return updated)
    // TODO retrieve by pair of valid identifiers in a bundle (should return updated)
    // TODO retrieve with one valid, one invalid identifier in a bundle (should return updated)
  }

}
