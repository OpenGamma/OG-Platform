/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeTypeDictionary;

import com.opengamma.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastListIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMapIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMapLongDoubleTimeSeries;
import com.opengamma.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.localdate.LocalDateEpochDaysConverter;
import com.opengamma.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.MapZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ZonedDateTimeEpochMillisConverter;

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
      fudgeContext.getObjectDictionary().addAllAnnotatedBuilders();
      fudgeContext.getTypeDictionary().addAllAnnotatedSecondaryTypes();
      
      FudgeTypeDictionary td = fudgeContext.getTypeDictionary();
      td.registerClassRename("com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries", FastArrayIntDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.fast.integer.FastListIntDoubleTimeSeries", FastListIntDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.fast.integer.FastMapIntDoubleTimeSeries", FastMapIntDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries", FastArrayLongDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.fast.longint.FastListLongDoubleTimeSeries", FastListLongDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.fast.longint.FastMapLongDoubleTimeSeries", FastMapLongDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries", ArrayLocalDateDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries", ListLocalDateDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries", MapLocalDateDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries", ArrayZonedDateTimeDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries", ListZonedDateTimeDoubleTimeSeries.class);
      td.registerClassRename("com.opengamma.util.timeseries.zoneddatetime.MapZonedDateTimeDoubleTimeSeries", MapZonedDateTimeDoubleTimeSeries.class);
      
      td.registerClassRename("com.opengamma.util.timeseries.localdate.LocalDateEpochDaysConverter", LocalDateEpochDaysConverter.class);
      td.registerClassRename("com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeEpochMillisConverter", ZonedDateTimeEpochMillisConverter.class);

      return fudgeContext;
    }
  }

}
