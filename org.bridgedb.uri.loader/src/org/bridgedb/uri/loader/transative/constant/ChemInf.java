package org.bridgedb.uri.loader.transative.constant;

public class ChemInf {

	private static final String PREFIX = "http://semanticscience.org/resource/";
	
    
    public static final String CHEMICAL_ENTITY = PREFIX + "SIO_010004";
    public static final String INCHI_KEY = PREFIX + "CHEMINF_000059";  
    
    public static final String GENE = PREFIX + "SIO_010035";
    public static final String PROTEIN = PREFIX + "SIO_010043";
    public static final String PROTEIN_CODING_GENE = PREFIX + "SIO_000985";
    public static final String FUNCTIONAL_RNA_CODING_GENE = PREFIX + "SIO_000986";
    public static final String PATHWAY = PREFIX + "SIO_001107";
	public static final String hasStereoundefinedParent = PREFIX + "CHEMINF_000456";
    public static final String hasOPSNormalizedCounterpart = PREFIX + "CHEMINF_000458";
	public static final String hasIsotopicallyUnspecifiedParent = PREFIX + "CHEMINF_000459";
	public static final String hasUnchargedCounterpart = PREFIX + "CHEMINF_000460";
	public static final String hasComponentWithUnchargedCounterpart = PREFIX + "CHEMINF_000480";
	public static final String hasMajorTautomerAtpH7point4 = PREFIX + "CHEMINF_000486";

}
