<#escape x as x?html>
{
    "header": {
        "type": "RegionHistory",
        <#if versionsResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
        </#if>
        "object_id": "${region.uniqueId.objectId}",
        "dataFields": ["version", "name", "validFrom", "validTo", "correctionFrom", "correctionTo"]
    },
    "data": [<#if versionsResult??>
        <#list versionsResult.documents as item>
        "${item.uniqueId.version}|${item.region.name}|${item.versionFromInstant}|${item.versionToInstant}|${item.correctionFromInstant}|${item.correctionToInstant}"<#if item_has_next>,</#if>
        </#list>
     </#if>]
}
</#escape>
