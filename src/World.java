import java.util.ArrayList;
import org.jline.terminal.Size;

public class World {
    /* colours  :)  */                        // code here vvv
    private static final String DARK_GRAY   = "\u001b[38;5;236m";
    private static final String MID_GRAY    = "\u001b[38;5;238m";
    private static final String GRAY        = "\u001b[38;5;241m";
    private static final String CYAN        = "\u001b[38;5;51m";
    private static final String CLUE        = "\u001b[38;5;9m";
    private static final String FAINT_CLUE  = "\u001b[38;5;52m";
    private static final String DOOR        = "\u001b[38;5;39m";
    private static final String RESET       = "\u001b[0m";

    private static final int VISIBILITY_RADIUS = 20;
    private static final transient boolean COLLISIONS_ENABLED = true; 
    
    public final int WORLD_HEIGHT = 1001;    // should be odd
    public final int WORLD_WIDTH = WORLD_HEIGHT - 1;     // should be even
    // 1mb of memory isn't too bad
    // maybe we shouldn't generate the whole maze in one go...
    private char[][] world = new char[WORLD_HEIGHT][WORLD_WIDTH];
    public Player player = new Player();

    private ArrayList<Point> yellow_keycards = new ArrayList<>();
    private ArrayList<Point> clues = new ArrayList<>();
    private ArrayList<Point> found_clues = new ArrayList<>();

    // clue GUI
    private boolean opened_clue = false;
    private Point opened_clue_location;
    private Item clue_item;

    private long EXIT_CODE;

