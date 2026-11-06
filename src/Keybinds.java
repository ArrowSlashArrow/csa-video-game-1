public enum Keybinds {
    MOVE_UP,
    MOVE_LEFT,
    MOVE_DOWN,
    MOVE_RIGHT,
    EXIT,
    PICK_UP,
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
            case 'p' -> { return Keybinds.PICK_UP; }
            case 27 -> { return Keybinds.ESCAPE; }
            default -> { return Keybinds.UNKNOWN; }
        }
    }
}
