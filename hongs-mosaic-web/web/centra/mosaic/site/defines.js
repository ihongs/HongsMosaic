
function in_centra_mosaic_site_list(context, listobj) {
    listobj._fill_state_text = function(td, v) {
        if (this._info.state)
        switch (parseInt(this._info.state)) {
            case 11: td.css("color","green" ); break;
            case 14: td.css("color","orange"); break;
            case 21: td.css("color","blue"); break;
            case 24: td.css("color","red" ); break;
        }
        return v;
    };
    
    context.on("click", ".verify", function() {
        var btn = this;
        var ids = listobj.getIds(this);
        var req = hsSerialDat(ids);
        var snd =  function(state) {
            return function() {
                if (state) req.state = state;
                req.cause = $(this).closest(".modal").find("input").val();
                listobj.send(btn, "", "centra/mosaic/site/update.act", req);
            };
        };
        $.hsMask({
            title: "审核站点",
            html : '<input type="text" class="form-control" placeholder="审核意见"/>',
            mode : "warn"
        }, {
            label: "锁定",
            glass: "btn-danger" ,
            click: snd(24)
        }, {
            label: "审核不过",
            glass: "btn-warning",
            click: snd(14)
        }, {
            label: "审核通过",
            glass: "btn-success",
            click: snd(1)
        }, {
            label: "补充意见",
            glass: "btn-default",
            click: snd()
        }, {
            label: "取消",
            glass: "btn-default"
        });
    });
}
