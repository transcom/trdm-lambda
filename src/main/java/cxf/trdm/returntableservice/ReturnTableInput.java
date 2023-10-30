
package cxf.trdm.returntableservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReturnTableInput complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReturnTableInput"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="TRDM"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;all&gt;
 *                   &lt;element name="physicalName" type="{http://trdm/ReturnTableService}physicalNameType"/&gt;
 *                   &lt;element name="returnContent" minOccurs="0"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}boolean"&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="contentUpdatedSinceDateTime" minOccurs="0"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}dateTime"&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="returnRowStatus" minOccurs="0"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}boolean"&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="returnMetadata" minOccurs="0"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}boolean"&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="returnLastUpdate" minOccurs="0"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}boolean"&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="returnColumns" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="column" type="{http://trdm/ReturnTableService}column" maxOccurs="200"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="columnFilterMatchesCriteria" type="{http://trdm/ReturnTableService}columnFilterMatchesCriteria" minOccurs="0"/&gt;
 *                   &lt;element name="columnFilters" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="columnFilter" type="{http://trdm/ReturnTableService}columnFilter" maxOccurs="200"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/all&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReturnTableInput", propOrder = {

})
public class ReturnTableInput {

    @XmlElement(name = "TRDM", required = true)
    protected ReturnTableInput.TRDM trdm;

    /**
     * Gets the value of the trdm property.
     * 
     * @return
     *     possible object is
     *     {@link ReturnTableInput.TRDM }
     *     
     */
    public ReturnTableInput.TRDM getTRDM() {
        return trdm;
    }

