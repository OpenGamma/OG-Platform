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
                    $('body').append(
                        '<div class="tooltip north">' +
                            'Lorem ipsum dolor sit amet, consectetur adipiscing elit.'+
                            'Nullam consectetur quam a sapien egestas eget scelerisque'+
                            'lectus tempor. Duis placerat tellus at erat pellentesque nec'+
                            'ultricies erat molestie. Integer nec orci id tortor molestie'+
                            'porta. Suspendisse eu sagittis quam.'+
                        '</div>'
                    )
                }}).pipe(function () {
                    // For testing purposes only, should be removed/refactored once
                    // implementation details have been agreed.
                    var tool_cta = $('.tooltip-cta'), tooltip = $('div.tooltip.north'),
                        tooltip_offsets = tooltip.offset();
                    tool_cta.on('click', function (event) {
                        var elem = $(this), offsets = elem.offset(), width = elem.width(), height = elem.height();
                        // North orientation, after testing dims; to be implemented
                        tooltip.css({
                            top: offsets.top + height + 'px',
                            left: offsets.left - width/2 + 'px'
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