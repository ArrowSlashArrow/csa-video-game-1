public class Item {
    public int amount;
    public int id; // stackable items have the same id; unstackable items do not
    public boolean stackable;
    public String image;
    public String text;
    public Point compass_target;

    public Item(int a, int i, boolean s) {
        this.amount = a;
        this.id = i;
        this.stackable = s;
    }

    // return as 30x15
    public String[] render() {
        // todo: output lines of ascii art to blit onto screen in World.render()
        switch (this.id) {
            case 0 -> { 
                return to_lines("""
                
                """);   
            }
            case 1 -> { 
                return to_lines("""
                    
                    
                    
                    
                    
                    
                    
                    """);
            }
        }

        return new String[] {
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
        };
    }

    public String get_name() {
        switch (this.id) {
            case 0 -> { return "Nothing"; }
            case 1 -> { return "Note"; }
            case 11 -> {return "Note that points someplace"; }
            case 2 -> { return "Compass"; }
            case 3 -> { return "Marker"; }
            case 4 -> { return "Grenade"; }
            case 5 -> { return "Yellow Keycard"; }
            case 6 -> { return "Flashlight"; }
            case 7 -> { return "Radar"; }
            case 8 -> { return "Map"; }
            case 9 -> { return "Blue Keycard"; }
        }
        return "mystery item";
    }

    private String[] to_lines(String i) { return i.split("\n"); }
}

/* ascii art

xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
....x....x....x....x....x....x
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
....x....x....x....x....x....x
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
xxxx.xxxx.xxxx.xxxx.xxxx.xxxx.
....x....x....x....x....x....x



  . ,      _ .   ,     .   _ 
    ^      ,  .     *    /   
;  `    '    *   .    ,   _  
 .- _  &   -  .    *   -     
                            .x
                             .
         .              .    .
x                          xx.
x                            .
....x.                     ..x
xxxx                        x.
x                          xx.
xx                          x.
x                          xx.
..                       ....x










*/