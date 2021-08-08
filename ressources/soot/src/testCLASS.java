
public class testCLASS {
	public testCLASS() {
		
	}
	
	public static Object tstmethod() {
		Object a = null;
		return a;
	}
	public static void no(String [] argv) {
		int a,b,c;
		Object d,e,f;
		a = 1;
		b = 2;
		d = null;
		f = new testCLASS();
		e = tstmethod();
		int n = 0;
		while(n < 7) {
			n++;
			if (n == 3) {
				break;
			}
		}
		if (e == d) {
			c = 4;
			f = d;
		} else {
			c = a-b;
		}
		if (c == 3) {
			d = f;
		} else {
			f.toString();
		}
		System.out.println("Itworkednot");
	}
}

