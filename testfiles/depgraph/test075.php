<? //

// was a bug: the dependence should lead to $get2,
// and not to $get1

$a[0] = $get1;
$b[0] = $get2;
foo($a, $b);
function foo($f1, $f2) {
    $x = $f2;
    echo $x[0];
}





?>
