public class Utils {
    // encode each line individually
    public static String b64_encode(String s) {
        String[] encoded = new String[(int)(s.chars().filter(ch -> ch == '\n').count())];
        int line_idx = 0;
        for (String line : s.split("\n")) {
            encoded[line_idx] = b64_encode_segment(line);
            line_idx += 1;
        }

        return String.join("\n", encoded);
    }

    public static String BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static String b64_encode_segment(String s) {
        String encoded = "";
        while (s.length() >= 3) {
            int[] segment = s.substring(0, 3).chars().toArray();
            s = s.substring(3);
            int segm = (segment[0] << 16) | (segment[1] << 8) | segment[2];

            for (int j = 0; j < 4; j++) {
                int b64char = (segm >> (18 - j * 6)) & 0b111111;
                encoded += BASE64_CHARS.charAt(b64char);
            }
        }

        if (s.length() == 2) {
            int[] segment = s.chars().toArray();
            int segm = (segment[0] << 10) | (segment[1] << 2);
            for (int j = 0; j < 3; j++) {
                int b64char = (segm >> (18 - j * 6)) & 0b111111;
                encoded += BASE64_CHARS.charAt(b64char);
            }
            encoded += "=";
        } else if (s.length() == 1) {
            int segm = (int)s.charAt(0) << 4;
            for (int j = 0; j < 2; j++) {
                int b64char = (segm >> (18 - j * 6)) & 0b111111;
                encoded += BASE64_CHARS.charAt(b64char);
            }
            encoded += "==";
        }

        return encoded;
    }

    public static int hash_location(int x, int y) {
        return x>=y?x*x+x+y:x+y*y;
    }

    public static int picker(int hash, int m) {
        return ((hash * 71) >> 5) % m;
    }

    public static String write_string(String original, String paste, int start_pos) {
        int end = Math.min(start_pos + paste.length(), original.length());
        return original.substring(0, start_pos) + paste + original.substring(end);
    }

    public static String center_string(String original, int center_amount) {
        if (center_amount <= original.length()) return original;
        int buffer_space = center_amount - original.length();
        int left = buffer_space / 2;
        int right = buffer_space - left;
        return " ".repeat(left) + original + " ".repeat(right);
    }

    public static String HEX_CHARS = "0123456789abcdef";

    public static String hex_encode(String original) {
        String out = "";
        int index = 0;
        for (int ch : original.chars().toArray()) {
            index += 1;
            int upper = ch >> 4;
            int lower = ch & 0x0f;
            out += HEX_CHARS.charAt(upper) + HEX_CHARS.charAt(lower);
            if (original.length() != index) { out += " "; }
        }

        return out;
    }

    public static String bin_encode(String original) {
        String[] out = new String[(int)(original.chars().count())];
        int index = 0;
        for (int ch : original.chars().toArray()) {
            out[index] = Integer.toBinaryString(ch);
            index += 1;
        }

        return String.join(" ", out);
    }
}
