/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class FudgeMsgCSVIterator implements Iterator<FudgeMsg> {
  
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  
  private CSVReader _csvReader;
  private String[] _header;
  private String[] _currentRow;
  
  public FudgeMsgCSVIterator(InputStream input, char separator, char quotechar, char escape) {
    ArgumentChecker.notNull(input, "inputstream");
    ArgumentChecker.notNull(separator, "separator");
    ArgumentChecker.notNull(quotechar, "quotechar");
    ArgumentChecker.notNull(escape, "escape");
    
    try {
      _csvReader = new CSVReader(new BufferedReader(new InputStreamReader(input)), separator, quotechar, escape);
      _header = _csvReader.readNext();
      if (_header == null) {
        throw new OpenGammaRuntimeException("Column headers is missing, can not create iterator");
      } else {
        trimColumnHeaders();
      }
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("IO Exception trying to read next line", ex);
    }
  }

  private void trimColumnHeaders() {
    for (int i = 0; i < _header.length; i++) {
      _header[i] = StringUtils.trim(_header[i]);
    }
  }

  @Override
  public boolean hasNext() {
    try {
      _currentRow = _csvReader.readNext();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("IO Exception trying to read next row", ex);
    }
    if (_currentRow == null) {
      IOUtils.closeQuietly(_csvReader);
      _csvReader = null;
    }
    return _currentRow != null;
  }

  @Override
  public FudgeMsg next() {
    MutableFudgeMsg currentMsg = s_fudgeContext.newMessage();
    int size = getMessageSize();
    for (int i = 0; i < size; i++) {
      String currentRow = StringUtils.trimToNull(_currentRow[i]);
      if (currentRow != null) {
        currentMsg.add(_header[i], currentRow);
      }
    }
    return currentMsg;
  }

  private int getMessageSize() {
    int size = _header.length;
    if (_currentRow.length < size) {
      size = _currentRow.length;
    }
    return size;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Cannot remove CSV row");
  }

  @Override
  protected void finalize() throws Throwable {
    //close file inputstream if it is still hanging around
    if (_csvReader != null) {
      IOUtils.closeQuietly(_csvReader);
    }
  }

}
