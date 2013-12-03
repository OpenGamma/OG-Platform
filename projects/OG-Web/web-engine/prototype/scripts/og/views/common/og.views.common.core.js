/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.common.Core',
    dependencies: ['og.common.util.ui', 'og.common.routes'],
    obj: function () {
        var common = og.common, ui = common.util.ui, routes = common.routes;
        return function (page_name) {
            var view = this, search;
            view.check_state = function (conditions) {og.views.common.state.check('/' + page_name, conditions); };
            view.default_details = function () {og.views.common.default_details(page_name, view.name, view.options); };
            view.dependencies = ['id', 'version'];
            view.error = function (message) {ui.dialog({type: 'error', message: message}); };
            view.filter = $.noop;
            view.init = function () {for (var rule in view.rules) routes.add(view.rules[rule]);};
            view.layout = null;
            view.load = function (args) {
                view.layout = og.views.common.layout;
                view.check_state({args: args, conditions: [
                    {new_page: function (args) {view.search(args), common.masthead.menu.set_tab('configs');}}
                ]});
                if (!args.id) view.default_details();
            };
            view.load_filter = function (args) {
                view.check_state({args: args, conditions: [{new_value: 'id', method: function (args) {
                    view[args.id ? 'load_item' : 'load'](args);
                }}]});
                view.filter();
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
            view.name = page_name.replace(/_/g, ' ').replace(/^(.)|\s(.)/g, function($1) {return $1.toUpperCase();});
            view.notify = function (message, duration) {
                if (!message) return ui.message({location: '.OG-layout-admin-details-center', destroy: true});
                ui.message({location: '.OG-layout-admin-details-center', css: {left: 0}, message: message});
                if (duration) setTimeout(view.notify.partial(null), duration);
            };
            view.update = function (delivery) {
                view.notify(delivery.reset ? 'The connection has been reset.' : 'This item has been updated.', 1500);
                view.search(routes.current().args);
                view.details(routes.current().args, {hide_loading: true});
            };
            view.rules = function (filters, extras) {
                var filter, prefix = 'og.views.' + page_name, page = '/' + page_name, optionals = [
                    filter = (filters || []).length ? filters.join(':?/') + ':?' : '',
                    (extras || []).length ? extras.join(':?/') + ':?' : ''
                ].filter(Boolean).join('/');
                view.filter_params = filters || [];
                view.extra_params = extras || [];
                return {
                    load: {route: page + '/' + filter, method: prefix + '.load'},
                    load_filter: {route: page + '/filter:/:id?/' + optionals, method: prefix + '.load_filter'},
                    load_item: {route: page + '/:id/' + optionals, method: prefix + '.load_item'}
                };
            },
            view.search = function (args) {
                if (!search) (search = common.search_results.core()), view.filter = search.filter;
                search.load(view.options.slickgrid);
            };
        };
    }
});