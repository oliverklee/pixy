<?php

function foo()
{
	global $template;
    $template->set_filenames();
	$template->assign_vars();
}

?>