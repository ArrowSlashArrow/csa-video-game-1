// do you like ascii art?

public class Item {
    public int id; // stackable items have the same id; unstackable items do not
    public String text = "";
    public Point compass_target = new Point(0, 0);
    public double flashlight_battery = 100.0;

    public Item(int i) {
        this.id = i;
    }

    public String get_name() {
        switch (this.id) {
            case 0 -> { return "Nothing"; }
            case 1 -> { return "Note"; }
            case 2 -> { return "Compass"; }
            case 3 -> { return "Marker"; }
            case 4 -> { return "Grenade"; }
            case 5 -> { return "Yellow Keycard"; }
            case 6 -> { return "Flashlight"; }
            case 7 -> { return "Radar"; }
            case 8 -> { return "Map"; }
            case 9 -> { return "Blue Keycard"; }
            /* internal items */
            case 11 -> { return "Exit door"; }
            case 12 -> { return "Exit door"; }
        }
        return "mystery item";
    }

    public static String[] to_lines(String i) { return i.split("\n"); }

    public String[] compass_phase(double angle) {
        double degrees = (360.0 + (angle * 180.0 / Math.PI)) % 360.0;
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
        System.out.println("did not get a valid compass phase for " + angle);
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