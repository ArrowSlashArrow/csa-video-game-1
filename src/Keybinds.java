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
    UNKNOWN,
    N1,
    N2,
    N3,
    N4,
    N5,
    N6,
    N7,
    N8,
    N9,
    N0,
    ENTER,
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
            case '1' -> { return Keybinds.N1; }
            case '2' -> { return Keybinds.N2; }
            case '3' -> { return Keybinds.N3; }
            case '4' -> { return Keybinds.N4; }
            case '5' -> { return Keybinds.N5; }
            case '6' -> { return Keybinds.N6; }
            case '7' -> { return Keybinds.N7; }
            case '8' -> { return Keybinds.N8; }
            case '9' -> { return Keybinds.N9; }
            case '0' -> { return Keybinds.N0; }
            case 27 -> { return Keybinds.ESCAPE; } // esc key
            case '\r' -> { return Keybinds.ENTER; }
            default -> { return Keybinds.UNKNOWN; }
        }
    }
}
