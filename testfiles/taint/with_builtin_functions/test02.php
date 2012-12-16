<?php

// "~_test_" node

$x1 = _junit_test_01($_UNTAINTED);
$x2 = _junit_test_01($_TAINTED);
$x3 = _junit_test_02($_UNTAINTED, $_UNTAINTED);
$x4 = _junit_test_02($_UNTAINTED, $_TAINTED);
$x5 = _junit_test_03($_UNTAINTED, $_UNTAINTED);
$x6 = _junit_test_03($_UNTAINTED, $_TAINTED);

~_hotspot0;         // x1:U/C
                    // x2:T/D
                    // x3:U/C
                    // x4:T/D
                    // x5:U/C
                    // x6:T/D

?>


