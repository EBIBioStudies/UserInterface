package uk.ac.ebi.ae15.utils.files;

import uk.ac.ebi.ae15.utils.RegExpHelper;

import java.io.File;

public class FtpFileEntry
{
    public String location = null;
    public Long size = null;
    public Long lastModified = null;

    public FtpFileEntry( File file )
    {
        if (file.isFile()) {
            location = file.getAbsolutePath();
            size = file.length();
            lastModified = file.lastModified();
        }
    }

    public static String getAccession( FtpFileEntry entry )
    {
        return (null != entry.location) ? accessionRegExp.matchFirst(entry.location) : "";
    }

    public static String getName( FtpFileEntry entry )
    {
        return (null != entry.location) ? nameRegExp.matchFirst(entry.location) : "";
    }

    private static final RegExpHelper accessionRegExp
            = new RegExpHelper("/([aAeE]-\\w{4}-\\d+)/");

    private static final RegExpHelper nameRegExp
            = new RegExpHelper("/([^/])$");
}
