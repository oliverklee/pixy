<? //

// was a bug during depgraph construction which led to
// an exception

$x = false;
if ($cond) {
    $x = array();
    $x[0] = 7;
}
if ($cond) {
    echo $x[0];
}





?>