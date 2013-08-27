/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.snapshot.writer;

/**
 * Interface for a snapshot writer, which is able to write positions and securities, and manipulate the snapshot's
 * tree structure.
 */
public interface SnapshotWriter {

  void flush();

  void writeCurves();

  void writeGlobalValues();

  void writeVoliatilitySurface();

  void writeYieldCurves();

  void writeName();

  void writeBasisViewName();
  
  void close();
}
