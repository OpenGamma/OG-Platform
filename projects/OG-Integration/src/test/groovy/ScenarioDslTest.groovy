/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

import com.opengamma.id.VersionCorrection
import org.threeten.bp.Instant

// nonsense scenario definition to test the DSL classes
scenario 'scenario name', {
  valuationTime Instant.now()
  resolverVersionCorrection VersionCorrection.LATEST
  calculationConfigurations 'Default', 'calcConfig1'
  curve {
    named 'Forward6M'
    currencies 'USD', 'GBP'
    apply {
      parallelShift 0.1
      singleShift 10, 0.2
    }
  }
  curve {
    named 'Forward3M'
    currencies 'AUD'
    apply {
      parallelShift 0.15
    }
  }
  marketData {
    idMatches 'BLOOMBERG_TICKER', '.* Curncy'
    apply {
      scaling 1.2
    }
  }
  surface {
    nameMatches 'someSurface.*'
    quoteTypes 'TYPE_A', 'TYPE_B'
    apply {
      multipleAdditiveShifts([0.1, 0.2], [1.1, 1.2], [2.3, 2.4])
      singleAdditiveShift 0.1, 0.1, 2.2
    }
  }
}
