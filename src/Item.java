// do you like ascii art?

public class Item {
    public int amount;
    public int id; // stackable items have the same id; unstackable items do not
    public boolean stackable;
    public String text = "";
    public Point compass_target = new Point(0, 0);

    public Item(int a, int i, boolean s) {
        this.amount = a;
        this.id = i;
        this.stackable = s;
    }

    // return as 30x15
    public String[] render(int cx, int cy) {

        switch (this.id) {
            case 0 -> { /* nothing/scrapes */
                return to_lines("""
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
                String[] base = to_lines("""
                 _,,,..>***^^^^^^^^***<..,,,_ 
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
                 ^&+++=>***^^-^^^-^***<=+++=* 
                """);

                String[] lines = this.text.split("\n");
                int index = (15 - lines.length) / 2;
                for (String line : lines) {
                    // center string
                    base[index] = Utils.write_string(base[index], line, Math.max(2, 15 - (line.length() / 2)));
                    if (index >= base.length) { break; }
                    index += 1;
                }

                return base;
            }
            case 2 -> { // compass
                int dx = cx - this.compass_target.x();
                int dy = cy - this.compass_target.y();
                double angle_to_target = Math.atan2(dy, dx);

                return compass_phase(angle_to_target);
            }
            case 3 -> { // marker
                return to_lines(
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
""");
            }
            case 4 -> { // grenade
                return to_lines(
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
    ~7...###%#/%%**=.k~`     .
     `:vK.U27!(*2/....       .
         ```^::^```          .
                             .
""");
            }
            case 5 -> { // yellow keycard
                return to_lines(
"""
no item sprite yet                  
""");
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

    private String[] compass_phase(double angle) {
        double degrees = (angle * 180.0 / Math.PI) % 360.0;
        if ((0.0 <= degrees && degrees < 15.0) || (345.0 <= degrees && degrees < 360.0)) {
            return to_lines(
            """
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/             \\@)     .
    !#'               ^#.    . 
    #[                 $!    . 
   ;#`        o=====>  ;$;   .
    ](                 /@    .
    "&s               .#!    .
     `%*.           ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
"""
            );
        } else if (15.0 <= degrees && degrees < 45.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/             \\@)     . 
    !#'           _   ^#.    . 
    #[         ,u&7    $!    . 
   ;#`        o^       ;$;   .
    ](                 /@    .
    "&s               .#!    .
     `%*.           ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        } else if (45.0 <= degrees && degrees < 75.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/        .    \\@)     . 
    !#'        &^     ^#.    . 
    #[        ,%       $!    . 
   ;#`        o        ;$;   .
    ](                 /@    .
    "&s               .#!    .
     `%*.           ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        } else if (75.0 <= degrees && degrees < 105.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/      ^      \\@)     . 
    !#'       |       ^#.    . 
    #[        |        $!    . 
   ;#`        o        ;$;   .
    ](                 /@    .
    "&s               .#!    .
     `%*.           ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        } else if (105.0 <= degrees && degrees < 135.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/    .        \\@)     . 
    !#'     ^&        ^#.    .
    #[       %,        $!    . 
   ;#`        o        ;$;   .
    ](                 /@    .
    "&s               .#!    .
     `%*.           ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        } else if (135.0 <= degrees && degrees < 165.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/             \\@)     . 
    !#'  ._           ^#.    .
    #[    5ju.         $!    . 
   ;#`       ^o        ;$;   .
    ](                 /@    .
    "&s               .#!    .
     `%*.           ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        } else if (165.0 <= degrees && degrees < 195.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/             \\@)     .
    !#'               ^#.    .
    #[                 $!    .
   ;#`  <=====o        ;$;   .
    ](                 /@    .
    "&s               .#!    .
     `%*.           ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        } else if (195.0 <= degrees && degrees < 225.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/             \\@)     . 
    !#'               ^#.    . 
    #[                 $!    . 
   ;#`       .o        ;$;   .
    ](    .u$`         /@    .
    "&s  *"           .#!    .
     `%*.           ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        } else if (225.0 <= degrees && degrees < 255.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/             \\@)     . 
    !#'               ^#.    . 
    #[                 $!    . 
   ;#`        o        ;$;   .
    ](       %^        /@    .
    "&s     ;#        .#!    .
     `%*.   ^       ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        } else if (255.0 <= degrees && degrees < 285.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/             \\@)     .
    !#'               ^#.    .
    #[                 $!    . 
   ;#`        o        ;$;   .
    ](        |        /@    .
    "&s       |       .#!    .
     `%*.     v     ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        } else if (285.0 <= degrees && degrees < 315.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/             \\@)     . 
    !#'               ^#.    . 
    #[                 $!    . 
   ;#`        o        ;$;   .
    ](        ^&       /@    .
    "&s        "\\     .#!   . 
     `%*.       !*  ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        } else if (315.0 <= degrees && degrees < 345.0) {
            return to_lines(
"""
:                            :
                             .
         _x=&##&=+c.         .
       v#%R^      "$Sg.      .
     T#/             \\@)     . 
    !#'               ^#.    . 
    #[                 $!    . 
   ;#`        o_       ;$;   .
    ](         `+=?.   /@    .
    "&s           "*  .#!    .
     `%*.           ,:#7     .
       ^&%q      ,s~#P       .
        '*1#HvvH#G?          .
                             .
:                            |
""");
        }
        return null; // unreachable
    }
}

/* ascii art size template

|                            |
|                            |
|                            |
|                            |
|                            |
|                            |
|                            |
|                            |
|                            |
|                            |
|                            |
|                            |
|                            |
|                            |
|                            |



*/