    public World() {
        // worldgen
        MazeGenerator m = new MazeGenerator(WORLD_WIDTH, WORLD_HEIGHT);
        this.world = m.generate_maze();
        generate_clues();

        EXIT_CODE = (int)(Math.random() * 10_000_000_000L);

        // generate exit door
        while (true) { 
            int random_position = (int)(Math.random() * WORLD_WIDTH);
            if (world[1][random_position] == '.') {
                world[0][random_position] = '^';
                break;
            }
        }

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

    private void generate_ring(int mx, int my, double radius, int c) {
        double ring_width = 10.0;
        for (int i = 0; i < c; i++) {
            double angle = Math.random() * Math.TAU;
            double radius_jiggle = ring_width * (Math.random() - 0.5);
            int cx = (int)(Math.cos(angle) * (radius + radius_jiggle) + mx);
            int cy = (int)(Math.sin(angle) * (radius + radius_jiggle) + my);
            if (this.world[cy][cx] != '#') { // only replace walls with clues
                i -= 1;
                continue;
            }
            this.world[cy][cx] = '%';

            Item item = get_item_at(cx, cy);

            if (item.name.equals("Yellow keycard")) {
                yellow_keycards.add(new Point(cx, cy));
            }
            clues.add(new Point(cx, cy));
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
        lines[0] = Utils.write_string(lines[0], "Inventory", sidebar_start);

        int item_line_index = 1;
        for (Item i : this.player.items) {
            if (!(i != null) || i.amount > 0) continue;
            item_line_index += 1;
            String item_str = String.format(" - %dx %s", i.amount, i.name);
            lines[item_line_index] = Utils.write_string(lines[item_line_index], item_str, sidebar_start);
        }

        /* gui elements (in the maze area) */

        // objective (at the top)
        int objective_dialog_width = 29;
        String objective_dialog_covers = "-".repeat(objective_dialog_width);
        String objective_dialog_message = "|" + Utils.center_string(get_objective(this.player.level), (objective_dialog_width - 2)) + "|";

        int objective_dialog_topleft_x = (maze_width - objective_dialog_width) / 2;

        String[] objective_dialog = {
            objective_dialog_covers, objective_dialog_message, objective_dialog_covers
        };

        for (int i = 0; i < objective_dialog.length; i++) {
            int idx = i + 2;
            lines[idx] = Utils.write_string(
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
            case 1 -> { return "Find a yellow keycard"; }   // if the player does not have a yellow keycard
            case 2 -> { return "Find a blue keycard."; }    // if the player does not have a blue keycard
            case 3 -> { return "Locate an exit clue."; }    // if the player has a blue keycard
            case 4 -> { return "Locate the exit."; }        // if the player has the code
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
            case Keybinds.PICK_UP -> {
                if (!this.opened_clue) { return false; }
                if (this.clue_item.id == 0) { return false; } // can't pick up nothing
                this.player.items.add(clue_item);
                this.clue_item = new Item("- nothing -", 0, 0);

                found_clues.add(this.opened_clue_location);
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
            case '^' -> { return DOOR; }
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
                Point here = new Point(nx, ny);
                Item item;
                
                if (found_clues.contains(here)) {
                    item = new Item("- nothing -", 0, 0);
                } else {
                    item = get_item_at(nx, ny);
                }

                opened_clue = true;
                clue_item = item;
                return false; 
            }
            default ->  { return true; }
        }
    }

    private Item choose_item(int itype, int item_hash) {
        switch (itype) {
            case 0 -> { // note
                if (0 <= item_hash && item_hash < 20) {
                    Item note = new Item("Note that points somewhere", 1, System.currentTimeMillis());
                    return note;
                } else if (20 < item_hash && item_hash <= 30) {
                    Item note = new Item("Partial exit code", 1, System.currentTimeMillis());
                    note.image = "The code is: \n[   4  2  8]"; // todo: randomly pick digits
                } else {
                    /* there is a <item> nearby, <distance> units away */
                    // choose an item that hasn't been opened
                    // if everything is opened, return a Nothing

                }
            }
            case 1 -> { // encrypted message with varying ciphers
                Item note = choose_item(0, item_hash);
                if (0 <= item_hash && item_hash < 15) { // b64 encode
                    note.text = Utils.b64_encode(note.text);
                } else if (15 <= item_hash && item_hash < 30) { // hex
                    note.text = Utils.hex_encode(note.text);
                } else if (30 <= item_hash && item_hash < 40) { // bin
                    note.text = Utils.bin_encode(note.text);
                } else if (40 <= item_hash && item_hash < 65) { // hex + b64
                    note.text = Utils.b64_encode(Utils.hex_encode(note.text));
                } else if (65 <= item_hash && item_hash < 80) { // bin + b64
                    note.text = Utils.b64_encode(Utils.bin_encode(note.text));
                } else if (80 <= item_hash) { // hex + bin + b64
                    note.text = Utils.b64_encode(Utils.bin_encode(Utils.hex_encode(note.text)));
                }
            }
        }
    }

    private Item get_item_at(int x, int y) {
        int loc_hash = Utils.hash_location(x, y);
        int item_hash = Utils.picker(loc_hash, 100);
        return choose_item(Utils.picker(loc_hash, 4), item_hash);
    }
}

/* types of clues */
// 1. direct note (note item)
//  - direction: arrow pointing to where another clue is
    // Item - note, ID: <unique>
//  - code is 123... for exit (requires keycard)    [10%]
    // Item - note. ID: <unique> 
//  - there is a yellow keycard nearby
    // Item - note, ID: <unique>
// 2. encrypted message: same variants as first type but encrypted 
//  - base64
//  - to binary
//  - to hex
//  - 1 + 3
//  - 2 + 3, 
//  - 1 + 2 + 3 
// 3. utility item 
//  - compass               [0-15]  15% points to some clue that's within 200 units of where it was found
    // Item - compass, ID: 1, data: fixed location, render: compass art to point to a location
//  - dead-end marker       [15-40] 25% visual marker for the player
    // Item - marker, ID: 2, data: none, action: use - place on ground at player
//  - grenade               [40-60] 20% blows a hole in the wall
    // Item - grenade, ID: 3, data: none, action: use - detonate at current location
//  - yellow keycard        [60-70] 10% unlocks some more clues
    // Item - yel_key, ID: <unique>, data: none, action: use - to unlock key
//  - blue keycard          [70-72] 2%  unlocks exit        
    // same as above
//  - flashlight            [72-92] 20% illuminates more stuff
    // Item - fllight, ID: 4, data: battery (0 - 1), action: use - to illuminate thing
//  - radar (for clues)     [92-97] 5%  scans nearby area for possible clues (not that reliable)
    // Item - radar, ID: 5, data: battery (0 - 1), action: use - ping for locations (shows up on item view)
//  - nothing               [97-100]    
// 4. map (around a fixed location but tells you where it is)
    // Item - map, ID: <unique>, data: center point, render: area

record Point(int x, int y) {}