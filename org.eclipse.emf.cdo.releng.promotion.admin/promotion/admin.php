<?php
ini_set('display_errors', true);

require_once 'Project.php';
require_once 'Drop.php';

print "<h1>CDO Promotion Admin</h1>";

$cdo = new Project("CDO", "/home/data/httpd/download.eclipse.org/modeling/emf/cdo/drops");
foreach ($cdo->getDrops() as $drop)
{
	if ($drop->isVisible())
	{
		echo $drop->getQualifier();
	}
	else
	{
		echo '<font color="#444444">';
		echo $drop->getQualifier();
		echo '</font>';
	}

	echo "<br>\n";
}

?>
