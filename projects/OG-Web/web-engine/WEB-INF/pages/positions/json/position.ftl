{
    "template_data": {
        "name": "${position.name}",
        "object_id": "${position.uniqueId.objectId}",
        <#if position.uniqueId.version?has_content>
        "version_id": "${position.uniqueId.version}",
        </#if>
        <#if timeSeriesId?has_content>"hts_id": "${timeSeriesId}",</#if>
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
    <#--if attributes?has_content-->
    "attributes" : { <#list attributes?keys as key>"${key}":"${attributes[key]}"<#if key_has_next>,</#if></#list> },
    <#-- /#if -->
    <#if position.trades?has_content>
    "trades": [
        <#list position.trades as trade>{
            <#assign dealAttr = tradeAttrModel.getDealAttributes(trade.uniqueId)>
            <#assign userAttr = tradeAttrModel.getUserAttributes(trade.uniqueId)>
            <#if dealAttr?has_content || userAttr?has_content>
            "attributes":{<#if dealAttr?has_content>"dealAttributes" : {<#list dealAttr?keys as key>"${key}":"${dealAttr[key]}"<#if key_has_next>,</#if></#list>}<#if userAttr?has_content>,</#if> </#if>
                          <#if userAttr?has_content>"userAttributes" : {<#list userAttr?keys as key>"${key}":"${userAttr[key]}"<#if key_has_next>,</#if></#list>} </#if>},
            </#if>
            "id": "${trade.uniqueId.objectId}",
            "trade_date_time": "<#if trade.tradeDate?has_content>${trade.tradeDate}</#if><#if trade.tradeTime?has_content> ${trade.tradeTime.toString(timeFormatterJson)} (${trade.tradeTime.toString(offsetFormatterJson)})</#if>",
            "premium": "<#if trade.premium?has_content>${trade.premium}</#if><#if trade.premiumCurrency?has_content> ${trade.premiumCurrency}</#if>",
            "premium_date_time": "<#if trade.premiumDate?has_content>${trade.premiumDate}</#if><#if trade.premiumTime?has_content> ${trade.premiumTime.toString(timeFormatterJson)} (${trade.premiumTime.toString(offsetFormatterJson)})</#if>",
            "quantity": "${trade.quantity}",
            "counterParty": "${trade.counterpartyExternalId}",
            "tradeDate": "${trade.tradeDate}"

        }<#if trade_has_next>,</#if></#list>
    ],
    </#if>
    "securities": [
        <#list position.securityLink.externalId.externalIds as item>{"scheme": "${item.scheme.name}", "value": "${item.value}" }<#if item_has_next>,</#if></#list>
    ],
    "xml":"${positionXml!''}"
}