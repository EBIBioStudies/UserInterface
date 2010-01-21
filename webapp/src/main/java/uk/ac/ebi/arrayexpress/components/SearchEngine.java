package uk.ac.ebi.arrayexpress.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.saxon.search.Controller;
import uk.ac.ebi.arrayexpress.utils.saxon.search.SearchExtension;

public class SearchEngine extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Controller controller;

    public SearchEngine()
    {
        super("SearchEngine");
    }

    public void initialize()
    {
        try {
            this.controller = new Controller(getApplication().getResource("/WEB-INF/classes/aeindex.xml"));
            SearchExtension.setController(getController());
        } catch (Exception x) {
            logger.error("Caught an exception:", x);
        }

    }

    public void terminate()
    {
        SearchExtension.setController(null);
    }

    public Controller getController()
    {
        return this.controller;
    }
}
