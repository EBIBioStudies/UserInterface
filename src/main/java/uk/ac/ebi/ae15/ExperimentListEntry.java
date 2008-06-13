package uk.ac.ebi.ae15;

// TODO: do we need this class???
public class ExperimentListEntry {

    int id;
    String accession;
    boolean isPublic;

    public ExperimentListEntry( int _id, String _accession, boolean _public )
    {
        id = _id;
        accession = _accession;
        isPublic = _public;
    }
}