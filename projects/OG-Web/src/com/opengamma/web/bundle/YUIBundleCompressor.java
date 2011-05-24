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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.util.ArgumentChecker;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Compressor implementation using YUI compressor.
 */
public class YUIBundleCompressor implements CompressedBundleSource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(YUIBundleCompressor.class);

  /**
   * The bundle manager.
   */
  private final BundleManager _bundleManager;
  /**
   * The compressor options.
   */
  private final YUICompressorOptions _compressorOptions;

  /**
   * Create a compressor.
   * 
   * @param bundleManager       the bundle manager, not null
   * @param compressorOptions   the YUICompressor options, not null
   */
  public YUIBundleCompressor(BundleManager bundleManager, YUICompressorOptions compressorOptions) {
    ArgumentChecker.notNull(bundleManager, "bundleManager");
    ArgumentChecker.notNull(compressorOptions, "compressorOptions");
    
    _bundleManager = bundleManager;
    _compressorOptions = compressorOptions;
  }

  @Override
  public String getBundle(String bundleId) {
    Bundle bundle = _bundleManager.getBundle(bundleId);
    String source = readBundleSource(bundle);
    return compress(source, bundleId);
  }

  private String readBundleSource(Bundle bundle) {
    List<Fragment> allFragments = bundle.getAllFragments();
    StringBuilder buf = new StringBuilder(1024);
    for (Fragment fragment : allFragments) {
      try {
        buf.append(FileUtils.readFileToString(fragment.getFile()));
        buf.append("\n");
      } catch (IOException ex) {
        throw new DataNotFoundException("IOException reading " + fragment.getFile());
      }
    }
    return buf.toString();
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
