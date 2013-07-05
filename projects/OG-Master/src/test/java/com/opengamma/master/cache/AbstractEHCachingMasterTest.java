/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatcher;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;

/**
 * Common properties and methods for testing EHCaching masters. This abstract class declares document variables of
 * generic type without initial values, and provides a populate method which can be called by a test subclass to
 * populate a mock master with the documents, but only after the the subclass initialises the documents with objects
 * of the correct type.
 */
public abstract class AbstractEHCachingMasterTest<M extends AbstractChangeProvidingMaster<D>, D extends AbstractDocument> {

  protected static final String ID_SCHEME = "Test";

  protected static final Instant now = Instant.now();

  // Document A (100, 200, 300)
  protected static final ObjectId A_OID = ObjectId.of(ID_SCHEME, "A");
  protected static final UniqueId A100_UID = UniqueId.of(A_OID, "100");
  protected static final UniqueId A200_UID = UniqueId.of(A_OID, "200");
  protected static final UniqueId A300_UID = UniqueId.of(A_OID, "300");  
  protected D docA100_V1999to2010_Cto2011;
  protected D docA200_V2010to;
  protected D docA300_V1999to2010_C2011to;

  // Document B (200, 400, 500)
  protected static final ObjectId B_OID = ObjectId.of(ID_SCHEME, "B");
  protected static final UniqueId B200_UID = UniqueId.of(B_OID, "200");
  protected static final UniqueId B400_UID = UniqueId.of(B_OID, "400");
  protected static final UniqueId B500_UID = UniqueId.of(B_OID, "500");
  protected D docB200_V2000to2009;
  protected D docB400_V2009to2011;
  protected D docB500_V2011to;

  // Document C (100, 300)
  protected static final ObjectId C_OID = ObjectId.of(ID_SCHEME, "C");
  protected static final UniqueId C100_UID = UniqueId.of(C_OID, "100");
  protected static final UniqueId C300_UID = UniqueId.of(C_OID, "300");
  protected D docC100_Vto2011;
  protected D docC300_V2011to;

  // Document to add
  protected static final ObjectId ADDED_OID = ObjectId.of(ID_SCHEME, "ADDED");
  protected static final UniqueId ADDED_UID = UniqueId.of(ADDED_OID, "1");
  protected D DOC_TO_ADD;
  protected D DOC_ADDED;

