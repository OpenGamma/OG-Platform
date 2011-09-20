/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.FudgeRuntimeIOException;
import org.fudgemsg.wire.json.JSONSettings;
import org.fudgemsg.wire.types.FudgeWireType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.collect.Lists;

/**
 * A Fudge reader that interprets JSON.
 */
public class FudgeMsgJSONReader {
  
  /**
   * The taxonomy identifier to use for any messages that are passed without envelopes. 
   */
  private static final short DEFAULT_TAXONOMY_ID = 0;
  /**
   * The schema version to add to the envelope header for any messages that are passed without envelopes.
   */
  private static final int DEFAULT_MESSAGE_VERSION = 0;
  /**
   * The processing directive flags to add to the envelope header for any messages that are passed without envelopes.
   */
  private static final int DEFAULT_MESSAGE_PROCESSING_DIRECTIVES = 0;
  
  private final JSONSettings _settings;
  private final FudgeContext _fudgeContext;
  private final Reader _underlying;
  private int _taxonomyId = DEFAULT_TAXONOMY_ID;
  private FudgeTaxonomy _taxonomy;
  private int _processingDirectives = DEFAULT_MESSAGE_PROCESSING_DIRECTIVES;
  private int _schemaVersion = DEFAULT_MESSAGE_VERSION;
  private JSONObject _jsonObject;
  private final List<String> _envelopeAttibutesFields = Lists.newArrayList();
  
  /**
   * Creates a new instance for reading a Fudge stream from a JSON reader.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param reader  the underlying reader, not null
   */
  public FudgeMsgJSONReader(final FudgeContext fudgeContext, final Reader reader) {
    this(fudgeContext, reader, new JSONSettings());
  }

  /**
   * Creates a new instance for reading a Fudge stream from a JSON reader.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param reader  the underlying reader, not null
   * @param settings  the JSON settings to fine tune the read, not null
   */
  public FudgeMsgJSONReader(final FudgeContext fudgeContext, final Reader reader, final JSONSettings settings) {
    _fudgeContext = fudgeContext;
    _underlying = reader;
    _settings = settings;
    try {
      _jsonObject = new JSONObject(new JSONTokener(reader));
      init(_fudgeContext, _jsonObject, _settings);
    } catch (JSONException ex) {
      wrapException("Creating json object from reader", ex);
    }
  }

