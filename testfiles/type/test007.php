<?

// was a bug in method backpatching

include('./' . 'test007a.php');

init();

// both of these calls go to MyClass::foo()
$x->foo();
$x->foo();

function init()
{
	global $x;
	$x = new MyClass();
}


class SomeClass
{
	function foo()
	{
        echo $_GET['two'];
	}
}



?>