package org.bridgedb.webservice.cronos;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.2.6
 * Mon Oct 07 15:05:37 BST 2013
 * Generated source version: 2.2.6
 * 
 */
 
@WebService(targetNamespace = "http://webservice.cronos/", name = "CronosWS")
@XmlSeeAlso({ObjectFactory.class})
public interface CronosWS {

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "isinRedList", targetNamespace = "http://webservice.cronos/", className = "org.bridgedb.webservice.cronos.IsinRedList")
    @ResponseWrapper(localName = "isinRedListResponse", targetNamespace = "http://webservice.cronos/", className = "org.bridgedb.webservice.cronos.IsinRedListResponse")
    @WebMethod
    public boolean isinRedList(
        @WebParam(name = "name", targetNamespace = "")
        java.lang.String name,
        @WebParam(name = "organism_3_letter", targetNamespace = "")
        java.lang.String organism3Letter
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "cronosWS", targetNamespace = "http://webservice.cronos/", className = "org.bridgedb.webservice.cronos.CronosWS_Type")
    @ResponseWrapper(localName = "cronosWSResponse", targetNamespace = "http://webservice.cronos/", className = "org.bridgedb.webservice.cronos.CronosWSResponse")
    @WebMethod
    public java.lang.String cronosWS(
        @WebParam(name = "input_id", targetNamespace = "")
        java.lang.String inputId,
        @WebParam(name = "organism_3_letter", targetNamespace = "")
        java.lang.String organism3Letter,
        @WebParam(name = "query_int_id", targetNamespace = "")
        int queryIntId,
        @WebParam(name = "target_int_id", targetNamespace = "")
        int targetIntId
    );
}
