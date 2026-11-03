
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
        w.player.x = w.WORLD_WIDTH / 2 + 1;
        w.player.y = w.WORLD_HEIGHT / 2 + 1;
        w.render();

        while (true) {
            if (w.get_input()) {
                break;
            }

            w.render();
        }
    }
}