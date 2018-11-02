package calculator.house.com.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyClass {

    public static void main(String[] args) {
        String PATTERN= "(?s).*([0-9ABCDEFGHJKLNPRSTUVWXYZ]{13}[0-9]{4}).*";
        String content="TOYOTA MOTOR CORPORATION\n" +
                "AUSTRALIA LIMITED\n" +
                "TOYOTA KLUGER 50 SER\n" +
                "GVM 02670\n" +
                "SEATS\n" +
                "7\n" +
                "11/14 VIN 5TDYK3FH60S044352\n" +
                "THIS VEHICLE WAS MANUFACTURED TO COMPLY\n" +
                "WITH THE MOTOR VEHICLE STANDARDS ACT 1989\n";
        String content1=  "7\n" + "11/14 VIN 5ADYK3FH60SJ44352\n";
        Matcher matcher= Pattern.compile(PATTERN).matcher(content1);
        if(matcher.matches()){
            System.out.println(matcher.group(1)); // Display the string.
        }

    }
}
