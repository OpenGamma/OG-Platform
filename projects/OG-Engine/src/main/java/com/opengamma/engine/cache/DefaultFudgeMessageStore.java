/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.EncodedFudgeMsg;
import org.fudgemsg.wire.FudgeDataOutputStreamWriter;
import org.fudgemsg.wire.FudgeEncoded;

import com.opengamma.util.ArgumentChecker;

/**
 * A default {@link FudgeMessageStore} implementation that serializes messages into and out
 * of a {@link BinaryDataStore}.
 */
public class DefaultFudgeMessageStore implements FudgeMessageStore {

  private final BinaryDataStore _binaryData;
  private final FudgeContext _fudgeContext;

  public DefaultFudgeMessageStore(final BinaryDataStore binaryData, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(binaryData, "binaryData");
    _binaryData = binaryData;
    _fudgeContext = fudgeContext;
  }

  private BinaryDataStore getBinaryData() {
    return _binaryData;
  }

  private FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public void delete() {
    getBinaryData().delete();
  }

  @Override
  public FudgeMsg get(long identifier) {
    final byte[] data = getBinaryData().get(identifier);
    return (data != null) ? new EncodedFudgeMsg(data, getFudgeContext()) : null;
  }

  @Override
  public Map<Long, FudgeMsg> get(Collection<Long> identifiers) {
    final Map<Long, byte[]> dataValues = getBinaryData().get(identifiers);
    final Map<Long, FudgeMsg> resultValues = new HashMap<Long, FudgeMsg>();
    for (Map.Entry<Long, byte[]> data : dataValues.entrySet()) {
      resultValues.put(data.getKey(), new EncodedFudgeMsg(data.getValue(), getFudgeContext()));
    }
    return resultValues;
  }

  @Override
  public void put(long identifier, FudgeMsg dataMessage) {
    final byte[] data;
    if (dataMessage instanceof FudgeEncoded) {
      data = ((FudgeEncoded) dataMessage).getFudgeEncoded();
    } else {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final FudgeDataOutputStreamWriter writer = new FudgeDataOutputStreamWriter(getFudgeContext(), baos);
      writer.writeFields(dataMessage);
      data = baos.toByteArray();
    }
    getBinaryData().put(identifier, data);
  }

  @Override
  public void put(Map<Long, FudgeMsg> dataMessages) {
    final Map<Long, byte[]> dataBytes = new HashMap<Long, byte[]>();
    ByteArrayOutputStream baos = null;
    FudgeDataOutputStreamWriter writer = null;
    for (Map.Entry<Long, FudgeMsg> dataMessage : dataMessages.entrySet()) {
      final byte[] data;
      if (dataMessage.getValue() instanceof FudgeEncoded) {
        data = ((FudgeEncoded) dataMessage.getValue()).getFudgeEncoded();
      } else {
        if (baos == null) {
          baos = new ByteArrayOutputStream();
          writer = new FudgeDataOutputStreamWriter(getFudgeContext(), baos);
        } else {
          baos.reset();
        }
        writer.writeFields(dataMessage.getValue());
        data = baos.toByteArray();
      }
      dataBytes.put(dataMessage.getKey(), data);
    }
    getBinaryData().put(dataBytes);
  }
}
