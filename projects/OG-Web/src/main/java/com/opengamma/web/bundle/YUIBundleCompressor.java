/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Compressor implementation using YUI compressor.
 */
public class YUIBundleCompressor implements BundleCompressor {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(YUIBundleCompressor.class);

  /**
   * The compressor options.
   */
  private final YUICompressorOptions _compressorOptions;

  /**
   * Create a compressor.
   * 
   * @param compressorOptions  the YUICompressor options, not null
   */
  public YUIBundleCompressor(final YUICompressorOptions compressorOptions) {
    ArgumentChecker.notNull(compressorOptions, "compressorOptions");
    _compressorOptions = compressorOptions;
  }

  //-------------------------------------------------------------------------
  @Override
  public String compressBundle(Bundle bundle) {
    String source = BundleUtils.readBundleSource(bundle);
    return compress(source, bundle.getId());
  }

  private String compress(String content, String bundleId) {
    BundleType type = BundleType.getType(bundleId);
    switch (type) {
      case CSS:
        return compressCss(content);
      case JS:
        return compressJs(content);
      default:
        return content;
    }
  }

  private String compressJs(String content) {
    StringWriter writer = new StringWriter(1024);
    StringReader reader = new StringReader(content);
    try {
      JavaScriptCompressor jsCompressor = createJavaScriptCompressor(reader);
      jsCompressor.compress(writer, _compressorOptions.getLineBreakPosition(), _compressorOptions.isMunge(), _compressorOptions.isWarn(), 
          _compressorOptions.isPreserveAllSemiColons(), !_compressorOptions.isOptimize());
    } catch (IOException ex) {
      s_logger.error("Unexpected IOException", ex);
    }
    return writer.toString();
  }

  private JavaScriptCompressor createJavaScriptCompressor(Reader in) throws IOException {
    return new JavaScriptCompressor(in, new ErrorReporter() {
      private String getMessage(String source, String message, int line, int lineOffset) {
        String logMessage;
        if (line < 0) {
          logMessage = (source != null) ? source + ":" : "" + message;
        } else {
          logMessage = (source != null) ? source + ":" : "" + line + ":" + lineOffset + ":" + message;
        }
        return logMessage;
      }
      
      @Override
      public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        s_logger.warn(getMessage(sourceName, message, line, lineOffset));
      }
      
      @Override
      public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        s_logger.error(getMessage(sourceName, message, line, lineOffset));
        return new EvaluatorException(message);
      }
      
      @Override
      public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        s_logger.error(getMessage(sourceName, message, line, lineOffset));
      }
    });
  }

  private String compressCss(String content) {
    StringWriter stringWriter = new StringWriter(1024);
    try {
      CssCompressor compressor = new CssCompressor(new StringReader(content));
      compressor.compress(stringWriter, _compressorOptions.getLineBreakPosition());
    } catch (IOException ex) {
      s_logger.error("Unexpected IOException", ex);
    }
    return stringWriter.toString();
  }

}
