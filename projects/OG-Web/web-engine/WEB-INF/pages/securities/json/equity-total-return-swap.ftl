<#escape x as x?html>
<#include "security-header.ftl">
    "effectiveDate":"${security.effectiveDate}",
    "maturityDate":"${security.maturityDate}",
    "numberOfShares":"${security.numberOfShares}",
    "notionalCurrency":"${security.notionalCurrency}",
    "notionalAmount":"${security.notionalAmount}",
    "dividendPercentage":"${security.dividendPercentage}",
<#include "security-footer.ftl">
</#escape>