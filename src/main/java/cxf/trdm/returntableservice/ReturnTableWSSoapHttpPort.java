package cxf.trdm.returntableservice;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 4.0.3
 * 2023-10-17T18:18:34.799-04:00
 * Generated source version: 4.0.3
 *
 */
@WebService(targetNamespace = "http://trdm/ReturnTableService", name = "ReturnTableWSSoapHttpPort")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface ReturnTableWSSoapHttpPort {

    @WebMethod(action = "getTable")
    @WebResult(name = "getTableResponseElement", targetNamespace = "http://trdm/ReturnTableService", partName = "getTableResponseElement")
    public ReturnTableResponseElement getTable(

        @WebParam(partName = "getTableRequestElement", name = "getTableRequestElement", targetNamespace = "http://trdm/ReturnTableService")
        ReturnTableRequestElement getTableRequestElement
    );

    @WebMethod(action = "getLastTableUpdate")
    @WebResult(name = "getLastTableUpdateResponseElement", targetNamespace = "http://trdm/ReturnTableService", partName = "getLastTableUpdateResponseElement")
    public ReturnTableLastUpdateResponse getLastTableUpdate(

        @WebParam(partName = "getLastTableUpdateRequestElement", name = "getLastTableUpdateRequestElement", targetNamespace = "http://trdm/ReturnTableService")
        ReturnTableLastUpdateRequest getLastTableUpdateRequestElement
    );
}
