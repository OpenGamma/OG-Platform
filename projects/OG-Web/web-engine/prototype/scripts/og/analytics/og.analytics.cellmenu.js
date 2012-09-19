/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.CellMenu',
    dependencies: ['og.common.routes'],
    obj: function () {
        var icons = '.og-num, .og-icon-new-window-2', open_icon = '.og-icon-right-chevron',
            expand_class = 'og-expanded', routes = og.common.routes;
        /**
         * TODO: replace with api / this temporary solution until the api is ready
         */
        var get_url = function (cell, panel) {
            /**
             * Mappings for cell data tyes / gadgets and panel defaults
             */
            var mappings = {
                gadgets: ['dep_graph', 'data', 'surface', 'curve', 'timeseries'],
                functions: [
                    function () {type = 'depgraph'; id = cell.col + '|' + cell.row},
                    function () {type = 'data'; id = cell.col + '|' + cell.row},
                    function () {type = 'surface'; id = 1},
                    function () {type = 'curve'; id = '1|1'},
                    function () {type = 'timeseries'; id = 1}
                ],
                type_map: { // available views per data type
                    CURVE: [1, 3],
                    DOUBLE: [0],
                    LABELLED_MATRIX_1D: [0, 1],
                    LABELLED_MATRIX_2D: [0, 1, 2, 3],
                    LABELLED_MATRIX_3D: [0, 1],
                    PRIMITIVE: [0],
                    SURFACE_DATA: [2, 1],
                    TENOR: [0],
                    TIME_SERIES: [4, 1],
                    UNKNOWN: [0]
                },
                order: { // default view preference for data type per location
                    'south': [0, 2, 4, 3, 1],
                    'dock-north': [2, 4, 3, 1, 0],
                    'dock-center': [2, 4, 3, 1, 0],
                    'dock-south': [2, 4, 3, 1, 0],
                    'new-window': [2, 4, 3, 1, 0]
                }
            };
            var order = mappings.order[panel || 'new-window'], type_map = mappings.type_map[cell.type], i, k, id, type;
                test: for (i = 0; i < order.length; i++) for (k = 0; k < type_map.length; k++) {
                    if (order[i] === type_map[k]) {
                        mappings.functions[order[i]]();
                        break test;
                    }
                }
            return (!panel)
                ? 'gadget.ftl#/gadgetscontainer/' + type + ':' + id
                : (function () {
                    var rule = og.views.analytics2.rules.load_item, args = routes.current().args, add = {};
                    add[panel] = type + ':' + id;
                    return routes.hash(rule, args, {add: add});
                })();
        };
        return function () {
            var self = this, timer, cur_cell, panels = ['south', 'dock-north', 'dock-center', 'dock-south'],
                handler = function (tmpl) {
                    self.hide = function () {self.menu.hide()};
                    self.menu = $(tmpl);
                    self.show = function (cell) {
                        cur_cell = cell;
                        if (self.menu.length) self.menu.css({top: cell.top, left: cell.right - 32}).show();
                    };
                    self.menu.hide()
                        .on('mouseleave', function () {
                            clearTimeout(timer);
                            self.menu.removeClass(expand_class);
                        })
                        .on('mouseenter', open_icon, function () {
                            clearTimeout(timer);
                            timer = setTimeout(function () {self.menu.addClass(expand_class);}, 500);
                        })
                        .on('click', open_icon, function () {self.menu.addClass(expand_class);})
                        .on('mouseenter', icons, function () {
                            var panel = panels[$(this).text() - 1];
                            panels.forEach(function (v) {og.analytics.containers[v].highlight(true, !!(v === panel));});
                        })
                        .on('mouseleave', icons, function () {
                            panels.forEach(function (v) {og.analytics.containers[v].highlight(false)});
                        })
                        .on('click', icons, function () {
                            var panel = panels[$(this).text() - 1], hash = get_url(cur_cell, panel);
                            if (panel === void 0) console.log(hash), window.open(hash);
                            else routes.go(hash);
                            self.hide();
                        });
                    $('body').append(self.menu);
                };
            $.when(og.api.text({module: 'og.analytics.cell_options'})).then(handler);
        }
    }
});