  private void init(FudgeContext fudgeContext, final JSONObject jsonObject, final JSONSettings settings) throws JSONException {
    String processingDirectivesField = settings.getProcessingDirectivesField();
    if (jsonObject.has(processingDirectivesField)) {
      _processingDirectives = integerValue(jsonObject.get(processingDirectivesField));
      _envelopeAttibutesFields.add(processingDirectivesField);
    }
    String schemaVersionField = getSettings().getSchemaVersionField();
    if (jsonObject.has(schemaVersionField)) {
      _schemaVersion = integerValue(jsonObject.get(schemaVersionField));
      _envelopeAttibutesFields.add(schemaVersionField);
    }
    String taxonomyField = getSettings().getTaxonomyField();
    if (jsonObject.has(taxonomyField)) {
      _taxonomyId = integerValue(jsonObject.get(taxonomyField));
      _taxonomy = fudgeContext.getTaxonomyResolver().resolveTaxonomy((short) _taxonomyId);
      _envelopeAttibutesFields.add(taxonomyField);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying reader.
   * 
   * @return the reader, not null
   */
  public Reader getUnderlying() {
    return _underlying;
  }
  
  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  /**
   * Gets the settings.
   * @return the settings
   */
  public JSONSettings getSettings() {
    return _settings;
  }

  private RuntimeException wrapException(String message, final JSONException ex) {
    message = "Error " + message + " from JSON stream";
    if (ex.getCause() instanceof IOException) {
      return new FudgeRuntimeIOException(message, (IOException) ex.getCause());
    } else {
      return new FudgeRuntimeException(message, ex);
    }
  }
  
  
  /**
   * Reads the next message, discarding the envelope.
   * 
   * @return the message read without the envelope
   */
  public FudgeMsg readMessage() {
    final FudgeMsgEnvelope msgEnv = readMessageEnvelope();
    if (msgEnv == null) {
      return null;
    }
    return msgEnv.getMessage();
  }
  
  
  /**
   * Reads the next message, returning the envelope.
   * 
   * @return the {@link FudgeMsgEnvelope}
   */
  public FudgeMsgEnvelope readMessageEnvelope() {
    FudgeMsgEnvelope msgEnv = null;
    try { 
      MutableFudgeMsg msg = processFields(_jsonObject);
      msgEnv = getFudgeMsgEnvelope(msg, _jsonObject);
    } catch (JSONException ex) {
      wrapException("reading message envelope", ex);
    }
    return msgEnv;
  }
  
  private FudgeMsgEnvelope getFudgeMsgEnvelope(MutableFudgeMsg fudgeMsg, final JSONObject jsonObject) throws JSONException {    
    return new FudgeMsgEnvelope(fudgeMsg, _schemaVersion, _processingDirectives);
  }
  
  private int integerValue(final Object o) {
    if (o instanceof Number) {
      return ((Number) o).intValue();
    } else {
      return 0;
    }
  }

  private MutableFudgeMsg processFields(final JSONObject jsonObject) {
    MutableFudgeMsg fudgeMsg = getFudgeContext().newMessage();
    @SuppressWarnings("unchecked")
    Iterator<String> keys = jsonObject.keys();
    while (keys.hasNext()) {
      final String fieldName = keys.next();
      if (isValidField(fieldName)) {
        final Object fieldValue = getFieldValue(jsonObject, fieldName);
        if (fieldValue instanceof JSONObject) {
          final MutableFudgeMsg subMsg = processFields((JSONObject) fieldValue);
          addField(fudgeMsg, fieldName, FudgeWireType.SUB_MESSAGE, subMsg);
        } else if (fieldValue instanceof JSONArray) {
          final JSONArray jsonArray = (JSONArray) fieldValue;
          if (jsonArray.length() > 0) {
            if (isPrimitiveArray(jsonArray) && !isEncodedAsSubMessage(fieldName, jsonObject)) {
              try {
                final Object primitiveArray = jsonArrayToPrimitiveArray(jsonArray);
                addField(fudgeMsg, fieldName, getFieldType(primitiveArray), primitiveArray);
              } catch (JSONException e) {
                wrapException("converting json array to primitive array", e);
              }
            } else {
              //treat as repeated fields
              addRepeatedFields(fudgeMsg, fieldName, jsonArray);
            }
          }
        } else {
          addField(fudgeMsg, fieldName, getFieldType(fieldValue), fieldValue);
        }
      }
    }
    return fudgeMsg;
  }

  private void addField(MutableFudgeMsg fudgeMsg, final String fieldName, FudgeFieldType fieldType, final Object fieldValue) {
    Integer ordinal = null;
    String name = null;
    try {
      ordinal = Integer.parseInt(fieldName);
    } catch (NumberFormatException nfe) {
      if (StringUtils.isNotEmpty(fieldName)) {
        if (!getPreserveFieldNames()) {
          if (_taxonomy != null) {
            ordinal = _taxonomy.getFieldOrdinal(fieldName);
          }
        } else {
          name = fieldName;
        }
      }
    }
    fudgeMsg.add(name, ordinal, fieldType, fieldValue);
  }
  
  private boolean getPreserveFieldNames() {
    return getSettings().getPreserveFieldNames();
  }

  private void addRepeatedFields(MutableFudgeMsg fudgeMsg, final String fieldName, final JSONArray jsonArray) {
    try {
      for (int i = 0; i < jsonArray.length(); i++) {
        final Object arrValue = jsonArray.get(i);
        if (arrValue instanceof JSONObject) {
          final MutableFudgeMsg subMsg = processFields((JSONObject) arrValue);
          addField(fudgeMsg, fieldName, FudgeWireType.SUB_MESSAGE, subMsg);
        } else {
          addField(fudgeMsg, fieldName, getFieldType(arrValue), arrValue);
        }
      }
    } catch (JSONException e) {
      wrapException("adding repeated fields", e);
    }
  }
  
  private FudgeFieldType getFieldType(final Object fieldValue) {
    return getFudgeContext().getTypeDictionary().getByJavaType(fieldValue.getClass());
  }
  
  private Object jsonArrayToPrimitiveArray(final JSONArray arr) throws JSONException {
    boolean arrInt = true, arrDouble = true, arrLong = true;
    for (int j = 0; j < arr.length(); j++) {
      Object arrValue = arr.get(j);
      if (JSONObject.NULL.equals(arrValue)) {
        arrInt = false;
        arrDouble = false;
        break;
      } else if (arrValue instanceof Number) {
        if (arrValue instanceof Double) {
          arrInt = false;
          arrLong = false;
        } else if (arrValue instanceof Long) {
          arrInt = false;
        } else {
          if (!(arrValue instanceof Integer)) {
            arrInt = false;
            arrDouble = false;
          }
        }
      } else if (arrValue instanceof JSONObject) {
        arrInt = false;
        arrDouble = false;
        break;
      } else if (arrValue instanceof JSONArray) {
        arrInt = false;
        arrDouble = false;
        break;
      }
    }
    if (arrInt) {
      final int[] data = new int[arr.length()];
      for (int j = 0; j < data.length; j++) {
        data[j] = ((Number) arr.get(j)).intValue();
      }
      return data;
    } else if (arrLong) {
      final long[] data = new long[arr.length()];
      for (int j = 0; j < data.length; j++) {
        data[j] = ((Number) arr.get(j)).longValue();
      }
      return data;
    } else if (arrDouble) {
      final double[] data = new double[arr.length()];
      for (int j = 0; j < data.length; j++) {
        data[j] = ((Number) arr.get(j)).doubleValue();
      }
      return data;
    } else {
      return null;
    }
  }
  
  private boolean isPrimitiveArray(final JSONArray jsonArray) {
    try {
      for (int i = 0; i < jsonArray.length(); i++) {
        Object arrValue = jsonArray.get(i);
        if (!(arrValue instanceof Number)) {
          return false;
        }
      }
    } catch (JSONException e) {
      wrapException("is primitive array", e);
    }
    return true;
  }
  
  private boolean isEncodedAsSubMessage(final String fieldName, final JSONObject jsonObject) {
    try {
      String typeSuffix = getSettings().getTypeSuffix();
      if (typeSuffix != null) {
        String typeName = fieldName + typeSuffix;
        if (jsonObject.has(typeName)) {
          Object typeValue = jsonObject.get(typeName);
          if (typeValue instanceof Number) {
            int typeValueInt = (Integer) typeValue;
            if (typeValueInt == FudgeWireType.SUB_MESSAGE_TYPE_ID) {
              return true;
            }
          }
        }
      }
    } catch (JSONException ex) {
      wrapException("reading typ suffix", ex);
    }
    return false;
  }
  
  private Object getFieldValue(final JSONObject jsonObject, final String fieldName) {
    Object fieldValue = null;
    try {
      fieldValue = jsonObject.get(fieldName);
      if (JSONObject.NULL.equals(fieldValue)) {
        fieldValue = IndicatorType.INSTANCE;
      }
    } catch (JSONException e) {
      wrapException("reading field value", e);
    }
    return fieldValue;
  }
  
  private boolean isValidField(final String fieldName) {
    String typeSuffix = getSettings().getTypeSuffix();
    if (!_envelopeAttibutesFields.contains(fieldName) && (typeSuffix != null && !fieldName.endsWith(typeSuffix))) {
      return true;
    } 
    return false;
  }

}
