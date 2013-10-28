/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bridgedb.Xref;

/**
 *
 * @author Christian
 */
public class SqlIterator implements Iterator<Xref>{

    private ResultSet resultSet;
    private Xref nextRef;
    
    public SqlIterator(ResultSet rs){
        resultSet = rs;
        nextRef = readNextXref();
    }
    
    @Override
    public boolean hasNext() {
        return nextRef != null;
    }

    @Override
    public Xref next() {
        if (nextRef == null){
            throw new NoSuchElementException("No more Xref available.");
        }
        Xref result = nextRef;
        nextRef = readNextXref();
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not allowed.");
    }

    private Xref readNextXref() {
        try {
            if(resultSet.next()){
                String id = resultSet.getString(SQLListener.SOURCE_ID_COLUMN_NAME);
                String sysCode = resultSet.getString(SQLListener.SOURCE_DATASOURCE_COLUMN_NAME);
                IdSysCodePair pair = new IdSysCodePair(id, sysCode);
                return pair.toXref();
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to get next xref. ", ex);
        }
    }
    
}
