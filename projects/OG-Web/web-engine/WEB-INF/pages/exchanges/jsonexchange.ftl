<#escape x as x?html>
{
    "name": "${exchange.name}",
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