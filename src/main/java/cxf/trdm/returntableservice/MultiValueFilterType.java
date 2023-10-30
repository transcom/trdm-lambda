
package cxf.trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for multiValueFilterType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="multiValueFilterType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="IS_IN_LIST"/&gt;
 *     &lt;enumeration value="IS_NOT_IN_LIST"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "multiValueFilterType")
@XmlEnum
public enum MultiValueFilterType {

    IS_IN_LIST,
    IS_NOT_IN_LIST;

    public String value() {
        return name();
    }

    public static MultiValueFilterType fromValue(String v) {
        return valueOf(v);
    }

}
