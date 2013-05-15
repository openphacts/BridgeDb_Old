package org.bridgedb.linkset.transative;

import org.bridgedb.linkset.ChemInf;
import org.bridgedb.linkset.OboConstants;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;

public class JustificationMakerTest {

	@Test
	public void testCombine_same() throws RDFHandlerException {		
		URI predicate = new URIImpl("http://www.example.org/test#justification");
		assertEquals(predicate, JustificationMaker.combine(predicate , predicate));
	}
	
	@Test(expected=RDFHandlerException.class)
	public void testCombine_diff() throws RDFHandlerException {		
		URI predicate1 = new URIImpl("http://www.example.org/test#justification");
		URI predicate2 = new URIImpl("http://www.example.com/test#different");
		JustificationMaker.combine(predicate1, predicate2);
	}	

	@Test
	public void testCombine_inchi() throws RDFHandlerException {
		assertEquals(ChemInf.INCHI_KEY_URI, 
				JustificationMaker.combine(ChemInf.INCHI_KEY_URI, ChemInf.INCHI_KEY_URI));
	}

	@Test
	public void testCombine_inchi_oboHasParts() throws RDFHandlerException {
		assertEquals(OboConstants.HAS_PART, 
				JustificationMaker.combine(ChemInf.INCHI_KEY_URI, OboConstants.HAS_PART));
	}

	@Test
	public void testCombine_oboHasParts_inchi() throws RDFHandlerException {
		assertEquals(OboConstants.HAS_PART, 
				JustificationMaker.combine(OboConstants.HAS_PART, ChemInf.INCHI_KEY_URI));
	}
	
}
