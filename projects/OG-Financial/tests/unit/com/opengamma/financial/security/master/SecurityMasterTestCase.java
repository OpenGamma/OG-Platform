/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

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

  private UniqueIdentifier putSecurity(final ManageableSecurity security) {
    s_logger.debug("putting security = {}", security);
    SecurityDocument document = new SecurityDocument();
    document.setSecurity(security);
    document = _secMaster.add(document);
    assertNotNull(document);
    final UniqueIdentifier uid = document.getSecurityId();
    s_logger.debug("Security {} stored with identifier {}", security.getClass(), uid);
    return uid;
  }

  private UniqueIdentifier updateSecurity(final ManageableSecurity security) {
    SecurityDocument document = new SecurityDocument();
    document.setSecurity(security);
    document.setSecurityId(security.getUniqueIdentifier());
    document = _secMaster.update(document);
    assertNotNull(document);
    final UniqueIdentifier uid = document.getSecurityId();
    s_logger.debug("Security {} updated; new identifier {}", security.getClass(), uid);
    return uid;
  }

  private Security getSecurity(final Collection<IdentifierBundle> identifiers) {
    s_logger.debug("Search for security with identifiers {}", identifiers);
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.getIdentifiers().addAll(identifiers);
    request.setFullDetail(true);
    final SecuritySearchResult result = _secMaster.search(request);
    assertNotNull(result);
    final List<SecurityDocument> documents = result.getDocuments();
    assertNotNull(documents);
    assertEquals(true, documents.size() > 0);
    final SecurityDocument document = documents.get(documents.size() - 1);
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
  
  private void normalizeBondFutureSecurity(final BondFutureSecurity security) {
    final List<BondFutureDeliverable> basket = new ArrayList<BondFutureDeliverable>(security.getBasket());
    Collections.sort(basket, new Comparator<BondFutureDeliverable>() {
      @Override
      public int compare(BondFutureDeliverable o1, BondFutureDeliverable o2) {
        return o1.getIdentifiers().compareTo(o2.getIdentifiers());
      }
    });
    security.setBasket(basket);
  }
  
  /**
   * Shuffles things around so that the equality comparison is valid. E.g. sorts stuff that might (correctly) be in an
   * arbitrary order.
   */
  private void normalizeSecurity (final Security security) {
    assertNotNull(security);
    if (security instanceof BondFutureSecurity) {
      normalizeBondFutureSecurity ((BondFutureSecurity)security);
    }
  }

  @Override
  protected <T extends ManageableSecurity> void testSecurity(final Class<T> securityClass, final T security) {
    normalizeSecurity (security);
    s_logger.debug("Testing {} instance {}", securityClass, security.hashCode());
    final UniqueIdentifier uid = putSecurity(security);
    assertNotNull(uid);
    s_logger.debug("UID = {}", uid);
    Security sec;
    // retrieve by unique identifier
    sec = getSecurity(uid);
    normalizeSecurity(sec);
    assertEquals(security, sec);
    IdentifierBundle bundle = null;
    if (security.getIdentifiers().size() > 0) {
      final Iterator<Identifier> iterator = security.getIdentifiers().iterator();
      bundle = IdentifierBundle.EMPTY;
      // retrieve with one identifier
      bundle = bundle.withIdentifier(iterator.next());
      sec = getSecurity(Collections.singleton(bundle));
      normalizeSecurity(sec);
      assertEquals(security, sec);
      // retrieve with one valid and one incorrect identifier
      sec = getSecurity(Arrays.asList(bundle, IdentifierBundle.of(Identifier.of("FOO", "BAR"))));
      normalizeSecurity(sec);
      assertEquals(security, sec);
      // retrieve with exact bundle
      sec = getSecurity(Collections.singleton(security.getIdentifiers()));
      normalizeSecurity(sec);
      assertEquals(security, sec);
    }
    final String originalName = security.getName ();
    final String newName = "UPDATED " + originalName;
    security.setName(newName);
    final UniqueIdentifier newuid = updateSecurity (security);
    assertNotNull(newuid);
    s_logger.debug("New UID = {}", newuid);
    assertFalse(uid.equals(newuid));
    // retrieve with original uid - gets original
    sec = getSecurity(uid);
    assertNotNull(sec);
    assertEquals(originalName, sec.getName());
    // retrieve with new uid - gets updated
    sec = getSecurity(newuid);
    assertNotNull(sec);
    assertEquals(newName, sec.getName());
    // retrieve with a "latest" uid - gets updated
    sec = getSecurity(uid.toLatest());
    assertNotNull(sec);
    assertEquals(newName, sec.getName());
    // retrieving by the earlier bundle - gets updated
    if (bundle != null) {
      sec = getSecurity(Collections.singleton(bundle));
      assertNotNull(sec);
      assertEquals(newName, sec.getName());
    }
    // TODO: could extend this with delete and correction operations etc ...
  }

}
