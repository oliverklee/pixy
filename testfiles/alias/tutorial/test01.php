<?php

                // u{}, a{}
if ($u) {
                // u{}, a{}
    $a =& $b;   // u{(a,b)}, a{}
} else {
                // u{}, a{}
    $a =& $c;   // u{(a,c)}, a{}
}
~_hotspot0;     // u{} a{(a,b) (a,c)}
?>