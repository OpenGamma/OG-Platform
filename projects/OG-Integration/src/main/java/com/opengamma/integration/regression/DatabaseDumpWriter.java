/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.xml.FormattingXmlStreamWriter;

/**
 * Writes a database dump somewhere. To file system and zip are implemented here.
 */
abstract class DatabaseDumpWriter implements AutoCloseable {

  private static final Logger s_logger = LoggerFactory.getLogger(DatabaseDumpWriter.class);
  
  private static final FudgeContext s_ctx = new FudgeContext(OpenGammaFudgeContext.getInstance());
  private static final FudgeSerializer s_serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());

  
  /**
   * Write the passed object to the dump.
   * @param outputSubDir subdir to write to
   * @param fileName name of the dumped file
   * @param object object to serialize and write
   * @throws IOException if an IO failure occurs
   */
  public void writeToFudge(String outputSubDir, String fileName, Object object) throws IOException {
    writeToFudge(outputSubDir + "/" + fileName, object);
  }

  
  /**
   * Write the passed object to the dump.
   * @param fileName name of the dumped file
   * @param object object to serialize and write
   * @throws IOException if an IO failure occurs
   */
  public abstract void writeToFudge(String fileName, Object object) throws IOException;
  
  //utility method for writing a fudge-serializable object to an output stream
  void writeToFudge(OutputStream os, Object object) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(os);
    
    FormattingXmlStreamWriter xmlStreamWriter = FormattingXmlStreamWriter
      .builder(writer)
      .indent(true)
      .build();
    
    FudgeXMLStreamWriter streamWriter = new FudgeXMLStreamWriter(s_ctx, xmlStreamWriter);
    @SuppressWarnings("resource") //flushed below
    FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(streamWriter);
    MutableFudgeMsg msg = s_serializer.objectToFudgeMsg(object);
    FudgeSerializer.addClassHeader(msg, object.getClass());
    fudgeMsgWriter.writeMessage(msg);
    s_logger.debug("Wrote object {}", object);
    fudgeMsgWriter.flush();
    
  }

  //utility method for creating an output directory
  void createDir(File outputDir) {
    if (!outputDir.exists()) {
      boolean success = outputDir.mkdirs();
      if (success) {
        s_logger.debug("Created directory {}", outputDir);
      } else {
        throw new OpenGammaRuntimeException("Failed to create directory " + outputDir);
      }
    }
  }


  
  /**
   * Create a new dump writer which writes to a zip file.
   * @param outputDir the directory to write the zip file into
   * @param zipfileName the name of the zip file
   * @return a DatabaseDumpWriter which writes to a zip
   * @throws IOException 
   */
  public static DatabaseDumpWriter createZipWriter(File outputDir, String zipfileName) throws IOException {
    return new ZipDatabaseDumpWriter(outputDir, zipfileName);
  }
  
  /**
   * Create a new dump writer which writes to a directory structure.
   * @param outputDir the directory to write the dump to
   * @return a writer
   */
  public static DatabaseDumpWriter createFileWriter(File outputDir) {
    return new FileDatabaseDumpWriter(outputDir);
  }
  
  
  
  private static class FileDatabaseDumpWriter extends DatabaseDumpWriter {
    
    private final File _outputDir;

    public FileDatabaseDumpWriter(File outputDir) {
      _outputDir = outputDir;
      createDir(outputDir);
      s_logger.info("Dumping database to {}", _outputDir.getAbsolutePath());
    }
    
    @Override
    public void writeToFudge(String fileName, Object object) throws IOException {
      File outputFile = new File(_outputDir, fileName);
      createDir(outputFile.getParentFile());
      
      s_logger.info("Writing to {}", outputFile.getAbsolutePath());

      try (FileOutputStream fos = new FileOutputStream(outputFile)) {
        writeToFudge(fos, object);
      }
    }

    @Override
    public void close() throws Exception {
      //nothing to do
    }


  }
  
  private static class ZipDatabaseDumpWriter extends DatabaseDumpWriter {
    
    private final ZipArchiveOutputStream _zipArchive;
    
    public ZipDatabaseDumpWriter(File outputDir, String zipfileName) throws IOException {
      createDir(outputDir);
      _zipArchive = new ZipArchiveOutputStream(new File(outputDir, zipfileName));
    }
    
    @Override
    public void close() throws Exception {
      _zipArchive.close();
    }
    
    @Override
    public void writeToFudge(String fileName, Object object) throws IOException {
      ZipArchiveEntry entry = new ZipArchiveEntry(fileName);
      _zipArchive.putArchiveEntry(entry);
      writeToFudge(_zipArchive, object);
      _zipArchive.closeArchiveEntry();
    }
    
    
  }
  
}
