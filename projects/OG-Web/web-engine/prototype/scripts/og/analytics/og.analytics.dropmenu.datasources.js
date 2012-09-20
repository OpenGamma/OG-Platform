
$.register_module({
    name: 'og.analytics.dropmenu.datasources',
    dependencies: ['og.analytics.dropmenu'],
    obj: function () { 
        return function (config) {
            var Dropmenu = this,
                populate_marketdata = function () {
                    og.api.rest.marketdatasnapshots.get().pipe(function (resp) {
                        console.log(resp.data[0].snapshots);
                    });
                },
                select_handler = function (event) {
                    var $elem = $(event.srcElement);
                    if ($elem.is('.type')) {
                        switch($elem.val()) {
                            case 'Snapshot': populate_marketdata(); break;
                        }
                    }
                };
            return Dropmenu = new og.analytics.dropmenu(config), Dropmenu;
        }
    }
});