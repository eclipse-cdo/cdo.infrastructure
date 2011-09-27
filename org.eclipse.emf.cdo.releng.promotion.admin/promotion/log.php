<?php
// ini_set('display_errors', true);

$logfile = "/tmp/promotion.emf.cdo/promoter.log";

echo '<font face="Helvetica,Arial">';
echo "<h1>$logfile</h1>";
echo "</font>\n\n";

echo ("<script type=\"text/javascript\">\n");
echo ("  function toggle(id)\n");
echo ("  {\n");
echo ("    e = document.getElementById(id);\n");
echo ("    e.style.display = (e.style.display == \"\" ? \"none\" : \"\");\n");
echo ("  }\n");
echo ("</script>\n");
echo ("\n");

$runs = array();
$run = null;

$lines = file($logfile);
foreach ($lines as $line)
{
	if (strpos($line, "Starting promotion with") !== false)
	{
		addRun();
		$run = new Run($line);
	}
	else
	{
		if ($run != null)
		{
			$run->addBody($line);
		}
	}
}

addRun();
$runs = array_reverse($runs);

echo "<tt>\n\n";

$id = 0;
foreach ($runs as $run)
{
	$run->generate(++$id);
}

echo "</tt>\n";


function addRun()
{
	global $runs, $run;
	if ($run != null)
	{
		$runs[count($runs)] = $run;
	}
}

class Run
{
	public $head;
	public $noop = false;
	public $body = array();

	function __construct($head)
	{
		$this->head = str_replace("\n", "", htmlspecialchars($head));
	}

	function addBody($line)
	{
		$this->body[count($this->body)] = str_replace("\n", "<br>", str_replace("\t", "&nbsp;&nbsp;&nbsp;", htmlspecialchars($line)));
		if (strpos($line, "No new builds or tasks have been found") !== false)
		{
			$this->noop = true;
		}
	}

	function generate($id)
	{
		echo '<div>';
		echo "<a href=\"javascript:toggle('run$id')\">";
		if ($this->noop)
		{
			echo '<font color="#444444">';
		}
		else
		{
			echo '<b>';
		}

		echo $this->head;
		if ($this->noop)
		{
			echo '</font>';
		}
		else
		{
			echo '</b>';
		}

		echo '</a>';
		echo "</div>\n";

		echo "<div id=\"run$id\" style=\"display:none; margin-left:24px;\">";
		foreach ($this->body as $line)
		{
			echo $line;
		}

		echo "</div>\n";
	}
}
?>
