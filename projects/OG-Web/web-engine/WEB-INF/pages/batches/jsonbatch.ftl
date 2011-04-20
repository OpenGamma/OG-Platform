<#escape x as x?html>
{
    "templateData": {
            "name": "${batch.observationDate} - ${batch.observationTime}",
            "date": "${batch.observationDate}",
            "time": "${batch.observationTime}",
            "version": "${batch.status}",
            "total_results": "${resultPaging.totalItems}",
            "total_errors": "${errorPaging.totalItems}"
    },
    "data": {
        "batch_results": [
        <#list batchResult as item>
            "${item.calculationConfiguration}|${item.computedValue.specification.targetSpecification.uniqueId}|${item.computedValue.specification.valueName}|${item.computedValue.specification.functionUniqueId}|${item.computedValue.value}"
            <#if item_has_next>,</#if>
        </#list>
        ],
        "batch_errors": [
        <#list batchErrors as item>"
            ${item.calculationConfiguration}|
            ${item.computationTarget}|
            ${item.valueName}|
            ${item.functionUniqueId}|
            ${item.exceptionClass}|
            ${item.exceptionMsg}|
            ${item.stackTrace}
            "<#if item_has_next>,</#if>
        </#list>
        ]
    }
}
</#escape>