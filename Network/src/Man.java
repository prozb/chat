import java.lang.reflect.Array;
import java.util.ArrayList;

public class Man {
    public static void main(String[] args) {
        ArrayList<String> arr = new ArrayList<>();
        arr.add("anus");
        arr.add("ura");
        arr.add("misha");

        System.out.println(arr.toString());
        String val = arr.toString();
        val = val.replaceAll(",",":");
        val = val.replaceAll("\\[","");
        val = val.replaceAll("]","");
        System.out.println(val);

    }
}
