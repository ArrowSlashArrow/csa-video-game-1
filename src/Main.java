
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // hide cursor (for immersion)
        System.out.println("\u001b[?25l");
        Inputs.init();
        GameRunner g = new GameRunner();
        g.run();
        
        // bring cursor back and reset colour
        System.out.println("\u001b[?25h\u001b[0m");
    }
}

class GameRunner {
    public void run() {
        World w = new World();
        System.out.println("Loading maze...");
        w.render();
        while (true) {
            if (w.get_input()) {
                break;
            }

            w.render();
        }
    }
}