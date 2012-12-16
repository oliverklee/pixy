<?php

// interesting case for demonstrating call-string limitations:
// if you perform taint analysis as call-string analysis with k=1,
// $x1 is tainted in the end; functional analysis or call-string analysis
// with k > 1 is more precise and can determine that it is untainted

foo(); 
$x1 = 'good';
foo();
~_hotspot0;     // x1: <see explanation above>

function foo()
{
    bar();
}

function bar() {
    // do nothing
}


?>
