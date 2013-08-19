/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import com.opengamma.integration.copier.sheet.writer.SheetWriter;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract class for a snapshot writer that writes to a single sheet
 */
public abstract class SingleSheetSnapshotWriter implements SnapshotWriter {

  private SheetWriter _sheet;         // The spreadsheet to which to export

  public SingleSheetSnapshotWriter(SheetWriter sheet) {
    ArgumentChecker.notNull(sheet, "sheet");
    _sheet = sheet;
  }

  public SheetWriter getSheet() {
    return _sheet;
  }

  public void setSheet(SheetWriter sheet) {
    ArgumentChecker.notNull(sheet, "sheet");
    _sheet = sheet;
  }
  

  @Override
  public void flush() {
    _sheet.flush();
  }

  @Override
  public void close() {
    flush();
    _sheet.close();
  }

}
