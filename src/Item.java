public class Item {
    public String name;
    public int amount;
    public long id; // stackable items have the same id; unstackable items do not
    public String image;
    public String text;
    public Point compass_target;

    public Item(String n, int a, long i) {
        this.name = n;
        this.amount = a;
        this.id = i;
    }

    // set render height
    public String[] render() {
        // todo: output lines of ascii art to blit onto screen in World.render()
        switch (this.id) {

        }
    }
}
