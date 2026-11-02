import org.jline.terminal.Size;

public class World {
    private final int WORLD_HEIGHT = 1000;
    private final int WORLD_WIDTH = 1000;
    // 1mb of memory isn't too bad
    // maybe we shouldn't generate the whole maze in one go...
    private char[][] world = new char[WORLD_HEIGHT][WORLD_WIDTH];
    public Player player = new Player();

    public World() {
        for (int i = 0; i < WORLD_HEIGHT; i++) {
            for (int j = 0; j < WORLD_WIDTH; j++) {
                int m = (int)(Math.random() * 1000.0);
                if (m < 80) {
                    world[i][j] = '#';
                } else {
                    world[i][j] = '.';
                }
            }
        }
    }

    // renders world around player
    public void render() {
        // move cursor to top-left of window
        System.out.print("\u001b[H");
        Size termsize = Inputs.terminal.getSize();
        int width = termsize.getColumns();
        int height = termsize.getRows();
        
        int start_x = this.player.x - (width / 2);
        int start_y = this.player.y - (height / 2);

        String result = "";

        for (int i = start_y; i < start_y + height; i++) {
            if (i < 0 || i > WORLD_HEIGHT) {
                result += " ".repeat(width);
                continue;
            }

            for (int j = start_x; j < start_x + width; j++) {
                if (j < 0 || j >= WORLD_WIDTH) {
                    result += " ".repeat(start_x + width - WORLD_WIDTH);
                    break;
                }

                if (this.player.x == j && this.player.y == i) {
                    result += "@";
                } else {
                    result += world[i][j];
                }
            }
            result += "\n";
        }

        System.out.print(result.substring(0, result.length() - 1)); // remove last \n
        System.out.flush();
    }

    public boolean get_input() {
        switch (Inputs.get_keypress()) {
            case Keybinds.MOVE_UP -> {
                this.player.y = Math.max(0, this.player.y - 1);
            }
            case Keybinds.MOVE_LEFT -> {
                this.player.x = Math.max(0, this.player.x - 1);
            }
            case Keybinds.MOVE_DOWN -> {
                this.player.y = Math.min(999, this.player.y + 1);
            }
            case Keybinds.MOVE_RIGHT -> {
                this.player.x = Math.min(999, this.player.x + 1);
            }
            case Keybinds.EXIT -> { return true; }
            default -> { return false; }
        }

        return false;
    }
}
