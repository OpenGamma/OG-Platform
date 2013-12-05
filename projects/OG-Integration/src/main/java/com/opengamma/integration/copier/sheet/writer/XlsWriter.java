/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.sheet.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

/**
 *  Util to create and output xls
 */
public class XlsWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(XlsWriter.class);
  private HSSFWorkbook _workbook;
  private String _fileName;
  private OutputStream _output;

  public XlsWriter(String filename) {
    _workbook = new HSSFWorkbook();
    _fileName = filename;
    _output = openFile(_fileName);
  }

  public HSSFWorkbook getWorkbook() {
    return _workbook;
  }

  protected static OutputStream openFile(String filename) {
    // Open input file for writing
    FileOutputStream fileOutputStream;
    try {
      fileOutputStream = new FileOutputStream(filename);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not open file " + filename + " for writing.");
    }

    return fileOutputStream;
  }

  public void close() {
    try {
      _workbook.write(_output);
      _output.close();
      s_logger.info("XLS successfully written: {}", _fileName);
    } catch (IOException e) {
      s_logger.error("Error writing/outputting XLS: {}", _fileName);
      e.printStackTrace();
    }
  }
}
