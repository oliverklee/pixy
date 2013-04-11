<? //

// was a bug

$x = 'harmless';

while ($c) {
    sql_query('select') ;
    echo $x;
}

function sql_query($query) {
    mysql_query($query);
}








?>