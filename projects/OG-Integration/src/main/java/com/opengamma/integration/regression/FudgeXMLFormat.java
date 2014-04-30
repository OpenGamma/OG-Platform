/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.xml.FormattingXmlStreamWriter;

/**
 * Implementation of I/O using an XML representation of the Fudge binary encoding. Note that this is simply a human readable form of the Fudge binary data - as a result it is not as efficient or
 * appear as natural as other XML representations which may be available.
 */
public class FudgeXMLFormat implements RegressionIO.Format {

  /**
   * The default file extension.
   */
  /* package */static final String FILE_EXTENSION = ".xml";

  private static final Logger s_logger = LoggerFactory.getLogger(FudgeXMLFormat.class);

  private static final class Context {

    private final FudgeContext _ctx;

    private final FudgeSerializer _serializer;

    private final FudgeDeserializer _deserializer;

    private Context(final FudgeContext ctx, final FudgeSerializer write, final FudgeDeserializer read) {
      _ctx = ctx;
      _serializer = write;
      _deserializer = read;
    }

    public static Context of(final FudgeContext ctx, final FudgeSerializer write, final FudgeDeserializer read) {
      if ((write != null) || (read != null)) {
        return new Context(ctx, write, read);
      } else {
        return null;
      }
    }

  }

  @Override
  public Object openRead(final Object context) {
    if (context == null) {
      final FudgeContext ctx = OpenGammaFudgeContext.getInstance();
      return new Context(ctx, null, new FudgeDeserializer(ctx));
    } else {
      final Context ctx = (Context) context;
      assert ctx._deserializer == null;
      return new Context(ctx._ctx, ctx._serializer, new FudgeDeserializer(ctx._ctx));
    }
  }

  @Override
  public Object openWrite(final Object context) {
    if (context == null) {
      final FudgeContext ctx = OpenGammaFudgeContext.getInstance();
      return new Context(ctx, new FudgeSerializer(ctx), null);
    } else {
      final Context ctx = (Context) context;
      assert ctx._serializer == null;
      return new Context(ctx._ctx, new FudgeSerializer(ctx._ctx), ctx._deserializer);
    }
  }

  @Override
  public String getLogicalFileExtension(final Object context) {
    return FILE_EXTENSION;
  }

  @Override
  public void write(final Object context, final Object o, final OutputStream dest) throws IOException {
    final Context ctx = (Context) context;
    final Writer writer = new OutputStreamWriter(dest);
    FormattingXmlStreamWriter xmlStreamWriter = FormattingXmlStreamWriter.builder(writer)
                                               .indent(true)
                                               .build();
    final FudgeXMLStreamWriter streamWriter = new FudgeXMLStreamWriter(ctx._ctx, xmlStreamWriter);
    // Don't close fudgeMsgWriter; the caller will close the stream later
    @SuppressWarnings("resource")
    FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(streamWriter);
    MutableFudgeMsg msg = ctx._serializer.objectToFudgeMsg(o);
    FudgeSerializer.addClassHeader(msg, o.getClass());
    fudgeMsgWriter.writeMessage(msg);
    fudgeMsgWriter.flush();
    writer.append("\n");
    s_logger.debug("Wrote object {}", o);
  }

  @Override
  public Object read(final Object context, final InputStream in) throws IOException {
    final Context ctx = (Context) context;
    final Reader reader = new InputStreamReader(in);
    FudgeXMLStreamReader streamReader = new FudgeXMLStreamReader(ctx._ctx, reader);
    // Don't close fudgeMsgReader; the caller will close the stream later
    @SuppressWarnings("resource")
    FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(streamReader);
    FudgeMsg msg = fudgeMsgReader.nextMessage();
    final Object object = ctx._deserializer.fudgeMsgToObject(msg);
    s_logger.debug("Read object {}", object);
    return object;
  }

  @Override
  public Object closeRead(final Object context) {
    final Context ctx = (Context) context;
    assert ctx._deserializer != null;
    return Context.of(ctx._ctx, ctx._serializer, null);
  }

  @Override
  public Object closeWrite(final Object context) {
    final Context ctx = (Context) context;
    assert ctx._serializer != null;
    return Context.of(ctx._ctx, null, ctx._deserializer);
  }

}
