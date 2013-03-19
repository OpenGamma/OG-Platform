/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link SecuritySource} based resolvers
 */
@Test(groups = TestGroup.UNIT)
public class SecuritySourceResolverTest {

  private final InMemorySecuritySource SECURITY_SOURCE = new InMemorySecuritySource();
  private final SimpleSecurity SECURITY = new SimpleSecurity("TEST");
  private final UniqueId BAD_ID = UniqueId.of("Bad", "Id");
  private final ExternalId GOOD_ID = ExternalId.of("Good", "Id");

  public SecuritySourceResolverTest() {
    SECURITY.addExternalId(GOOD_ID);
    SECURITY_SOURCE.addSecurity(SECURITY);
  }

  private SecuritySourceResolver resolver() {
    return new SecuritySourceResolver(SECURITY_SOURCE);
  }

  public void object_resolved() {
    assertEquals(resolver().resolveObject(SECURITY.getUniqueId(), VersionCorrection.LATEST), SECURITY);
  }

  public void object_unresolved() {
    assertEquals(resolver().resolveObject(BAD_ID, VersionCorrection.LATEST), null);
  }

  public void identifier_resolved() {
    assertEquals(resolver().resolveExternalId(ExternalIdBundle.of(GOOD_ID), VersionCorrection.LATEST), SECURITY.getUniqueId());
  }

  public void identifier_unresolved() {
    assertEquals(resolver().resolveExternalId(ExternalIdBundle.EMPTY, VersionCorrection.LATEST), null);
  }

  public void identifier_multiple() {
    final Set<ExternalIdBundle> request = new HashSet<ExternalIdBundle>();
    request.add(ExternalIdBundle.of(GOOD_ID));
    request.add(ExternalIdBundle.EMPTY);
    assertEquals(resolver().resolveExternalIds(request, VersionCorrection.LATEST), Collections.singletonMap(ExternalIdBundle.of(GOOD_ID), SECURITY.getUniqueId()));
  }

  public void objectid_resolved() {
    assertEquals(resolver().resolveObjectId(SECURITY.getUniqueId().getObjectId(), VersionCorrection.LATEST), SECURITY.getUniqueId());
  }

  public void objectid_unresolved() {
    assertEquals(resolver().resolveObjectId(BAD_ID.getObjectId(), VersionCorrection.LATEST), null);
  }

  public void objectid_multiple() {
    final Set<ObjectId> request = new HashSet<ObjectId>();
    request.add(SECURITY.getUniqueId().getObjectId());
    request.add(BAD_ID.getObjectId());
    assertEquals(resolver().resolveObjectIds(request, VersionCorrection.LATEST), Collections.singletonMap(SECURITY.getUniqueId().getObjectId(), SECURITY.getUniqueId()));
  }

}
