<? //

// was a bug during TAC conversion, must not
// lead to an exception

static $x = array('one', 'two', 'three');

foo();

function foo()
{
    static $y = array('four');
    static $z = array();
}





?>
