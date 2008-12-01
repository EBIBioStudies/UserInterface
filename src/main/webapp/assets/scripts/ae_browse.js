//
//  AE Browse Page Scripting Support. Requires jQuery 1.2.3 and JSDefeered.jQuery 0.2.1
//

// query object is a global variable
var query = new Object();
var user = "";

function
aeClearKeywords()
{
    $("#ae_keywords").val("");
    $("#ae_wholewords").removeAttr("checked");
    $("#ae_inatlas").removeAttr("checked");
}

function
aeResetFilters()
{
    $("#ae_species").val("");
    $("#ae_array").val("");
    $("#ae_exptype").val("");
}

function
aeResetOptions()
{
    $("#ae_pagesize").val("25");
    $("#ae_detailedview").removeAttr("checked");
}

function
aeShowLoginForm()
{
    $("#ae_login_link").hide();
    $("#ae_help_link").hide();
    $("#ae_login_status").text("");
    $("#ae_login_box").show();
    $("#ae_user_field").focus();
}

function
aeHideLoginForm()
{
    $("#ae_login_box").hide();
    $("#ae_help_link").show();
    $("#ae_login_status").text("");
    if ( "" != user ) {
        $("#ae_login_link").hide();
        $("#ae_login_info em").text(user);
        $("#ae_login_info").show();
    } else {
        $("#ae_login_link").show();
    }
}


function
aeDoLogin()
{
    user = $("#ae_user_field").val();
    var pass = $("#ae_pass_field").val();
    $("#ae_pass_field").val("");
    $("#ae_login_status").text("");
    $("#ae_login_submit").attr("disabled", "true");
    $.get("verify-login.txt", { u: user, p: pass }).next(aeDoLoginNext);
}

function
aeDoLoginNext(text)
{
    if ( "" != text ) {
        $("#ae_login_box").hide();
        $("#ae_help_link").show();
        $("#ae_login_submit").removeAttr("disabled");

        $.cookie("AeLoggedUser", user, {expires: 365, path: '/'});
        $.cookie("AeLoginToken", text, {expires: 365, path: '/'});

        $("#ae_login_info em").text(user);
        $("#ae_login_info").show();
        window.location.href = decodeURI(window.location.pathname) + $.query.toString();        
    } else {
        user = "";
        $("#ae_login_status").text("The information you provided does not match our records. Please verity the entry and try again.");
        $("#ae_login_submit").removeAttr("disabled");
        $("#ae_user_field").focus();
    }
}

function
aeDoLogout()
{
    $("#ae_login_info").hide();
    $("#ae_login_link").show();
    $.cookie("AeLoggedUser", null, {path: '/' });
    $.cookie("AeLoginToken", null, {path: '/' });
    user = "";
    window.location.href = decodeURI(window.location.pathname) + $.query.toString();
}

function
aeSort( sortby )
{
    if ( -1 != String("accession name assays species releasedate fgem raw atlas").indexOf(sortby) ) {
        var innerElt = $( "#ae_results_header_" + sortby ).find("div.table_header_inner");
        var sortorder = "ascending";
        if ( -1 != String("accession name species").indexOf(sortby)) {
            if ( undefined != innerElt && innerElt.hasClass("table_header_sort_asc") )
                sortorder = "descending";
        } else {
            sortorder = "descending";
            if ( undefined != innerElt && innerElt.hasClass("table_header_sort_desc") )
                sortorder = "ascending";
        }
        var newQuery = $.query.set( "sortby", sortby ).set( "sortorder", sortorder ).toString();
        window.location.href = "browse.html" + newQuery;
    }
}

function
aeToggleExpand( id )
{
    id = String(id);
    var mainElt = $("#" + id);
    var extElt = $("#" + id.replace("_main", "_ext"));
    if ( mainElt.hasClass("tr_main_expanded")) {
        // collapse now
        mainElt.removeClass("tr_main_expanded");
        extElt.hide();
    } else {
        mainElt.addClass("tr_main_expanded");
        extElt.show();
    }
    onWindowResize();
}

///////////////////////////////////////////////////////////////////////////////////////////////////

