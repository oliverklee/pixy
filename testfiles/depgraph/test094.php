<? //


// basic block check

$a = foo($evil);
$b = foo('good');

echo $a;    // vuln
echo $b;    // ok


function foo($p) {
    $x = 1;
    $r = $p;    // enclosed in basic block
    $y = 2;
    return $r;
}



?>