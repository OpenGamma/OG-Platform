/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.copier;

import com.opengamma.integration.copier.snapshot.reader.SnapshotReader;
import com.opengamma.integration.copier.snapshot.writer.SnapshotWriter;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple snapshot copier that copies positions from readers to the specified writer.
 */
public class SimpleSnapshotCopier implements SnapshotCopier {

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
    snapshotWriter.writeVolatilitySurface(snapshotReader.readVolatilitySurfaces());
  }
}
