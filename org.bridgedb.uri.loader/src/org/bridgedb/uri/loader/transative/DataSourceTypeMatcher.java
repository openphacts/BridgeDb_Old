/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.uri.loader.transative;

import java.util.HashMap;
import org.bridgedb.DataSource;
import org.bridgedb.uri.loader.transative.constant.ChemInf;

/**
 *
 * @author Christian
 */
public class DataSourceTypeMatcher {
    
    private HashMap<DataSource,String> mappings;
    
    private DataSourceTypeMatcher(){
        mappings = new HashMap<DataSource,String>();
        //ConceptWiki various
 //       ChemInf.CHEMICAL_ENTITY = <http://semanticscience.org/resource/SIO_010004>
 //       http://identifiers.org/cas/$id = ChemInf:SIO_010004 //CHEMICAL_ENTITY
  //      http://identifiers.org/obo.chebi/CHEBI:$id
        mappings.put(DataSource.getBySystemCode("Chembl16Molecule"), ChemInf.CHEMICAL_ENTITY);
        mappings.put(DataSource.getBySystemCode("Chembl16TargetComponent"), ChemInf.TARGET);
        mappings.put(DataSource.getBySystemCode("Cs"), ChemInf.CHEMICAL_ENTITY);
        mappings.put(DataSource.getBySystemCode("D"), ChemInf.GENE); //SGD
        mappings.put(DataSource.getBySystemCode("drugbankDrugs"), ChemInf.CHEMICAL_ENTITY);
        mappings.put(DataSource.getBySystemCode("En"), ChemInf.GENE); //Ensembl
        mappings.put(DataSource.getBySystemCode("F"), ChemInf.GENE); //FlyBase
        mappings.put(DataSource.getBySystemCode("Ip"), ChemInf.PROTEIN); //IPI
        mappings.put(DataSource.getBySystemCode("L"), ChemInf.GENE); //Entrez Gene
        mappings.put(DataSource.getBySystemCode("MGI_URI"), ChemInf.GENE);
        mappings.put(DataSource.getBySystemCode("MSH"), ChemInf.CHEMICAL_ENTITY); //mesh
        mappings.put(DataSource.getBySystemCode("Om"), ChemInf.GENE);//omim
        mappings.put(DataSource.getBySystemCode("OPS-CRS"), ChemInf.CHEMICAL_ENTITY);
        mappings.put(DataSource.getBySystemCode("Pd"), ChemInf.PROTEIN);//PDB
        mappings.put(DataSource.getBySystemCode("R"), ChemInf.GENE); //RGD
         //mappings.put(DataSource.getBySystemCode("Q"), VARIOUS); //RefSeq
        mappings.put(DataSource.getBySystemCode("S"), ChemInf.PROTEIN); //Uniprot
        mappings.put(DataSource.getBySystemCode("UniGene_URI"), ChemInf.GENE);
    /*    mappings.put(DataSource.getBySystemCode("Z"), ChemInf.GENE); //ZFIN
        mappings.put(DataSource.getBySystemCode("Ck"), ChemInf); //Kegg Compound
        mappings.put(DataSource.getBySystemCode("Cpc"), ChemInf);
        mappings.put(DataSource.getBySystemCode("Wi"), ChemInf);
        mappings.put(DataSource.getBySystemCode("Wp"), ChemInf);
        mappings.put(DataSource.getBySystemCode("obo.pw/PW"), ChemInf);
        mappings.put(DataSource.getBySystemCode("drugbankTarget"), ChemInf);
        mappings.put(DataSource.getBySystemCode("http://purl.org/obo/owl/GO#$id"), ChemInf);
        mappings.put(DataSource.getBySystemCode("AERS"), ChemInf);
   */ }
    
    public String getType(DataSource source){
        return "test";
    }
}
