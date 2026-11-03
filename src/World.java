import org.jline.terminal.Size;

public class World {
    /* colours  :)  */
    private static final String DARK_GRAY = "\u001b[38;5;236m";
    private static final String GRAY = "\u001b[38;5;241m";
    private static final String CYAN = "\u001b[38;5;51m";
    

    public final int WORLD_HEIGHT = 501;    // should be odd
    public final int WORLD_WIDTH = WORLD_HEIGHT - 1;     // should be even
    // 1mb of memory isn't too bad
    // maybe we shouldn't generate the whole maze in one go...
    private char[][] world = new char[WORLD_HEIGHT][WORLD_WIDTH];
    public Player player = new Player();

    public World() {
        // worldgen
        MazeGenerator m = new MazeGenerator(WORLD_WIDTH, WORLD_HEIGHT);
        this.world = m.generate_maze();

        player.x = WORLD_WIDTH / 2 + 1;
        player.y = WORLD_HEIGHT / 2 + 1;

        // ensure the player doesn't spawn in a wall
        while (this.world[this.player.y][this.player.x] == '#') {
            this.player.x += 1;
        }
    }

    /* UI concept
    +--------------------------------+
    | Maze Game v0.0.1 [..]  Inv.    |  < 1
    | ##..##..##......###   - 1x ?   |
    | ######..##..#######   - 2x ??  |
    | ##..##..##@.....###            |
    | ##..##########..###    Item    |
    | ##..............###   +------+ |
    | ############..#####   |  .%  | |
    | ###################   |  @^  | |
    |                       +------+ |
    +--------------------------------+
                            <- 30 ->

    */

    // renders world around player
    public void render() {
        // move cursor to top-left of window and reset styling
        System.out.print("\u001b[H\u001b[0m");
        Size termsize = Inputs.terminal.getSize();
        int width = termsize.getColumns();
        int height = termsize.getRows();

        // render buffer
        String[] lines = new String[height];
        String status_bar_template = "%-" + width + "s"; // align to left with spacing
        lines[0] = String.format(status_bar_template, "Maze game v0.0.1 [11-03-2026]");
        
        int sizebar_width = Math.min(40, width / 4);
        
        int maze_width = width - sizebar_width;
        int maze_height = height - 1;
        int start_x = this.player.x - (maze_width / 2);
        int start_y = this.player.y - (maze_height / 2);
        
        // render maze first
        int lines_index = 1;    // first line used by status bar
        for (int i = start_y; i < start_y + maze_height; i++) {
            String current_line = "";
            if (i < 0 || i >= WORLD_HEIGHT) {
                current_line += " ".repeat(width);
                lines[lines_index] = current_line;
                lines_index += 1;
                continue;
            }

            for (int j = start_x; j < start_x + maze_width; j++) {
                if (j < 0) {
                    // repeat space until the end of the line (outside of maze)
                    current_line += " ".repeat(-j);
                    j = 0;

                } 
                if (j >= WORLD_WIDTH) {
                    // repeat space until the end of the line (outside of maze)
                    current_line += " ".repeat(start_x + maze_width - WORLD_WIDTH);
                    break;
                }

                char ch;
                if (this.player.x == j && this.player.y == i) {
                    ch = '@';
                } else {
                    ch = world[i][j];
                }
                current_line += char_colour(ch) + ch;
            }

            // fill the sidebar with spaces temporarily
            current_line += " ".repeat(width - maze_width);
            lines[lines_index] = current_line;

            lines_index += 1;
        }
        System.out.print(String.join("\n", lines));
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

    public static String char_colour(char ch) {
        switch (ch) {
            case '#' -> { return DARK_GRAY; }
            case '.' -> { return GRAY; }
            case '@' -> { return CYAN; }
            default -> { return ""; }
        }
    }
}
