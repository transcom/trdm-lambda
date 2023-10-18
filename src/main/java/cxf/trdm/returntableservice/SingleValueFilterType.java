
package trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleValueFilterType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="singleValueFilterType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="IS_EQUAL"/&gt;
 *     &lt;enumeration value="IS_NOT_EQUAL"/&gt;
 *     &lt;enumeration value="STARTS_WITH"/&gt;
 *     &lt;enumeration value="ENDS_WITH"/&gt;
 *     &lt;enumeration value="CONTAINS"/&gt;
 *     &lt;enumeration value="DOES_NOT_CONTAIN"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "singleValueFilterType")
@XmlEnum
public enum SingleValueFilterType {

    IS_EQUAL,
    IS_NOT_EQUAL,
    STARTS_WITH,
    ENDS_WITH,
    CONTAINS,
    DOES_NOT_CONTAIN;

    public String value() {
        return name();
    }

    public static SingleValueFilterType fromValue(String v) {
        return valueOf(v);
    }

}
