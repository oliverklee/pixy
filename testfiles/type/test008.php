<?

// was a bug in file inclusion

require('test008a.php');
foo();
require('test008a.php');

class emailer
{
	function assign_vars()
	{
        echo $_GET['two'];
	}
}


class Template {
	function assign_vars()
	{
        echo $_GET['one'];  // this one is called
	}

	function set_filenames()
	{
	}
}



?>
