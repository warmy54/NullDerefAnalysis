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



import java.util.ArrayList;
import java.util.Iterator;

import soot.NormalUnitPrinter;
import soot.Value;
import soot.jimple.ClassConstant;
import soot.jimple.NullConstant;
import soot.jimple.StringConstant;

public class NullableValueSet implements Iterable<NullableValue>{
	public ArrayList<NullableValue> valueSet;
	public NullableValueSet() {
		valueSet = new ArrayList<NullableValue> ();
	}
	
	public static NullableValueSet merge(NullableValueSet set1,NullableValueSet set2) {
		//System.out.println("merge "+set1.valueSet.size()+"  " +set2.valueSet.size());
		NullableValueSet outset = new NullableValueSet();
		Iterator<NullableValue> it1 = set1.valueSet.iterator();
		Iterator<NullableValue> it2 = set2.valueSet.iterator();
		while (it1.hasNext()){
			NullableValue currentVal = (NullableValue) it1.next();
			if (set2.contains(currentVal)){
				String status2 = NullableValueSet.getStatus(set2, currentVal.value);
				outset.add(currentVal.value, NullableValue.statusMerge(currentVal.status, status2));
			} else {
				outset.add(currentVal.value,currentVal.status);
			}
		}
		while(it2.hasNext()) {
			NullableValue currentVal = it2.next();
			if(outset.contains(currentVal)) {
				
			}else {
				outset.add(currentVal.value,currentVal.status);
			}
		}
		//System.out.println("merge set1 = "+set1.valueSet.size()+"  set 2 = " +set2.valueSet.size() + " out =" + outset.valueSet.size());
		return outset;
	}
	
	public void add(Value v, String s) {
		if (this.contains(v, s)){
			this.valueSet.removeIf(nulval -> (nulval.value == v));
		}
		valueSet.add(new NullableValue(v,s));
	}
	
	@Override
	public NullableValueSet clone() {
		NullableValueSet result = new NullableValueSet();
		Iterator<NullableValue> it = this.valueSet.iterator();
		while(it.hasNext()) {
			NullableValue current = (NullableValue) it.next();
			result.valueSet.add(current.copy());
		}
		return result;
	}
	
	public boolean contains(NullableValue tested) {
		Iterator<NullableValue> it1 = this.valueSet.iterator();
		while(it1.hasNext()) {
			if(it1.next().value.equals(tested.value)) {
				return true;
			}
		}
		return false;
	}
	public boolean contains(Value tested) {
		Iterator<NullableValue> it1 = this.valueSet.iterator();
		while(it1.hasNext()) {
			if(it1.next().value.equals(tested)) {
				return true;
			}
		}
		return false;
	}
	public boolean contains(Value v, String s) {
		Iterator<NullableValue> it1 = this.valueSet.iterator();
		while(it1.hasNext()) {
			if(it1.next().value.equals(v)) {
				return true;
			}
		}
		return false;
	}
	
	
	public static String getStatus(NullableValueSet set,Value v) {
		
		Iterator<NullableValue> it = set.valueSet.iterator();
		while(it.hasNext()) {
			NullableValue current = it.next();
			if (current.value.equals(v)) {
				return current.status;
			}
		}
		throw new RuntimeException("expected value not found");
		
	}

	@Override
	public Iterator<NullableValue> iterator() {
		
		return valueSet.iterator();
	}
	
	public void print() {
		Iterator<NullableValue> it = this.iterator();
		while(it.hasNext()) {
			NullableValue current = (NullableValue) it.next();
			System.out.println("val = " + current.value.toString() + " Status = " +  current.status);
		
		}
	}
	public String get(Value v) {
		Iterator it = this.iterator();
		while(it.hasNext()) {
			NullableValue current = (NullableValue) it.next();
			if(current.value.equivTo(v)) {
				return current.status;
			}
		}
		return "NotFound";
	}
	
	@Override
	public boolean equals(Object set) {
		if (set instanceof NullableValueSet) {
			return this.valueSet.equals(((NullableValueSet) set).valueSet);
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		String result = "Nullable Value Set of size "+ valueSet.size() +" :\n";
		for (NullableValue current : valueSet) {
			result = result.concat("val = " + current.value.toString() + " Status = " +  current.status);
		}
		return result;
	}
	public void replace(NullableValueSet remplacant) {
		this.valueSet.clear();
		for (NullableValue next : remplacant.valueSet) {
			NullableValue nextcopy = new NullableValue(next.value,next.status);
			this.valueSet.add(nextcopy);
		}
		
	}
	public static void main(String[] argv) {
		StringConstant v1 = StringConstant.v("test1");
		StringConstant v2 = StringConstant.v("test2");
		StringConstant v3 = StringConstant.v("test3");
		StringConstant v4 = StringConstant.v("test1");
		NullableValueSet set1 = new NullableValueSet();
		NullableValueSet set2 = new NullableValueSet();
		NullableValueSet set3;
		set1.add(v1, "Null");
		set1.add(v2, "NCP");
		set2.add(v4, "NonNull"); 
		set2.add(v3, "Null-E");
		System.out.println(v1.toString());
		set3 = NullableValueSet.merge(set1, set2);
		System.out.println("set 1");
		set1.print();
		System.out.println("set 2");
		set2.print();
		System.out.println("set 3");
		set3.print();
		
		System.out.println("equality with it self");
		boolean eq = set1.equals(set1);
		System.out.println(eq);
		System.out.println("equality with it different");
		boolean neq = set1.equals(set2);
		System.out.println(neq);
		
		System.out.println("set 3 after remplacement with 2");
		set3.replace(set2);
		set3.print();
		
		System.out.println("Get status de test1");
		System.out.println(set3.get(v2));
		
	}
}

