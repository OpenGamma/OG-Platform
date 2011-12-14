<#escape x as x?html>
{
    "template_data": {
        "name": "${position.name}",
        "object_id": "${position.uniqueId.objectId}",
        "version_id": "${position.uniqueId.version}",
	"hts_id": "${timeSeriesId}",
        <#if deleted>
        "deleted": "${positionDoc.versionToInstant}",
        </#if>
        <#if security?has_content>
        "security_name": "${security.name}",
        "security_unique_id": "${security.uniqueId.objectId}",
        "security_type": "${security.securityType}",
        </#if>
        <#if position.securityLink.objectId?has_content>
        "security_object_id": "${position.securityLink.objectId}",
        </#if>
        "quantity": "${position.quantity}"
    },
    "securities": [
        <#list position.securityLink.externalId.externalIds as item>{
            "scheme": "${item.scheme.name}",
            "value": "${item.value}"
        }<#if item_has_next>,</#if></#list>
    ],
    "trades": [
        <#list position.trades as trade>{
            "id": "${trade.uniqueId.objectId}",
            "premium": "${trade.premium}",
            "premiumCurrency": "${trade.premiumCurrency}",
            "premiumDate": "${trade.premiumDate}",
            <#if trade.premiumTime?has_content>
            "premiumTime": "${trade.premiumTime.toString(timeFormatter)}",
            "premiumTimeOffset": "${trade.premiumTime.toString(offsetFormatter)}",
            </#if>
            "quantity": "${trade.quantity}",
            "counterParty": "${trade.counterpartyExternalId}",
            "tradeDate": "${trade.tradeDate}",
            <#if trade.tradeTime?has_content>
            "tradeTime": "${trade.tradeTime.toString(timeFormatter)}",
            "tradeTimeOffset": "${trade.tradeTime.toString(offsetFormatter)}",
            </#if>
            <#assign dealAttr = tradeAttrModel.getDealAttributes(trade.uniqueId)>
            <#assign userAttr = tradeAttrModel.getUserAttributes(trade.uniqueId)>
            "attributes":{"dealAttributes" : {<#list dealAttr?keys as key>"${key}":"${dealAttr[key]}"<#if key_has_next>,</#if></#list>}, 
                          "userAttributes" : {<#list userAttr?keys as key>"${key}":"${userAttr[key]}"<#if key_has_next>,</#if></#list>}}
        }<#if trade_has_next>,</#if></#list>
    ]
}
</#escape>
