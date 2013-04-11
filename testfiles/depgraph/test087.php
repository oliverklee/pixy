<? //

// was a bug: constants in default parameters

define("BLOB", 'gaga');
$y = foo();
echo $y;

function foo($x = BLOB) {
    return $x;
}



?>