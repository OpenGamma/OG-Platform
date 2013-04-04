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
                        '<div class="tooltip">' +
                            'Lorem ipsum dolor sit amet, consectetur adipiscing elit.'+
                            'Nullam consectetur quam a sapien egestas eget scelerisque'+
                            'lectus tempor. Duis placerat tellus at erat pellentesque nec'+
                            'ultricies erat molestie. Integer nec orci id tortor molestie'+
                            'porta. Suspendisse eu sagittis quam.'+
                        '</div>'
                    )
                }}).pipe(function () {
                    var tooltip = $('div.tooltip'), tooltip_offsets = tooltip.offset();
                    $('.tooltip-cta').on('click', function (event) {
                        var elem = $(this), offsets = elem.offset(), width = elem.width(), height = elem.height(),
                            margins = elem.css('margins');

                        // North
                        /*tooltip.addClass('north').css({
                            top: offsets.top + height + 'px',
                            left: offsets.left - (tooltip.outerWidth()/2) + width/2 + 'px'
                        }).show();*/

                        // East
                        /*tooltip.addClass('east').css({
                            top: offsets.top - ((tooltip.outerHeight()-height)/2) + 'px',
                            left: offsets.left - tooltip.outerWidth() + 'px'
                        }).show();*/

                        // South
                        /*tooltip.addClass('south').css({
                            top: offsets.top - tooltip.outerHeight() + 'px',
                            left: offsets.left - (tooltip.outerWidth()/2) + width/2 + 'px'
                        }).show();*/

                        // West
                        tooltip.addClass('west').css({
                            top: offsets.top - ((tooltip.outerHeight()-height)/2) + 'px',
                            left: offsets.left + width + 'px'
                        }).show();

                    }).on('mouseout', function () { tooltip.removeAttr('style').hide(); });
                });
            },
            load_item: function (args) {
                view.check_state({args: args, conditions: [{new_page: view.load}]});
            },
            init: function () { for (var rule in view.rules) routes.add(view.rules[rule]); },
            rules: { load_item: { route: '/', method: module.name + '.load_item' } }
        }
    }
});