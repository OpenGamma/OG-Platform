/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.slickgrid.formatters.portfolios',
    dependencies: ['og.common.routes'],
    obj: function () {
        return function (row, cell, value, columnDef, dataContext) {
            var routes = og.common.routes,
                rule = og.views.portfolios.rules['load_portfolios'],
                href = routes.hash(rule, {id: routes.current().args.id, node: dataContext.id});
            return  '<a href="#' + href + '">' + dataContext.name + '</a>';
        };
    }
});