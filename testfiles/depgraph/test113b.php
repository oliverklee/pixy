<? //

function foo()
{
    global $root;
    //if ($result)
    if ($row = $db->sql_fetchrow($result))
    {
        include($root . 'test113c.php');
        $f = new MyClass();
        $f->bar();
    }
}



?>
