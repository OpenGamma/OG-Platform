/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import com.google.common.collect.Lists;

/**
 * Implementation of RegressionIO which reads and writes a dump to a zip file.
 */
public class ZipFileRegressionIO extends RegressionIO implements AutoCloseable {

  /**
   * The output stream - null in read only mode
   */
  private ZipArchiveOutputStream _zipArchiveOS;
  
  /**
   * The zip input stream. Not null.
   */
  private ZipFile _zipFile;
  

  public ZipFileRegressionIO(File zipFile, Format format, boolean readMode) throws IOException {
    super(zipFile, format);
    initZipfile(zipFile, readMode);
  }

  private void initZipfile(File zipFile, boolean readMode) throws IOException {
    if (!readMode) {
      _zipArchiveOS = new ZipArchiveOutputStream(zipFile);
    }
    //should exist by now, whether read mode enabled or not
    if (!zipFile.exists()) {
      throw new IllegalStateException("Unable to locate specified zip file on the file system: " + zipFile.getPath());
    }
    _zipFile = new ZipFile(zipFile);
  }

  
  private ZipArchiveEntry createZipEntry(final String type, String identifier) {
    
    String fileName = createFilename(identifier);
    if (type != null) {
      fileName = type + "/" + fileName;
    }
    return new ZipArchiveEntry(fileName);
  }

  
  @Override
  public void write(String type, Object o, String identifier) throws IOException {
    ZipArchiveEntry entry = createZipEntry(type, identifier);
    
    _zipArchiveOS.putArchiveEntry(entry);
    getFormat().write(getFormatContext(), o, _zipArchiveOS);
    _zipArchiveOS.flush();
    _zipArchiveOS.closeArchiveEntry();

  }

  @Override
  public Object read(String type, String identifier) throws IOException {
    
    String objectPath = type + "/" + identifier;
    ZipArchiveEntry entry = _zipFile.getEntry(objectPath);
    
    if (entry == null) {
      throw new IllegalArgumentException(objectPath + " does not exist in this archive.");
    }
    
    InputStream inputStream = _zipFile.getInputStream(entry);
    
    return getFormat().read(getFormatContext(), inputStream);
    
  }

  @Override
  public List<String> enumObjects(String type) throws IOException {
    
    List<String> objectIdentifiers = Lists.newLinkedList();
    Pattern typeNameFormat = Pattern.compile("(.*)/(.*)");
    
    @SuppressWarnings("unchecked")
    Enumeration<ZipArchiveEntry> entries = _zipFile.getEntries();
    
    while (entries.hasMoreElements()) {
      ZipArchiveEntry nextEntry = entries.nextElement();
      String entryName = nextEntry.getName();
      Matcher matcher = typeNameFormat.matcher(entryName);
      if (matcher.matches()) {
        String objectType = matcher.group(1);
        if (objectType.equals(type)) {
          String objectIdentifier = matcher.group(2);
          objectIdentifiers.add(objectIdentifier);
        }
      }
    }
    
    return objectIdentifiers;
  }
  
  @Override
  public void endWrite() throws IOException {
    super.endWrite();
    if (_zipArchiveOS != null) {
      _zipArchiveOS.close();
    }
  }

  @Override
  public void close() throws Exception {
    _zipFile.close();
    if (_zipArchiveOS != null) {
      _zipArchiveOS.close();
    }
  }

}
