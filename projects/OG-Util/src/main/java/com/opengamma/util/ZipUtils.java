/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Utility methods to assist with ZIP files.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class ZipUtils {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ZipUtils.class);

  /**
   * Restricted constructor
   */
  private ZipUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Unzips a ZIP archive.
   * 
   * @param archive  the archive file, not null
   * @param outputDir  the output directory, not null
   * @throws IOException if an error occurs
   */
  public static void unzipArchive(final File archive, final File outputDir) throws IOException {
    ArgumentChecker.notNull(archive, "archive");
    ArgumentChecker.notNull(outputDir, "outputDir");
    try {
      ZipFile zipfile = new ZipFile(archive);
      Enumeration<?> e = zipfile.entries();
      while (e.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) e.nextElement();
        unzipEntry(zipfile, entry, outputDir);
      }
    } catch (IOException ex) {
      s_logger.error("Error while extracting file {}, rethrowing..", archive, ex);
      throw ex;
    }
  }

  private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {
    if (entry.isDirectory()) {
      createDir(new File(outputDir, entry.getName()));
      return;
    }
    
    File outputFile = new File(outputDir, entry.getName());
    if (!outputFile.getParentFile().exists()) {
      createDir(outputFile.getParentFile());
    }
    
    s_logger.debug("Extracting: " + entry);
    
    try (BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
      IOUtils.copy(inputStream, outputStream);
    }
  }

  private static void createDir(File dir) {
    s_logger.debug("Creating dir {}", dir.getName());
    if (!dir.mkdirs()) {
      throw new OpenGammaRuntimeException("Can not create dir " + dir);
    }
  }

}
