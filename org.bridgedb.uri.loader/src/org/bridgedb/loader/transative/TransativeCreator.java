/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.loader.transative;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import org.apache.log4j.Logger;
import org.bridgedb.rdf.UriPattern;
import org.bridgedb.sql.SQLAccess;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.sql.SqlFactory;
import org.bridgedb.statistics.DataSetInfo;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.uri.UriMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.DirectoriesConfig;
import org.bridgedb.utils.Reporter;
import org.bridgedb.utils.StoreType;
import org.openrdf.model.URI;

/**
 *
 * @author Christian
 */
class TransativeCreator {

    private final SQLAccess sqlAccess;
    private final UriMapper mapper;
    private final StoreType storeType;
    private final MappingSetInfo leftInfo;
    private final MappingSetInfo rightInfo;
    private final BufferedWriter buffer;
    private final String predicate;
    private final String justification;
    private final File outputFile;
    private final UriPattern sourceUriPattern;
    private final UriPattern targetUriPattern;

    private static URI GENERATE_PREDICATE = null;

    static final Logger logger = Logger.getLogger(TransativeCreator.class);
    
    private TransativeCreator(MappingSetInfo left, MappingSetInfo right, StoreType storeType) 
            throws BridgeDBException, IOException{
        sqlAccess = SqlFactory.createTheSQLAccess(storeType);
        mapper = SQLUriMapper.factory(false, storeType);
        this.storeType = storeType;
        leftInfo = left;
        rightInfo = right;
        predicate = PredicateMaker.combine(left.getPredicate(), right.getPredicate());
        justification = JustificationMaker.combine(left.getJustification(), right.getJustification());
        sourceUriPattern = getUriPattern(left.getSource());
        targetUriPattern = getUriPattern(right.getTarget());
        checkTransativeLegal(left, right);
        File parent = DirectoriesConfig.getTransativeDirectory();
        outputFile = new File(parent, "TransativeLinkset" + left.getStringId() + "and" + right.getStringId() + ".ttl");
        Reporter.println("Writing transative to " + outputFile.getAbsolutePath());
        FileWriter writer = new FileWriter(outputFile);
        buffer = new BufferedWriter(writer);
        buffer.flush();
    }
    
    public static File doTransativeIfPossible(MappingSetInfo left, MappingSetInfo right, StoreType storeType) throws BridgeDBException, IOException {
        TransativeCreator creator = new TransativeCreator(left, right, storeType);
        boolean result = creator.getSQL();
        if (result){
            return creator.getOutputFile();
        } else {
            return null;
        }
    }

    public static File doTransativeIfPossible(int leftId, int rightId, StoreType storeType) 
            throws BridgeDBException, IOException {
        SQLUriMapper mapper = SQLUriMapper.factory(false, storeType);
        MappingSetInfo left = mapper.getMappingSetInfo(leftId);
        MappingSetInfo right = mapper.getMappingSetInfo(rightId);
        return doTransativeIfPossible(left, right, storeType);
    }
    
    private boolean getSQL() throws BridgeDBException, IOException {
        boolean found = false;
        buffer.newLine();
        StringBuilder query = new StringBuilder(
                "SELECT mapping1.sourceId, mapping2.targetId ");
        query.append("FROM mapping as mapping1, mapping as mapping2 ");
        query.append("WHERE mapping1.targetId = mapping2.sourceId ");
        query.append("AND mapping1.mappingSetId = ");
            query.append(leftInfo.getIntId());
            query.append(" ");
        query.append("AND mapping2.mappingSetId = ");
            query.append(rightInfo.getIntId());
            query.append(" ");
        System.out.println(query);
        Connection connection = sqlAccess.getConnection();
        java.sql.Statement statement;
        try {
            statement = connection.createStatement();
        } catch (SQLException ex) {
           throw new BridgeDBException("Unable to get statement. ", ex);
        }
        try {
            logger.info("Running " + query.toString());
            ResultSet rs = statement.executeQuery(query.toString());
            logger.info("processing results");
            while (rs.next()){
                String sourceId = rs.getString("mapping1.sourceId");
                String targetId = rs.getString("mapping2.targetId");
                if (!sourceId.equals(targetId)){
                    String sourceUri = sourceUriPattern.getPrefix() + sourceId + sourceUriPattern.getPostfix();
                    String targetUri = targetUriPattern.getPrefix() + targetId + targetUriPattern.getPostfix();
                    found = true;
                    buffer.write("<");
                        buffer.write(sourceUri);
                    buffer.write("> <");
                        buffer.write(predicate);
                    buffer.write( "> <");
                        buffer.write(targetUri);
                    buffer.write("> . "); 
                    buffer.newLine();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BridgeDBException("Unable to run query. " + query, ex);
        }
        buffer.flush();
        buffer.close();
        return found;
    }
    
    private File getOutputFile() {
        return outputFile;
    }

    private UriPattern getUriPattern(DataSetInfo info) throws BridgeDBException {
        Set<String> patterns = mapper.getUriPatterns(info.getSysCode());
        if (patterns.isEmpty()){
            throw new BridgeDBException("No URI pattern known for " + info.getSysCode());
        }
        String pattern = patterns.iterator().next();
        return UriPattern.byPattern(pattern);
    }

    private void checkTransativeLegal(MappingSetInfo left, MappingSetInfo right) throws BridgeDBException {
        if (!left.getTarget().equals(right.getSource())){
            throw new BridgeDBException("Left target " + left.getTarget() + " does not match right source " + right.getSource());
        }
    }

    
}