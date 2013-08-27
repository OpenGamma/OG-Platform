/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;

public class FileSnapshotWriter implements SnapshotWriter{

  public FileSnapshotWriter(String filename, MarketDataSnapshotMaster marketDataSnapshotMaster) {

    if (filename == null) {
      throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
    }

    if (SheetFormat.of(filename) == SheetFormat.CSV || SheetFormat.of(filename) == SheetFormat.XLS) {

    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .XLS");
    }
  }

  @Override
  public void flush() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeCurves() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeGlobalValues() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeVoliatilitySurface() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeYieldCurves() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeName() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeBasisViewName() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void close() {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
