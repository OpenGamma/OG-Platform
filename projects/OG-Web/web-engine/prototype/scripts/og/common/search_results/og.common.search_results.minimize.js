/*
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 *
 * TODO: needs implementing as a generic function
 */
$.register_module({
    name: 'og.common.search_results.minimize',
    dependencies: [],
    obj: function () {
        return function (min, max) {
            $(min).mousedown(function () {
                $('#OG-sr').hide();
                $('#OG-details').width('100%');
                $('#OG-details .og-icon-maximize').show();
                $('#OG-details .og-icon-minimize').hide();
            });
            $(max).mousedown(function () {
                var details_width = localStorage['resize_panes_#OG-details'] || '60%',
                    sr_width = localStorage['resize_panes_#OG-sr'] || '40%';
                $('#OG-sr').show().width(sr_width);
                $('#OG-details').width(details_width);
                $('#OG-details .og-icon-maximize').hide();
                $('#OG-details .og-icon-minimize').show();
            });
        };
    }
});