$(document).ready( function() {

    // step 0: hack for IE to work with this funny EBI header/footer (to be redeveloped with jQuery)
    if (-1 != navigator.userAgent.indexOf('MSIE')) {
        document.getElementById('head').allowTransparency = true;
        document.getElementById('ae_contents').style.zIndex = 1;
    }

    if ($.browser.opera && $.browser.version < 9.5) {
        onWindowResize();
        $(window).resize( onWindowResize );
    } else {
        onWindowResize();
    }

    var _user = $.cookie("AeLoggedUser");
    var _token = $.cookie("AeLoginToken");
    if ( undefined != _user && undefined != _token ) {
        user = _user;
        $("#ae_login_link").hide();
        $("#ae_login_info em").text(_user);
        $("#ae_login_info").show();
    }

    // adds a callback to close a login form on escape
    $("#ae_login_form input").keypress(
            function (e) {
                if (e.keyCode == 27) {
                    aeHideLoginForm();
                }
            }
        );

    if ("" != $.query.get("accnum")) {
        query.keywords = $.query.get("accnum");
        query.detailedview = true;
    }

    if ("" != $.query.get("keywords"))
        query.keywords = $.query.get("keywords");

    if ("" != $.query.get("wholewords"))
        query.wholewords = true;

    query.species = $.query.get("species");
    query.array = $.query.get("array");
    query.exptype = $.query.get("exptype");

    if ("" != $.query.get("inatlas"))
        query.inatlas = true;

    if ("" != $.query.get("page"))
        query.page = $.query.get("page");

    if ("" != $.query.get("pagesize"))
        query.pagesize = $.query.get("pagesize");
    else
        query.pagesize = "25";

    if ("" != $.query.get("sortby"))
        query.sortby = $.query.get("sortby");
    else
        query.sortby = "releasedate";

    if ("" != $.query.get("sortorder"))
        query.sortorder = $.query.get("sortorder");
    else
        query.sortorder = "descending";

    if ("" != $.query.get("detailedview"))
        query.detailedview = true;

    initControls();

    $("#ae_results_body_inner").ajaxError(onQueryError);
    $.get( "browse-table.html", query ).next(onExperimentQuery);
});

function
onWindowResize()
{
    var outerWidth = $("#ae_results_body").width();
    var innerWidth = $("#ae_results_body table").width();
    var padding = outerWidth - innerWidth;
    if ( padding > 0 && padding < 30 ) {
        $("#ae_results_hdr").css( "right", padding + "px" );
    } else if ( padding == 0 && $.browser.opera && $.browser.version < 9.5 ) {
        $("#ae_results_hdr").css( "right", "-1px" );
    }
}

function
onExperimentQuery( tableHtml )
{
    // remove progress gif
    $("#ae_results_body_inner").removeClass("ae_results_table_loading");

    // populate table with data
    $("#ae_results_tbody").html(tableHtml);

    // adjust header width to accomodate scroller (for Opera <9.5)
    if ($.browser.opera && $.browser.version < 9.5)
        onWindowResize();

    // get stats from the first row
    var total = $("#ae_results_total").text();
    var totalAssays = $("#ae_results_total_assays").text();
    var from = $("#ae_results_from").text();
    var to = $("#ae_results_to").text();
    var curpage = $("#ae_results_page").text();
    var pagesize = $("#ae_results_pagesize").text();


    if ( total > 0 ) {

        // assign valid hrefs to save to tab/xls
        $("#ae_results_save a").attr("href", "ArrayExpress-Experiments.txt" + $.query.toString());
        $("#ae_results_save_xls a").attr("href", "ArrayExpress-Experiments.xls" + $.query.toString());
        $("#ae_results_save_feed a").attr("href", "rss/experiments" + $.query.toString());
        // show controls
        $(".status_icon").show();

        var totalPages = total > 0 ? Math.floor( ( total - 1 ) / pagesize ) + 1 : 0;
        $("#ae_results_status").html(
            total + " experiment" + (total != 1 ? "s" : "" ) + ", " +
            totalAssays + " assay" + (totalAssays != 1 ? "s" : "" ) + "." +
            ( totalPages > 1 ? (" Displaying experiments " + from + " to " + to + ".") : "" )
            );

        if ( totalPages > 1 ) {
            var pagerHtml = "Pages: ";
            for ( var page = 1; page <= totalPages; page++ ) {
                if ( curpage == page ) {
                    pagerHtml = pagerHtml + "" + page + "";
                } else if ( 2 == page && curpage > 6 && totalPages > 11 ) {
                    pagerHtml = pagerHtml + "..";
                } else if ( totalPages - 1 == page && totalPages - curpage > 5 && totalPages > 11 ) {
                    pagerHtml = pagerHtml + "..";
                } else if ( 1 == page || ( curpage < 7 && page < 11 ) || ( Math.abs( page - curpage ) < 5 ) || ( totalPages - curpage < 6 && totalPages - page < 10 ) || totalPages == page || totalPages <= 11 ) {
                    var newQuery = $.query.set( "page", page ).set( "pagesize", pagesize ).toString();
                    pagerHtml = pagerHtml + "<a href=\"browse.html" + newQuery + "\">" + page + "</a>";
                }
                if ( page < totalPages ) {
                    pagerHtml = pagerHtml + " ";
                }
            }
            $("#ae_results_pager").html( pagerHtml );
        }
    }

    // attach handlers
    $(".ae_results_tr_main").each(addExpansionHandlers);

}

