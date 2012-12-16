<?php

if ($u) {
    $a =& $c;   // u{(a,c)} a{}
    $b =& $c;   // u{(a,b,c)} a{}
    $d =& $f;   // u{(a,b,c) (d,f)} a{}
    $e =& $f;   // u{(a,b,c) (d,e,f)} a{}
} else {
    $b =& $e;   // u{(b,e)} a{}
    $c =& $e;   // u{(b,c,e)} a{}
    $d =& $e;   // u{(b,c,d,e)} a{}
}
~_hotspot0;     // u{(b,c) (d,e)} a{(a,b) (a,c) (d,f) (e,f) (b,d) (b,e) (c,d) (c,e)}

?>
