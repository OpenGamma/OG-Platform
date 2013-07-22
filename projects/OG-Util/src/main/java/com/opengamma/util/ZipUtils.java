/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
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
   */
  public static void unzipArchive(final File archive, final File outputDir) {
    ArgumentChecker.notNull(archive, "archive");
    ArgumentChecker.notNull(outputDir, "outputDir");

    s_logger.debug("Unzipping file:{} to {}", archive, outputDir);
    try {
      FileUtils.forceMkdir(outputDir);
      unzipArchive(new ZipFile(archive), outputDir);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Error while extracting file: " + archive + " to: " + outputDir, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Unzips a ZIP archive.
   * 
   * @param zipFile  the archive file, not null
   * @param outputDir  the output directory, not null
   */
  public static void unzipArchive(final ZipFile zipFile, final File outputDir) {
    ArgumentChecker.notNull(zipFile, "zipFile");
    ArgumentChecker.notNull(outputDir, "outputDir");
    
    try {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          FileUtils.forceMkdir(new File(outputDir, entry.getName()));
          continue;
        }
        File entryDestination = new File(outputDir, entry.getName());
        entryDestination.getParentFile().mkdirs();
        InputStream in = zipFile.getInputStream(entry);
        OutputStream out = new FileOutputStream(entryDestination);
        IOUtils.copy(in, out);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
      }
      zipFile.close();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Error while extracting file: " + zipFile.getName() + " to: " + outputDir, ex);
    }
  }

}
