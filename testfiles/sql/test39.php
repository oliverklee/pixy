<? //

// was an inaccuracy in SQLAnalysis.multiDependency();
// should NOT report a vulnerability


$templatesused = 'h';
unp_cacheTemplates($templatesused);

function unp_cacheTemplates($templatesused)
{
	$templatesused = str_replace(',',"','", addslashes($templatesused));
    mysql_query("'$templatesused'");
}





?>
