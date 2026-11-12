public enum Keybinds {
    MOVE_UP,
    MOVE_LEFT,
    MOVE_DOWN,
    MOVE_RIGHT,
    EXIT,
    PICK_UP,
    USE,
    ITEM_DOWN,
    ITEM_UP,
    COPY_TEXT,
    ESCAPE,
    UNKNOWN
    ;

    public static Keybinds get_associated_keybind(int keycode) {
        switch (keycode) {
            case 'w' -> { return Keybinds.MOVE_UP; }
            case 'a' -> { return Keybinds.MOVE_LEFT; }
            case 's' -> { return Keybinds.MOVE_DOWN; }
            case 'd' -> { return Keybinds.MOVE_RIGHT; }
            case 'q' -> { return Keybinds.EXIT; }
            case 'e' -> { return Keybinds.PICK_UP; }
            case 'f' -> { return Keybinds.USE; }
            case 'z' -> { return Keybinds.ITEM_UP; }
            case 'x' -> { return Keybinds.ITEM_DOWN; }
            case 'c' -> { return Keybinds.COPY_TEXT; }
            case 27 -> { return Keybinds.ESCAPE; } // esc key
            default -> { return Keybinds.UNKNOWN; }
        }
    }
}
