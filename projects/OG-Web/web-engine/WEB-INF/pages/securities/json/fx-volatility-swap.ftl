<#escape x as x?html>
<#include "security-header.ftl">
      "currency":"${security.currency}",
      baseCurrency:"${security.baseCurrency}",
      counterCurrency:"${security.counterCurrency}",
      strike:"${security.strike}",
      notional:"${security.notional}",
      settlementDate:"${security.settlementDate}",
      maturityDate:"${security.maturityDate}",
      volatilitySwapType:"${security.volatilitySwapType.name}",
      annualizationFactor:"${security.annualizationFactor}",
      firstObservationDate:"${security.firstObservationDate}",
      lastObservationDate:"${security.lastObservationDate}",
      observationFrequency:"${security.observationFrequency}"},
<#include "security-footer.ftl">
</#escape>