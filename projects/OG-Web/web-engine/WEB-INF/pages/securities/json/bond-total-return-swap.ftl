<#escape x as x?html>
<#include "security-header.ftl">
    "effectiveDate":"${security.effectiveDate}",
    "maturityDate":"${security.maturityDate}",
    "notionalCurrency":"${security.notionalCurrency}",
    "notionalAmount":"${security.notionalAmount}",
<#include "security-footer.ftl">
</#escape>