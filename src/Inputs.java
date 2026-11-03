import java.io.IOException;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

public class Inputs {
    public static Terminal terminal;
    private static NonBlockingReader reader;
    private static boolean inited = false;

    // prevent one key from being registered every single tick it's held

    public static void init() throws IOException {
        if (inited) return; // ensures we don't accidentally create two terminals
        terminal = TerminalBuilder.builder().build();
        terminal.enterRawMode();
        reader = terminal.reader();
        inited = true;
    }

    // only returns new keys pressed on this tick
    // non-blocking
    public static Keybinds get_keypress() {
        int ch;
        try {
            ch = reader.read();
        } catch (Exception e) {
            System.out.println("Error while reading input: " + e);
            return Keybinds.UNKNOWN;
        }
        return Keybinds.get_associated_keybind(ch); 
    }
}
