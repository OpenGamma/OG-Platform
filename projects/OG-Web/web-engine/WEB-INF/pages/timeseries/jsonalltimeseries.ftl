<#escape x as x?html>
{
    "header": {
        "type": "Time Series",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
        </#if>
        "dataFields": ["id","identifier","data_source","data_provider","data_field","observation_time"]
    },
    "data": [<#if searchResult??><#list searchResult.documents as doc>
        "${doc.object.uniqueId.objectId}|<#list doc.object.externalIdBundle.externalIds as identifier>${identifier}<#if identifier_has_next>, </#if></#list>|${doc.object.dataSource}|${doc.object.dataProvider}|${doc.object.dataField}|${doc.object.observationTime}"
    <#if item_has_next>,</#if></#list> </#if>]
}
</#escape>