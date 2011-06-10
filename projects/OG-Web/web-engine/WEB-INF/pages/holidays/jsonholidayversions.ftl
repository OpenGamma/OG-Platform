<#escape x as x?html>
{
    "header": {
        "type": "HolidayHistory",
        <#if versionsResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "page": ${"${paging.page}"?replace(',','')},
        "pageSize": ${"${paging.pagingSize}"?replace(',','')},
        </#if>
        "object_id": "${holiday.uniqueId.objectId}",
        "dataFields": ["version", "name", "validFrom", "validTo", "correctionFrom", "correctionTo"]
    },
    "data": [<#if versionsResult??>
        <#list versionsResult.documents as item>
        "${item.uniqueId.version}|${item.name}|${item.versionFromInstant}|${item.versionToInstant}|${item.correctionFromInstant}|${item.correctionToInstant}"<#if item_has_next>,</#if>
        </#list>
     </#if>]
}
</#escape>
