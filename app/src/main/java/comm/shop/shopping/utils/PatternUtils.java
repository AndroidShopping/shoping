package comm.shop.shopping.utils;

import java.util.regex.Pattern;

public class PatternUtils {
    public static boolean isPort(String str) {
        String regex = "^([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$";

        // 匹配1 和匹配2均可实现Ip判断的效果
        Pattern pattern = Pattern.compile(regex);

        return pattern.matcher(str).matches();

    }

    public static boolean isIP(String str) {

        // 匹配 1
        // String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        // 匹配 2
        String regex = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";

        // 匹配1 和匹配2均可实现Ip判断的效果
        Pattern pattern = Pattern.compile(regex);

        return pattern.matcher(str).matches();

    }
}
