//
//  AE Index Page Scripting Support. Requires jQuery 1.2.3 and JSDefeered.jQuery 0.2.1
//

// runs on page reload after rendering is done
$(document).ready(function()
{
    // step 0: hack for IE to work with this funny EBI header/footer (to be redeveloped with jQuery)
    if (-1 != navigator.userAgent.indexOf('MSIE')) {
        document.getElementById('head').allowTransparency = true;
    }

    // check if there is a old-fasioned request which
    // we need to dispatch to the new browse interface
    if ("" != window.location.hash) {
        var hash = String(window.location.hash);
        if ( -1 != hash.indexOf("ae-browse") ) {
        var location = "browse.html";
            var pattern = new RegExp("ae-browse\/q=([^\[]*)", "ig");
            var results = pattern.exec(hash);
            if (undefined != results && undefined != results[1]) {
                location = location + "?keywords=" + results[1];
            }
            window.location.href = location;
        }

    }

    // adds a trigger callback for more/less intro text switching
    $("a.ae_intro_more_toggle").click(function()
    {
        $("div.ae_intro_more").toggle();
    });

    // gets aer stats and updates the page
    $.get("servlets/query/stats").next(updateAerStats);

    // gets aew stats and updates the page
    Deferred.parallel([
        $.get("${interface.application.link.solr_gene_stats.url}").next(getNumDocsFromSolrStats),
        $.get("${interface.application.link.solr_exp_stats.url}").next(getNumDocsFromSolrStats)
    ]).next(function (values)
    {
        var aew_avail_info = values[1] + " experiments, " + values[0] + " genes available";
        $("#aew_avail_info").text(aew_avail_info);
    });

    // loads news page
    $("#ae_news").load("${interface.application.link.news_xml.url} div ul");

    // loads links page
    $("#ae_links").load("${interface.application.link.links_xml.url} div ul");
    $("#ae_news_links_area").show();
});

function
        trimString(stringToTrim)
{
    return String(stringToTrim).replace(/^\s+|\s+$/g, "");
}

function
        updateAerStats(xml)
{
    var aer_avail_info = "The information is unavailable at the moment";
    if (undefined != xml) {
        var ae_repxml = $($(xml).find("experiments")[0]);
        var etotal = ae_repxml.attr("total");
        var htotal = ae_repxml.attr("total-assays");
        if (etotal != undefined && etotal > 0) {
            aer_avail_info = etotal + " experiments, " + htotal + " assays available";
        }
    }
    $("#aer_avail_info").text(aer_avail_info);
}

function
        getNumDocsFromSolrStats(xml)
{
    return trimString($($(xml).find("stat[name='numDocs']")[0]).text());
}