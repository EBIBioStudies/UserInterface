package uk.ac.ebi.arrayexpress.utils.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.utils.users.UserList;
import uk.ac.ebi.arrayexpress.utils.users.UserRecord;

import java.util.Map;

public class PersistableUserList extends UserList implements Persistable
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PersistableUserList()
    {
        super();
    }

    public PersistableUserList( UserList listToAdopt )
    {
        super.putAll(listToAdopt);
    }

    public String toPersistence()
    {
        StringBuilder sb = new StringBuilder();

        for ( Map.Entry<String, UserRecord> entry : this.entrySet() ) {
            UserRecord user = entry.getValue();
            sb.append(entry.getKey()).append('\t')
                    .append(user.getId()).append('\t')
                    .append(user.getName()).append('\t')
                    .append(user.getPassword()).append('\t')
                    .append(nullToEmpty(user.getEmail())).append('\t')
                    .append(user.isPrivileged()).append('\n');
        }

        return sb.toString();
    }

    public void fromPersistence( String str )
    {
        this.clear();

        int beginIndex = 0;
        int eolIndex = str.indexOf(EOL, beginIndex);
        while ( -1 != eolIndex && eolIndex < str.length() ) {
            String line = str.substring(beginIndex, eolIndex);
            String[] fields = line.split("\t");
            if (6 == fields.length) {
                this.put(fields[0]
                        , new UserRecord(
                            Long.parseLong(fields[1])
                            , fields[2]
                            , fields[3]
                            , fields[4]
                            , Boolean.parseBoolean(fields[5])
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
        return (0 == this.size());
    }

    private String nullToEmpty( String original )
    {
        return ( null == original ) ? "" : original;
    }
}

