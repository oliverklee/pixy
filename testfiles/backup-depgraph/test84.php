<? //

// disabled alias analysis: was a bug that led to an exception;
// currently not supported

foo();

function foo()
{
    global ${$settings['varname']};
}



?>
