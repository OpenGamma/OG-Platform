<#escape x as x?html>
<#include "security-header.ftl"> 
        "expirydate": {
            "datetime": "${security.expiry.expiry.toOffsetDateTime()}",
            "timezone": "${security.expiry.expiry.zone}"
        },
        "expiryAccuracy":"${security.expiry.accuracy?replace("_", " ")}",
        "tradingExchange":"${security.tradingExchange}",
        "settlementExchange":"${security.settlementExchange}",
        "currency":"${security.currency}",
        "unitAmount": ${security.unitAmount},
        <#if futureSecurityType == "BondFuture">
            "firstDeliveryDate":"${security.firstDeliveryDate}",
            "lastDeliveryDate":"${security.lastDeliveryDate}",
            "underlyingBond":{<#list basket?keys as key>"${key}":"${basket[key]}"<#if key_has_next>,</#if></#list>},
        <#else>
            <#if security.underlyingId?has_content>
              "underlyingId":"${security.underlyingId.scheme.name}-${security.underlyingId.value}",
            </#if>
            <#if underlyingSecurity?has_content>
              "underlyingName":"${underlyingSecurity.name}",
              "underlyingOid":"${underlyingSecurity.uniqueId.objectId}",
            </#if>
        </#if>
<#include "security-footer.ftl"> 
</#escape>