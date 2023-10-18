
package trdm.returntableservice;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for columnFilterTypeAndValues complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="columnFilterTypeAndValues"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="25"&gt;
 *         &lt;element name="noValueFilter" type="{http://trdm/ReturnTableService}noValueFilter" minOccurs="0"/&gt;
 *         &lt;element name="singleValueFilter" type="{http://trdm/ReturnTableService}singleValueFilter" minOccurs="0"/&gt;
 *         &lt;element name="singleValueNumericalFilter" type="{http://trdm/ReturnTableService}singleValueNumericalFilter" minOccurs="0"/&gt;
 *         &lt;element name="singleValueDateFilter" type="{http://trdm/ReturnTableService}singleValueDateFilter" minOccurs="0"/&gt;
 *         &lt;element name="singleValueDateTimeFilter" type="{http://trdm/ReturnTableService}singleValueDateTimeFilter" minOccurs="0"/&gt;
 *         &lt;element name="twoValueNumericalFilter" type="{http://trdm/ReturnTableService}twoValueNumericalFilter" minOccurs="0"/&gt;
 *         &lt;element name="twoValueDateTimeFilter" type="{http://trdm/ReturnTableService}twoValueDateTimeFilter" minOccurs="0"/&gt;
 *         &lt;element name="multiValueFilter" type="{http://trdm/ReturnTableService}multiValueFilter" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "columnFilterTypeAndValues", propOrder = {
    "noValueFilterOrSingleValueFilterOrSingleValueNumericalFilter"
})
public class ColumnFilterTypeAndValues {

    @XmlElements({
        @XmlElement(name = "noValueFilter", type = NoValueFilter.class),
        @XmlElement(name = "singleValueFilter", type = SingleValueFilter.class),
        @XmlElement(name = "singleValueNumericalFilter", type = SingleValueNumericalFilter.class),
        @XmlElement(name = "singleValueDateFilter", type = SingleValueDateFilter.class),
        @XmlElement(name = "singleValueDateTimeFilter", type = SingleValueDateTimeFilter.class),
        @XmlElement(name = "twoValueNumericalFilter", type = TwoValueNumericalFilter.class),
        @XmlElement(name = "twoValueDateTimeFilter", type = TwoValueDateTimeFilter.class),
        @XmlElement(name = "multiValueFilter", type = MultiValueFilter.class)
    })
    protected List<Object> noValueFilterOrSingleValueFilterOrSingleValueNumericalFilter;

    /**
     * Gets the value of the noValueFilterOrSingleValueFilterOrSingleValueNumericalFilter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the noValueFilterOrSingleValueFilterOrSingleValueNumericalFilter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNoValueFilterOrSingleValueFilterOrSingleValueNumericalFilter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MultiValueFilter }
     * {@link NoValueFilter }
     * {@link SingleValueDateFilter }
     * {@link SingleValueDateTimeFilter }
     * {@link SingleValueFilter }
     * {@link SingleValueNumericalFilter }
     * {@link TwoValueDateTimeFilter }
     * {@link TwoValueNumericalFilter }
     * 
     * 
     */
    public List<Object> getNoValueFilterOrSingleValueFilterOrSingleValueNumericalFilter() {
        if (noValueFilterOrSingleValueFilterOrSingleValueNumericalFilter == null) {
            noValueFilterOrSingleValueFilterOrSingleValueNumericalFilter = new ArrayList<Object>();
        }
        return this.noValueFilterOrSingleValueFilterOrSingleValueNumericalFilter;
    }

}
