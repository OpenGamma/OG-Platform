/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.List;

import javax.time.Instant;

import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.financial.Currency;

/**
 * 
 */
public class ConfigDBInterpolatedYieldCurveDefinitionSource implements InterpolatedYieldCurveDefinitionSource {
  
  private ConfigSource _curveSource;

  public ConfigDBInterpolatedYieldCurveDefinitionSource(ConfigSource curveSource) {
    _curveSource = curveSource;
  }
  
  protected ConfigSource getConfigSource() {
    return _curveSource;
  }

  public YieldCurveDefinition getDefinition(Currency ccy, String name) {
    ConfigSearchRequest configSearchRequest = new ConfigSearchRequest();
    configSearchRequest.setName(name + "_" + ccy.getISOCode());
    List<YieldCurveDefinition> definitions = _curveSource.search(YieldCurveDefinition.class, configSearchRequest);
    if (definitions.size() > 0) {
      YieldCurveDefinition curveDefinition = definitions.iterator().next();
      return curveDefinition;
    } else {
      return null;
    }
  }
  
  public YieldCurveDefinition getDefinition(Currency ccy, String name, Instant version, Instant correction) {
    ConfigSearchRequest configSearchRequest = new ConfigSearchRequest();
    configSearchRequest.setName(name + "_" + ccy.getISOCode());
    configSearchRequest.setVersionAsOfInstant(version);
    List<YieldCurveDefinition> definitions = _curveSource.search(YieldCurveDefinition.class, configSearchRequest);
    if (definitions.size() > 0) {
      YieldCurveDefinition curveDefinition = definitions.iterator().next();
      return curveDefinition;
    } else {
      return null;
    }
  }
}
