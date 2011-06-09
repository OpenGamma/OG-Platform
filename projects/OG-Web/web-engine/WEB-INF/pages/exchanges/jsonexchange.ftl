<#escape x as x?html>
{
    "template_data": {
        "name": "${exchange.name}",
        "object_id": "${exchange.uniqueId.objectId}",
        "version_id": "${exchange.uniqueId.version}"
        <#if deleted>
        "deleted": "${exchangeDoc.versionToInstant}",
        </#if>
    },
    "regionKey": {
        <#list exchange.regionKey.identifiers as item>
        "Region id": "${item.scheme.name} - ${item.value}"<#if item_has_next>,</#if>
        </#list>
    },
    "identifiers": {
        <#list exchange.identifiers.identifiers as item>
        "Key": "${item.scheme.name} - ${item.value}"<#if item_has_next>,</#if>
        </#list>
    }
}
</#escape>