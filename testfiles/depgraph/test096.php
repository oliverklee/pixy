<? //

// default params check (2)

define('MYCONST', $very_evil);

$a = foo($evil);
$b = foo('good');
$c = foo();

echo $a;
echo $b;
echo $c;


function foo($p = MYCONST) {
    $r = $p;
    return $r;
}

?>