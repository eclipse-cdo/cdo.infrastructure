<?php

require_once 'Drop.php';

class Project
{
	public $name;
	public $path;
	public $trains;
	public $drops = array();
	private $dropsByQualifier = array();

	function __construct($name, $path, $trains)
	{
		$this->name = $name;
		$this->path = $path;
		$this->trains = $trains;

		$d = dir($this->path);
		while (false !== ($entry = $d->read()))
		{
			if (strpos($entry, ".") !== 0)
			{
				$this->addDrop($entry);
			}
		}

		$d->close();
	}

	function getDrop($qualifier)
	{
		return $this->dropsByQualifier[$qualifier];
	}

	function process()
	{
		if (isset($_GET["Stage"]))
		{
			$drop = $this->getDrop($_GET["Stage"]);
			$drop->stage();
		}

		if (isset($_GET["Hide"]))
		{
			$drop = $this->getDrop($_GET["Hide"]);
			$drop->hide();
		}

		if (isset($_GET["Show"]))
		{
			$drop = $this->getDrop($_GET["Show"]);
			$drop->show();
		}

		if (isset($_GET["Delete"]))
		{
			$drop = $this->getDrop($_GET["Delete"]);
			$drop->delete();
		}
	}

	function generate()
	{
		echo "<h1>".$this->name." Promotion Admin</h1>";
		echo '<p><a href="'.$_SERVER['PHP_SELF'].'">Reload Page</a></p>';

		echo '<table border="1" cellpadding="8">';
		foreach ($this->getStreams() as $stream => $v)
		{
			echo '<tr>';
			echo '<td colspan="6"><h2>'.$stream.' Stream</h2></td>';
			echo '</tr>';

			echo '<tr>';
			echo '<th>Drop</th>';
			echo '<th>Label</th>';
			echo '<th>Train</th>';
			echo '<th colspan="3">Actions</th>';
			echo '</tr>';

			foreach ($this->drops as $drop)
			{
				if ($drop->stream == $stream)
				{
					$drop->generate();
				}
			}
		}

		echo '</table>';
	}

	private function addDrop($qualifier)
	{
		$drop = new Drop($this, $qualifier);
		$this->drops[count($this->drops)] = $drop;
		$this->dropsByQualifier[$drop->qualifier] = $drop;
		return $drop;
	}

	private function getStreams()
	{
		$streams = array();
		foreach ($this->drops as $drop)
		{
			$streams[$drop->stream] = true;
		}

		uksort($streams, "cmpStreams");
		return $streams;
	}
}

function cmpStreams($a, $b)
{
	$a = explode(".", $a);
	$b = explode(".", $b);

	if (intval($a[0]) > intval($b[0])) return -1;
	if (intval($a[0]) < intval($b[0])) return 1;
	if (intval($a[1]) > intval($b[1])) return -1;
	if (intval($a[1]) < intval($b[1])) return 1;
	return 0;
}

?>
