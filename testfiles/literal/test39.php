<?php

// interprocedural propagation of constants 

C1; C2;
define('C1', '1');
a();
$x1 = C1;
$x2 = C2;
~_hotspot0;     // x1:1, x2:2

function a() {
    define('C2', '2');
    $a1 = C1;
    $a2 = C2;
    ~_hotspot1;         // a1:1, a2:2
}

?>


