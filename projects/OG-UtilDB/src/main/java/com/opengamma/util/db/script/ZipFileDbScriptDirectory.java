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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link DbScriptDirectory}  which reads from inside a zipped file, e.g. a JAR file.
 */
public class ZipFileDbScriptDirectory implements DbScriptDirectory {

  /*package*/ static final String SEPARATOR = "/";
  
  private final File _zipFile;
  private final String _path;
  
  /**
   * Constructs an instance.
   * 
   * @param zipFile  the zip file in which to look, not null
   * @param path  the path inside the zip file, not null
   */
  public ZipFileDbScriptDirectory(File zipFile, String path) {
    ArgumentChecker.notNull(zipFile, "zipFile");
    ArgumentChecker.notNull(path, "path");
    if (path.startsWith(SEPARATOR)) {
      path = path.substring(1);
    }
    if (!path.endsWith(SEPARATOR)) {
      path = path + SEPARATOR;
    }
    _zipFile = zipFile;
    _path = path;
  }
  
  //-------------------------------------------------------------------------
  private File getZipFile() {
    return _zipFile;
  }
  
  private String getPath() {
    return _path;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String getName() {
    String path = getPath().substring(0, getPath().length() - 1);
    int separatorIdx = path.lastIndexOf(SEPARATOR);
    if (separatorIdx == -1) {
      return path;
    }
    return path.substring(separatorIdx + 1);
  }

  @Override
  public Collection<DbScriptDirectory> getSubdirectories() {
    Set<String> subdirectories = new HashSet<String>();
    try (ZipInputStream zippedIn = new ZipInputStream(new FileInputStream(getZipFile()))) {
      ZipEntry entry;
      while ((entry = zippedIn.getNextEntry()) != null) {
        // Directory entries do not have to be present, so best to imply from files
        if (entry.isDirectory() || !entry.getName().startsWith(getPath())) {
          continue;
        }
        String subPath = entry.getName().substring(getPath().length());
        int firstSeparatorIdx = subPath.indexOf(SEPARATOR);
        if (firstSeparatorIdx == -1) {
          // It's a file just below this level
          continue;
        }
        String firstDir = subPath.substring(0, firstSeparatorIdx);
        subdirectories.add(firstDir);
      }
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Zip file not found: " + getZipFile());
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Error reading from zip file: " + getZipFile(), ex);
    }
    Collection<DbScriptDirectory> result = new ArrayList<DbScriptDirectory>();
    for (String subdirectory : subdirectories) {
      result.add(new ZipFileDbScriptDirectory(getZipFile(), getPath() + subdirectory));
    }
    return result;
  }

  @Override
  public DbScriptDirectory getSubdirectory(String name) {
    String path = getPath() + name + SEPARATOR;
    ZipInputStream zippedIn = null;
    try {
      zippedIn = new ZipInputStream(new FileInputStream(getZipFile()));
      ZipEntry entry;
      while ((entry = zippedIn.getNextEntry()) != null) {
        if (path.equals(entry.getName())) {
          return new ZipFileDbScriptDirectory(getZipFile(), path);
        }
      }
    } catch (FileNotFoundException e) {
      throw new OpenGammaRuntimeException("Zip file not found: " + getZipFile());
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error reading from zip file: " + getZipFile());
    } finally {
      if (zippedIn != null) {
        try {
          zippedIn.close();
        } catch (IOException e) {
        }        
      }
    }
    throw new OpenGammaRuntimeException("Zip file " + getZipFile() + " does not contain a directory entry for " + path);
  }

  @Override
  public Collection<DbScript> getScripts() {
    Collection<DbScript> scripts = new ArrayList<DbScript>();
    ZipInputStream zippedIn = null;
    try {
      zippedIn = new ZipInputStream(new FileInputStream(getZipFile()));
      ZipEntry entry;
      while ((entry = zippedIn.getNextEntry()) != null) {
        if (!entry.isDirectory() && entry.getName().startsWith(getPath()) && entry.getName().lastIndexOf(SEPARATOR) == getPath().length() - 1) {
          scripts.add(new ZipFileDbScript(getZipFile(), entry.getName()));
        }
      }
    } catch (FileNotFoundException e) {
      throw new OpenGammaRuntimeException("Zip file not found: " + getZipFile());
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error reading from zip file: " + getZipFile());
    } finally {
      if (zippedIn != null) {
        try {
          zippedIn.close();
        } catch (IOException e) {
        }        
      }
    }
    return scripts;
  }

}
