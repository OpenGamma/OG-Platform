<#escape x as x?html>
<#include "security-header.ftl"> 
        "callAmount":"${security.callAmount}",
        "callCurrency":"${security.callCurrency}",
        "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "isLong":"${security.long?string?upper_case}",
        "putAmount":"${security.putAmount}",
        "putCurrency":"${security.putCurrency}",
        "settlementDate":"${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}",
        "deliveryCurrency":"${security.deliveryCurrency}",
<#include "security-footer.ftl"> 
</#escape>
