/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.copier;

import com.opengamma.integration.copier.snapshot.reader.SnapshotReader;
import com.opengamma.integration.copier.snapshot.writer.SnapshotWriter;

/**
 * Provides the ability to copy snapshots within a master, across masters, between streams/files and masters, and
 * between streams/files.
 */
public abstract interface SnapshotCopier {
 
  void copy(SnapshotReader snapshotReader, SnapshotWriter snapshotWriter);

  void copy(SnapshotReader snapshotReader, SnapshotWriter snapshotWriter, SnapshotCopierVisitor visitor);

}
