/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Implementation of {@link DbScript} which reads the script from an entry in a zip file, e.g. a JAR file.
 */
public class ZipFileDbScript implements DbScript {

  private final File _zipFile;
  private final String _entryName;

  public ZipFileDbScript(File zipFile, String entryName) {
    _zipFile = zipFile;
    _entryName = entryName;
  }

  //-------------------------------------------------------------------------
  private File getZipFile() {
    return _zipFile;
  }

  private String getEntryName() {
    return _entryName;
  }

  //-------------------------------------------------------------------------  
  @Override
  public String getName() {
    return getEntryName().substring(getEntryName().lastIndexOf(ZipFileDbScriptDirectory.SEPARATOR) + 1);
  }

  @Override
  public String getScript() throws IOException {
    try (ZipInputStream zippedIn = new ZipInputStream(new FileInputStream(getZipFile()))) {
      ZipEntry entry;
      while ((entry = zippedIn.getNextEntry()) != null) {
        if (entry.getName().equals(getEntryName())) {
          break;
        }
      }
      if (entry == null) {
        throw new OpenGammaRuntimeException("No entry found in zip file " + getZipFile() + " with name " + getEntryName());
      }
      return IOUtils.toString(zippedIn);
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Zip file not found: " + getZipFile());
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Error reading from zip file: " + getZipFile());
    }
  }

  @Override
  public String toString() {
    return getZipFile() + "!/" + getEntryName();
  }

}
