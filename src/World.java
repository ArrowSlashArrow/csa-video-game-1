import org.jline.terminal.Size;

public class World {
    /* colours  :)  */                        // code here vvv
    private static final String DARK_GRAY   = "\u001b[38;5;236m";
    private static final String GRAY        = "\u001b[38;5;241m";
    private static final String CYAN        = "\u001b[38;5;51m";
    private static final String CLUE        = "\u001b[38;5;9m";
    private static final String RESET       = "\u001b[0m";

    private static final int VISIBILITY_RADIUS = 20;
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

        generate_ring(mx, my, c / 25.0, 6);
        generate_ring(mx, my, c / 15.0, 7);
        generate_ring(mx, my, c / 11.0, 9);
        generate_ring(mx, my, c / 8.0, 10);
        generate_ring(mx, my, c / 6.0, 11);
        generate_ring(mx, my, c / 4.0, 12);
        generate_ring(mx, my, c / 3.0, 13);

    }

    private void generate_ring(int mx, int my, double radius, int clues) {
        for (int i = 0; i < clues; i++) {
            double angle = Math.random() * Math.TAU;
            double radius_jiggle = 10.0 * (Math.random() - 0.5);
            int cx = (int)(Math.cos(angle) * (radius + radius_jiggle) + mx);
            int cy = (int)(Math.sin(angle) * (radius + radius_jiggle) + my);
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

                boolean not_visible = Math.hypot(j - this.player.x, (i - this.player.y) * 2) > VISIBILITY_RADIUS;
                if (not_visible && ch == '#') ch = ',';
                else if (not_visible && ch == ',') ch = ' ';

                current_line += ch;
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
            if (!(i != null) || i.amount > 0) continue;
            item_line_index += 1;
            String item_str = String.format(" - %dx %s", i.amount, i.name);
            lines[item_line_index] = write_string(lines[item_line_index], item_str, sidebar_start);
        }

        /* gui elements (in the maze area) */

        // objective (at the top)
        int objective_dialog_width = 29;
        String objective_dialog_covers = "-".repeat(objective_dialog_width);
        String objective_dialog_message = "|" + center_string(get_objective(this.player.level), (objective_dialog_width - 2)) + "|";

        int objective_dialog_topleft_x = (maze_width - objective_dialog_width) / 2;

        String[] objective_dialog = {
            objective_dialog_covers, objective_dialog_message, objective_dialog_covers
        };

        for (int i = 0; i < objective_dialog.length; i++) {
            int idx = i + 2;
            lines[idx] = write_string(
                lines[idx], 
                objective_dialog[i], 
                objective_dialog_topleft_x
            );
        }

        /* apply colours */
        for (int i = 0; i < lines.length; i++) {
            String line = "";
            for (char c : lines[i].toCharArray()) {
                line += char_colour(c) + c;
            }
            lines[i] = line;
        }


        System.out.print(String.join("\n", lines));
        System.out.flush();
    }

    private String get_objective(int player_level) {
        switch (player_level) {
            case 0 -> { return "Locate a clue (%)."; }
            default -> { return ""; }
        }
    }

    public boolean get_input() {
        switch (Inputs.get_keypress()) {
            case Keybinds.MOVE_UP -> {
                int new_pos = Math.max(0, this.player.y - 1);
                if (check_coll(this.player.x, new_pos) || !COLLISIONS_ENABLED) {
                    this.player.y = new_pos;
                }
            }
            case Keybinds.MOVE_LEFT -> {
                int new_pos = Math.max(0, this.player.x - 1);
                if (check_coll(new_pos, this.player.y) || !COLLISIONS_ENABLED) {
                    this.player.x = new_pos;
                }
            }
            case Keybinds.MOVE_DOWN -> {
                int new_pos = Math.min(WORLD_HEIGHT - 1, this.player.y + 1);
                if (check_coll(this.player.x, new_pos) || !COLLISIONS_ENABLED) {
                    this.player.y = new_pos;
                }
            }
            case Keybinds.MOVE_RIGHT -> {
                int new_pos = Math.min(WORLD_WIDTH - 1, this.player.x + 1);
                if (check_coll(new_pos, this.player.y) || !COLLISIONS_ENABLED) {
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
            case ',' -> { return DARK_GRAY; }
            case '#' -> { return GRAY; }
            case '@' -> { return CYAN; }
            case '%' -> { return CLUE; }
            default ->  { return RESET; }
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

    private int hash_location(int x, int y) {
        return x>=y?x*x+x+y:x+y*y;
    }


    private String write_string(String original, String paste, int start_pos) {
        int end = Math.min(start_pos + paste.length(), original.length());
    return original.substring(0, start_pos)
            + paste
            + original.substring(end);
    }

    private String center_string(String original, int center_amount) {
        if (center_amount <= original.length()) return original;
        int buffer_space = center_amount - original.length();
        int left = buffer_space / 2;
        int right = buffer_space - left;
        return " ".repeat(left) + original + " ".repeat(right);
    }
}

/* types of clues */
// 1. direct note
// 2. encrypted note
// 3. 