    /**
     * Sets the value of the trdm property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReturnTableInput.TRDM }
     *     
     */
    public void setTRDM(ReturnTableInput.TRDM value) {
        this.trdm = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;all&gt;
     *         &lt;element name="physicalName" type="{http://trdm/ReturnTableService}physicalNameType"/&gt;
     *         &lt;element name="returnContent" minOccurs="0"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}boolean"&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="contentUpdatedSinceDateTime" minOccurs="0"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}dateTime"&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="returnRowStatus" minOccurs="0"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}boolean"&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="returnMetadata" minOccurs="0"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}boolean"&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="returnLastUpdate" minOccurs="0"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}boolean"&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="returnColumns" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="column" type="{http://trdm/ReturnTableService}column" maxOccurs="200"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="columnFilterMatchesCriteria" type="{http://trdm/ReturnTableService}columnFilterMatchesCriteria" minOccurs="0"/&gt;
     *         &lt;element name="columnFilters" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="columnFilter" type="{http://trdm/ReturnTableService}columnFilter" maxOccurs="200"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/all&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    public static class TRDM {

        @XmlElement(required = true)
        protected String physicalName;
        protected Boolean returnContent;
        protected XMLGregorianCalendar contentUpdatedSinceDateTime;
        protected Boolean returnRowStatus;
        protected Boolean returnMetadata;
        protected Boolean returnLastUpdate;
        protected ReturnTableInput.TRDM.ReturnColumns returnColumns;
        @XmlSchemaType(name = "string")
        protected ColumnFilterMatchesCriteria columnFilterMatchesCriteria;
        protected ReturnTableInput.TRDM.ColumnFilters columnFilters;

        /**
         * Gets the value of the physicalName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPhysicalName() {
            return physicalName;
        }

        /**
         * Sets the value of the physicalName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPhysicalName(String value) {
            this.physicalName = value;
        }

        /**
         * Gets the value of the returnContent property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isReturnContent() {
            return returnContent;
        }

        /**
         * Sets the value of the returnContent property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setReturnContent(Boolean value) {
            this.returnContent = value;
        }

        /**
         * Gets the value of the contentUpdatedSinceDateTime property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getContentUpdatedSinceDateTime() {
            return contentUpdatedSinceDateTime;
        }

        /**
         * Sets the value of the contentUpdatedSinceDateTime property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setContentUpdatedSinceDateTime(XMLGregorianCalendar value) {
            this.contentUpdatedSinceDateTime = value;
        }

        /**
         * Gets the value of the returnRowStatus property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isReturnRowStatus() {
            return returnRowStatus;
        }

        /**
         * Sets the value of the returnRowStatus property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setReturnRowStatus(Boolean value) {
            this.returnRowStatus = value;
        }

        /**
         * Gets the value of the returnMetadata property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isReturnMetadata() {
            return returnMetadata;
        }

        /**
         * Sets the value of the returnMetadata property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setReturnMetadata(Boolean value) {
            this.returnMetadata = value;
        }

        /**
         * Gets the value of the returnLastUpdate property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isReturnLastUpdate() {
            return returnLastUpdate;
        }

        /**
         * Sets the value of the returnLastUpdate property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setReturnLastUpdate(Boolean value) {
            this.returnLastUpdate = value;
        }

        /**
         * Gets the value of the returnColumns property.
         * 
         * @return
         *     possible object is
         *     {@link ReturnTableInput.TRDM.ReturnColumns }
         *     
         */
        public ReturnTableInput.TRDM.ReturnColumns getReturnColumns() {
            return returnColumns;
        }

        /**
         * Sets the value of the returnColumns property.
         * 
         * @param value
         *     allowed object is
         *     {@link ReturnTableInput.TRDM.ReturnColumns }
         *     
         */
        public void setReturnColumns(ReturnTableInput.TRDM.ReturnColumns value) {
            this.returnColumns = value;
        }

        /**
         * Gets the value of the columnFilterMatchesCriteria property.
         * 
         * @return
         *     possible object is
         *     {@link ColumnFilterMatchesCriteria }
         *     
         */
        public ColumnFilterMatchesCriteria getColumnFilterMatchesCriteria() {
            return columnFilterMatchesCriteria;
        }

        /**
         * Sets the value of the columnFilterMatchesCriteria property.
         * 
         * @param value
         *     allowed object is
         *     {@link ColumnFilterMatchesCriteria }
         *     
         */
        public void setColumnFilterMatchesCriteria(ColumnFilterMatchesCriteria value) {
            this.columnFilterMatchesCriteria = value;
        }

        /**
         * Gets the value of the columnFilters property.
         * 
         * @return
         *     possible object is
         *     {@link ReturnTableInput.TRDM.ColumnFilters }
         *     
         */
        public ReturnTableInput.TRDM.ColumnFilters getColumnFilters() {
            return columnFilters;
        }

        /**
         * Sets the value of the columnFilters property.
         * 
         * @param value
         *     allowed object is
         *     {@link ReturnTableInput.TRDM.ColumnFilters }
         *     
         */
        public void setColumnFilters(ReturnTableInput.TRDM.ColumnFilters value) {
            this.columnFilters = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="columnFilter" type="{http://trdm/ReturnTableService}columnFilter" maxOccurs="200"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "columnFilter"
        })
        public static class ColumnFilters {

            @XmlElement(required = true)
            protected List<ColumnFilter> columnFilter;

            /**
             * Gets the value of the columnFilter property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the Jakarta XML Binding object.
             * This is why there is not a <CODE>set</CODE> method for the columnFilter property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getColumnFilter().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link ColumnFilter }
             * 
             * 
             */
            public List<ColumnFilter> getColumnFilter() {
                if (columnFilter == null) {
                    columnFilter = new ArrayList<ColumnFilter>();
                }
                return this.columnFilter;
            }

        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="column" type="{http://trdm/ReturnTableService}column" maxOccurs="200"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "column"
        })
        public static class ReturnColumns {

            @XmlElement(required = true)
            protected List<String> column;

            /**
             * Gets the value of the column property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the Jakarta XML Binding object.
             * This is why there is not a <CODE>set</CODE> method for the column property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getColumn().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             * 
             * 
             */
            public List<String> getColumn() {
                if (column == null) {
                    column = new ArrayList<String>();
                }
                return this.column;
            }

        }

    }

}
