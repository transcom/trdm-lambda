
package cxf.trdm.returntableservice;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the trdm.returntableservice package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetLastTableUpdateRequestElement_QNAME = new QName("http://trdm/ReturnTableService", "getLastTableUpdateRequestElement");
    private final static QName _GetLastTableUpdateResponseElement_QNAME = new QName("http://trdm/ReturnTableService", "getLastTableUpdateResponseElement");
    private final static QName _GetTableRequestElement_QNAME = new QName("http://trdm/ReturnTableService", "getTableRequestElement");
    private final static QName _GetTableResponseElement_QNAME = new QName("http://trdm/ReturnTableService", "getTableResponseElement");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: trdm.returntableservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ReturnTableOutput }
     * 
     */
    public ReturnTableOutput createReturnTableOutput() {
        return new ReturnTableOutput();
    }

    /**
     * Create an instance of {@link ReturnTableOutput.TRDM }
     * 
     */
    public ReturnTableOutput.TRDM createReturnTableOutputTRDM() {
        return new ReturnTableOutput.TRDM();
    }

    /**
     * Create an instance of {@link ReturnTableInput }
     * 
     */
    public ReturnTableInput createReturnTableInput() {
        return new ReturnTableInput();
    }

    /**
     * Create an instance of {@link ReturnTableInput.TRDM }
     * 
     */
    public ReturnTableInput.TRDM createReturnTableInputTRDM() {
        return new ReturnTableInput.TRDM();
    }

    /**
     * Create an instance of {@link ReturnTableLastUpdateRequest }
     * 
     */
    public ReturnTableLastUpdateRequest createReturnTableLastUpdateRequest() {
        return new ReturnTableLastUpdateRequest();
    }

    /**
     * Create an instance of {@link ReturnTableLastUpdateResponse }
     * 
     */
    public ReturnTableLastUpdateResponse createReturnTableLastUpdateResponse() {
        return new ReturnTableLastUpdateResponse();
    }

    /**
     * Create an instance of {@link ReturnTableRequestElement }
     * 
     */
    public ReturnTableRequestElement createReturnTableRequestElement() {
        return new ReturnTableRequestElement();
    }

    /**
     * Create an instance of {@link ReturnTableResponseElement }
     * 
     */
    public ReturnTableResponseElement createReturnTableResponseElement() {
        return new ReturnTableResponseElement();
    }

    /**
     * Create an instance of {@link NoValueFilter }
     * 
     */
    public NoValueFilter createNoValueFilter() {
        return new NoValueFilter();
    }

    /**
     * Create an instance of {@link SingleValueFilter }
     * 
     */
    public SingleValueFilter createSingleValueFilter() {
        return new SingleValueFilter();
    }

    /**
     * Create an instance of {@link SingleValueDateFilter }
     * 
     */
    public SingleValueDateFilter createSingleValueDateFilter() {
        return new SingleValueDateFilter();
    }

    /**
     * Create an instance of {@link SingleValueDateTimeFilter }
     * 
     */
    public SingleValueDateTimeFilter createSingleValueDateTimeFilter() {
        return new SingleValueDateTimeFilter();
    }

    /**
     * Create an instance of {@link SingleValueNumericalFilter }
     * 
     */
    public SingleValueNumericalFilter createSingleValueNumericalFilter() {
        return new SingleValueNumericalFilter();
    }

    /**
     * Create an instance of {@link TwoValueNumericalFilter }
     * 
     */
    public TwoValueNumericalFilter createTwoValueNumericalFilter() {
        return new TwoValueNumericalFilter();
    }

    /**
     * Create an instance of {@link TwoValueDateTimeFilter }
     * 
     */
    public TwoValueDateTimeFilter createTwoValueDateTimeFilter() {
        return new TwoValueDateTimeFilter();
    }

    /**
     * Create an instance of {@link MultiValueFilter }
     * 
     */
    public MultiValueFilter createMultiValueFilter() {
        return new MultiValueFilter();
    }

    /**
     * Create an instance of {@link ColumnFilterTypeAndValues }
     * 
     */
    public ColumnFilterTypeAndValues createColumnFilterTypeAndValues() {
        return new ColumnFilterTypeAndValues();
    }

    /**
     * Create an instance of {@link ColumnFilter }
     * 
     */
    public ColumnFilter createColumnFilter() {
        return new ColumnFilter();
    }

    /**
     * Create an instance of {@link Status }
     * 
     */
    public Status createStatus() {
        return new Status();
    }

    /**
     * Create an instance of {@link Attribute }
     * 
     */
    public Attribute createAttribute() {
        return new Attribute();
    }

    /**
     * Create an instance of {@link EntitySource }
     * 
     */
    public EntitySource createEntitySource() {
        return new EntitySource();
    }

    /**
     * Create an instance of {@link ReturnTableOutput.TRDM.Metadata }
     * 
     */
    public ReturnTableOutput.TRDM.Metadata createReturnTableOutputTRDMMetadata() {
        return new ReturnTableOutput.TRDM.Metadata();
    }

    /**
     * Create an instance of {@link ReturnTableInput.TRDM.ReturnColumns }
     * 
     */
    public ReturnTableInput.TRDM.ReturnColumns createReturnTableInputTRDMReturnColumns() {
        return new ReturnTableInput.TRDM.ReturnColumns();
    }

    /**
     * Create an instance of {@link ReturnTableInput.TRDM.ColumnFilters }
     * 
     */
    public ReturnTableInput.TRDM.ColumnFilters createReturnTableInputTRDMColumnFilters() {
        return new ReturnTableInput.TRDM.ColumnFilters();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReturnTableLastUpdateRequest }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ReturnTableLastUpdateRequest }{@code >}
     */
    @XmlElementDecl(namespace = "http://trdm/ReturnTableService", name = "getLastTableUpdateRequestElement")
    public JAXBElement<ReturnTableLastUpdateRequest> createGetLastTableUpdateRequestElement(ReturnTableLastUpdateRequest value) {
        return new JAXBElement<ReturnTableLastUpdateRequest>(_GetLastTableUpdateRequestElement_QNAME, ReturnTableLastUpdateRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReturnTableLastUpdateResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ReturnTableLastUpdateResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://trdm/ReturnTableService", name = "getLastTableUpdateResponseElement")
    public JAXBElement<ReturnTableLastUpdateResponse> createGetLastTableUpdateResponseElement(ReturnTableLastUpdateResponse value) {
        return new JAXBElement<ReturnTableLastUpdateResponse>(_GetLastTableUpdateResponseElement_QNAME, ReturnTableLastUpdateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReturnTableRequestElement }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ReturnTableRequestElement }{@code >}
     */
    @XmlElementDecl(namespace = "http://trdm/ReturnTableService", name = "getTableRequestElement")
    public JAXBElement<ReturnTableRequestElement> createGetTableRequestElement(ReturnTableRequestElement value) {
        return new JAXBElement<ReturnTableRequestElement>(_GetTableRequestElement_QNAME, ReturnTableRequestElement.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReturnTableResponseElement }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ReturnTableResponseElement }{@code >}
     */
    @XmlElementDecl(namespace = "http://trdm/ReturnTableService", name = "getTableResponseElement")
    public JAXBElement<ReturnTableResponseElement> createGetTableResponseElement(ReturnTableResponseElement value) {
        return new JAXBElement<ReturnTableResponseElement>(_GetTableResponseElement_QNAME, ReturnTableResponseElement.class, null, value);
    }

}
