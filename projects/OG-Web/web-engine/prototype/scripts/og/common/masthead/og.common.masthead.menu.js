/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.masthead.menu',
    dependencies: [],
    obj: function () {

        var keyboard;

        /*
         * TODO: setup keyboard shortcuts
         */
        keyboard = {
            'A': 'OG-JS-analytics',
            'P': 'OG-JS-portfolio',
            'S': 'OG-JS-securities',
            'E': 'OG-JS-exchanges',
            'T': 'OG-JS-timeSeries',
            'H': 'OG-JS-holidays',
            'R': 'OG-JS-regions',
            'C': 'OG-JS-configurations',
            'N': 'OG-JS-new',
            'F': 'OG-JS-feedback'
        };

        return {
            init: function () {
                $('.og-icon-header-dropdown').parent().hover(function () {
                    $(this).addClass('open').find('.og-dd-menu').show();
                });

                /*
                 * TODO: Add in delay
                 */
                $('.og-icon-header-dropdown').parent().mouseleave(function () {
                    $(this).removeClass('open').find('.og-dd-menu').hide();
                });

                /*
                    TODO
                    the old live api was used here to populated views, new rest call must be made instead. the old
                    code is left here for reference
                    live.views.subscribe(function (message) {
                        var views = message.data.availableViewNames.map(function (val) {
                            return {hash: encodeURIComponent(val), title: val};
                        });
                        $('#OG-masthead .OG-js-analytics-menu').html($('#OG-js-analytics-menu-template').tmpl(views));
                    });
                */
            },
            set_tab: function (name) {
                $('#OG-masthead li').removeClass('og-active');
                $('#OG-masthead .og-' + name).addClass('og-active');
            }
        };

    }
});