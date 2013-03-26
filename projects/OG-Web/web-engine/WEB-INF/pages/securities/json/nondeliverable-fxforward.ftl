<#escape x as x?html>
<#include "security-header.ftl"> 
        "forwardDate":"${security.forwardDate.toLocalDate()} - ${security.forwardDate.zone}",
        "region":"${security.regionId.scheme}-${security.regionId.value}",
        "payCurrency":"${security.payCurrency}",
        "payAmount":"${security.payAmount}",
        "receiveCurrency":"${security.receiveCurrency}",
        "receiveAmount":"${security.receiveAmount}",
<#include "security-footer.ftl"> 
</#escape>
