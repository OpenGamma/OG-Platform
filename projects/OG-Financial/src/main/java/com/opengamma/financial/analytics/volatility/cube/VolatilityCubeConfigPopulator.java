/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.time.Tenor;

/**
 * Loads a default vol cube defns into the configuration database.
 */
public class VolatilityCubeConfigPopulator {

  private static final Logger s_logger = LoggerFactory.getLogger(VolatilityCubeConfigPopulator.class);

  public VolatilityCubeConfigPopulator(ConfigMaster cfgMaster) {
    populateVolatilityCubeConfigMaster(cfgMaster);
  }

  public static ConfigMaster populateVolatilityCubeConfigMaster(ConfigMaster cfgMaster) {
    ConfigItem<VolatilityCubeDefinition<Tenor, Tenor, Double>> item = ConfigItem.of(createDefaultDefinition("DEFAULT_USD", UniqueId.of("WAHT", "WAHT")));  //TODO waht UID ????
    item.setName("DEFAULT_USD");
    s_logger.debug("Populating vol cube defn " + item.getName());
    ConfigMasterUtils.storeByName(cfgMaster, item);
    return cfgMaster;
  }

  private static VolatilityCubeDefinition<Tenor, Tenor, Double> createDefaultDefinition(final String name, final UniqueIdentifiable target) {

    Tenor[] swapTenors = new Tenor[] {Tenor.ofMonths(3),
      Tenor.ofYears(1),
      Tenor.ofYears(2),
      Tenor.ofYears(5),
      Tenor.ofYears(10),
      Tenor.ofYears(15),
      Tenor.ofYears(20),
      Tenor.ofYears(30) };
    Tenor[] optionExpiries = new Tenor[] {Tenor.ofMonths(3),
      Tenor.ofMonths(6),
      Tenor.ofYears(1),
      Tenor.ofYears(2),
      Tenor.ofYears(4),
      Tenor.ofYears(5),
      Tenor.ofYears(10),
      Tenor.ofYears(15),
      Tenor.ofYears(20) };

    int[] values = new int[] {0, 20, 25, 50, 70, 75, 100, 200, 5 };
    List<Double> relativeStrikes = new ArrayList<>(values.length * 2 - 1);
    for (int value : values) {
      relativeStrikes.add(Double.valueOf(value));
      if (value != 0) {
        relativeStrikes.add(Double.valueOf(-value));
      }
    }
    Collections.sort(relativeStrikes);

    return new VolatilityCubeDefinition<>(name, target, swapTenors, optionExpiries, relativeStrikes.toArray(new Double[0]));
  }

}
