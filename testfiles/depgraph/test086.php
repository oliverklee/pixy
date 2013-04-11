<? //

// methods should not interfere with functions that have
// the same name

$x = $f->bar($evilmethod);
echo $x;
$y = bar($evilfunction);
echo $y;

class Foo {
    function bar($pm) {
        return $pm;
    }
}

function bar($pf) {
    return $pf;
}


?>