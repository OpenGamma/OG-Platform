<#escape x as x?html>
<#include "security-header.ftl"> 
        "startDate":"${security.startDate.toLocalDate()} - ${security.startDate.zone}",
        "maturityDate":"${security.maturityDate.toLocalDate()} - ${security.maturityDate.zone}",
        "notional":"${security.notional}",
        "underlyingId":"${security.underlyingId.scheme}-${security.underlyingId.value}",
        <#if underlyingSecurity??>
          "underlyingName":"${underlyingSecurity.name}",
          "underlyingOid":"${underlyingSecurity.uniqueId.objectId}",
        </#if>
        "strike":"${security.strike}",
        "frequency":"${security.frequency.conventionName}",
        "currency":"${security.currency}",
        "dayCount":"${security.dayCount.conventionName}",
        <#if security.payer>
          "Direction":"Pay",
        <#else>
          "Direction":"Receive",
        </#if>
        <#if security.cap>
          "Cap/Floor":"Cap",
        <#else>
          "Cap/Floor":"Floor",
        </#if>
        <#if security.ibor>
          "Type":"IBOR",
        <#else>
          "Type":"CMS",
        </#if>
<#include "security-footer.ftl"> 
</#escape>
