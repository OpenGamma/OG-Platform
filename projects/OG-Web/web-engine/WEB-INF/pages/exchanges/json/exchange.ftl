<#escape x as x?html>
{
    "template_data": {
        "name": "${exchange.name}",
        "object_id": "${exchange.uniqueId.objectId}",
        "version_id": "${exchange.uniqueId.version}"
        <#if deleted>
        ,"deleted": "${exchangeDoc.versionToInstant}"
        </#if>
    },
    "regionKey": {
<#if exchange.regionIdBundle?has_content>
        <#list exchange.regionIdBundle.externalIds as item>
        "Region id": "${item.scheme.name} - ${item.value}"<#if item_has_next>,</#if>
        </#list>
</#if>
    },
    "identifiers": {
        <#list exchange.externalIdBundle.externalIds as item>
        "Key": "${item.scheme.name} - ${item.value}"<#if item_has_next>,</#if>
        </#list>
    }
    <#if exchange.timeZone?has_content>
    ,"timeZone": "${exchange.timeZone}"
    </#if>
}
</#escape>