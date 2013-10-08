/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
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

  //-------------------------------------------------------------------------
  /**
   * GZIP compress a {@code String}.
   * <p>
   * This only produces GZIP format.
   * 
   * @param input  the string to compress, not null
   * @return the compressed bytes, not null
   */
  public static byte[] zipString(final String input) {
    return zipString(input, false);
  }

  /**
   * GZIP compress a {@code String}.
   * <p>
   * If optimizing then the compressor checks if UTF-8 is shorter and just uses that.
   * 
   * @param input  the string to compress, not null
   * @param optimize  true to optimize length by not compressing if shorter
   * @return the compressed bytes, not null
   */
  public static byte[] zipString(final String input, boolean optimize) {
    ArgumentChecker.notNull(input, "input");
    byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
    if (optimize && input.length() < 20) {
      return bytes;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length());
    try (GZIPOutputStream out = new GZIPOutputStream(baos)) {
      out.write(bytes);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
    byte[] result = baos.toByteArray();
    if (optimize && result.length > bytes.length) {
      return bytes;
    }
    return result;
  }

  /**
   * GZIP uncompress to a {@code String}.
   * <p>
   * This expects the magic '1F 9D' prefix for GZIP.
   * If it does not find it then uncompressed UTF-8 is assumed.
   * 
   * @param input  the bytes to compress, not null
   * @return the compressed string, not null
   */
  public static String unzipString(final byte[] input) {
    ArgumentChecker.notNull(input, "input");
    if (input.length < 2 || input[0] != 31 || input[1] != -117) {  // GZIP (1F 9D)
      return new String(input, StandardCharsets.UTF_8);
    }
    try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
      try (GZIPInputStream in = new GZIPInputStream(bais)) {
        byte[] bytes = IOUtils.toByteArray(in);
        return new String(bytes, StandardCharsets.UTF_8);
      }
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * ZLIB compress a {@code String}.
   * <p>
   * This only produces ZLIB format using {@code Deflater}.
   * 
   * @param input  the string to compress, not null
   * @return the compressed bytes, not null
   */
  public static byte[] deflateString(final String input) {
    ArgumentChecker.notNull(input, "input");
    byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
    deflater.setInput(bytes);
    ByteArrayList collector = new ByteArrayList(bytes.length + 32);
    byte[] buf = new byte[1024];
    deflater.finish();
    while (deflater.finished() == false) {
      int size = deflater.deflate(buf);
      collector.addElements(collector.size(), buf, 0, size);
    }
    deflater.end();
    return collector.toByteArray();
  }

  /**
   * ZLIB uncompress to a {@code String}.
   * <p>
   * This only handles ZLIB format using {@code Inflater}.
   * 
   * @param input  the bytes to compress, not null
   * @return the compressed string, not null
   */
  public static String inflateString(final byte[] input) {
    ArgumentChecker.notNull(input, "input");
    try {
      Inflater inflater = new Inflater();
      inflater.setInput(input);
      ByteArrayList collector = new ByteArrayList(input.length * 4);
      byte[] buf = new byte[1024];
      while (inflater.finished() == false) {
        int size = inflater.inflate(buf);
        collector.addElements(collector.size(), buf, 0, size);
      }
      inflater.end();
      byte[] bytes = collector.toByteArray();
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (DataFormatException ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
  }

}
