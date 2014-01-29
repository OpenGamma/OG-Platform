/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.OpenGammaRuntimeException;

/**
 * The type of the stub.
 */
public enum StubType {

  /**
   * No stub.
   */
  NONE {

    @Override
    public com.opengamma.analytics.financial.credit.isdastandardmodel.StubType toAnalyticsType() {
      return com.opengamma.analytics.financial.credit.isdastandardmodel.StubType.NONE;
    }
  },
  /**
   * Short stub at the start of the schedule.
   */
  SHORT_START {

    @Override
    public com.opengamma.analytics.financial.credit.isdastandardmodel.StubType toAnalyticsType() {
      return com.opengamma.analytics.financial.credit.isdastandardmodel.StubType.FRONTSHORT;
    }
  },
  /**
   * Long stub at the start of the schedule.
   */
  LONG_START {

    @Override
    public com.opengamma.analytics.financial.credit.isdastandardmodel.StubType toAnalyticsType() {
      return com.opengamma.analytics.financial.credit.isdastandardmodel.StubType.FRONTLONG;
    }
  },
  /**
   * Short stub at the end of the schedule.
   */
  SHORT_END {

    @Override
    public com.opengamma.analytics.financial.credit.isdastandardmodel.StubType toAnalyticsType() {
      return com.opengamma.analytics.financial.credit.isdastandardmodel.StubType.BACKSHORT;
    }
  },
  /**
   * Long stub at the end of the schedule.
   */
  LONG_END {

    @Override
    public com.opengamma.analytics.financial.credit.isdastandardmodel.StubType toAnalyticsType() {
      return com.opengamma.analytics.financial.credit.isdastandardmodel.StubType.BACKLONG;
    }
  },
  /**
   * Stubs at the both start and end of the schedule.
   */
  BOTH {

    @Override
    public com.opengamma.analytics.financial.credit.isdastandardmodel.StubType toAnalyticsType() {
      throw new OpenGammaRuntimeException("Unsupported ISDA stub type of BOTH");
    }
  };

  public abstract com.opengamma.analytics.financial.credit.isdastandardmodel.StubType toAnalyticsType();
}
