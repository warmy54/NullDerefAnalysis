package dfa;

/*-
 * #%L
 * NullDerefAnalysis a static analisis tool to find null dereference
 * %%
 * Copyright (C) 2021 Frédéric Necker
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import java.util.Iterator;

import soot.Value;

public class NullableValue {
	public Value value;
	public String status;
	public NullableValue(Value val, String stat) {
		value = val;
		status = stat;
	}
	public NullableValue fuse(NullableValue v2) {
		if (this.value.equals(v2.value)){
			return new NullableValue(this.value,NullableValue.statusMerge(this.status,v2.status));
		} else {
			throw new RuntimeException("tried to fuse 2 different values");
		}
	}
	public NullableValue copy() {
		return new NullableValue(this.value,this.status);
	}
	public static String statusMerge(String st1, String st2) {
		if(st1 == "Primary"|| st2 == "Primary") {
			if(st1 == "Primary"	&& st2 == "Primary") {
				return "Primary";
			} else {
				throw new RuntimeException("tried to fuse primary with non primary");
			}
		}
		switch(st1) {
		case "NSP":
			return "NSP";
		case "Null":
			switch(st2) {
			case "Null":
			case "Null-E":
				return "Null";
			default:
				return "NSP";
			}
		case "NSP-E":
			switch(st2) {
			case "Null":
			case "NSP":
				return "NSP";
			default:
				return "NSP-E";
			}
		case "Null-E":
			switch(st2) {
			case "Null":
				return "Null";
			case "NSP":
				return "NSP";
			case "Null-E":
				return "Null-E";
			default: 
				return "NSP-E";
			}
		case "NCP":
			switch(st2) {
			case "Null-E":
			case "NSP-E":
				return "NSP-E";
			case "Null":
			case "NSP":
				return "NSP";
			default:
				return "NCP";
			}
		case "NonNull":
			switch(st2) {
			case "NonNull":
				return "NonNull";
			case "NCP":
				return "NCP";
			case "Null-E":
			case "NSP-E":
				return "NSP-E";
			case "Null":
			case "NSP":
				return "NSP";
			default:
				throw new RuntimeException("Matched NonNull with unexpected answer");
			}
		default:
			throw new RuntimeException("st1 got unexpected status during statusMerge status = " + st1);

		}
	}
	
	@Override
	public NullableValue clone() {
		NullableValue result = new NullableValue(this.value,this.status);
		
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NullableValue) {
			return (this.value.equivTo(((NullableValue) obj).value) &&  this.status.equals(((NullableValue) obj).status));
		} else {
			return false;
		}
	}
	
}