  /**
   * Creates a fresh mock master and configures it to respond as though it contains the above documents
   * @return the mock master
   */
  protected AbstractChangeProvidingMaster<D> populateMockMaster(M mockUnderlyingMaster) {

    ChangeManager changeManager = new BasicChangeManager();
    when(mockUnderlyingMaster.changeManager()).thenReturn(changeManager);

    // Set up VersionFrom, VersionTo, CorrectionFrom, CorrectionTo

    // Document A 100: v 1999 to 2010, c to 2011

    docA100_V1999to2010_Cto2011.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(1999,
                                                                                        1,
                                                                                        1,
                                                                                        12,
                                                                                        0,
                                                                                        0,
                                                                                        0),
                                                                                        ZoneOffset.UTC).toInstant());
    docA100_V1999to2010_Cto2011.setVersionToInstant(ZonedDateTime.of(LocalDateTime.of(2010, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    docA100_V1999to2010_Cto2011.setCorrectionToInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document A 200: v 2010 to
    docA200_V2010to.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(2010, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document A 300 (corrects A100): v 1999 to 2010, c 2011 to
    docA300_V1999to2010_C2011to.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(1999, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    docA300_V1999to2010_C2011to.setVersionToInstant(ZonedDateTime.of(LocalDateTime.of(2010, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    docA300_V1999to2010_C2011to.setCorrectionFromInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document B 200: v 2000 to 2009
    docB200_V2000to2009.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(2000, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    docB200_V2000to2009.setVersionToInstant(ZonedDateTime.of(LocalDateTime.of(2009, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document B 400: v 2009 to 2011
    docB400_V2009to2011.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(2009, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    docB400_V2009to2011.setVersionToInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document B 500: v 2011 to
    docB500_V2011to.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document C 100: v to 2011
    docC100_Vto2011.setVersionToInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document C 300: v 2011 to
    docC300_V2011to.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

     // Configure mock master to respond to versioned unique ID gets
    when(mockUnderlyingMaster.get(docA100_V1999to2010_Cto2011.getUniqueId())).thenReturn(docA100_V1999to2010_Cto2011);
    when(mockUnderlyingMaster.get(docA200_V2010to.getUniqueId())).thenReturn(docA200_V2010to);
    when(mockUnderlyingMaster.get(docA300_V1999to2010_C2011to.getUniqueId())).thenReturn(docA300_V1999to2010_C2011to);
    when(mockUnderlyingMaster.get(docB200_V2000to2009.getUniqueId())).thenReturn(docB200_V2000to2009);
    when(mockUnderlyingMaster.get(docB400_V2009to2011.getUniqueId())).thenReturn(docB400_V2009to2011);
    when(mockUnderlyingMaster.get(docB500_V2011to.getUniqueId())).thenReturn(docB500_V2011to);
    when(mockUnderlyingMaster.get(docC100_Vto2011.getUniqueId())).thenReturn(docC100_Vto2011);
    when(mockUnderlyingMaster.get(docC300_V2011to.getUniqueId())).thenReturn(docC300_V2011to);

    // Configure mock master to respond to unversioned unique ID gets (should return latest version)
    when(mockUnderlyingMaster.get(docA100_V1999to2010_Cto2011.getUniqueId().toLatest())).thenReturn(docA200_V2010to);
    when(mockUnderlyingMaster.get(docB200_V2000to2009.getUniqueId().toLatest())).thenReturn(docB500_V2011to);
    when(mockUnderlyingMaster.get(docC100_Vto2011.getUniqueId().toLatest())).thenReturn(docC300_V2011to);

    // Configure mock master to respond to object ID/Version-Correction gets
    when(mockUnderlyingMaster.get(
        eq(A_OID), argThat(new IsValidFor(docA100_V1999to2010_Cto2011)))).thenReturn(docA100_V1999to2010_Cto2011);
    when(mockUnderlyingMaster.get(
        eq(A_OID), argThat(new IsValidFor(docA200_V2010to)))).thenReturn(docA200_V2010to);
    when(mockUnderlyingMaster.get(
        eq(A_OID), argThat(new IsValidFor(docA300_V1999to2010_C2011to)))).thenReturn(docA300_V1999to2010_C2011to);
    when(mockUnderlyingMaster.get(
        eq(B_OID), argThat(new IsValidFor(docB200_V2000to2009)))).thenReturn(docB200_V2000to2009);
    when(mockUnderlyingMaster.get(
        eq(B_OID), argThat(new IsValidFor(docB400_V2009to2011)))).thenReturn(docB400_V2009to2011);
    when(mockUnderlyingMaster.get(
        eq(B_OID), argThat(new IsValidFor(docB500_V2011to)))).thenReturn(docB500_V2011to);
    when(mockUnderlyingMaster.get(
        eq(C_OID), argThat(new IsValidFor(docC100_Vto2011)))).thenReturn(docC100_Vto2011);
    when(mockUnderlyingMaster.get(
        eq(C_OID), argThat(new IsValidFor(docC300_V2011to)))).thenReturn(docC300_V2011to);

    // Configure mock master to respond to add
    when(mockUnderlyingMaster.add(DOC_TO_ADD)).thenReturn(DOC_ADDED);
    when(mockUnderlyingMaster.get(ADDED_UID)).thenReturn(DOC_ADDED);
    when(mockUnderlyingMaster.get(eq(ADDED_OID), argThat(new IsValidFor(DOC_ADDED)))).thenReturn(DOC_ADDED);

    return mockUnderlyingMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Mockito argument matcher that checks whether a VersionCorrection is within a document's v/c range
   */
  class IsValidFor extends ArgumentMatcher<VersionCorrection> {
    private Instant _fromVersion, _fromCorrection;
    private Instant _toVersion, _toCorrection;

    public IsValidFor(AbstractDocument document) {
      _fromVersion = document.getVersionFromInstant();
      _toVersion = document.getVersionToInstant();
      _fromCorrection = document.getCorrectionFromInstant();
      _toCorrection = document.getCorrectionToInstant();
    }

    public boolean matches(Object o) {
      VersionCorrection vc = (VersionCorrection) o;
      return  (_fromVersion == null || vc.getVersionAsOf() == null || vc.getVersionAsOf().isAfter(_fromVersion)) &&
              (_toVersion == null || vc.getVersionAsOf() != null && vc.getVersionAsOf().isBefore(_toVersion)) &&
              (_fromCorrection == null || vc.getCorrectedTo() == null || vc.getCorrectedTo().isAfter(_fromCorrection)) &&
              (_toCorrection == null || vc.getCorrectedTo() != null && vc.getCorrectedTo().isBefore(_toCorrection));
    }
  }

}
