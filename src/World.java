import org.jline.terminal.Size;

public class World {
    public final int WORLD_HEIGHT = 101;
    public final int WORLD_WIDTH = 101;
    // 1mb of memory isn't too bad
    // maybe we shouldn't generate the whole maze in one go...
    private char[][] world = new char[WORLD_HEIGHT][WORLD_WIDTH];
    public Player player = new Player();

    public World() {
        // worldgen
        MazeGenerator m = new MazeGenerator(WORLD_WIDTH, WORLD_HEIGHT);
        this.world = m.generate_maze();
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
                if (j < 0) {
                    // repeat space until the end of the line (outside of maze)
                    result += " ".repeat(-j);
                    j = 0;

                } 
                if (j >= WORLD_WIDTH) {
                    // repeat space until the end of the line (outside of maze)
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
                int new_pos = Math.max(0, this.player.y - 1);
                if (this.world[new_pos][this.player.x] == '.') {
                    this.player.y = new_pos;
                }
            }
            case Keybinds.MOVE_LEFT -> {
                int new_pos = Math.max(0, this.player.x - 1);
                if (this.world[this.player.y][new_pos] == '.') {
                    this.player.x = new_pos;
                }
            }
            case Keybinds.MOVE_DOWN -> {
                int new_pos = Math.min(WORLD_HEIGHT - 1, this.player.y + 1);
                if (this.world[new_pos][this.player.x] == '.') {
                    this.player.y = new_pos;
                }
            }
            case Keybinds.MOVE_RIGHT -> {
                int new_pos = Math.min(WORLD_WIDTH - 1, this.player.x + 1);
                if (this.world[this.player.y][new_pos] == '.') {
                    this.player.x = new_pos;
                }
            }
            case Keybinds.EXIT -> { return true; }
            default -> { return false; }
        }

        return false;
    }
}
