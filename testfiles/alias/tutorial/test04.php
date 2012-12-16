<?php

// extended with g-shadows (compared to the version in the tutorial)

$x1; $x2; $x3;  
a();
~_hotspot0;     // u{(main.x1, main.x2, main.x3)} a{}

function a() {                          
    $a1 =& $a2;                         
    $GLOBALS['x1'] =& $GLOBALS['x2'];   
    ~_hotspot1;                         // u{(a.a1, a.a2) (main.x3, a.x3_gs) (main.x1, main.x2, a.x2_gs)} a{}
    b();
    ~_hotspot2;                         // u{(a.a1, a.a2) (main.x1, main.x2, main.x3, a.x2_gs)} a{}
}

function b() {                          
    $GLOBALS['x3'] =& $GLOBALS['x1'];   
    ~_hotspot3;                         // u{(main.x1, main.x2, main.x3, b.x1_gs b.x2_gs)} a{}
                                        
}

?>
