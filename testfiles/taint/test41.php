<?

// conditional function definition:
// approximate by using the first one
// later: transform calls accordingly

$x1 = foo();
$x2 = bar();
~_hotspot0;     // x1:U/C, x2:T/D

if ($cond) {
    function foo() {
        return 'harmless';
    }
    function bar() {
        return $GLOBALS['evil'];
    }
} else {
    function foo() {
        return $GLOBALS['evil'];
    }
    function bar() {
        return 'harmless';
    }
}


?>