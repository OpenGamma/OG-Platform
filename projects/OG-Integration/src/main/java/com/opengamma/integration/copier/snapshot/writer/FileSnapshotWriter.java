/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.sheet.writer.CsvSheetWriter;
import com.opengamma.integration.copier.sheet.writer.SheetWriter;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;

/**
 * Writes a snapshot from an to exported file
 */
public class FileSnapshotWriter implements SnapshotWriter {

  private CsvSheetWriter _sheetWriter;

  public FileSnapshotWriter(String filename, MarketDataSnapshotMaster marketDataSnapshotMaster) {

    if (filename == null) {
      throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
    }

    if (SheetFormat.of(filename) == SheetFormat.CSV || SheetFormat.of(filename) == SheetFormat.XLS) {
      String[] columns = {"one", "two", "three" };
      _sheetWriter = new CsvSheetWriter(filename, columns);
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .XLS");
    }
  }

  @Override
  public void flush() {
    _sheetWriter.flush();
  }

  @Override
  public void writeCurves(Map<CurveKey, CurveSnapshot> curves) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeGlobalValues(UnstructuredMarketDataSnapshot globalValues) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeVoliatilitySurface(Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> volatilitySurface) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeYieldCurves(Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeName(String name) {
    Map<String, String> tempRow = new HashMap<>();
    tempRow.put("one", name);
    _sheetWriter.writeNextRow(tempRow);
  }

  @Override
  public void writeBasisViewName(String basisName) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void close() {
    flush();
    _sheetWriter.close();
  }
}
