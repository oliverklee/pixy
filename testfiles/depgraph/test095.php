<? //

// default params check (1)

$a = foo($evil);
$b = foo('good');
$c = foo();

echo $a;
echo $b;
echo $c;


function foo($p = 7) {
    $r = $p;
    return $r;
}

?>
