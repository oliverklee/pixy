<?php

// unset

$x =& $z;
if ($u) {
    $y =& $z;
}
~_hotspot0;     // u{ (main.x main.z) }
                // a{ (main.y main.z) (main.y main.x) }
unset($z);
~_hotspot1;     // u{ }
                // a{ (main.y main.x) }

?>