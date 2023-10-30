
package cxf.trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for twoValueFilterType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="twoValueFilterType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="IS_BETWEEN"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "twoValueFilterType")
@XmlEnum
public enum TwoValueFilterType {

    IS_BETWEEN;

    public String value() {
        return name();
    }

    public static TwoValueFilterType fromValue(String v) {
        return valueOf(v);
    }

}
