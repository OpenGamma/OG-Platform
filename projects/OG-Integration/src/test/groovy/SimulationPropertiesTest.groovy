/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

import com.opengamma.id.VersionCorrection
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime 

simulation "sim", {
  scenario "scen1", {
    resolverVersionCorrection VersionCorrection.ofVersionAsOf(Instant.EPOCH)
    calculationConfigurations "config2", "config3"
  }
  scenario "scen2", {
  }
  valuationTime ZonedDateTime.of(1972, 3, 10, 21, 30, 0, 0, ZoneOffset.UTC)
  calculationConfigurations "config0", "config1"
}
