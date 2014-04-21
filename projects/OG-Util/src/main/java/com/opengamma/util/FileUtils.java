/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.opengamma.OpenGammaRuntimeException;

// NOTE kirk 2013-12-10 -- Before adding anything to this, check to see if
// the functionality you require is in another class or project
// (in particular IOUtils).
/**
 * A collection of utility methods for working with files.
 */
public final class FileUtils {
  /**
   * A convenience reference to the java.io.tmpdir location.
   */
  public static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));
  
  private FileUtils() {
  }
  
  public static File copyResourceToTempFile(InputStream resource) {
    return copyResourceToTempFile(null, resource);
  }
  
  public static File copyResourceToTempFile(InputStream resource, String fileName) {
    return copyResourceToTempFile((String) null, resource, fileName);
  }
  
  public static File copyResourceToTempFile(String subdirName, InputStream resource) {
    return copyResourceToTempFile(subdirName, resource, null);
  }

  public static File copyResourceToTempFile(String subdirName, InputStream resource, String fileName) {
    File tempDir = TEMP_DIR;
    if (!(subdirName == null)) {
      tempDir = new File(TEMP_DIR, subdirName);
      if (tempDir.exists()) {
        if (!tempDir.isDirectory()) {
          throw new IllegalStateException("" + tempDir + " already exists and is not a directory.");
        }
      } else {
        tempDir.mkdirs();
      }
    }
    return copyResourceToTempFile(tempDir, resource, fileName);
  }

  public static File copyResourceToTempFile(File tempDir, InputStream resource, String fileName) {
    ArgumentChecker.notNull(resource, "resource");
    
    File tempFile = null;
    if (fileName == null) {
      tempFile = new File(tempDir, "test-" + System.nanoTime());
    } else {
      tempFile = new File(tempDir, fileName);
    }
    if (tempFile.exists()) {
      tempFile.delete();
    }
    
    try {
      org.apache.commons.io.FileUtils.copyInputStreamToFile(resource, tempFile);
      
      IOUtils.closeQuietly(resource);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to copy resource to " + tempFile, e);
    }
    
    tempFile.deleteOnExit();
    return tempFile;
  }

}
