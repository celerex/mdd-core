package be.celerex.mdd.core;

import java.math.BigDecimal;
import java.math.BigInteger;

import be.celerex.mdd.api.ScalarProvider;

public class ParsingScalarProvider implements ScalarProvider {

	@Override
	public Object unmarshal(String value, Object parent, String key) {
		if (value == null) {
			return null;
		}
		else if (value.matches("^[0-9]+$")) {
			return new BigInteger(value);
		}
		else if (value.matches("^[0-9]+\\.[0-9]+$")) {
			return new BigDecimal(value);
		}
		else if (value.equals("true")) {
			return true;
		}
		else if (value.equals("false")) {
			return false;
		}
		return value;
	}

	@Override
	public String marshal(Object value, Object parent, String key) {
		return value == null ? null : value.toString();
	}

}
