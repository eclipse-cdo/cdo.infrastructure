<?php

require_once 'Drop.php';

class Project
{
	public $name;
	public $path;
	public $drops = array();
	private $dropsByQualifier = array();

	function __construct($name, $path)
	{
		$this->name = $name;
		$this->path = $path;

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

	function generate()
	{
		echo "<h1>".$this->name." Promotion Admin</h1>";

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

		echo '<p><a href="">Reload Page</a></p>';

		echo '<table border="1" cellpadding="8">';
		foreach ($this->drops as $drop)
		{
			$drop->generate();
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
}

?>
