/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.CellMenu',
    dependencies: ['og.common.routes', 'og.common.gadgets.mapping'],
    obj: function () {
        var icons = '.og-num, .og-icon-new-window-2', open_icon = '.og-icon-right-chevron',
            expand_class = 'og-expanded', routes = og.common.routes, mapping = og.common.gadgets.mapping;
        /**
         * TODO: replace with api / this temporary solution until the api is ready
         */
        var get_url = function (cell, panel) {
            var order = mapping.panel_preference[panel || 'new-window'],
                type_map = mapping.typemap[cell.type], i, k, id, type;
            test: for (i = 0; i < order.length; i++) for (k = 0; k < type_map.length; k++) {
                if (order[i] === type_map[k]) {
                    type = mapping.gadgets[order[i]], id = id = cell.col + '|' + cell.row;
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
