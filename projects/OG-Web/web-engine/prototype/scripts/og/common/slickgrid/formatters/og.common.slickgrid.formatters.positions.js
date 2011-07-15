/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.slickgrid.formatters.positions',
    dependencies: ['og.common.routes'],
    obj: function () {
        return function (row, cell, value, columnDef, dataContext) {
            var routes = og.common.routes,
                rule = og.views.positions.rules['load_positions'],
                href = routes.hash(rule, {id: dataContext.id});
            return  '<a href="#' + href + '">' + dataContext.name + '</a>';
        };
    }
});