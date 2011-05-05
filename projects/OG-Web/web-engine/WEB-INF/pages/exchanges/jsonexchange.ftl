<#escape x as x?html>
{
    "templateData": {
      "name": "${exchange.name}",
      "objectId":"${exchange.uniqueId.objectId}",
      "versionId":"${exchange.uniqueId.version}",
<#if deleted>
      "deleted":"${exchangeDoc.versionToInstant}",
</#if>
    },
    "regionKey" : {
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