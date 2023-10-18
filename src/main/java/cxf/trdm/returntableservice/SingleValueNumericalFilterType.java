
package trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleValueNumericalFilterType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="singleValueNumericalFilterType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="IS_GREATER_THAN"/&gt;
 *     &lt;enumeration value="IS_GREATER_THAN_OR_EQUAL"/&gt;
 *     &lt;enumeration value="IS_LESS_THAN"/&gt;
 *     &lt;enumeration value="IS_LESS_THAN_OR_EQUAL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "singleValueNumericalFilterType")
@XmlEnum
public enum SingleValueNumericalFilterType {

    IS_GREATER_THAN,
    IS_GREATER_THAN_OR_EQUAL,
    IS_LESS_THAN,
    IS_LESS_THAN_OR_EQUAL;

    public String value() {
        return name();
    }

    public static SingleValueNumericalFilterType fromValue(String v) {
        return valueOf(v);
    }

}
