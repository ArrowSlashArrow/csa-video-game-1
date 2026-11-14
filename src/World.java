import java.util.ArrayList;
import org.jline.terminal.Size;

import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

public class World {
    /* colours :) */ // code here vvv
    private static final String DARK_GRAY = "\u001b[38;5;236m";
    private static final String MID_GRAY = "\u001b[38;5;238m";
    private static final String GRAY = "\u001b[38;5;241m";
    private static final String CYAN = "\u001b[38;5;51m";
    private static final String CLUE = "\u001b[38;5;9m";
    private static final String FAINT_CLUE = "\u001b[38;5;52m";
    private static final String DOOR = "\u001b[38;5;39m";
    private static final String DARK_DOOR = "\u001b[38;5;17m";
    private static final String YELLOW = "\u001b[38;5;11m";
    private static final String RESET = "\u001b[0m";

    private static int VISIBILITY_RADIUS = 20;
    private static final transient boolean COLLISIONS_ENABLED = true;

    public final int WORLD_HEIGHT = 1001; // should be odd
    public final int WORLD_WIDTH = WORLD_HEIGHT - 1; // should be even
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

    // ending door parameters
    private long EXIT_CODE;
    private long entered_exit_code;
    private Point exit_door_location;
    private boolean used_blue_keycard = false;
    private boolean entered_code = false;
    private boolean opened_exit_door = false;
    public boolean won = false;
    public boolean allowed_to_leave = true;

    // inventory
    private int selected_item_idx = -1; // -1: not selecting anything

    public World() {
        // worldgen
        MazeGenerator m = new MazeGenerator(WORLD_WIDTH, WORLD_HEIGHT);
        this.world = m.generate_maze();
        System.out.println("maze is generated");

        generate_clues();
        System.out.println("clues r generated");

        EXIT_CODE = (long) (Math.random() * 10_000_000_000L);

        // generate exit door
        while (true) {
            int random_position = (int) (Math.random() * WORLD_WIDTH);
            if (world[1][random_position] == ',') {
                world[0][random_position] = '^';
                exit_door_location = new Point(random_position, 0);
                break;
            }
        }
        System.out.println("door is generated");

        player.x = WORLD_WIDTH / 2 + 1;
        player.y = WORLD_HEIGHT / 2 + 1;

        // ensure the player doesn't spawn in a wall
        while (this.world[this.player.y][this.player.x] == '#') {
            this.player.x += 1;
        }
    }

    private void generate_clues() {
        double c = Math.hypot(WORLD_WIDTH, WORLD_HEIGHT); // constant for scaling circles
        int mx = WORLD_WIDTH / 2; // midpoint
        int my = WORLD_HEIGHT / 2;

        int debug_spawn_multiplier = 4;

        generate_ring(mx, my, c / 10.0, 2 * debug_spawn_multiplier);
        generate_ring(mx, my, c / 25.0, 8 * debug_spawn_multiplier);
        generate_ring(mx, my, c / 15.0, 9 * debug_spawn_multiplier);
        generate_ring(mx, my, c / 11.0, 10 * debug_spawn_multiplier);
        generate_ring(mx, my, c / 8.0, 12 * debug_spawn_multiplier);
        generate_ring(mx, my, c / 6.0, 14 * debug_spawn_multiplier);
        generate_ring(mx, my, c / 4.0, 17 * debug_spawn_multiplier);
        generate_ring(mx, my, c / 3.0, 20 * debug_spawn_multiplier);
    }

