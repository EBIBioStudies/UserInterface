package uk.ac.ebi.ae15.utils.files;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FtpFilesMap
{
    protected List<FtpFileEntry> entries = new ArrayList<FtpFileEntry>();

    private EntriesCollectionMap accessionMap = new EntriesCollectionMap();
    private EntriesCollectionMap nameMap = new EntriesCollectionMap();

    public void putEntry( FtpFileEntry entry )
    {
        entries.add(entry);
        int entryIndex = entries.size() - 1;
        accessionMap.putEntry(FtpFileEntry.getAccession(entry), entryIndex);
        nameMap.putEntry(FtpFileEntry.getName(entry), entryIndex);
    }

    public FtpFileEntry getEntry( String accession, String name )
    {
        Set<Integer> set = new HashSet<Integer>(accessionMap.get(accession));
        set.retainAll(nameMap.get(name));
        if (1 == set.size()) {
            return entries.get(set.iterator().next());
        }

        return null;
    }

    public List<FtpFileEntry> getEntriesByAccession( String accession )
    {
        List<FtpFileEntry> result = null;
        if (doesAccessionExist(accession)) {
            Set<Integer> set = new HashSet<Integer>(accessionMap.get(accession));
            if (0 < set.size()) {
                result = new ArrayList<FtpFileEntry>(set.size());
                for ( Integer entryIndex : set ) {
                    result.add(entries.get(entryIndex));
                }
            }
        }
        return result;
    }

    public List<FtpFileEntry> getEntriesByName( String name )
    {
        List<FtpFileEntry> result = null;
        Set<Integer> set = new HashSet<Integer>(nameMap.get(name));
        if (0 < set.size()) {
            result = new ArrayList<FtpFileEntry>(set.size());
            for ( Integer entryIndex : set ) {
                result.add(entries.get(entryIndex));
            }
        }

        return result;
    }

    public List<FtpFileEntry> getEntries()
    {
        return entries;
    }

    public boolean doesNameExist( String name )
    {
        return nameMap.containsKey(name);
    }

    public boolean doesAccessionExist( String accession )
    {
        return accessionMap.containsKey(accession);
    }

    public boolean doesExist( String accession, String name )
    {
        return accessionMap.containsKey(accession) && nameMap.containsKey(name);
    }
}
