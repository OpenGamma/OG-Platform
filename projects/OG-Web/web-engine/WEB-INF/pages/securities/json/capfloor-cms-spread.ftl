<#escape x as x?html>
<#include "security-header.ftl"> 
        "startDate":"${security.startDate.toLocalDate()} - ${security.startDate.zone}",
        "maturityDate":"${security.maturityDate.toLocalDate()} - ${security.maturityDate.zone}",
        "notional":"${security.notional}",
        "longId":"${security.longId.scheme}-${security.longId.value}",
        <#if longSecurity??>
          "underlyingName":"${longSecurity.name}",
          "underlyingOid":"${longSecurity.uniqueId.objectId}",
        </#if>
        "shortId":"${security.shortId.scheme}-${security.shortId.value}",
        "strike":"${security.strike}",
        <#if shortSecurity??>
          "underlyingName":"${shortSecurity.name}",
          "underlyingOid":"${shortSecurity.uniqueId.objectId}",
        </#if>
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
<#include "security-footer.ftl"> 
</#escape>
