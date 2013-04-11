<?php

// easy basic may-example

~_hotspot0;         // u{} a{}
if ($unknown) {
    $a =& $b;
    ~_hotspot1;     // u{(a,b)} a{}
}
~_hotspot2;         // u{} a{(a,b)}

?>