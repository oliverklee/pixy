<?php

// define()

// case-sensitive (default param)
define('C1', $_UNTAINTED);    
$C1 = C1;
$c1 = c1;
define('C2', $_TAINTED);    
$C2 = C2;
$c2 = c2;
~_hotspot0;             // C1:U, c1:U
                        // C2:T, c2:U

// case-sensitive (using "false" explicitly)
// ...not necessary

// case-insensitive
define('C3', $_UNTAINTED, true);    
$C3 = C3; 
$c3 = c3;
define('C4', $_TAINTED, true);    
$C4 = C4; 
$c4 = c4;
~_hotspot1;             // C3:U, c3:U
                        // C4:T, c4:T

// unknown case sensitivity
define('C5', $_UNTAINTED, $u);
$C5 = C5; 
$c5 = c5;
define('C6', $_TAINTED, $u);
$C6 = C6; 
$c6 = c6;
~_hotspot2;             // C5:U, c5:U
                        // C6:T, c6:T


?>
