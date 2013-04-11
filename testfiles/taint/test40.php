<?

// we had a small bug in the conversion of foreach-loops into TAC,
// which lead to a wrong result for cases such as this one

$x = foo();
~_hotspot0;       // x:T/F

function foo() {

    foreach ($arr as $el) {
        return $_TAINTED;
    }
    return false;
}
?>