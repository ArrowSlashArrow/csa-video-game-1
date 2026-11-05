public class Item {
    public String name;
    public int amount;
    public long id; // stackable items have the same id; unstackable items do not
    public String image;

    public Item(String n, int a, long i) {
        this.name = n;
        this.amount = a;
        this.id = i;
    }
}
