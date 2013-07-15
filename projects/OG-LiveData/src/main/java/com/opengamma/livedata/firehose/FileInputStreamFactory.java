/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.firehose;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link InputStreamFactory} that opens a {@link File}.
 */
public class FileInputStreamFactory implements InputStreamFactory {
  private static final Logger s_logger = LoggerFactory.getLogger(FileInputStreamFactory.class);
  private final File _inputFile;
  private String _description;
  
  public FileInputStreamFactory(String fileName) {
    this(new File(fileName));
  }
  
  public FileInputStreamFactory(File inputFile) {
    ArgumentChecker.notNull(inputFile, "inputFile");
    _inputFile = inputFile;
    
    _description = "File[" + _inputFile.getAbsolutePath() + "]";
  }

  @Override
  public InputStream openConnection() {
    s_logger.info("Opening connection to {}", _inputFile.getAbsolutePath());
    try {
      return new FileInputStream(_inputFile);
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Unable to open file " + _inputFile.getAbsolutePath(), ex);
    }
  }

  @Override
  public String getDescription() {
    return _description;
  }

}
