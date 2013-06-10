/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.analytics',
    dependencies: [],
    obj: function () {
        var routes = og.common.routes, module = this, view,
            main_selector = '.OG-layout-analytics-center';
        module.rules = {load: {route: '/', method: module.name + '.load'}};
        og.api.rest
            .on('disconnect', og.analytics.status.disconnected)
            .on('reconnect', og.analytics.status.reconnected);
        return view = {
            check_state: og.views.common.state.check.partial('/'),
            load: function (args) {
                $('.OG-masthead .og-analytics-beta').addClass('og-active');
                var new_page = false, layout = og.views.common.layout;
                view.check_state({args: args, conditions: [
                    {new_page: function () {
                        new_page = true;
                        og.analytics.containers.initialize();
                    }}
                ]});
                og.api.text({module: 'og.views.analytics.default', handler: function (template) {
                    var layout = og.views.common.layout,
                        $html = $.tmpl(template, {
                            recent_list: og.common.util.history.get_html('history.analytics.recent') ||
                                'no recently viewed views'
                        });
                    $('.OG-layout-analytics-center').html($html);
                }});
                og.analytics.resize({
                    selector: '.OG-layout-analytics-center',
                    tmpl: '<div class="OG-analytics-resize og-resizer" title="Drag (resize) / Right click (menu)" />',
                    mouseup_handler: function (right, bottom) {
                        layout.inner.sizePane('south', bottom);
                        layout.main.sizePane('east', right);
                    },
                    right_handler:  function (resizer) {
                        var inner = layout.inner.sizePane, $resizer = resizer,
                        right = layout.right.sizePane, main = layout.main.sizePane;
                        $.when(og.api.text({module: 'og.analytics.resize_menu_tash'})).then(function (template) {
                        $menu = $(template).position({my: 'left top', at: 'right bottom', of: $resizer})
                            .on('mousedown', 'div', function () {
                                // extract preset number from class and load
                            switch (+$(this).find('> span').attr('class').replace(/^(?:.*)([1-3])$/, '$1')) {
                                case 1: inner('south', '50%'); main('east', '25%'); break;
                                case 2: inner('south', '50%'); main('east', '50%'); break;
                                case 3: inner('south', '15%'); main('east', '10%'); break;
                            }
                            right('north', '33%');
                            right('south', '33%');
                                $menu.remove(); // it should already be gone, but just in case you are IE8
                            }).appendTo('body').blurkill();
                        });
                        return false;
                    }
                });
                if (!new_page && !args.data && og.analytics.url.last.main) {
                    og.analytics.url.clear_main(), $(main_selector).html('');
                }
            },
            load_item: function (args) {
                view.check_state({args: args, conditions: [{new_page: view.load}]});
                og.analytics.url.process(args, function () {});
            },
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            rules: {
                load_item: {route: '/:data?', method: module.name + '.load_item'}
            }
        };
    }
});