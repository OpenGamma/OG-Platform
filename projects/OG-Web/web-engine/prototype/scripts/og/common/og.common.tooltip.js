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
                    viewport, timed_hover, total_width, total_height, blurkill = false,
                    tmpl = '<div class="OG-tooltip og-large">{{{tip}}}</div>';

                var hide_tooltip = function (event) {
                    tooltip.removeAttr('style').removeClass(orientation).hide();
                    blurkill = false;
                };

                var show_tooltip = function (dir) {
                    orientation = dir;
                    switch (dir) {
                        case 'og-north' :
                            tooltip.addClass(orientation).css({
                                top: offsets.top + height + 'px',
                                left: offsets.left - (tooltip.outerWidth()/2) + width/2 + 'px'
                            });
                            break;
                        case 'og-north-east-flip' :
                            tooltip.addClass(orientation).css({
                                top: offsets.top + height/2 + 'px',
                                left: offsets.left - tooltip.outerWidth() + 'px'
                            });
                            break;
                        case 'og-north-west-flip' :
                            tooltip.addClass(orientation).css({
                                top: offsets.top + height/2 + 'px',
                                left: offsets.left + width + 'px'
                            });
                            break;
                        case 'og-east' :
                            tooltip.addClass(orientation).css({
                                top: offsets.top - ((tooltip.outerHeight()-height)/2) + 'px',
                                left: offsets.left - tooltip.outerWidth() + 'px'
                            });
                            break;
                        case 'og-south' :
                            tooltip.addClass(orientation).css({
                                top: offsets.top - tooltip.outerHeight() + 'px',
                                left: offsets.left - (tooltip.outerWidth()/2) + width/2 + 'px'
                            });
                            break;
                        case 'og-south-east-flip' :
                            tooltip.addClass(orientation).css({
                                top: offsets.top - tooltip.outerHeight() + height/2 + 'px',
                                left: offsets.left - tooltip.outerWidth() + 'px'
                            });
                            break;
                        case 'og-south-west-flip' :
                            tooltip.addClass(orientation).css({
                                top: offsets.top - tooltip.outerHeight() + height/2 + 'px',
                                left: offsets.left + width + 'px'
                            });
                            break;
                        case 'og-west' :
                            tooltip.addClass(orientation).css({
                                top: offsets.top - ((tooltip.outerHeight()-height)/2) + 'px',
                                left: offsets.left + width + 'px'
                            });
                            break;
                    }
                    tooltip.show();
                };

                $('[data-tooltip-type=large]').live('click', function (event) {
                    var elem = $(this);
                    tooltip.removeClass(orientation);
                    tooltip_offsets = tooltip.offset();
                    offsets = elem.offset(); width = elem.outerWidth(); height = elem.outerHeight();
                    viewport = { height: $(window).height(), width: $(window).width() };
                    total_width = offsets.left + width, total_height = offsets.top + height;

                    setTimeout(function () {
                        if (blurkill) return;
                        blurkill = true;
                        tooltip.blurkill(hide_tooltip);
                    }, 0);

                    // north
                    if (total_height - tooltip.outerHeight() < 0 && total_width - tooltip.outerWidth() > 0 &&
                        total_width + tooltip.outerWidth() < viewport.width) return show_tooltip('og-north');

                    // north east
                    else if (total_width + tooltip.outerWidth() > viewport.width &&
                        total_height - tooltip.outerHeight() < 0) return show_tooltip('og-north-east-flip');

                    // north west
                    else if (total_height - tooltip.outerHeight() < 0 && total_width - tooltip.outerWidth() < 0)
                        return show_tooltip('og-north-west-flip');

                    // south
                    else if (total_height + tooltip.outerHeight() > viewport.height &&
                        total_width + tooltip.outerWidth() < viewport.width &&
                        total_width - tooltip.outerWidth() > 0) return show_tooltip('og-south');

                    //south east
                    else if (total_height + tooltip.outerHeight() > viewport.height &&
                        total_width + tooltip.outerWidth() > viewport.width) return show_tooltip('og-south-east-flip');

                    // south west
                    else if (total_height + tooltip.outerHeight() > viewport.height &&
                        total_width - tooltip.outerWidth() < 0) return show_tooltip('og-south-west-flip');

                    // east or west
                    else {
                        if (total_width + tooltip.outerWidth() > viewport.width) return show_tooltip('og-east');
                        else if (total_width + tooltip.outerWidth() < viewport.width) return show_tooltip('og-west');
                    }
                });

                $(function () {
                    if (!tooltip) $('body').append(tooltip = $(tmpl));
                });
            })()
        };
    }
});