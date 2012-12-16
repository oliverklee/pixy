<? //

// was a bug: constants in default parameters;
// this time with a method call

define("BLOB", 'gaga');
$y = $f->foo();
echo $y;

class MyClass {
    function foo($x = BLOB) {
        return $x;
    }
}





?>
