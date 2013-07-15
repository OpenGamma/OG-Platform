/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.env',
    dependencies: ['og.common.routes', 'og.api.text'],
    obj: function () {
        var view, module = this, routes = og.common.routes, items = og.env,
            gadget_selector = '.OG-env .og-module-container .og-module', menu_selector = '.og-menu-container';
        /* Populate menu */
        $.when(og.api.text({module: 'og.env.menu_tash'})).then(function (tmpl) {
            var arr = [];
            for (var item in items) arr.push({
                name: item.toLowerCase(),
                f_name: item.replace(/^(.)|\s(.)/g, function($1) {return $1.toUpperCase();})
            })
            $(menu_selector).html(Handlebars.compile(tmpl)({items: arr}));
        });
        return view = {
            load_item: function (args) {if (items[args.item]) items[args.item](gadget_selector);},
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            rules: {load_item: {route: '/:item', method: module.name + '.load_item'}}
        }
    }
});