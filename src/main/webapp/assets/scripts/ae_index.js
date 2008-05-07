//
//  AE Index Page Scripting Support. Requires jQuery 1.2.3 and JSDefeered.jQuery 0.2.1
//

// runs on page reload after rendering is done
$(document).ready( function() {

    // adds a trigger callback for more/less intro text switching
    $("a.ae_intro_more_toggle").click( function() {
        $("div.ae_intro_more").toggle();
    });

    // gets aer stats and updates the page
    $.get("/microarray-as/ae/test/aer_avail_info.xml").next(updateAerStats);

    // gets aew stats and updates the page
    Deferred.parallel([
        $.get("/microarray-as/ae/test/aew_gene_avail_info.xml").next(getNumDocsFromSolrStats),
        $.get("/microarray-as/ae/test/aew_exp_avail_info.xml").next(getNumDocsFromSolrStats)
    ]).next( function (values) {
        var aew_avail_info = values[0] + " genes, " + values[1] + " experiments available";
        $("#aew_avail_info").text(aew_avail_info);
    });

    // loads news page
    $("#ae_news").load("/microarray-as/ae/test/ae_news.xml div ul");

    // loads links page
    $("#ae_links").load("/microarray-as/ae/test/ae_links.xml div ul");
    $("#ae_news_links_area").show();
});

function
trimString( stringToTrim ) {
	return String( stringToTrim ).replace( /^\s+|\s+$/g, "" );
}

function
updateAerStats( xml ) {
    var ae_repxml = $( $(xml).find("ae_repxml")[0] );
    var etotal = ae_repxml.attr("etotal");
    var htotal = ae_repxml.attr("htotal");
    var avail = ae_repxml.attr("avail");
    if ( "true" == avail ) {
        var aer_avail_info = htotal + " hybridizations, " + etotal + " experiments available";
        $("#aer_avail_info").text(aer_avail_info);
    }
}

function
getNumDocsFromSolrStats( xml ) {
    return trimString($( $(xml).find("stat[name='numDocs']")[0] ).text());
}