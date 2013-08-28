/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.copier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.integration.copier.snapshot.reader.SnapshotReader;
import com.opengamma.integration.copier.snapshot.writer.SnapshotWriter;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A simple snapshot copier that copies positions from readers to the specified writer.
 */
public class SimpleSnapshotCopier implements SnapshotCopier {

  private static final Logger s_logger = LoggerFactory.getLogger(SimpleSnapshotCopier.class);

  private String[] _structure;

  public SimpleSnapshotCopier() {
    _structure = null;
  }

  public SimpleSnapshotCopier(String[] structure) {
    _structure = structure;
  }

  @Override
  public void copy(SnapshotReader snapshotReader, SnapshotWriter snapshotWriter) {
    copy(snapshotReader, snapshotWriter, null);
  }

  public void copy(SnapshotReader snapshotReader, SnapshotWriter snapshotWriter, SnapshotCopierVisitor visitor) {
    ArgumentChecker.notNull(snapshotWriter, "snapshotWriter");
    ArgumentChecker.notNull(snapshotReader, "snapshotReader");
    snapshotWriter.writeName(snapshotReader.getName());
    snapshotWriter.writeBasisViewName(snapshotReader.getBasisViewName());
    snapshotWriter.writeCurves(snapshotReader.readCurves());
    snapshotWriter.writeYieldCurves(snapshotReader.readYieldCurves());
    snapshotWriter.writeGlobalValues(snapshotReader.readGlobalValues());
    snapshotWriter.writeVoliatilitySurface(snapshotReader.readVolatilitySurfaces());
  }
}
