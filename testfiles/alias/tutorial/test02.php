<?php

                // u{} a{}
if ($u) {
                // u{} a{}
    $a =& $b;   // u{(a,b)} a{}
    $c =& $b;   // u{(a,b,c)} a{}
} else { 
                // u{} a{}
    $a =& $b;   // u{(a,b)} a{}
}
~_hotspot0;     // u{(a,b)} a{(b,c) (a,c)}

?>
