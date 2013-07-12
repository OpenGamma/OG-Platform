/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.AnnotationReflector;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeTypeDictionary;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;

/**
 * Provides a shared singleton {@code FudgeContext} for use throughout OpenGamma.
 * <p>
 * The {@code FudgeContext} is a low-level object necessary to use the Fudge messaging system.
 * Providing the context to Fudge on demand would clutter code and configuration.
 * This class instead provides a singleton that can be used whenever necessary.
 */
public final class OpenGammaFudgeContext {

  /**
   * Restricted constructor.
   */
  private OpenGammaFudgeContext() {
  }

  /**
   * Gets the singleton instance of the context, creating it if necessary.
   * @return the singleton instance, not null
   */
  public static FudgeContext getInstance() {
    return ContextHolder.INSTANCE;
  }

  /**
   * Avoid double-checked-locking using the Initialization-on-demand holder idiom.
   */
  static final class ContextHolder {
    static final FudgeContext INSTANCE = constructContext();
    private static FudgeContext constructContext() {
      FudgeContext fudgeContext = new FudgeContext();
      ExtendedFudgeBuilderFactory.init(fudgeContext.getObjectDictionary());
      InnerClassFudgeBuilderFactory.init(fudgeContext.getObjectDictionary());
      AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
      fudgeContext.getObjectDictionary().addAllAnnotatedBuilders(reflector);
      fudgeContext.getTypeDictionary().addAllAnnotatedSecondaryTypes(reflector);
      
      FudgeTypeDictionary td = fudgeContext.getTypeDictionary();
      td.registerClassRename("com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries", ImmutableZonedDateTimeDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries", ImmutableZonedDateTimeDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries", ImmutableZonedDateTimeDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries", ImmutableZonedDateTimeDoubleTimeSeries.class);
      
      td.registerClassRename("com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries", ImmutableLocalDateDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.timeseries.localdate.ArrayLocalDateDoubleTimeSeries", ImmutableLocalDateDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries", ImmutableLocalDateDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.timeseries.localdate.ListLocalDateDoubleTimeSeries", ImmutableLocalDateDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries", ImmutableLocalDateDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.timeseries.localdate.MapLocalDateDoubleTimeSeries", ImmutableLocalDateDoubleTimeSeries.class);

      return fudgeContext;
    }
  }

}