function
onQueryError()
{
    $(this).removeClass("ae_results_table_loading");
    $("#ae_results_tbody").html("<tr class=\"ae_results_tr_error\"><td colspan=\"9\">There was an error processing the query. Please try again later.</td></tr>");
}

function
initControls()
{
    // keywords
    $("#ae_keywords").val(query.keywords);
    if (query.wholewords)
        $("#ae_wholewords").attr("checked","true");
    if (query.inatlas)
        $("#ae_inatlas").attr("checked","true");
    $("#ae_sortby").val(query.sortby);
    $("#ae_sortorder").val(query.sortorder);
    $("#ae_pagesize").val(query.pagesize);
    if (query.detailedview)
        $("#ae_detailedview").attr("checked","true");

    $.get("species-list.html").next( function(data) {
        $("#ae_species").html(data).removeAttr("disabled").val(query.species);
        
    });

    $.get("exptypes-list.html").next( function(data) {
        $("#ae_exptype").html(data).removeAttr("disabled").val(query.exptype);

    });

    $.get("arrays-list.html").next( function(data) {
        addHtmlToSelect("ae_array", data);
        $("#ae_array").removeAttr("disabled").val(query.array);
    });

    if ( "" != query.sortby ) {
        var thElt = $("#ae_results_header_" + query.sortby);
        if ( null != thElt ) {
            thElt.addClass("table_header_box_selected").removeClass("table_header_box").removeClass("sortable");

            if ( "" != query.sortorder) {
                var divElt = thElt.find("div.table_header_inner");
                if ( null != divElt ) {
                    divElt.addClass( "descending" == query.sortorder ? "table_header_sort_desc" : "table_header_sort_asc" );
                }
            }
        }

    }
}

function
addExpansionHandlers()
{
    $(this).mouseover(onTableRowMouseOver)
            .mouseout(onTableRowMouseOut)
            .find("div.table_row_expand")
            .wrap("<a href=\"javascript:aeToggleExpand('" + this.id  + "');\" title=\"Click to reveal/hide more information on the experiment\"><div class=\"table_row_expander\"></div></a>");

    var ext_id = String(this.id).replace("_main", "_ext");
    $("#" + ext_id).mouseover(onTableRowMouseOver).mouseout(onTableRowMouseOut);
}

function
onTableRowMouseOver()
{
    var id = String(this.id).split("_")[0];
    $("#" + id + "_main").addClass("tr_main_active");
    $("#" + id + "_ext").addClass("tr_ext_active");
}

function
onTableRowMouseOut()
{
    var id = String(this.id).split("_")[0];
    $("#" + id + "_main").removeClass("tr_main_active");
    $("#" + id + "_ext").removeClass("tr_ext_active");
}

function
addHtmlToSelect( selectEltId, html )
{
    if ( $.browser.opera ) {
        var htmlParsed = $.clean( new Array(html) );
        var select = $( "#" + selectEltId ).empty();
        for ( var i = 0; i < htmlParsed.length; i++ ) {
            select[0].appendChild(htmlParsed[i].cloneNode(true));
        }
    } else {
        $( "#" + selectEltId ).html(html);
    }
}