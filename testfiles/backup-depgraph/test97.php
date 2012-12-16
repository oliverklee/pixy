<? //

// default params check (3);
// nasty case: a constant is
// set depending on context;
// also, there is a call-string effect for $a

$a = foo($evil);
$b = foo('good');

echo $a;
echo $b;

function foo($p) {
    define('MYCONST', $p);
    $r = bar();
    return $r;
}

function bar($q = MYCONST) {
    $t = $q;
    return $t;
}

?>
