<#escape x as x?html>
{
    "template_data": {
        "name": "${position.name}",
        "object_id": "${position.uniqueId.objectId}",
        "version_id": "${position.uniqueId.version}",
        <#if deleted>
        "deleted": "${positionDoc.versionToInstant}",
        </#if>
        "quantity": "${position.quantity}"
    },
    <#if security?has_content>
    "security": {
        "name": "${security.name}",
        "unique_id": "${security.uniqueId.objectId}",
        "security_type": "${security.securityType}"
    },
    </#if>
    "securities": [
        <#list position.securityKey.identifiers as item>{
            "scheme": "${item.scheme.name}",
            "value": "${item.value}"
        }<#if item_has_next>,</#if></#list>
    ],
    "trades": [
        <#list position.trades as item>{
            "id": "${item.uniqueId.objectId}",
            "quantity": "${item.quantity}",
            "counterParty": "${item.counterpartyKey}",
            "date": "${item.tradeDate}"
        }<#if item_has_next>,</#if></#list>
    ]
}
</#escape>