    private void generate_ring(int mx, int my, double radius, int c) {
        double ring_width = 20.0;
        for (int i = 0; i < c; i++) {
            double angle = Math.random() * Math.TAU;
            double radius_jiggle = ring_width * (Math.random() - 0.5);
            int cx = (int) (Math.cos(angle) * (radius + radius_jiggle) + mx);
            int cy = (int) (Math.sin(angle) * (radius + radius_jiggle) + my);
            if (this.world[cy][cx] != '#') { // only replace walls with clues
                i -= 1;
                continue;
            }
            this.world[cy][cx] = '%';

            Item item = get_item_at(cx, cy);

            if (item.id == 5) {
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
        lines[0] = String.format(status_bar_template, "Maze game v0.1.1 [11-10-2026]");

        int sidebar_width = Math.min(40, width / 4);

        int maze_width = width - sidebar_width - 1; // -1 for 1-char buffer between maze and sidebar
        int maze_height = height - 1;
        int start_x = this.player.x - (maze_width / 2);
        int start_y = this.player.y - (maze_height / 2);

        /* render maze */

        int lines_index = 1; // first line used by status bar
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
                        case '#' -> {
                            ch = ':';
                        }
                        case ',' -> {
                            ch = ' ';
                        }
                        case '%' -> {
                            ch = '!';
                        }
                        case '^' -> {
                            ch = '*';
                        }
                        default -> {
                        }
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
            item_line_index += 1;
            String template = " - %s";
            if (item_line_index - 2 == selected_item_idx) {
                template = " > %s";
                // render item at the bottom right of the screen
                int item_start_y = height - 16;
                int item_start_x = width - 31;
                lines[item_start_y - 1] = Utils.write_string(lines[item_start_y - 1], "Press F to use", item_start_x);

                String[] item_render = render(i, this.player.x, this.player.y);
                for (int n = item_start_y; n < height - 1; n++) {
                    lines[n] = Utils.write_string(
                            lines[n],
                            item_render[n - item_start_y],
                            item_start_x);
                }
            }
            String item_str = String.format(template, i.get_name());
            lines[item_line_index] = Utils.write_string(lines[item_line_index], item_str, sidebar_start);
        }

        /* gui elements (in the maze area) */

        // opened clue

        if (this.opened_clue) {
            lines[0] = Utils.write_string(lines[0], "Press C to copy text", sidebar_start - 22);
            int item_width = 30;
            int item_height = 15;
            int item_start_char = (maze_width - item_width - 2) / 2;
            int item_start_idx = (maze_height - item_height - 2) / 2;

            String[] item_sprite = render(clue_item, this.player.x, this.player.y);
            String top_line = "=" + Utils.center_string(clue_item.get_name(), item_width) + "=";
            lines[item_start_idx - 1] = Utils.write_string(lines[item_start_idx - 1], top_line, item_start_char);
            int line_idx = 0;
            for (String line : item_sprite) {
                lines[item_start_idx + line_idx] = Utils.write_string(lines[item_start_idx + line_idx],
                        "|" + line + "|", item_start_char);
                line_idx += 1;
            }

            String bottom_line = "=" + Utils.center_string("Press E to pick up", item_width) + "=";
            lines[item_start_idx + item_sprite.length] = Utils.write_string(lines[item_start_idx + item_sprite.length],
                    bottom_line, item_start_char);
        }

        // objective (at the top)
        int objective_dialog_width = 29;
        String objective_dialog_covers = "-".repeat(objective_dialog_width);
        String objective_dialog_message = "|"
                + Utils.center_string(get_objective(this.player.level), (objective_dialog_width - 2)) + "|";

        int objective_dialog_topleft_x = (maze_width - objective_dialog_width) / 2;

        String[] objective_dialog = {
                objective_dialog_covers, objective_dialog_message, objective_dialog_covers
        };

        for (int i = 0; i < objective_dialog.length; i++) {
            int idx = i + 2;
            lines[idx] = Utils.write_string(
                    lines[idx],
                    objective_dialog[i],
                    objective_dialog_topleft_x);
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
            case 0 -> {
                return "Locate a clue (%).";
            }
            case 1 -> {
                return "Find a yellow keycard";
            } // if the player does not have a yellow keycard
            case 2 -> {
                return "Find a blue keycard.";
            } // if the player does not have a blue keycard
            case 3 -> {
                return "Locate the exit.";
            } // if the player has a blue keycard
            default -> {
                return "";
            }
        }
    }

    public boolean get_input() {
        switch (Inputs.get_keypress()) {
            case Keybinds.MOVE_UP -> {
                int new_pos = Math.max(0, this.player.y - 1);
                // returns `true` if I can move over there
                if ((check_coll(this.player.x, new_pos) && !this.opened_clue) || !COLLISIONS_ENABLED) {
                    this.player.y = new_pos;
                }
            }
            /* ... more cases down here */
            case Keybinds.MOVE_LEFT -> {
                int new_pos = Math.max(0, this.player.x - 1);
                if ((check_coll(new_pos, this.player.y) && !this.opened_clue) || !COLLISIONS_ENABLED) {
                    this.player.x = new_pos;
                }
            }
            case Keybinds.MOVE_DOWN -> {
                int new_pos = Math.min(WORLD_HEIGHT - 1, this.player.y + 1);
                if ((check_coll(this.player.x, new_pos) && !this.opened_clue) || !COLLISIONS_ENABLED) {
                    this.player.y = new_pos;
                }
            }
            case Keybinds.MOVE_RIGHT -> {
                int new_pos = Math.min(WORLD_WIDTH - 1, this.player.x + 1);
                if ((check_coll(new_pos, this.player.y) && !this.opened_clue) || !COLLISIONS_ENABLED) {
                    this.player.x = new_pos;
                }
            }
            case Keybinds.PICK_UP -> {
                if (!this.opened_clue || this.clue_item.id == 0 || this.opened_exit_door) {
                    return false;
                }
                this.player.items.add(clue_item);
                if (this.clue_item.id == 5 && this.player.level == 1) { // progression level
                    this.player.level = 2;
                }
                if (this.clue_item.id == 9 && this.player.level < 3) { // progression level
                    this.player.level = 3;
                }
                this.clue_item = new Item(0);

                found_clues.add(new Point(this.opened_clue_location.x(), this.opened_clue_location.y()));
            }
            case Keybinds.ITEM_DOWN -> {
                if (selected_item_idx + 1 < this.player.items.size()) {
                    selected_item_idx += 1;
                }
            }
            case Keybinds.ITEM_UP -> {
                if (selected_item_idx > -1) {
                    selected_item_idx -= 1;
                }
            }
            case Keybinds.USE -> {
                if (selected_item_idx == -1) {
                    return false;
                }
                if (use_item(this.player.items.get(this.selected_item_idx))) {
                    this.player.items.remove(selected_item_idx);
                    this.selected_item_idx -= 1;
                }
            }
            case Keybinds.COPY_TEXT -> {
                String text;
                if (!this.opened_clue && this.selected_item_idx > -1
                        && this.player.items.get(selected_item_idx).id == 1) {
                    text = this.player.items.get(selected_item_idx).text;
                } else if (this.opened_clue) {
                    text = this.clue_item.text;
                } else {
                    return false;
                }
                StringSelection stringSelection = new StringSelection(text);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
            case Keybinds.N1 -> {
                if (!(opened_exit_door && used_blue_keycard) || entered_exit_code >= 1_000_000_000L) {
                    return false;
                }
                entered_exit_code *= 10;
                entered_exit_code += 1;
            }
            case Keybinds.N2 -> {
                if (!(opened_exit_door && used_blue_keycard) || entered_exit_code >= 1_000_000_000L) {
                    return false;
                }
                entered_exit_code *= 10;
                entered_exit_code += 2;
            }
            case Keybinds.N3 -> {
                if (!(opened_exit_door && used_blue_keycard) || entered_exit_code >= 1_000_000_000L) {
                    return false;
                }
                entered_exit_code *= 10;
                entered_exit_code += 3;
            }
            case Keybinds.N4 -> {
                if (!(opened_exit_door && used_blue_keycard) || entered_exit_code >= 1_000_000_000L) {
                    return false;
                }
                entered_exit_code *= 10;
                entered_exit_code += 4;
            }
            case Keybinds.N5 -> {
                if (!(opened_exit_door && used_blue_keycard) || entered_exit_code >= 1_000_000_000L) {
                    return false;
                }
                entered_exit_code *= 10;
                entered_exit_code += 5;
            }
            case Keybinds.N6 -> {
                if (!(opened_exit_door && used_blue_keycard) || entered_exit_code >= 1_000_000_000L) {
                    return false;
                }
                entered_exit_code *= 10;
                entered_exit_code += 6;
            }
            case Keybinds.N7 -> {
                if (!(opened_exit_door && used_blue_keycard) || entered_exit_code >= 1_000_000_000L) {
                    return false;
                }
                entered_exit_code *= 10;
                entered_exit_code += 7;
            }
            case Keybinds.N8 -> {
                if (!(opened_exit_door && used_blue_keycard) || entered_exit_code >= 1_000_000_000L) {
                    return false;
                }
                entered_exit_code *= 10;
                entered_exit_code += 8;
            }
            case Keybinds.N9 -> {
                if (!(opened_exit_door && used_blue_keycard) || entered_exit_code >= 1_000_000_000L) {
                    return false;
                }
                entered_exit_code *= 10;
                entered_exit_code += 9;
            }
            case Keybinds.N0 -> {
                if (!(opened_exit_door && used_blue_keycard) || entered_exit_code >= 1_000_000_000L) {
                    return false;
                }
                entered_exit_code *= 10;
                entered_exit_code += 0;
            }
            case Keybinds.ENTER -> {
                if (!(opened_exit_door && used_blue_keycard)) {
                    return false;
                }
                if (entered_exit_code == EXIT_CODE) {
                    win();
                } else {
                    this.player.items.add(new Item(13));
                    this.entered_exit_code = 0;
                }
            }
            case Keybinds.EXIT -> {
                return true;
            }
            case Keybinds.ESCAPE -> {
                this.opened_clue = false;
                this.opened_exit_door = false;
            }
            default -> {
                return false;
            }
        }

        return false;
    }

    // returns true if the item was used, false if not
    public boolean use_item(Item item) {
        switch (item.id) {
            case 0 -> {
            } // nothing
            case 1 -> {
            } // note
            case 2 -> {
            } // compass
            case 3 -> { // marker
                world[this.player.y][this.player.x] = 'X';
                return true;
            }
            case 4 -> { // grenade
                int grenade_radius = 5;

                for (int x = this.player.x - grenade_radius * 2; x < this.player.x + grenade_radius * 2; x++) {
                    for (int y = this.player.y - grenade_radius; y < this.player.y + grenade_radius; y++) {
                        if (Math.hypot((this.player.x - x) / 2, (this.player.y - y)) < grenade_radius) {
                            this.world[y][x] = ',';
                        }
                    }
                }
                return true;
            }
            case 5 -> { // yellow keycard
                // if you close and reopen the clue after using the yellow keycard it will
                // re-lock.
                if (!this.opened_clue || this.clue_item.id != 10) {
                    return false;
                }
                this.clue_item = new Item(9);
                return true;
            }
            case 6 -> { // flashlight
                if (VISIBILITY_RADIUS == 30)
                    VISIBILITY_RADIUS = 20;
                else
                    VISIBILITY_RADIUS = 30;
            }
            case 7 -> {
            } // radar
            case 8 -> {
            } // map
            case 9 -> { // blue keycard
                if (!this.opened_exit_door || this.clue_item.id != 11) {
                    return false;
                }
                this.clue_item = new Item(12);
                this.used_blue_keycard = true;
                return true;
            }
            case 10 -> {
            } // locked box (blue keycard)
        }
        return false;
    }

    public static String char_colour(char ch) {
        switch (ch) {
            case ',' -> {
                return DARK_GRAY;
            }
            case ':' -> {
                return MID_GRAY;
            }
            case '#' -> {
                return GRAY;
            }
            case '@' -> {
                return CYAN;
            }
            case '%' -> {
                return CLUE;
            }
            case '!' -> {
                return FAINT_CLUE;
            }
            case '^' -> {
                return DOOR;
            }
            case '*' -> {
                return DARK_DOOR;
            }
            case 'X' -> {
                return YELLOW;
            }
            default -> {
                return RESET;
            }
        }
    }

    // return true if good to go there
    // return false if there's an obstruction
    private boolean check_coll(int nx, int ny) {
        switch (this.world[ny][nx]) {
            case '@' -> {
                return true;
            }
            case '.' -> {
                return true;
            }
            case '#' -> {
                return false;
            }
            case '%' -> {
                // update progression level
                if (this.player.level == 0) {
                    this.player.level = 1;
                }

                boolean found_clue = false;
                for (Point p : found_clues) {
                    if (p.x() == nx && p.y() == ny) {
                        found_clue = true;
                        break;
                    }
                }

                Item item;
                if (found_clue) {
                    item = new Item(0);
                } else {
                    item = get_item_at(nx, ny);
                }

                opened_clue = true;
                this.opened_clue_location = new Point(nx, ny);
                clue_item = item;
                return false;
            }
            default -> {
                return true;
            }
            case '^' -> {
                opened_exit_door = true;
                if (used_blue_keycard && entered_code) {
                    win();
                }

                opened_clue = true;
                if (used_blue_keycard) {
                    clue_item = new Item(12);
                } else {
                    clue_item = new Item(11);
                }
                return false;
            }
        }
    }

    private Item choose_item(int itype, int item_hash) {
        switch (itype) {
            case 0 -> { // note
                if (0 <= item_hash && item_hash < 20) {
                    Item note = new Item(1);
                    String[] texts = {
                            "Now where could this exit\npossibly be?",
                            "I wonder where\nI should go next...",
                            "Try somewhere else.",
                            "I don't know what\nyou're looking for.",
                            "...zzz...",
                    };
                    note.text = texts[(int) (Math.random() * texts.length)];
                    return note;
                } else if (20 < item_hash && item_hash <= 40) {
                    Item note = new Item(1);
                    note.text = String.format("The code is: \n[%s]",
                            String.format("%-10s", Long.toString(this.EXIT_CODE)).replace(" ", "0"));
                    return note;
                } else if (found_clues.size() < clues.size()) {
                    Item note = new Item(1);

                    while (true) {
                        int rand_clue_idx = (int) (Math.random() * (double) clues.size());
                        if (found_clues.contains(clues.get(rand_clue_idx)))
                            continue;

                        Point c = clues.get(rand_clue_idx);
                        double dist = Math.hypot(this.player.x - c.x(), this.player.y - c.y());
                        note.compass_target = c;
                        note.text = String.format("There is a %s\nlocated %.2fm away.",
                                get_item_at(c.x(), c.y()).get_name(), dist);
                        break;
                    }

                    return note;
                } else {
                    Item note = new Item(0);
                    return note;
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
                // break up the text onto multiple lines
                String text = note.text;
                ArrayList<String> segments = new ArrayList<>();
                while (text.length() > 26) {
                    if (segments.size() > 12) {
                        segments.add(text);
                        break;
                    }
                    int space_pos = 26;
                    int char_idx = text.length() - 1;
                    while (char_idx >= 0 && space_pos >= 26) {
                        if (text.charAt(char_idx) == ' ') {
                            space_pos = char_idx;
                        }
                        char_idx -= 1;
                    }
                    segments.add(text.substring(0, space_pos));
                    text = text.substring(space_pos);
                }
                note.text = String.join("\n", segments);
                return note;
            }
            case 2 -> {
                if (0 <= item_hash && item_hash < 15) {
                    Item item = new Item(2); // compass
                    if (Math.random() < 0.05) {
                        item.compass_target = exit_door_location;
                    } else {
                        int clue_idx = (int) (Math.random() * clues.size());
                        item.compass_target = this.clues.get(clue_idx);
                    }
                    return item;
                } else if (15 <= item_hash && item_hash < 40) {
                    return new Item(3);
                } else if (40 <= item_hash && item_hash < 70) {
                    return new Item(4);
                } else if (70 <= item_hash && item_hash < 80) {
                    return new Item(5);
                } else if (80 <= item_hash && item_hash < 85) {
                    return new Item(10); // return a blue safe
                } else if (85 <= item_hash && item_hash < 95) {
                    return new Item(6);
                } else if (95 <= item_hash && item_hash < 100) {
                    return new Item(7);
                } else {
                    return new Item(0);
                }
            }
            case 3 -> {
                Item note = new Item(8);
                note.compass_target = new Point((int) (Math.random() * (double) (WORLD_WIDTH - 30)) + 15,
                        (int) (Math.random() * (double) (WORLD_HEIGHT - 16)) + 8);
                return note;
            }
        }
        return new Item(0);
    }

    private Item get_item_at(int x, int y) {
        int loc_hash = Utils.hash_location(x, y);
        int item_hash = Utils.picker(loc_hash, 100);
        return choose_item(Utils.picker(loc_hash, 4), item_hash);
    }

    public void win() {
        try {
            won = true;
            allowed_to_leave = false;
            System.out.print("\u001b[H\u001b[0m\u001b[2J");
            Thread.sleep(1000);
            // ellipsis
            for (int i = 0; i < 3; i++) {
                Thread.sleep(400);
                System.out.print(".");
                System.out.flush();
            }
            System.out.println("");
            System.out.println("Congratulations.");
            Thread.sleep(4000);
            System.out.println("You escaped the maze.");
            Thread.sleep(4000);
            System.out.println("Did you forget anything in there?");
            Thread.sleep(4000);
            System.out.println("Bit too late to get it out now...");
            Thread.sleep(4000);
            System.out.println("Thanks for playing.");
            Thread.sleep(4000);
            System.out.println("Press Q to win.");
            allowed_to_leave = true; // goodbye :)
        } catch (InterruptedException e) {
            /* */
        }
    }

    // return as 30x15
    public String[] render(Item i, int cx, int cy) {
        switch (i.id) {
            case 0 -> { /* nothing/scraps */
                return Item.to_lines("""
                          . ,      _ .   ,     .   _ .
                            ^      ,  .     *    /   .
                        ;  `    '        .    ,   _  .
                        .- _  &   -  .    *   -      .
                            :                    .x  .
                            ^      ,  .     *        .
                                .              .     .
                        x                          xx.
                        x       ;     '     ^        .
                        ....x.     . ?            ..x.
                        xxxx                    >   x.
                        x                          xx.
                        xx  ^      ,  .     *       x.
                        x                "          xx
                        ..    ----   __          ....x
                        """);
            }
            case 1 -> { /* note - render text split at lines */
                String[] base = Item.to_lines("""
                         _,,,..>***^^^^^^^^***<..,,,_.
                        ;%                          \\,
                        /                            |
                        ]                            [
                        [                            ]
                        ]                            [
                        [                            ]
                        ]                            [
                        [                            ]
                        ]                            [
                        [                            ]
                        ]                            ^
                        [                            ]
                        \\.                          &^
                         ^&+++=>***^^-^^^-^***<=+++=*.
                        """);

                String[] lines = i.text.split("\n");
                int index = (15 - lines.length) / 2;
                for (String line : lines) {
                    // center string
                    String truncated;
                    if (line.length() > 26) {
                        truncated = line.substring(0, 23) + "...";
                    } else {
                        truncated = line;
                    }
                    base[index] = Utils.write_string(base[index], truncated, Math.max(2, 15 - (line.length() / 2)));
                    if (index >= base.length) {
                        break;
                    }
                    index += 1;
                }

                return base;
            }
            case 2 -> { // compass
                int dx = cx - i.compass_target.x();
                int dy = cy - i.compass_target.y();
                double angle_to_target = Math.atan2(dy, dx);

                return i.compass_phase(angle_to_target);
            }
            case 3 -> { // marker
                return Item.to_lines(
                        """
                                                             .
                                                             .
                                       ._           ,_       .
                                     :[##$:      ,&##P>      .
                                       `!#$'.  ,i##$^        .
                                         `&#i-i##$^          .
                                          >#####<            .
                                        u*#:$-&##j.          .
                                      u/#&`    `$##j.        .
                                    *5##^        `$##k:      .
                                     `^            `*'       .
                                                             .
                                                             .
                                                             .
                                                             .
                                """);
            }
            case 4 -> { // grenade
                return Item.to_lines(
                        """
                                                             .
                                       -=#########           .
                                        7##########,--:\\    .
                                          ####  ####    |    .
                                          ###########    ]   .
                                          |%###   \\###  ;   .
                                          |%##|    "-##/     .
                                      _,=..###&*=+_.  ##     .
                                    ;`'*########L=...':,#    .
                                   ::=###########Up=...:     .
                                   :,*&##########%*=...o:    .
                                    ~7.*X###%#/%%**=.k~`     .
                                     `:vK.U27!(*2/....       .
                                         ```^::^```          .
                                                             .
                                """);
            }
            case 5 -> { // yellow keycard
                return Item.to_lines(
                        """
                                                             .
                                                             .
                                   +----------------------+  .
                                   | Yellow keycard       |  .
                                   |                      |  .
                                   | Use this to unlock   |  .
                                   | valuables...         |  .
                                   |                      |  .
                                   |                      |  .
                                   +----------------------+  .
                                                             .
                                                             .
                                                             .
                                                             .
                                                             .
                                                             .
                                """);
            }
            case 6 -> { // flashlight
                return Item.to_lines(
                        """
                                                             .
                                             ,_,             .
                                        .o$GGGGGG&=v         .
                                       :GGGGGGGGGGGG;        .
                                        7GGGGGGGGG%/         .
                                          ^*GGGGM"           .
                                           ._%>`             .
                                           ._%>`             .
                                           ._%>`             .
                                           ._%>`             .
                                           ._%>`             .
                                           ._%>`             .
                                           ._%>`             .
                                            ^~^              .
                                                             .
                                """);
            }
            case 7 -> { // radar
                String[] base = Item.to_lines(
                        """
                                         ,oGGGGGGo.          .
                                     ,:%%^        ^%%:,      .
                                   ,%^                `%.    .
                                  :%                    `&   .
                                 7*                      Tv  .
                                 $                        &  .
                                /                          $ .
                                %                          $ .
                                &                          $ .
                                 $                        $  .
                                 "                       ,~  .
                                  ^                     /^   .
                                   ?                  ,%"    .
                                   '`^%%*,        .*$@^      .
                                         `%$$$$$$&'          .
                                """);
                // render a 50x50 circle around the player
                // every pixel on the item represents a 2x4 cell area in the maze
                for (int x = 0; x < 25; x++) {
                    for (int y = 0; y < 12; y++) {
                        // check if the dot would fit on the radar
                        if (Math.hypot(x - 12, (y - 6)) > 12.5) {
                            continue;
                        }

                        int world_x = x * 2 + cx - 25;
                        int world_y = y * 4 + cy - 25;

                        if (world_x < 0 || world_y < 0 || world_x >= WORLD_WIDTH - 1 || world_y >= WORLD_HEIGHT - 3) {
                            continue;
                        }
                        // check in a 2x2 area for stuff
                        char thing = ' ';
                        if (world[world_y][world_x] == '^' || world[world_y][world_x + 1] == '^' ||
                                world[world_y + 1][world_x] == '^' || world[world_y + 1][world_x + 1] == '^' ||
                                world[world_y + 2][world_x] == '^' || world[world_y + 2][world_x + 1] == '^' ||
                                world[world_y + 3][world_x] == '^' || world[world_y + 3][world_x + 1] == '^') {
                            thing = '^';
                        } else if (world[world_y][world_x] == '%' || world[world_y][world_x + 1] == '%' ||
                                world[world_y + 1][world_x] == '%' || world[world_y + 1][world_x + 1] == '%' ||
                                world[world_y + 2][world_x] == '%' || world[world_y + 2][world_x + 1] == '%' ||
                                world[world_y + 3][world_x] == '%' || world[world_y + 3][world_x + 1] == '%') {
                            thing = '%';
                        }
                        base[y + 1] = base[y + 1].substring(0, x + 2) + thing + base[y + 1].substring(x + 3);
                    }
                }

                return base;
            }
            case 8 -> {
                String[] base = new String[] {
                        " ".repeat(30),
                        " ".repeat(30),
                        " ".repeat(30),
                        " ".repeat(30),
                        " ".repeat(30), // 5
                        " ".repeat(30),
                        " ".repeat(30),
                        " ".repeat(30),
                        " ".repeat(30),
                        " ".repeat(30), // 10
                        " ".repeat(30),
                        " ".repeat(30),
                        " ".repeat(30),
                        " ".repeat(30),
                        " ".repeat(30), // 15
                };

                for (int y = -7; y <= 7; y++) {
                    for (int x = -14; x <= 15; x++) {
                        base[y + 7] = base[y + 7].substring(0, x + 14)
                                + world[y + i.compass_target.y()][x + i.compass_target.x()]
                                + base[y + 7].substring(x + 15);
                    }
                }

                return base;
            }
            case 9 -> { // blue keycard
                return Item.to_lines(
                        """
                                                             .
                                                             .
                                   +-^-^-^-^-^-^-^-^-^-^-^+  .
                                   | ^BLUE^ keycard       |  .
                                   |                      |  .
                                   | Use this to unlock   |  .
                                   | something special ^  |  .
                                   |                      |  .
                                   |                      |  .
                                   +^-^-^-^-^-^-^-^-^-^-^-+  .
                                                             .
                                                             .
                                                             .
                                                             .
                                                             .
                                                             .
                                """);
            }
            /* internal items, used for screens */
            case 10 -> { // blue keycard (not unlocked)
                return Item.to_lines("""
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                        [ Requires yellow keycard ].
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .""");
            }
            case 11 -> { // exit door (keycard)
                return Item.to_lines("""
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                        [ Requires blue keycard ]  .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .""");
            }
            case 12 -> { // exit door (code)
                return Item.to_lines(String.format("""
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                        [ Code : %-10s     ]  .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .
                                                   .""", this.entered_exit_code));
            }
            case 13 -> { // failure item
                return Item.to_lines("""
                                           .
                                           .
                                           .
                                           .
                                           .
                                           .
                                           .
                        WRONG CODE!!!      .
                                           .
                                           .
                                           .
                                           .
                                           .
                                           .
                                           .""");
            }
        }
        // item that has no image
        return new String[] {
                " ".repeat(30),
                " ".repeat(30),
                " ".repeat(30),
                " ".repeat(30),
                " ".repeat(30), // 5
                " ".repeat(30),
                " ".repeat(30),
                " ".repeat(30),
                " ".repeat(30),
                " ".repeat(30), // 10
                " ".repeat(30),
                " ".repeat(30),
                " ".repeat(30),
                " ".repeat(30),
                " ".repeat(30), // 15
        };
    }
}

record Point(int x, int y) {
}
