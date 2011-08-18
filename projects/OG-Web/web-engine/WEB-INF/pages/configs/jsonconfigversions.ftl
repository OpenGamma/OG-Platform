<#escape x as x?html>
{
    "header": {
        "type": "ConfigHistory",
        <#if versionsResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "page": ${"${paging.page}"?replace(',','')},
        "pageSize": ${"${paging.pagingSize}"?replace(',','')},
        </#if>
        "object_id": "${config.uniqueId.objectId}",
        "dataFields": ["version", "name", "validFrom", "validTo", "correctionFrom", "correctionTo"]
    },
    "data": [<#if versionsResult??>
        <#list versionsResult.documents as item>
        "${item.uniqueId.version}|${item.value.name}|${item.versionFromInstant}|${item.versionToInstant}|${item.correctionFromInstant}|${item.correctionToInstant}"<#if item_has_next>,</#if>
        </#list>
     </#if>]
}
</#escape>