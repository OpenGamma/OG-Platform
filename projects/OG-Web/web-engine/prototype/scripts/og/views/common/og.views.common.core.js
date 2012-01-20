/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.Core',
    dependencies: [],
    obj: function () {
        var common = og.common, ui = common.util.ui, routes = common.routes;
        return function () {
            var view = this, search;
            view.check_state = function (conditions) {og.views.common.state.check('/' + view.page_name, conditions);};
            view.dependencies = ['id'];
            view.error = function (message) {ui.dialog({type: 'error', message: message});},
            view.filter = $.noop;
            view.init = function () {for (var rule in view.rules) routes.add(view.rules[rule]);};
            view.layout = null;
            view.load = function (args) {
                view.layout = og.views.common.layout;
                view.check_state({args: args, conditions: [
                    {new_page: function (args) {view.search(args), common.masthead.menu.set_tab(view.page_name);}}
                ]});
                if (!args.id) og.views.common.default_details(view.page_name, view.name, view.options);;
            };
            view.load_item = function (args) {
                view.check_state({args: args, conditions: [
                    {new_value: 'id', method: function (args) {
                        view.load(args);
                        view.layout.inner.options.south.onclose = null;
                        view.layout.inner.close.partial('south');
                    }}
                ]});
                view.details(args);
            };
            view.notify = function (message, duration) {
                if (!message) return ui.message({location: '.ui-layout-inner-center', destroy: true});
                ui.message({location: '.ui-layout-inner-center', css: {left: 0}, message: message});
                if (duration) setTimeout(view.notify.partial(null), duration);
            };
            view.update = function (delivery) {
                view.notify('This item has been updated.', 1500);
                view.details(routes.current().args, {hide_loading: true});
            };
            view.search = function (args) {
                if (!search) {
                    search = common.search_results.core();
                    view.filter = search.filter;
                }
                search.load($.extend(view.options.slickgrid, {url: args}));
            };
        };
    }
});