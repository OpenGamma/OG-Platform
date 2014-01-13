/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Implementation of the I/O pattern that uses a physical sub-directory for each object type and a single physical file for each object.
 */
public class SubdirsRegressionIO extends RegressionIO {

  private static final Logger s_logger = LoggerFactory.getLogger(SubdirsRegressionIO.class);

  /**
   * Creates a new instance. If the supplied directory does not exist, it will be created.
   * 
   * @param baseDir the directory to write the structures to - sub-directories will be created under this for each object type, not null
   * @param format the format to write each object in, not null
   * @param createIfAbsent true to create the folder if it doesn't exist (use this for writing, but probably not for reading)
   */
  public SubdirsRegressionIO(final File baseDir, final Format format, final boolean createIfAbsent) {
    super(baseDir, format);
    checkDirectory(baseDir, createIfAbsent);
  }

  private static void checkDirectory(final File dir, final boolean createIfAbsent) {
    if (dir.exists()) {
      if (!dir.isDirectory()) {
        throw new IllegalArgumentException("Location '" + dir + "' exists but is not a directory");
      }
    } else {
      if (createIfAbsent) {
        boolean success = dir.mkdirs();
        if (success) {
          s_logger.debug("Created directory {}", dir);
        } else {
          throw new OpenGammaRuntimeException("Failed to create directory " + dir);
        }
      } else {
        s_logger.debug("Directory {} does not exist", dir);
      }
    }
  }

  protected File getTypeFolder(final String type, final boolean createIfAbsent) {
    if (type == null) {
      return getBaseFile();
    } else {
      final File outputDir = new File(getBaseFile(), type);
      checkDirectory(outputDir, createIfAbsent);
      return outputDir;
    }
  }

  

  // RegressionIO

  @Override
  public void write(final String type, final Object o, final String identifier) throws IOException {
    try (OutputStream dest = new BufferedOutputStream(new FileOutputStream(new File(getTypeFolder(type, true), createFilename(identifier))))) {
      getFormat().write(getFormatContext(), o, dest);
      dest.flush();
    }
  }

  // TODO: Bulk write operation

  @Override
  public Object read(final String type, final String identifier) throws IOException {
    try (InputStream in = new BufferedInputStream(new FileInputStream(new File(getTypeFolder(type, false), createFilename(identifier))))) {
      return getFormat().read(getFormatContext(), in);
    }
  }

  @Override
  public List<String> enumObjects(final String type) throws IOException {
    final File subDir = getTypeFolder(type, false);
    if (!subDir.exists()) {
      s_logger.info("Directory {} doesn't exist", subDir);
      return Collections.<String>emptyList();
    }
    s_logger.info("Scanning {}", subDir.getAbsolutePath());
    final File[] files = subDir.listFiles();
    if (files == null) {
      throw new OpenGammaRuntimeException("No files found in " + subDir);
    }
    final List<String> identifiers = new ArrayList<String>(files.length);
    for (File file : files) {
      if (file.isFile()) {
        final String name = file.getName();
        if (isIdentifierIncluded(name)) {
          String identifier = stripIdentifierExtension(name);
          identifiers.add(identifier);
        }
      }
    }
    s_logger.debug("Found {} objects", identifiers.size());
    return identifiers;
  }
  
  // TODO: Bulk read operation

}
