<?php
ini_set('display_errors', true);

require_once 'Project.php';
require_once 'Drop.php';

echo '<center><font face="Helvetica,Arial">';
echo "<h1>CDO Promotion Admin</h1>";

$cdo = new Project("/home/data/httpd/download.eclipse.org/modeling/emf/cdo/drops", array("indigo", "juno"));

if (isset($_GET["action"]))
{
	$action = $_GET["action"];
	$drop = $cdo->getDrop($_GET["drop"]);

	if ($action($drop))
	{
		$cdo->generate();
	}
}
else
{
	$cdo->generate();
}

echo '</font></center>';

function scheduleTask($task)
{
	$publicFolder = "/tmp/promotion.emf.cdo/public";
	$tmpFolder = "$publicFolder/tasks.tmp";

	$attempt = 0;
	while (!mkdir($tmpFolder))
	{
		sleep(1);
		echo "Attempt to create $tmpFolder (".(++$attempt).")<br>";
	}

	chmod($tmpFolder, 0777);

	$start = time();
	$taskFile = "$tmpFolder/$start.task";
	file_put_contents($taskFile, $task);
	chmod($taskFile, 0666);

	$attempt = 0;
	while (!rename($tmpFolder, "$publicFolder/tasks"))
	{
		sleep(1);
		echo "Attempt to rename $tmpFolder (".(++$attempt).")<br>";
	}

	return true;
}

function Cancel($drop)
{
	return true;
}

function EditLabel($drop)
{
	echo '<h2>Drop '.$drop->qualifier.'</h2>';
	echo '<form method="GET">';
	echo '<input name="drop" type="hidden" value="'.$drop->qualifier.'"></input>';
	echo '<p>Label: <input name="value" type="text" value="'.$drop->label.'"></input></p>';
	echo '<input name="action" type="submit" value="ChangeLabel"></input>';
	echo '&nbsp;';
	echo '<input name="action" type="submit" value="Cancel"></input>';
	echo '</form>';
	return false;
}

function ChangeLabel($drop)
{
	$value = $_GET["value"];
	scheduleTask("ChangeLabel\n$drop->qualifier\n$value");
	return true;
}

function Show($drop)
{
	scheduleTask("Show\n$drop->qualifier");
	return true;
}

function Hide($drop)
{
	scheduleTask("Hide\n$drop->qualifier");
	return true;
}

function AskDelete($drop)
{
	echo '<h2>Drop '.$drop->qualifier.'</h2>';
	echo '<form method="GET">';
	echo '<input name="drop" type="hidden" value="'.$drop->qualifier.'"></input>';
	echo '<p>Are you sure to delete drop '.$drop->qualifier.'?</p>';
	echo '<input name="action" type="submit" value="Delete"></input>';
	echo '&nbsp;';
	echo '<input name="action" type="submit" value="Cancel"></input>';
	echo '</form>';
	return false;
}

function Delete($drop)
{
	scheduleTask("Delete\n$drop->qualifier");
	return true;
}

?>
