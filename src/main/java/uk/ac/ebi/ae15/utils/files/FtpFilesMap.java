package uk.ac.ebi.ae15.utils.files;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FtpFilesMap
{
    private List<FtpFileEntry> entries = new ArrayList<FtpFileEntry>();

    private EntriesCollectionMap accessionMap = new EntriesCollectionMap();
    private EntriesCollectionMap nameMap = new EntriesCollectionMap();

    public void putEntry( FtpFileEntry entry )
    {
        entries.add(entry);
        int entryIndex = entries.size();
        accessionMap.putEntry(FtpFileEntry.getAccession(entry), entryIndex);
        nameMap.putEntry(FtpFileEntry.getName(entry), entryIndex);
    }

    public FtpFileEntry getEntry( String accession, String name )
    {
        Set<Integer> set = new HashSet<Integer>(accessionMap.get(accession));
        set.retainAll(nameMap.get(name));
        if (1 == set.size()) {
            return entries.get(((Integer[])set.toArray())[1]);
        }

        return null;
    }
}
