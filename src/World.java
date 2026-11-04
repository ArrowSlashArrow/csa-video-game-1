import org.jline.terminal.Size;

public class World {
    /* colours  :)  */                        // code here vvv
    private static final String DARK_GRAY   = "\u001b[38;5;236m";
    private static final String GRAY        = "\u001b[38;5;241m";
    private static final String CYAN        = "\u001b[38;5;51m";
    private static final String CLUE        = "\u001b[38;5;9m";

    private static final transient boolean COLLISIONS_ENABLED = true; 
    

    public final int WORLD_HEIGHT = 1001;    // should be odd
    public final int WORLD_WIDTH = WORLD_HEIGHT - 1;     // should be even
    // 1mb of memory isn't too bad
    // maybe we shouldn't generate the whole maze in one go...
    private char[][] world = new char[WORLD_HEIGHT][WORLD_WIDTH];
    public Player player = new Player();

    public World() {
        // worldgen
        MazeGenerator m = new MazeGenerator(WORLD_WIDTH, WORLD_HEIGHT);
        this.world = m.generate_maze();

        generate_clues();

        player.x = WORLD_WIDTH / 2 + 1;
        player.y = WORLD_HEIGHT / 2 + 1;

        // ensure the player doesn't spawn in a wall
        while (this.world[this.player.y][this.player.x] == '#') {
            this.player.x += 1;
        }
    }


    private void generate_clues() {
        double c = Math.hypot(WORLD_WIDTH, WORLD_HEIGHT);   // constant for scaling circles
        int mx = WORLD_WIDTH / 2;   // midpoint
        int my = WORLD_HEIGHT / 2;

        generate_ring(mx, my, c / 25.0, 3);
        generate_ring(mx, my, c / 15.0, 4);
        generate_ring(mx, my, c / 11.0, 4);
        generate_ring(mx, my, c / 8.0, 4);
        generate_ring(mx, my, c / 6.0, 4);
        generate_ring(mx, my, c / 4.0, 4);
        generate_ring(mx, my, c / 3.0, 4);

    }

    private void generate_ring(int mx, int my, double radius, int clues) {
        for (int i = 0; i < clues; i++) {
            double angle = Math.random() * Math.TAU;
            int cx = (int)(Math.cos(angle) * radius + mx);
            int cy = (int)(Math.sin(angle) * radius + my);
            if (this.world[cy][cx] != '#') {
                i -= 1;
                continue;
            }
            if (check_clue(cx, cy)) {
                this.world[cy][cx] = '%';
            }
        }
    }

    // check that the clue has at least once air pocket next to it (so you can access it)
    private boolean check_clue(int x, int y) {
        return true;    // todo
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
        
        int sidebar_width = Math.min(40, width / 4);
        
        int maze_width = width - sidebar_width - 1; // -1 for 1-char buffer between maze and sidebar
        int maze_height = height - 1;
        int start_x = this.player.x - (maze_width / 2);
        int start_y = this.player.y - (maze_height / 2);
        
        /* render maze */

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

        /* render sidebar */

        int sidebar_start = width - sidebar_width;
        lines[0] = write_string(lines[0], "Inventory", sidebar_start);

        int item_line_index = 1;
        for (Item i : this.player.items) {
            if (!(i != null)) continue;
            item_line_index += 1;
            String item_str = String.format(" - %dx %s", i.amount, i.name);
            lines[item_line_index] = write_string(lines[item_line_index], item_str, sidebar_start);
        }


        System.out.print(String.join("\n", lines));
        System.out.flush();
    }

    public boolean get_input() {
        switch (Inputs.get_keypress()) {
            case Keybinds.MOVE_UP -> {
                int new_pos = Math.max(0, this.player.y - 1);
                if (check_coll(this.player.x, new_pos)) {
                    this.player.y = new_pos;
                }
            }
            case Keybinds.MOVE_LEFT -> {
                int new_pos = Math.max(0, this.player.x - 1);
                if (check_coll(new_pos, this.player.y)) {
                    this.player.x = new_pos;
                }
            }
            case Keybinds.MOVE_DOWN -> {
                int new_pos = Math.min(WORLD_HEIGHT - 1, this.player.y + 1);
                if (check_coll(this.player.x, new_pos)) {
                    this.player.y = new_pos;
                }
            }
            case Keybinds.MOVE_RIGHT -> {
                int new_pos = Math.min(WORLD_WIDTH - 1, this.player.x + 1);
                if (check_coll(new_pos, this.player.y)) {
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
            case '.' -> { return DARK_GRAY; }
            case '#' -> { return GRAY; }
            case '@' -> { return CYAN; }
            case '%' -> { return CLUE; }
            default -> { return ""; }
        }
    }

    // return true if good to go there
    // return false if there's an obstruction
    private boolean check_coll(int nx, int ny) {
        switch (this.world[ny][nx]) {
            case '@' -> { return true; }
            case '.' -> { return true; }
            case '#' -> { return false; }
            case '%' -> {
                // activate the clue at this specific location

                return false; 
            }
            default ->  { return true; }
        }
    }


    private String write_string(String original, String paste, int start_pos) {
        String first = original.substring(0, start_pos) + paste;
        int both_length = first.length();
        if (both_length < original.length()) first += original.substring(both_length);
        return first;
    }
}
