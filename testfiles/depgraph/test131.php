<? //

// was a bug: depgraph construction did not take into account
// that there can be array()'s inside function default cfgs

define('MYCONST', 'c');
foo();

function foo($x = array('a', 'b', MYCONST)) {
    echo $x[0];
    echo $x[1];
    echo $x[2];
    echo $x[$_GET['y']];
}


?>
