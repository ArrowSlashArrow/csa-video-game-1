public class Main {
    public static void main(String[] args) {
        World w = new World();
        w.player.x = 500;
        w.player.y = 500;
        System.out.println(w.render());

        while (true) {
            if (w.get_input()) {
                break;
            }

            System.out.println(w.render());
        }
    }
}