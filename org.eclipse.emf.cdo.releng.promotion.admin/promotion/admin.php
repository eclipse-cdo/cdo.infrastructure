<?php
ini_set('display_errors', true);

require_once 'Project.php';
require_once 'Drop.php';


$cdo = new Project("CDO", "/home/data/httpd/download.eclipse.org/modeling/emf/cdo/drops", array("indigo", "juno"));

echo '<center><font face="Helvetica,Arial">';
echo "<h1>".$this->name." Promotion Admin</h1>";

if (isset($_GET["action"]))
{
	$action = $_GET["action"];
	$drop = $this->getDrop($_GET["drop"]);

	$action($drop);
}
else
{
	$cdo->generate();
}

echo '</font></center>';

function Label($drop)
{
	echo '<h2>'.$drop->qualifier.'</h2>';
	echo '<form method="GET">';
	echo '<input name="drop" type="hidden" value="'.$drop->qualifier.'"></input>';
	echo 'Label: <input name="" type="text" value="'.$drop->label.'"></input><br>';
	echo '<input name="action" type="submit"></input>&nbsp;';
	echo '</form>';
}

?>
