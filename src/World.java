import org.jline.terminal.Size;

public class World {
    /* colours  :)  */                        // code here vvv
    private static final String DARK_GRAY   = "\u001b[38;5;236m";
    private static final String MID_GRAY    = "\u001b[38;5;238m";
    private static final String GRAY        = "\u001b[38;5;241m";
    private static final String CYAN        = "\u001b[38;5;51m";
    private static final String CLUE        = "\u001b[38;5;9m";
    private static final String FAINT_CLUE  = "\u001b[38;5;52m";
    private static final String RESET       = "\u001b[0m";

    private static final int VISIBILITY_RADIUS = 20;
    private static final transient boolean COLLISIONS_ENABLED = true; 
    
    public final int WORLD_HEIGHT = 1001;    // should be odd
    public final int WORLD_WIDTH = WORLD_HEIGHT - 1;     // should be even
    // 1mb of memory isn't too bad
    // maybe we shouldn't generate the whole maze in one go...
    private char[][] world = new char[WORLD_HEIGHT][WORLD_WIDTH];
    public Player player = new Player();

    private Point[] yellow_keycards = new Point[10];


    public World() {
        // worldgen
        MazeGenerator m = new MazeGenerator(WORLD_WIDTH, WORLD_HEIGHT);
        this.world = m.generate_maze();
        generate_clues();

        // generate exit and exit code


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

        generate_ring(mx, my, c / 10.0, 2);
        generate_ring(mx, my, c / 25.0, 8);
        generate_ring(mx, my, c / 15.0, 9);
        generate_ring(mx, my, c / 11.0, 10);
        generate_ring(mx, my, c / 8.0, 12);
        generate_ring(mx, my, c / 6.0, 14);
        generate_ring(mx, my, c / 4.0, 17);
        generate_ring(mx, my, c / 3.0, 20);
    }

    private void generate_ring(int mx, int my, double radius, int clues) {
        double ring_width = 10.0;
        for (int i = 0; i < clues; i++) {
            double angle = Math.random() * Math.TAU;
            double radius_jiggle = ring_width * (Math.random() - 0.5);
            int cx = (int)(Math.cos(angle) * (radius + radius_jiggle) + mx);
            int cy = (int)(Math.sin(angle) * (radius + radius_jiggle) + my);
            if (this.world[cy][cx] != '#') { // only replace walls with clues
                i -= 1;
                continue;
            }
            this.world[cy][cx] = '%';

            int location_hash = hash_location(cx, cy);

            if (picker(location_hash, 4) == 3) {
                int item_hash = picker(location_hash, 100);
                
            }
        }
    }

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

                if (Math.hypot(j - this.player.x, (i - this.player.y) * 2) > VISIBILITY_RADIUS) {
                    switch (ch) {
                        case '#' -> { ch = ':'; }
                        case ',' -> { ch = ' '; }
                        case '%' -> { ch = '!'; }
                        default -> {}
                    }
                }

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
            case ':' -> { return MID_GRAY; }
            case '#' -> { return GRAY; }
            case '@' -> { return CYAN; }
            case '%' -> { return CLUE; }
            case '!' -> { return FAINT_CLUE; }
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
                // since we don't store what each % holds, determine what it 
                // activate the clue at this specific location
                int location_hash = hash_location(nx, ny);

                switch (picker(location_hash, 3)) {
                    case 0 -> { // 
                        
                    }
                }

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
        return original.substring(0, start_pos) + paste + original.substring(end);
    }

    private String center_string(String original, int center_amount) {
        if (center_amount <= original.length()) return original;
        int buffer_space = center_amount - original.length();
        int left = buffer_space / 2;
        int right = buffer_space - left;
        return " ".repeat(left) + original + " ".repeat(right);
    }
    
    private int picker(int hash, int m) {
        return ((hash * 71) >> 5) % m;
    }

    private Item item_hash(int x, int y) {
        int loc_hash = hash_location(x, y);
        int item_hash = picker(loc_hash, 100);
        return choose_item(picker(loc_hash, 4), item_hash);
    }

    private Item choose_item(int itype, int item_hash) {
        switch (itype) {
            case 0 -> { // note
                if (0 <= item_hash && item_hash < 20) {
                    Item note = new Item("Note that points somewhere", 1, System.currentTimeMillis());
                    /* todo: set image of note to be some compass or something */
                    return note;
                } else if (20 < item_hash && item_hash <= 30) {
                    Item note = new Item("Partial exit code", 1, System.currentTimeMillis());
                    note.image = "The code is: \n[   4  2  8]";
                } else {
                    /* there is a <item> nearby */
                }
            }
            case 1 -> { // encrypted message with varying ciphers
                Item note = choose_item(0, item_hash);
                if (0 <= item_hash && item_hash < 15) {
                    // base64 encode (encode each line individually)

                }

            }
        };
    }

    // encode each line individually
    private String b64_encode(String s) {
        String[] encoded = new String[(int)(s.chars().filter(ch -> ch == '\n').count())];
        int line_idx = 0;
        for (String line : s.split("\n")) {
            encoded[line_idx] = b64_encode_segment(line);
            line_idx += 1;
        }

        return String.join("\n", encoded);
    }

    public static String BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private String b64_encode_segment(String s) {
        String encoded = "";
        while (s.length() >= 3) {
            int[] segment = s.substring(0, 3).chars().toArray();
            s = s.substring(3);
            int segm = (segment[0] << 16) | (segment[1] << 8) | segment[2];

            for (int j = 0; j < 4; j++) {
                int b64char = (segm >> (18 - j * 6)) & 0b111111;
                encoded += BASE64_CHARS.charAt(b64char);
            }
        }

        if (s.length() == 2) {
            int[] segment = s.chars().toArray();
            int segm = (segment[0] << 10) | (segment[1] << 2);
            for (int j = 0; j < 3; j++) {
                int b64char = (segm >> (18 - j * 6)) & 0b111111;
                encoded += BASE64_CHARS.charAt(b64char);
            }
            encoded += "=";
        } else if (s.length() == 1) {
            int segm = (int)s.charAt(0) << 4;
            for (int j = 0; j < 2; j++) {
                int b64char = (segm >> (18 - j * 6)) & 0b111111;
                encoded += BASE64_CHARS.charAt(b64char);
            }
            encoded += "==";
        }

        return encoded;
        
    }
}

/* types of clues */
// 1. direct note (note item)
//  - direction: arrow pointing to where another clue is
//  - code is 123... for exit (requires keycard)    [10%]
//  - there is a yellow keycard nearby
// 2. encrypted message: same variants as first type but encrypted 
//  - base64
//  - caesar cipher
//  - to hex
//  - 1 + 3
//  - 2 + 3, 
//  - 1 + 2 + 3 
// 3. utility item 
//  - compass               [0-15]  15% points to some clue that's within 200 units of where it was found
//  - dead-end marker       [15-40] 25% visual marker for the player
//  - grenade               [40-60] 20% blows a hole in the wall
//  - yellow keycard        [60-70] 10% unlocks some more clues
//  - blue keycard          [70-72] 2%  unlocks exit      
//  - flashlight            [72-92] 20% illuminates more stuff
//  - radar (for clues)     [92-97] 5%  scans nearby area for possible clues (not that reliable)
//  - nothing               [97-100]    
// 4. map (around a fixed location but tells you where it is)

record Point(int x, int y) {}