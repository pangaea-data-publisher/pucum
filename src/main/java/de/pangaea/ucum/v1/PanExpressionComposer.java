package de.pangaea.ucum.v1;

import org.fhir.ucum.Canonical;
import org.fhir.ucum.Canonical.CanonicalUnit;
import org.fhir.ucum.ExpressionComposer;

public class PanExpressionComposer extends ExpressionComposer {

	public String compose(Canonical can, boolean value) {
		StringBuilder b = new StringBuilder();
		if (value)
			b.append(can.getValue().asDecimal());
		boolean first = true;
		for (CanonicalUnit c : can.getUnits()) {
			if (first)
				first = false;
			else
				b.append(".");
			// the difference!! getDim instead of getCode
			b.append(c.getBase().getDim());
			if (c.getExponent() != 1)
				b.append(c.getExponent());
		}
		return b.toString();
	}

	public String compose(Canonical can, boolean value, boolean verbose) {
		StringBuilder b = new StringBuilder();
		if (value)
			b.append(can.getValue().asDecimal());
		boolean first = true;
		for (CanonicalUnit c : can.getUnits()) {
			if (first)
				first = false;
			else if (verbose == true)
				b.append(" &middot; ");
			else
				b.append(".");
			if (verbose == true)
				b.append(c.getBase().getProperty());
			else
				b.append(c.getBase().getCode());
			if (c.getExponent() != 1)
				if (verbose == true)
					b.append("<sup>" + c.getExponent() + "</sup>");
				else
					b.append(c.getExponent());
		}
		return b.toString();
	}

}
