
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Inputs.init();
        
        World w = new World();
        w.player.x = 500;
        w.player.y = 500;
        w.render();

        while (true) {
            if (w.get_input()) {
                break;
            }

            w.render();
        }
    }
}