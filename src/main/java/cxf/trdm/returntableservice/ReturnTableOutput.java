
package cxf.trdm.returntableservice;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReturnTableOutput complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReturnTableOutput"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="TRDM"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;all&gt;
 *                   &lt;element name="metadata" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="entityLogicalName"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                   &lt;maxLength value="250"/&gt;
 *                                 &lt;/restriction&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="entityPhysicalName" type="{http://trdm/ReturnTableService}physicalNameType"/&gt;
 *                             &lt;element name="entityDescription"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                   &lt;maxLength value="4000"/&gt;
 *                                 &lt;/restriction&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="entityNotes"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                   &lt;maxLength value="4000"/&gt;
 *                                 &lt;/restriction&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="entitySource" type="{http://trdm/ReturnTableService}entitySource" maxOccurs="100" minOccurs="0"/&gt;
 *                             &lt;element name="attribute" type="{http://trdm/ReturnTableService}attribute" maxOccurs="500"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="status" type="{http://trdm/ReturnTableService}status"/&gt;
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
@XmlType(name = "ReturnTableOutput", propOrder = {

})
public class ReturnTableOutput {

    @XmlElement(name = "TRDM", required = true)
    protected ReturnTableOutput.TRDM trdm;

    /**
     * Gets the value of the trdm property.
     * 
     * @return
     *     possible object is
     *     {@link ReturnTableOutput.TRDM }
     *     
     */
    public ReturnTableOutput.TRDM getTRDM() {
        return trdm;
    }

    /**
     * Sets the value of the trdm property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReturnTableOutput.TRDM }
     *     
     */
    public void setTRDM(ReturnTableOutput.TRDM value) {
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
     *         &lt;element name="metadata" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="entityLogicalName"&gt;
     *                     &lt;simpleType&gt;
     *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                         &lt;maxLength value="250"/&gt;
     *                       &lt;/restriction&gt;
     *                     &lt;/simpleType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="entityPhysicalName" type="{http://trdm/ReturnTableService}physicalNameType"/&gt;
     *                   &lt;element name="entityDescription"&gt;
     *                     &lt;simpleType&gt;
     *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                         &lt;maxLength value="4000"/&gt;
     *                       &lt;/restriction&gt;
     *                     &lt;/simpleType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="entityNotes"&gt;
     *                     &lt;simpleType&gt;
     *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                         &lt;maxLength value="4000"/&gt;
     *                       &lt;/restriction&gt;
     *                     &lt;/simpleType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="entitySource" type="{http://trdm/ReturnTableService}entitySource" maxOccurs="100" minOccurs="0"/&gt;
     *                   &lt;element name="attribute" type="{http://trdm/ReturnTableService}attribute" maxOccurs="500"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="status" type="{http://trdm/ReturnTableService}status"/&gt;
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

        protected ReturnTableOutput.TRDM.Metadata metadata;
        @XmlElement(required = true)
        protected Status status;

        /**
         * Gets the value of the metadata property.
         * 
         * @return
         *     possible object is
         *     {@link ReturnTableOutput.TRDM.Metadata }
         *     
         */
        public ReturnTableOutput.TRDM.Metadata getMetadata() {
            return metadata;
        }

        /**
         * Sets the value of the metadata property.
         * 
         * @param value
         *     allowed object is
         *     {@link ReturnTableOutput.TRDM.Metadata }
         *     
         */
        public void setMetadata(ReturnTableOutput.TRDM.Metadata value) {
            this.metadata = value;
        }

        /**
         * Gets the value of the status property.
         * 
         * @return
         *     possible object is
         *     {@link Status }
         *     
         */
        public Status getStatus() {
            return status;
        }

        /**
         * Sets the value of the status property.
         * 
         * @param value
         *     allowed object is
         *     {@link Status }
         *     
         */
        public void setStatus(Status value) {
            this.status = value;
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
         *         &lt;element name="entityLogicalName"&gt;
         *           &lt;simpleType&gt;
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *               &lt;maxLength value="250"/&gt;
         *             &lt;/restriction&gt;
         *           &lt;/simpleType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="entityPhysicalName" type="{http://trdm/ReturnTableService}physicalNameType"/&gt;
         *         &lt;element name="entityDescription"&gt;
         *           &lt;simpleType&gt;
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *               &lt;maxLength value="4000"/&gt;
         *             &lt;/restriction&gt;
         *           &lt;/simpleType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="entityNotes"&gt;
         *           &lt;simpleType&gt;
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *               &lt;maxLength value="4000"/&gt;
         *             &lt;/restriction&gt;
         *           &lt;/simpleType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="entitySource" type="{http://trdm/ReturnTableService}entitySource" maxOccurs="100" minOccurs="0"/&gt;
         *         &lt;element name="attribute" type="{http://trdm/ReturnTableService}attribute" maxOccurs="500"/&gt;
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
            "entityLogicalName",
            "entityPhysicalName",
            "entityDescription",
            "entityNotes",
            "entitySource",
            "attribute"
        })
        public static class Metadata {

            @XmlElement(required = true)
            protected String entityLogicalName;
            @XmlElement(required = true)
            protected String entityPhysicalName;
            @XmlElement(required = true, nillable = true)
            protected String entityDescription;
            @XmlElement(required = true, nillable = true)
            protected String entityNotes;
            protected List<EntitySource> entitySource;
            @XmlElement(required = true)
            protected List<Attribute> attribute;

            /**
             * Gets the value of the entityLogicalName property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getEntityLogicalName() {
                return entityLogicalName;
            }

            /**
             * Sets the value of the entityLogicalName property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEntityLogicalName(String value) {
                this.entityLogicalName = value;
            }

            /**
             * Gets the value of the entityPhysicalName property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getEntityPhysicalName() {
                return entityPhysicalName;
            }

            /**
             * Sets the value of the entityPhysicalName property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEntityPhysicalName(String value) {
                this.entityPhysicalName = value;
            }

            /**
             * Gets the value of the entityDescription property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getEntityDescription() {
                return entityDescription;
            }

            /**
             * Sets the value of the entityDescription property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEntityDescription(String value) {
                this.entityDescription = value;
            }

            /**
             * Gets the value of the entityNotes property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getEntityNotes() {
                return entityNotes;
            }

            /**
             * Sets the value of the entityNotes property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEntityNotes(String value) {
                this.entityNotes = value;
            }

            /**
             * Gets the value of the entitySource property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the Jakarta XML Binding object.
             * This is why there is not a <CODE>set</CODE> method for the entitySource property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getEntitySource().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link EntitySource }
             * 
             * 
             */
            public List<EntitySource> getEntitySource() {
                if (entitySource == null) {
                    entitySource = new ArrayList<EntitySource>();
                }
                return this.entitySource;
            }

            /**
             * Gets the value of the attribute property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the Jakarta XML Binding object.
             * This is why there is not a <CODE>set</CODE> method for the attribute property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAttribute().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Attribute }
             * 
             * 
             */
            public List<Attribute> getAttribute() {
                if (attribute == null) {
                    attribute = new ArrayList<Attribute>();
                }
                return this.attribute;
            }

        }

    }

}
