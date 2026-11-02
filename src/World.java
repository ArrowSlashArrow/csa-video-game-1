import java.util.Scanner;

public class World {
    // 1mb of memory isn't too bad
    // maybe we shouldn't generate the whole maze in one go...
    private char[][] world = new char[1000][1000];
    public Player player = new Player();
    private Scanner s;

    public World() {
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                int m = (int)(Math.random() * 1000.0);
                if (m < 80) {
                    world[i][j] = '#';
                } else {
                    world[i][j] = ' ';
                }
            }
        }

        this.s = new Scanner(System.in);
    }

    // renders world around player
    public String render() {
        int start_x = Math.min(990, Math.max(0, this.player.x - 5));
        int start_y = Math.min(990, Math.max(0, this.player.y - 5));
        int width = 10;
        int height = 10;

        String result = "";

        for (int i = start_y; i < start_y + height; i++) {
            for (int j = start_x; j < start_x + width; j++) {
                if (this.player.x == j && this.player.y == i) {
                    result += "@";
                } else {
                    result += world[i][j];
                }
            }
            result += "\n";
        }

        return result;
    }

    public boolean get_input() {
        String n = s.next();
        if (n.equals("q")) { return true; }
        if (n.equals("w")) this.player.y = Math.max(0, this.player.y - 1);
        if (n.equals("s")) this.player.y = Math.min(999, this.player.y + 1);
        if (n.equals("a")) this.player.x = Math.max(0, this.player.x - 1);
        if (n.equals("d")) this.player.x = Math.min(999, this.player.x + 1);

        return false;
    }
}
