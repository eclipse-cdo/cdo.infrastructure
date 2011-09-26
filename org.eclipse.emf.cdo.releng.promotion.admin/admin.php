<?php

print "<h1>CDO Promotion Admin</h1>";

$d = dir("/home/data/httpd/download.eclipse.org/modeling/emf/cdo/drops");
while (false !== ($entry = $d->read()))
{
	echo $entry."\n";
}

$d->close();

//print str_replace("\n", "<br>", htmlspecialchars(file_get_contents("/home/data/httpd/download.eclipse.org/modeling/emf/cdo/drops/I20110925-0426/build-info.xml")));

?>
