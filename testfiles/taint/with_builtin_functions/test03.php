<?

// check correct short-cut handling of builtin functions
// (with respect to shadow variables)

$x1;
a();

function a() {
    global $x1;
    $a1 =& $x1;
    intval(8);
    ~_hotspot0;  // a1:T/D
}



?>