/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.slickgrid.formatters.positions',
    dependencies: ['og.common.routes'],
    obj: function () {
        return function (row, cell, value, columnDef, dataContext) {
            return dataContext.quantity
                + '<div class="og-button"><div class="OG-icon og-icon-unhook"></div></div>'
        };
    }
});