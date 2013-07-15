<#escape x as x?html>
<#include "security-header.ftl"> 
        "currency":"${security.currency}",
        "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "isCashSettled":"${security.cashSettled?string?upper_case}",
        "isLong":"${security.long?string?upper_case}",
        "isPayer":"${security.payer?string?upper_case}",
        "underlyingId":"${security.underlyingId.scheme}-${security.underlyingId.value}",
        "underlyingExternalId":"${security.underlyingId.scheme}-${security.underlyingId.value}",
        <#if underlyingSecurity??>
          "underlyingName":"${underlyingSecurity.name}",
          "underlyingOid":"${underlyingSecurity.uniqueId.objectId}",
        </#if>
<#include "security-footer.ftl"> 
</#escape>
