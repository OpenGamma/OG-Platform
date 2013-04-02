/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.common.tooltip',
    dependencies: [],
    obj: function () {
        var routes = og.common.routes, module = this, view;
        module.rules = {load: {route: '/', method: module.name + '.load'}};
        return view = {
            check_state: og.views.common.state.check.partial('/'),
            load: function (args) {
                og.api.text({module: 'og.views.tooltip', handler: function (template) {
                    $('.OG-layout-tooltip').html(template);
                }}).pipe(function () {
                    // For testing purposes only, should be removed/refactored once
                    // implementation details have been agreed.
                    var anchor = $('a.tooltip'), tooltip = $('div.tooltip.north'), offsets,
                        left = 0, top = 0, parent = tooltip.parent();
                        while (parent && parent[0].tagName !== 'BODY') {
                            top += parent.offset().top;
                            left += parent.offset().left;
                            parent = parent.parent();
                        }
                    anchor.on('mouseover', function (event) {
                            offsets = anchor.offset();
                            tooltip.css({
                                top: (offsets.top - top) + anchor.height() + 'px',
                                left: (offsets.left - left) - tooltip.width()/2 + 'px',
                            }).show();
                    }).on('mouseout', function () {
                        tooltip.hide();
                    });
                });
                var display_tooltip = function (ref_elem, position) {
                    // var direction = get_dir(position);
                };
                var get_direction = function (position) {
                    var height = $(window).height(), width = $(window).width();
                };
            },
            load_item: function (args) {
                view.check_state({args: args, conditions: [{new_page: view.load}]});
            },
            init: function () { for (var rule in view.rules) routes.add(view.rules[rule]); },
            rules: { load_item: { route: '/', method: module.name + '.load_item' } }
        }
    }
});