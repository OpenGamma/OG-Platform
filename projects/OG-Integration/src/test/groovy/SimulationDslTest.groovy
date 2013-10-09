/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

// TODO remove these once they're automatically imported by the script superclass
import com.opengamma.id.VersionCorrection
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

// nonsense simulation definition to test the DSL classes
simulation "test simulation", {
  baseScenarioName "base"
  calculationConfigurations "default", "config1"
  resolverVersionCorrection VersionCorrection.LATEST
  valuationTime ZonedDateTime.of(2011, 3, 8, 2, 18, 0, 0, ZoneOffset.UTC)
  scenario "scenario 1", {
    curve {
      named "Forward6M"
      currencies "USD", "GBP"
      apply {
        parallelShift 0.1
        singleShift 10, 0.2
      }
    }
    marketData {
      idMatches "BLOOMBERG_TICKER", ".* Curncy"
      apply {
        scaling 1.2
      }
    }
    surface {
      nameMatches "someSurface.*"
      quoteTypes "TYPE_A", "TYPE_B"
      apply {
        singleAdditiveShift 0.1, 0.1, 2.2
      }
    }
  }
  scenario "scenario 2", {
    valuationTime ZonedDateTime.of(1972, 3, 10, 21, 30, 0, 0, ZoneOffset.UTC)
    resolverVersionCorrection VersionCorrection.ofVersionAsOf(Instant.EPOCH)
    calculationConfigurations "config2", "config3"
    curve {
      named "Discounting"
      currencies "AUD"
      apply {
        parallelShift 0.05
      }
    }
    curve {
      named "Forward3M"
      currencies "AUD"
      apply {
        parallelShift 0.15
      }
    }
    marketData {
      idMatches "BLOOMBERG_TICKER", ".* Comdty"
      apply {
        scaling 0.9
      }
    }
    surface {
      quoteTypes "TYPE_C", "TYPE_D"
      apply {
        parallelShift 0.1
      }
    }
  }
}
