<? //

// basic interprocedural test

$x1 = foo($a);
~_hotspot1;     // x1:(main.a,5)

function foo($x2) {
    ~_hotspot0;     // x2:(main.a,5)
    return $x2;
}


?>