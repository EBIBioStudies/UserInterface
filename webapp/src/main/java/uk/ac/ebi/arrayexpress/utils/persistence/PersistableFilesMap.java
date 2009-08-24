package uk.ac.ebi.arrayexpress.utils.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.utils.files.FtpFileEntry;
import uk.ac.ebi.arrayexpress.utils.files.FtpFilesMap;

public class PersistableFilesMap extends FtpFilesMap implements Persistable
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String toPersistence()
    {
        StringBuilder sb = new StringBuilder();

        for ( FtpFileEntry entry : this.entries ) {
            sb.append(entry.getLocation()).append('\t')
                    .append(String.valueOf(entry.getSize())).append('\t')
                    .append(String.valueOf(entry.getLastModified()))
                    .append('\n');
        }

        return sb.toString();
    }

    public void fromPersistence( String str )
    {
        this.entries.clear();

        int beginIndex = 0;
        int eolIndex = str.indexOf(EOL, beginIndex);
        while ( -1 != eolIndex && eolIndex < str.length() ) {
            String line = str.substring(beginIndex, eolIndex);
            String[] fields = line.split("\t");
            if (3 == fields.length) {
                this.putEntry(
                        new FtpFileEntry(
                                fields[0]
                                , Long.parseLong(fields[1])
                                , Long.parseLong(fields[2])
                        ));
            } else {
                logger.warn("No enough TABs found while parsing persistence string, line from [{}] to [{}]", beginIndex, eolIndex);
            }
            beginIndex = eolIndex + 1;
            eolIndex = str.indexOf(EOL, beginIndex);
        }
    }

    public boolean isEmpty()
    {
        return (0 == this.entries.size());
    }
}