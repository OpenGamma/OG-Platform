<#escape x as x?html>
<#include "security-header.ftl"> 
        "callAmount":"${security.callAmount}",
        "callCurrency":"${security.callCurrency}",
        "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "isLong":"${security.long?string?upper_case}",
        "putAmount":"${security.putAmount}",
        "putCurrency":"${security.putCurrency}",
        "paymentCurrency":"${security.paymentCurrency}",
        "settlementDate":"${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}",
<#include "security-footer.ftl"> 
</#escape>
