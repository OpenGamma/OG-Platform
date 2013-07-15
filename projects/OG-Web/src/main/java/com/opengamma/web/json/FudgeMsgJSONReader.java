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
import org.fudgemsg.wire.json.FudgeJSONSettings;
import org.fudgemsg.wire.types.FudgeWireType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;

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

  private final FudgeJSONSettings _settings;
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
    this(fudgeContext, reader, new FudgeJSONSettings());
  }

  /**
   * Creates a new instance for reading a Fudge stream from a JSON reader.
   *
   * @param fudgeContext  the Fudge context, not null
   * @param reader  the underlying reader, not null
   * @param settings  the JSON settings to fine tune the read, not null
   */
  public FudgeMsgJSONReader(final FudgeContext fudgeContext, final Reader reader, final FudgeJSONSettings settings) {
    _fudgeContext = fudgeContext;
    _underlying = reader;
    _settings = settings;
    try {
      _jsonObject = new JSONObject(new JSONTokener(reader));
      init(_fudgeContext, _jsonObject, _settings);
    } catch (final JSONException ex) {
      wrapException("Creating json object from reader", ex);
    }
  }

  private void init(final FudgeContext fudgeContext, final JSONObject jsonObject, final FudgeJSONSettings settings) throws JSONException {
    final String processingDirectivesField = settings.getProcessingDirectivesField();
    if (jsonObject.has(processingDirectivesField)) {
      _processingDirectives = integerValue(jsonObject.get(processingDirectivesField));
      _envelopeAttibutesFields.add(processingDirectivesField);
    }
    final String schemaVersionField = getSettings().getSchemaVersionField();
    if (jsonObject.has(schemaVersionField)) {
      _schemaVersion = integerValue(jsonObject.get(schemaVersionField));
      _envelopeAttibutesFields.add(schemaVersionField);
    }
    final String taxonomyField = getSettings().getTaxonomyField();
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
  public FudgeJSONSettings getSettings() {
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
      final JSONObject meta = (JSONObject) _jsonObject.get("meta");
      final JSONObject data = (JSONObject) _jsonObject.get("data");
      final MutableFudgeMsg msg = processFields(data, meta);
      msgEnv = getFudgeMsgEnvelope(msg, _jsonObject);
    } catch (final JSONException ex) {
      wrapException("reading message envelope", ex);
    }
    return msgEnv;
  }

  private FudgeMsgEnvelope getFudgeMsgEnvelope(final MutableFudgeMsg fudgeMsg, final JSONObject jsonObject) throws JSONException {
    return new FudgeMsgEnvelope(fudgeMsg, _schemaVersion, _processingDirectives);
  }

  private int integerValue(final Object o) {
    if (o instanceof Number) {
      return ((Number) o).intValue();
    } else if (o instanceof String) {
      return Integer.parseInt((String) o);
    } else {
      throw new NumberFormatException(o + " is not a number");
    }
  }

  private byte byteValue(final Object o) {
    if (o instanceof Number) {
      return ((Number) o).byteValue();
    } else if (o instanceof String) {
      return Byte.parseByte((String) o);
    } else {
      throw new NumberFormatException(o + " is not a number");
    }
  }

  private short shortValue(final Object o) {
    if (o instanceof Number) {
      return ((Number) o).shortValue();
    } else if (o instanceof String) {
      return Short.parseShort((String) o);
    } else {
      throw new NumberFormatException(o + " is not a number");
    }
  }

  private long longValue(final Object o) {
    if (o instanceof Number) {
      return ((Number) o).longValue();
    } else if (o instanceof String) {
      return Long.parseLong((String) o);
    } else {
      throw new NumberFormatException(o + " is not a number");
    }
  }

  private double doubleValue(final Object o) {
    if (o instanceof Number) {
      return ((Number) o).doubleValue();
    } else if (o instanceof String) {
      return Double.parseDouble((String) o);
    } else {
      throw new NumberFormatException(o + " is not a number");
    }
  }

  private float floatValue(final Object o) {
    if (o instanceof Number) {
      return ((Number) o).floatValue();
    } else if (o instanceof String) {
      return Float.parseFloat((String) o);
    } else {
      throw new NumberFormatException(o + " is not a number");
    }
  }

  private MutableFudgeMsg processFields(final JSONObject data, final JSONObject meta) {
    final MutableFudgeMsg fudgeMsg = getFudgeContext().newMessage();
    @SuppressWarnings("unchecked")
    final
    Iterator<String> keys = data.keys();
    while (keys.hasNext()) {
      final String fieldName = keys.next();
      final Object dataValue = getFieldValue(data, fieldName);
      final Object metaValue = getFieldValue(meta, fieldName);
      if (dataValue instanceof JSONObject) {
        final MutableFudgeMsg subMsg = processFields((JSONObject) dataValue, (JSONObject) metaValue);
        addField(fudgeMsg, fieldName, FudgeWireType.SUB_MESSAGE, subMsg);
      } else if (dataValue instanceof JSONArray) {
        final JSONArray dataArray = (JSONArray) dataValue;
        if (dataArray.length() > 0) {
          if (isPrimitiveArray(metaValue)) {
            try {
              final FudgeFieldType fieldType = getFieldType((String) metaValue);
              if (fieldType == null) {
                throw new OpenGammaRuntimeException("Unknown field type " + metaValue + " for " + fieldName + ":" + dataValue);
              }
              final Object primitiveArray = jsonArrayToPrimitiveArray(dataArray, fieldType);
              addField(fudgeMsg, fieldName, getFieldType((String) metaValue), primitiveArray);
            } catch (final JSONException e) {
              wrapException("converting json array to primitive array", e);
            }
          } else {
            //treat as repeated fields
            addRepeatedFields(fudgeMsg, fieldName, dataArray, (JSONArray) metaValue);
          }
        }
      } else {
        final FudgeFieldType fieldType = getFieldType((String) metaValue);
        if (fieldType == null) {
          throw new OpenGammaRuntimeException("Unknown field type " + metaValue + " for " + fieldName + ":" + dataValue);
        }
        addField(fudgeMsg, fieldName, fieldType, dataValue);
      }
    }
    return fudgeMsg;
  }

  private boolean isPrimitiveArray(final Object metaValue) {
    if (metaValue instanceof JSONArray) {
      return false;
    }
    return true;
  }

  private void addField(final MutableFudgeMsg fudgeMsg, final String fieldName, final FudgeFieldType fieldType, final Object fieldValue) {
    Integer ordinal = null;
    String name = null;
    try {
      ordinal = Integer.parseInt(fieldName);
    } catch (final NumberFormatException nfe) {
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
    switch (fieldType.getTypeId()) {
      case FudgeWireType.BYTE_TYPE_ID:
        fudgeMsg.add(name, ordinal, fieldType, byteValue(fieldValue));
        break;
      case FudgeWireType.SHORT_TYPE_ID:
        fudgeMsg.add(name, ordinal, fieldType, shortValue(fieldValue));
        break;
      case FudgeWireType.DOUBLE_TYPE_ID:
        fudgeMsg.add(name, ordinal, fieldType, doubleValue(fieldValue));
        break;
      case FudgeWireType.FLOAT_TYPE_ID:
        fudgeMsg.add(name, ordinal, fieldType, floatValue(fieldValue));
        break;
      case FudgeWireType.DATE_TYPE_ID:
        fudgeMsg.add(name, ordinal, fieldType, toLocalDate(fieldValue));
        break;
      case FudgeWireType.LONG_TYPE_ID:
        fudgeMsg.add(name, ordinal, fieldType, longValue(fieldValue));
        break;
      default:
        fudgeMsg.add(name, ordinal, fieldType, fieldValue);
        break;
    }
  }

  private LocalDate toLocalDate(final Object fieldValue) {
    if (fieldValue != null) {
      return LocalDate.parse((String) fieldValue);
    }
    return null;
  }

  private boolean getPreserveFieldNames() {
    return getSettings().getPreserveFieldNames();
  }

  private void addRepeatedFields(final MutableFudgeMsg fudgeMsg, final String fieldName, final JSONArray dataArray, final JSONArray metaArray) {
    try {
      for (int i = 0; i < dataArray.length(); i++) {
        final Object arrValue = dataArray.get(i);
        final Object metaValue = metaArray.get(i);
        if (arrValue instanceof JSONObject) {
          final MutableFudgeMsg subMsg = processFields((JSONObject) arrValue, (JSONObject) metaValue);
          addField(fudgeMsg, fieldName, FudgeWireType.SUB_MESSAGE, subMsg);
        } else {
          final FudgeFieldType fieldType = getFieldType((String) metaValue);
          if (fieldType == null) {
            throw new OpenGammaRuntimeException("Unknown field type " + metaValue + " for " + fieldName + ":" + arrValue);
          }
          addField(fudgeMsg, fieldName, fieldType, arrValue);
        }
      }
    } catch (final JSONException e) {
      wrapException("adding repeated fields", e);
    }
  }

  private FudgeFieldType getFieldType(final String typeValue) {
    final Integer typeId = getSettings().stringToFudgeTypeId(typeValue);
    return getFudgeContext().getTypeDictionary().getByTypeId(typeId);
  }

  private Object jsonArrayToPrimitiveArray(final JSONArray arr, final FudgeFieldType fieldType) throws JSONException {
    switch (fieldType.getTypeId()) {
      case FudgeWireType.BYTE_ARRAY_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_4_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_8_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_16_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_20_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_32_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_64_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_128_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_256_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_512_TYPE_ID:
        final byte[] byteArr = new byte[arr.length()];
        for (int j = 0; j < byteArr.length; j++) {
          byteArr[j] = ((Number) arr.get(j)).byteValue();
        }
        return byteArr;
      case FudgeWireType.SHORT_ARRAY_TYPE_ID:
        final short[] shortArr = new short[arr.length()];
        for (int j = 0; j < shortArr.length; j++) {
          shortArr[j] = ((Number) arr.get(j)).shortValue();
        }
        return shortArr;
      case FudgeWireType.INT_ARRAY_TYPE_ID:
        final int[] intArr = new int[arr.length()];
        for (int j = 0; j < intArr.length; j++) {
          intArr[j] = ((Number) arr.get(j)).intValue();
        }
        return intArr;
      case FudgeWireType.LONG_ARRAY_TYPE_ID:
        final long[] longArr = new long[arr.length()];
        for (int j = 0; j < longArr.length; j++) {
          longArr[j] = ((Number) arr.get(j)).longValue();
        }
        return longArr;
      case FudgeWireType.DOUBLE_ARRAY_TYPE_ID:
        final double[] doubleArr = new double[arr.length()];
        for (int j = 0; j < doubleArr.length; j++) {
          doubleArr[j] = ((Number) arr.get(j)).doubleValue();
        }
        return doubleArr;
      case FudgeWireType.FLOAT_ARRAY_TYPE_ID:
        final float[] floatArr = new float[arr.length()];
        for (int j = 0; j < floatArr.length; j++) {
          floatArr[j] = ((Number) arr.get(j)).floatValue();
        }
        return floatArr;
      default:
        return null;
    }
  }

  private Object getFieldValue(final JSONObject jsonObject, final String fieldName) {
    Object fieldValue = null;
    try {
      fieldValue = jsonObject.get(fieldName);
      if (JSONObject.NULL.equals(fieldValue)) {
        fieldValue = IndicatorType.INSTANCE;
      }
    } catch (final JSONException e) {
      wrapException("reading field value", e);
    }
    return fieldValue;
  }

}
