import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import java.nio.ByteBuffer;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

public class Inputs {
    public static Terminal terminal;
    private static NonBlockingReader reader;
    private static boolean inited = false;
    private static InputStream is;

    // prevent one key from being registered every single tick it's held
    private static Keybinds last_seen_key = Keybinds.UNKNOWN;

    public static void init() throws IOException {
        if (inited) return; // ensures we don't accidentally create two terminals
        terminal = TerminalBuilder.builder().build();
        terminal.enterRawMode();
        if (terminal.hasKittyKeyboardSupport()) {
            terminal.setKittyKeyboardMode(EnumSet.of(
                Terminal.KittyKeyboardMode.Disambiguate,
                Terminal.KittyKeyboardMode.ReportEvents // send release messages
            ));
            /* we good */
        } else {
            System.out.println("Warning: This terminal emulator does not support key releases.");
            throw new IOException("no kitty :(");
        }

        reader = terminal.reader();
        is = terminal.input();
        inited = true;
    }

    // only returns new keys pressed on this tick
    // non-blocking
    public static Keybinds get_keypress() {
        byte[] buf = new byte[16];

        while (true) { 
            int bytes_read = 0;
            try {
                bytes_read = is.read(buf);
            } catch (Exception e) {
                System.out.println("Error while reading key: " + e);
                return Keybinds.UNKNOWN;
            }
            if (bytes_read != 0) {
                break;
            }
        }

        KeyEvent ke = parse_keyevent(ByteBuffer.wrap(buf));

        Keybinds held_key = Keybinds.get_associated_keybind(ke.code());
        if (last_seen_key != Keybinds.UNKNOWN && held_key == last_seen_key) {
            last_seen_key = held_key;
            return Keybinds.UNKNOWN;
        } else {
            last_seen_key = held_key;
            return held_key;
        }

    }

    // using kitty protocol
    static KeyEvent parse_keyevent(ByteBuffer input) {
        ByteBuffer b = input.duplicate();

        // ignore null byte padding
        while (b.hasRemaining() && b.get(b.limit() - 1) == 0) {
            b.limit(b.limit() - 1);
        }

        if (b.remaining() >= 2 && b.get(b.position()) == 0x1B
                && b.get(b.position() + 1) == '[') {    // <esc>[
            b.position(b.position() + 2);
        } else if (b.hasRemaining() && (b.get(b.position()) & 0xFF) == 0x9B) {
            b.position(b.position() + 1);
        } else {
            throw new IllegalArgumentException("Expected Kitty CSI key event (input: " + input.toString() + ")");
        }

        int code = read_num(b);
        int modifiers = 1; // default: no modifiers
        int eventType = 1;  // default: press

        if (b.hasRemaining() && b.get(b.position()) == ';') {
            b.get(); // ';'
            modifiers = read_num(b);

            if (b.hasRemaining() && b.get(b.position()) == ':') {
                b.get(); // ':'
                eventType = read_num(b);
            }
        }

        if (!b.hasRemaining() || b.get() != 'u' || b.hasRemaining()) {
            throw new IllegalArgumentException("Malformed Kitty CSI u event");
        }
        if (modifiers < 1 || eventType < 1 || eventType > 3) {
            throw new IllegalArgumentException("Invalid modifier or event type");
        }

        int modifierBits = modifiers - 1;
        boolean ctrl = (modifierBits & 0b100) != 0;
        boolean pressed = eventType != 3; // Press or repeat; false for release

        return new KeyEvent(code, pressed, ctrl);
    }

    private static int read_num(ByteBuffer b) {
        int value = 0;
        int digits = 0;

        while (b.hasRemaining()) {
            int c = b.get(b.position()) & 0xFF;
            if (c < '0' || c > '9') break;

            b.get();
            value = Math.addExact(Math.multiplyExact(value, 10), c - '0');
            digits++;
        }

        if (digits == 0) {
            throw new IllegalArgumentException("Expected a number");
        }
        return value;
    }
}

record KeyEvent(int code, boolean is_pressed, boolean ctrl) {}


