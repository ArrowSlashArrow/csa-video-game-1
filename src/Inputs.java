import java.io.IOException;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Inputs {
    public static Terminal terminal;

    public static void init() throws IOException {
        terminal = TerminalBuilder.builder().build();
    }

}
