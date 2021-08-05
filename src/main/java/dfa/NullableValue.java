package dfa;

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
