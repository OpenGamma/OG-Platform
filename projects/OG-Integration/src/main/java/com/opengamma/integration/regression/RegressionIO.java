/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstraction of the I/O pattern for the database dump/restore tools. Data is written to a logical sub-directory for each object type with a logical file for each object. Sub-classes may implement
 * this with physical directories and files, or use a structured file format such as a ZIP file.
 */
public abstract class RegressionIO {

  /**
   * The base location. Depending on the sub-class this might be a physical directory root or a single physical file such as a ZIP archive.
   */
  private final File _baseFile;

  /**
   * Formatting SPI. Possible implementations might include, for example, Java serialization, Fudge binary encodings or more verbose XML representations.
   */
  public interface Format {

    /**
     * Initializes any internal context needed by an I/O instance for {@link #read} operations.
     * 
     * @param context the previous context (created by {@link #write} for example) or null if none
     * @return the context object, or null if none required
     */
    Object openRead(Object context);

    /**
     * Initializes any internal context needed by an I/O instance for {@link #write} operations.
     * 
     * @param context the previous context (created by {@link #write} for example) or null if none
     * @return the context object, or null if none required
     */
    Object openWrite(Object context);

    /**
     * Returns a logical file extension, if any.
     * 
     * @param context the context object returned by {@link #init}
     * @return the logical extension, for example ".txt", or null if none
     */
    String getLogicalFileExtension(Object context);

    /**
     * Formats an object to an output stream.
     * 
     * @param context the context object returned by {@link #init}
     * @param o the object to write, not null
     * @param dest the target stream, not null
     */
    void write(Object context, Object o, OutputStream dest) throws IOException;

    /**
     * Decodes an object from an input stream.
     * 
     * @param context the context object returned by {@link #init}
     * @param in the input stream, not null
     * @return the object read, not null
     */
    Object read(Object context, InputStream in) throws IOException;

    /**
     * Terminates a context returned by {@link #initRead}.
     * 
     * @param context the value returned by a previous call to {@code initRead}.
     * @return the updated context, or null if none now exists
     */
    Object closeRead(Object context);

    /**
     * Terminates a context returned by {@link #initWrite}.
     * 
     * @param context the value returned by a previous call to {@code initWrite}.
     * @return the updated context, or null if none now exists
     */
    Object closeWrite(Object context);

  }

  /**
   * The formatter to use for actual I/O.
   */
  private final Format _format;

  /**
   * The formatting context, if any.
   */
  private Object _formatContext;

  /**
   * Creates a new instance.
   * 
   * @param baseFile the base file - the exact meaning will depend on the sub-class, not null
   * @param format the format to use for each object, not null
   */
  public RegressionIO(final File baseFile, final Format format) {
    _baseFile = ArgumentChecker.notNull(baseFile, "notNull");
    _format = ArgumentChecker.notNull(format, "format");
  }

  /**
   * Returns the base location - the exact meaning will depend on the sub-class.
   * 
   * @return the base location, not null
   */
  protected File getBaseFile() {
    return _baseFile;
  }

  /**
   * Returns the format to use for each object.
   * 
   * @return the format, not null
   */
  protected Format getFormat() {
    return _format;
  }

  /**
   * Returns the current format context.
   * 
   * @return the format context, if any
   */
  protected Object getFormatContext() {
    return _formatContext;
  }

  /**
   * Performs any initialization prior to the first write, such as opening files or preparing caches/buffers.
   */
  public void beginWrite() throws IOException {
    _formatContext = getFormat().openWrite(_formatContext);
  }

  /**
   * Writes out an object.
   * 
   * @param type the type classifier, null for none
   * @param o the object to write out, not null
   * @param identifier the object identifier, must be unique within a given object type, not null
   */
  public abstract void write(String type, Object o, String identifier) throws IOException;

  /**
   * Bulk write operation.
   * <p>
   * The default implementation just calls {@link #write} but a sub-class might have a more efficient/appropriate form to use.
   * 
   * @param type the type classifier, null for none
   * @param os the objects to write out, as a map of identifier to value, not null
   */
  public void write(final String type, final Map<String, Object> os) throws IOException {
    for (Map.Entry<String, Object> oe : os.entrySet()) {
      write(type, oe.getValue(), oe.getKey());
    }
  }

  /**
   * Performs any finalization after the final write, such as flushing caches/buffers or closing files.
   */
  public void endWrite() throws IOException {
    _formatContext = getFormat().closeWrite(_formatContext);
  }

  /**
   * Performs any initialization prior to the first read, such as opening files or preparing caches/buffers.
   */
  public void beginRead() throws IOException {
    _formatContext = getFormat().openRead(_formatContext);
  }

  /**
   * Reads an object.
   * 
   * @param type the type classifier, null for none
   * @param identifier the object identifier, must be unique within a given object type, not null
   * @return the read object, not null
   */
  public abstract Object read(String type, String identifier) throws IOException;

  /**
   * Enumerates all available objects for a type.
   * 
   * @param type the type classifier, null for none
   * @return the object identifiers, not null
   */
  public abstract List<String> enumObjects(String type) throws IOException;

  /**
   * Bulk read operation to fetch an object sub-set.
   * <p>
   * The default implementation just calls {@link #read(String,String)} but a sub-class might have a more efficient/appropriate form to use.
   * 
   * @param type the type classifier, null for none
   * @param identifiers the object identifiers to read, not null
   * @return the objects as a map from identifiers to values, not null
   */
  public Map<String, Object> read(final String type, final Collection<String> identifiers) throws IOException {
    final Map<String, Object> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (String identifier : identifiers) {
      result.put(identifier, read(type, identifier));
    }
    return result;
  }

  /**
   * Bulk read operation to fetch all objects.
   * <p>
   * The default implementation just calls {@link #enumObjects} and {@link #read(String,Collection)} but a sub-class might have a more efficient/appropriate form to use.
   * 
   * @param type the type classifier, null for none
   * @return the objects as a map of identifiers to values, not null
   */
  public Map<String, Object> readAll(final String type) throws IOException {
    return read(type, enumObjects(type));
  }

  /**
   * Performs any finalization after the final read, such as closing files.
   */
  public void endRead() throws IOException {
    _formatContext = getFormat().closeRead(_formatContext);
  }

  protected String createFilename(final String identifier) {
    final String ext = getFormat().getLogicalFileExtension(getFormatContext());
    if (ext != null) {
      return identifier + ext;
    } else {
      return identifier;
    }
  }

  protected boolean isIdentifierIncluded(String name) {
    String ext = getFormat().getLogicalFileExtension(getFormatContext());
    return ext == null || name.endsWith(ext);
  }
  
  protected String stripIdentifierExtension(String name) {
    String ext = getFormat().getLogicalFileExtension(getFormatContext());
    if (ext == null) {
      return name;
    } else if (name.endsWith(ext)) {
      return name.substring(0, name.length() - ext.length());
    } else {
      return name;
    }
  }

}
