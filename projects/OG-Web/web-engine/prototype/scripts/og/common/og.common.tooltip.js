/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.tooltip',
    dependencies: [],
    obj: function () {
        return {
            init: (function () {
                var module = this, tooltip, tooltip_offsets, orientation = '', offsets, height, width,
                    viewport, timed_hover, total_width, total_height;

                $('body').append(
                    '<div class="tooltip">'+
                        'Lorem ipsum dolor sit amet, consectetur adipiscing elit.'+
                        'Nullam consectetur quam a sapien egestas eget scelerisque'+
                        'lectus tempor. Duis placerat tellus at erat pellentesque nec'+
                        'ultricies erat molestie. Integer nec orci id tortor molestie'+
                        'porta. Suspendisse eu sagittis quam.'+
                    '</div>'
                );

                var hide_tooltip = function (event) {
                    tooltip.removeAttr('style').removeClass(orientation).hide();
                };

                var show_tooltip = function (dir) {
                    orientation = dir;
                    switch (dir) {
                        case 'north' : {
                            tooltip.addClass(orientation).css({
                                top: offsets.top + height + 'px',
                                left: offsets.left - (tooltip.outerWidth()/2) + width/2 + 'px'
                            })
                            break;
                        }
                        case 'north-east-flip' : {
                            tooltip.addClass(orientation).css({
                                top: offsets.top + height/2 + 'px',
                                left: offsets.left - tooltip.outerWidth() + 'px'
                            })
                            break;
                        }
                        case 'north-west-flip' : {
                            tooltip.addClass(orientation).css({
                                top: offsets.top + height/2 + 'px',
                                left: offsets.left + width + 'px'
                            })
                            break;
                        }
                        case 'east' : {
                            tooltip.addClass(orientation).css({
                                top: offsets.top - ((tooltip.outerHeight()-height)/2) + 'px',
                                left: offsets.left - tooltip.outerWidth() + 'px'
                            });
                            break;
                        }
                        case 'south' : {
                            tooltip.addClass(orientation).css({
                                top: offsets.top - tooltip.outerHeight() + 'px',
                                left: offsets.left - (tooltip.outerWidth()/2) + width/2 + 'px'
                            });
                            break;
                        }
                        case 'south-east-flip' : {
                            tooltip.addClass(orientation).css({
                                top: offsets.top - tooltip.outerHeight() + height/2 + 'px',
                                left: offsets.left - tooltip.outerWidth() + 'px'
                            });
                            break;
                        }
                        case 'south-west-flip' : {
                            tooltip.addClass(orientation).css({
                                top: offsets.top - tooltip.outerHeight() + height/2 + 'px',
                                left: offsets.left + width + 'px'
                            });
                            break;
                        }
                        case 'west' : {
                            tooltip.addClass(orientation).css({
                                top: offsets.top - ((tooltip.outerHeight()-height)/2) + 'px',
                                left: offsets.left + width + 'px'
                            });
                            break;
                        }
                    }
                    tooltip.show();
                };

                $('[data-tooltip]').live('click', function (event) {
                    var elem = $(this);

                    offsets = elem.offset(); width = elem.outerWidth(); height = elem.outerHeight();
                    viewport = { height: $(window).height(), width: $(window).width() };
                    total_width = offsets.left + width, total_height = offsets.top + height;

                    if (!tooltip) {
                        tooltip = $('div.tooltip');
                        tooltip_offsets = tooltip.offset();
                    }

                    tooltip.removeClass(orientation);

                    // north
                    if (total_height - tooltip.outerHeight() < 0 && total_width - tooltip.outerWidth() > 0 &&
                        total_width + tooltip.outerWidth() < viewport.width) return show_tooltip('north');

                    // north east
                    else if (total_width + tooltip.outerWidth() > viewport.width &&
                        total_height - tooltip.outerHeight() < 0) return show_tooltip('north-east-flip');

                    // north west
                    else if (total_height - tooltip.outerHeight() < 0 && total_width - tooltip.outerWidth() < 0)
                        return show_tooltip('north-west-flip');

                    // south
                    else if (total_height + tooltip.outerHeight() > viewport.height &&
                        total_width + tooltip.outerWidth() < viewport.width &&
                        total_width - tooltip.outerWidth() > 0) return show_tooltip('south');

                    //south east
                    else if (total_height + tooltip.outerHeight() > viewport.height &&
                        total_width + tooltip.outerWidth() > viewport.width) return show_tooltip('south-east-flip');

                    // south west
                    else if (total_height + tooltip.outerHeight() > viewport.height &&
                        total_width - tooltip.outerWidth() < 0) return show_tooltip('south-west-flip');

                    // east or west
                    else {
                        if (total_width + tooltip.outerWidth() > viewport.width) return show_tooltip('east');
                        else if (total_width + tooltip.outerWidth() < viewport.width) return show_tooltip('west');
                    }
                });
            })()
        };
    }
});