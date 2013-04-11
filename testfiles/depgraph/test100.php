<? //


$a = foo($evil);
echo $a;

function foo($p) {
    $r = bar($p);
    return $r;
}

function bar($q) {
    $t = $q;
    return $t;
}





?>