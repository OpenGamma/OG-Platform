<#escape x as x?html>
<#include "security-header.ftl"> 
        "underlyingSpotId":"${security.spotUnderlyingId}",
        "currency":"${security.currency}",
        "strike":"${security.strike}",
        "notional":"${security.notional}",
        "parameterizedAsVariance":"${security.parameterizedAsVariance?string?upper_case}",
        "annualizationFactor":"${security.annualizationFactor}",
        "firstObservationDate":"${security.firstObservationDate.toLocalDate()} - ${security.firstObservationDate.zone}",
        "lastObservationDate":"${security.lastObservationDate.toLocalDate()} - ${security.lastObservationDate.zone}",
        "settlementDate":"${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}",
        "regionId":"${security.regionId.scheme} - ${security.regionId.value}",
        "observationFrequency":"${security.observationFrequency.conventionName}",
<#include "security-footer.ftl"> 
</#escape>
