/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;

import org.mockito.stubbing.OngoingStubbing;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link SimpleSecurityResolver}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleSecurityResolverTest {

  private static final ObjectId UNKNOWN_OID = ObjectId.of("Unknown", "Unknown");
  private static final UniqueId UNKNOWN_UID = UniqueId.of("Unknown", "Unknown", "Unknown");
  private static final ExternalIdBundle UNKNOWN_BUNDLE = ExternalIdBundle.of(ExternalId.of("Unknown", "Unknown"));
  private final ObjectId _objectId;
  private final ExternalId _securityExternalId;
  private final ExternalIdBundle _intersectingExternalIdBundle;
  private final Security _securityV1;
  private final Security _securityV2;
  private final Instant _securityV2ValidFrom;
  private final Instant _now = Instant.now();
  private final SecuritySource _securitySource;

  @SuppressWarnings({"unchecked", "rawtypes" })
  public SimpleSecurityResolverTest() {
    _securityExternalId = ExternalId.of("Scheme1", "Value1");
    ExternalIdBundle externalIdBundle = ExternalIdBundle.of(_securityExternalId, ExternalId.of("Scheme2", "Value2"));
    _intersectingExternalIdBundle = ExternalIdBundle.of(_securityExternalId, ExternalId.of("Scheme3", "Value3"));

    _objectId = ObjectId.of("Sec", "a");

    _securityV1 = new SimpleSecurity(_objectId.atVersion("1"), externalIdBundle, "Type", "Security V1");
    _securityV2 = new SimpleSecurity(_objectId.atVersion("2"), externalIdBundle, "Type", "Security V2");

    _securityV2ValidFrom = LocalDate.of(2011, 01, 01).atStartOfDay(ZoneOffset.UTC).toInstant();

    _securitySource = mock(SecuritySource.class);

    // By unique ID
    when(_securitySource.get(_securityV1.getUniqueId())).thenReturn(_securityV1);
    when(_securitySource.get(_securityV2.getUniqueId())).thenReturn(_securityV2);
    when(_securitySource.get(UNKNOWN_UID)).thenThrow(new DataNotFoundException(""));

    // By object ID and version-correction
    when(_securitySource.get(_objectId, VersionCorrection.of(_securityV2ValidFrom.minusMillis(1), _now))).thenReturn(_securityV1);
    when(_securitySource.get(_objectId, VersionCorrection.of(_securityV2ValidFrom, _now))).thenReturn(_securityV2);
    when(_securitySource.get(UNKNOWN_OID, VersionCorrection.of(Instant.ofEpochMilli(123), Instant.ofEpochMilli(123)))).thenThrow(new DataNotFoundException(""));

    // By external ID bundle and version-correction
    ((OngoingStubbing) when(_securitySource.get(ExternalIdBundle.of(_securityExternalId), VersionCorrection.of(_securityV2ValidFrom, _now)))).thenReturn(Collections.singleton(_securityV2));
    ((OngoingStubbing) when(_securitySource.get(_intersectingExternalIdBundle, VersionCorrection.of(_securityV2ValidFrom, _now)))).thenReturn(Collections.singleton(_securityV2));
    ((OngoingStubbing) when(_securitySource.get(_intersectingExternalIdBundle, VersionCorrection.of(_securityV2ValidFrom.minusMillis(1), _now)))).thenReturn(Collections.singleton(_securityV1));
    when(_securitySource.get(UNKNOWN_BUNDLE)).thenThrow(new DataNotFoundException(""));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullSecuritySource() {
    new SimpleSecurityResolver((SecuritySource) null, VersionCorrection.LATEST);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullVersionCorrection() {
    new SimpleSecurityResolver(_securitySource, (VersionCorrection) null);
  }

  //-------------------------------------------------------------------------
  @Test
  @SuppressWarnings("deprecation")
  public void testResolveLinkWithObjectId() {
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(_securityV2ValidFrom, _now));
    SecurityLink link = new SimpleSecurityLink(_objectId);
    Security resolvedSecurity = resolver.resolve(link);
    assertEquals(_securityV2, resolvedSecurity);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  @SuppressWarnings("deprecation")
  public void testResolveLinkWithUnknownObjectId() {
    VersionCorrection vc = VersionCorrection.of(Instant.ofEpochMilli(123), Instant.ofEpochMilli(123));
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, vc);
    SecurityLink link = new SimpleSecurityLink(UNKNOWN_OID);
    resolver.resolve(link);
  }

  @Test
  public void testResolveLinkWithExternalIdBundle() {
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(_securityV2ValidFrom, _now));

    SecurityLink link = new SimpleSecurityLink(_securityExternalId);
    Security resolvedSecurity = resolver.resolve(link);
    assertEquals(_securityV2, resolvedSecurity);

    link = new SimpleSecurityLink(ExternalIdBundle.of(_intersectingExternalIdBundle));
    resolvedSecurity = resolver.resolve(link);
    assertEquals(_securityV2, resolvedSecurity);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void testResolveLinkWithUnknownExternalIdBundle() {
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(Instant.ofEpochMilli(123), Instant.ofEpochMilli(123)));
    SecurityLink link = new SimpleSecurityLink(ExternalId.of("Unknown", "Unknown"));
    resolver.resolve(link);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void testResolveEmptyLink() {
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(Instant.ofEpochMilli(123), Instant.ofEpochMilli(123)));
    SecurityLink link = new SimpleSecurityLink();
    resolver.resolve(link);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetSecurityByUniqueId() {
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(_securityV2ValidFrom.minusMillis(1), _now));
    Security security = resolver.getSecurity(_securityV1.getUniqueId());
    assertEquals(_securityV1, security);

    // Should still return security even if not valid for the version-correction of resolver
    resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(_securityV2ValidFrom, _now));
    security = resolver.getSecurity(_securityV1.getUniqueId());
    assertEquals(_securityV1, security);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSecurityByUnknownUniqueId() {
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(_securityV2ValidFrom, _now));
    resolver.getSecurity(UNKNOWN_UID);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetSecurityByObjectId() {
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(_securityV2ValidFrom.minusMillis(1), _now));
    Security security = resolver.getSecurity(_objectId);
    assertEquals(_securityV1, security);

    resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(_securityV2ValidFrom, _now));
    security = resolver.getSecurity(_objectId);
    assertEquals(_securityV2, security);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSecurityByUnknownObjectId() {
    VersionCorrection vc = VersionCorrection.of(Instant.ofEpochMilli(123), Instant.ofEpochMilli(123));
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, vc);
    resolver.getSecurity(UNKNOWN_OID);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetSecurityByExternalIdBundle() {
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(_securityV2ValidFrom.minusMillis(1), _now));
    Security security = resolver.getSecurity(_intersectingExternalIdBundle);
    assertEquals(_securityV1, security);

    resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(_securityV2ValidFrom, _now));
    security = resolver.getSecurity(_intersectingExternalIdBundle);
    assertEquals(_securityV2, security);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSecurityByUnknownExternalIdBundle() {
    SimpleSecurityResolver resolver = new SimpleSecurityResolver(_securitySource, VersionCorrection.of(_securityV2ValidFrom, _now));
    resolver.getSecurity(UNKNOWN_BUNDLE);
  }

}
