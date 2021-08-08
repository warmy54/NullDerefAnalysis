public class contextChange{

public void a(Object i){
    i = new Object();
}
public static void main(String argv[]){
    contextChange cC = new contextChange();
    Object b = null;
    cC.a(b);
    b.toString();
}


}
