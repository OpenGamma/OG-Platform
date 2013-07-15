<#escape x as x?html>
{
    "template_data": {
        "name": "${holidayDoc.name}",
        "object_id": "${holidayDoc.uniqueId.objectId}",
        "version_id": "${holidayDoc.uniqueId.version}",
        <#if deleted>
        "deleted": "${holidayDoc.versionToInstant}",
        </#if>
        "type": "${holiday.type}"
    },
    "dates": {
        <#list holidayDatesByYear as item>
        "${item.first.value?c}": [<#list item.second as date> "${date?replace("${item.first.value?c}-","")}" <#if date_has_next>,</#if></#list>] <#if item_has_next>,</#if>
        </#list>
    }
}
</#escape>