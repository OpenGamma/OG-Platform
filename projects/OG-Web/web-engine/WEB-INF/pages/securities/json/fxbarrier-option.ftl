<#escape x as x?html>
<#include "security-header.ftl"> 
        "barrierDirection":"${security.barrierDirection}",
        "barrierLevel":"${security.barrierLevel}",
        "barrierType":"${security.barrierType}",
        "callAmount":"${security.callAmount}",
        "callCurrency":"${security.callCurrency}",
        "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "isLong":"${security.long?string?upper_case}",
        "monitoringType":"${security.monitoringType}",
        "putAmount":"${security.putAmount}",
        "putCurrency":"${security.putCurrency}",
        "samplingFrequency":"${security.samplingFrequency}",
        "settlementDate":"${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}",
<#include "security-footer.ftl"> 
</#escape>
